package voxel.view;

import com.jme3.scene.Mesh;

import voxel.model.ChunkModel;
import voxel.model.WorldModel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pipeline asynchrone de reconstruction des maillages de chunks.
 *
 * La construction d'un maillage (greedy meshing + occlusion ambiante) est la
 * partie coûteuse d'une mise à jour de chunk. Ce service la déporte sur des
 * threads d'arrière-plan : le thread de rendu se contente de demander une
 * reconstruction via {@link #requestRemesh}, puis d'appliquer les maillages
 * terminés avec un budget par frame via {@link #applyCompletedMeshes}.
 * La simulation reste ainsi fluide même quand des dizaines de chunks doivent
 * être reconstruits en même temps (par exemple quand beaucoup d'arbres
 * poussent simultanément).
 *
 * Cohérence : chaque chunk porte un numéro de version incrémenté à chaque
 * modification ({@link ChunkModel#markDirty()}). Le worker capture la version
 * au début de la construction ; si elle a changé au moment de l'application,
 * le maillage (potentiellement obsolète) est tout de même appliqué puis une
 * nouvelle reconstruction est automatiquement programmée. Le rendu converge
 * donc toujours vers l'état réel des données.
 */
public class ChunkMeshingService {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;

    /** Référence au renderer du monde, pour accéder aux ChunkRenderer et appliquer les maillages */
    private final WorldRenderer worldRenderer;

    /** Threads d'arrière-plan qui construisent les maillages */
    private final ExecutorService executor;

    /** Chunks actuellement en file ou en cours de construction (clés compactées), pour dédoublonner */
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

    /** Maillages terminés, en attente d'application par le thread de rendu */
    private final ConcurrentLinkedQueue<MeshResult> completedResults = new ConcurrentLinkedQueue<>();

    /**
     * Nombre maximum de maillages appliqués (upload GPU) par frame.
     * Limite le coût par frame côté thread de rendu : le reste de la file
     * est simplement appliqué aux frames suivantes.
     */
    private static final int MAX_APPLY_PER_FRAME = 8;

    /** Résultat d'une construction de maillage en arrière-plan */
    private static final class MeshResult {
        final int chunkX, chunkY, chunkZ;
        final long key;
        final int version;
        final Mesh opaqueMesh;
        final Mesh transparentMesh;

        MeshResult(int chunkX, int chunkY, int chunkZ, long key, int version, Mesh opaqueMesh, Mesh transparentMesh) {
            this.chunkX = chunkX;
            this.chunkY = chunkY;
            this.chunkZ = chunkZ;
            this.key = key;
            this.version = version;
            this.opaqueMesh = opaqueMesh;
            this.transparentMesh = transparentMesh;
        }
    }

    /**
     * Crée le service de meshing asynchrone.
     *
     * @param worldModel Le modèle du monde
     * @param worldRenderer Le renderer du monde
     */
    public ChunkMeshingService(WorldModel worldModel, WorldRenderer worldRenderer) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;

        // Quelques threads suffisent : un chunk se mesh en quelques millisecondes
        int threads = Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() - 2));
        AtomicInteger threadId = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable, "chunk-mesher-" + threadId.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        });
    }

    /**
     * Demande la reconstruction du maillage d'un chunk en arrière-plan.
     * Les demandes pour un chunk déjà en file sont ignorées (dédoublonnage) :
     * le contrôle de version à l'application garantit qu'une reconstruction
     * supplémentaire sera programmée si nécessaire.
     *
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     */
    public void requestRemesh(int chunkX, int chunkY, int chunkZ) {
        ChunkModel chunk = worldModel.getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) {
            return;
        }

        long key = pack(chunkX, chunkY, chunkZ);
        if (inFlight.add(key)) {
            executor.submit(() -> buildMeshes(chunk, chunkX, chunkY, chunkZ, key));
        }
    }

    /**
     * Construit les maillages d'un chunk (exécuté sur un thread d'arrière-plan).
     * Aucune interaction avec le scene graph ici : uniquement de la génération
     * de données de maillage, qui est sûre hors du thread de rendu.
     */
    private void buildMeshes(ChunkModel chunk, int chunkX, int chunkY, int chunkZ, long key) {
        try {
            // Capturer la version AVANT de lire les blocs : si elle change pendant
            // la construction, le résultat sera détecté comme obsolète à l'application
            int version = chunk.getVersion();

            ChunkRenderer renderer = worldRenderer.getChunkRenderer(chunkX, chunkY, chunkZ);
            if (renderer == null) {
                inFlight.remove(key);
                return;
            }

            Mesh opaqueMesh = renderer.buildOpaqueMesh();
            Mesh transparentMesh = renderer.buildTransparentMesh();

            completedResults.add(new MeshResult(chunkX, chunkY, chunkZ, key, version, opaqueMesh, transparentMesh));
        } catch (Exception e) {
            inFlight.remove(key);
            System.err.println("Erreur de meshing du chunk (" + chunkX + ", " + chunkY + ", " + chunkZ + ") : " + e);
        }
    }

    /**
     * Applique les maillages terminés au scene graph, dans la limite du budget
     * par frame. À appeler à chaque frame depuis le thread de rendu.
     */
    public void applyCompletedMeshes() {
        int applied = 0;
        MeshResult result;

        while (applied < MAX_APPLY_PER_FRAME && (result = completedResults.poll()) != null) {
            worldRenderer.applyChunkMeshes(result.chunkX, result.chunkY, result.chunkZ,
                                           result.opaqueMesh, result.transparentMesh);
            inFlight.remove(result.key);

            // Si le chunk a été modifié pendant la construction, reconstruire
            ChunkModel chunk = worldModel.getChunk(result.chunkX, result.chunkY, result.chunkZ);
            if (chunk != null && chunk.getVersion() != result.version) {
                requestRemesh(result.chunkX, result.chunkY, result.chunkZ);
            }

            applied++;
        }
    }

    /**
     * Arrête les threads de meshing. À appeler quand le monde est détruit.
     */
    public void shutdown() {
        executor.shutdownNow();
        completedResults.clear();
        inFlight.clear();
    }

    /** Compacte des coordonnées de chunk en une clé unique (21 bits par axe) */
    private static long pack(int chunkX, int chunkY, int chunkZ) {
        return ((long) (chunkX & 0x1FFFFF) << 42)
             | ((long) (chunkY & 0x1FFFFF) << 21)
             | (chunkZ & 0x1FFFFF);
    }
}
