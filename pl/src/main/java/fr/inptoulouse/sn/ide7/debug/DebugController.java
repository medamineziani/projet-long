package fr.inptoulouse.sn.ide7.debug;

import java.util.List;

/**
 * Contrôleur principal du module Debug.
 * Point d'entrée pour l'interface graphique.
 *
 */
public class DebugController {

    private final ExecutionService executionService;
    private final RuntimeExceptionParser runtimeParser;

    public DebugController() {
        this.executionService = new ExecutionService();
        this.runtimeParser    = new RuntimeExceptionParser();
    }

    /** Constructeur avec timeout personnalisé (utile pour les tests). */
    public DebugController(long timeoutSeconds) {
        this.executionService = new ExecutionService(timeoutSeconds);
        this.runtimeParser    = new RuntimeExceptionParser();
    }

    /**
     * Compile et exécute un fichier Java (méthode d'origine conservée).
     * Retourne la sortie standard sous forme de String.
     */
    public String runFile(String filePath, String className) {
        ExecutionResult result =
            executionService.compileAndRun(filePath, className);

        return result.getOutput();
    }

    /**
     * Compile et exécute un fichier Java avec résultat détaillé.
     *
     * Permet à l'interface de :
     *   - afficher stdout dans la console
     *   - afficher les exceptions parsées
     *   - savoir si le programme a été arrêté
     *
     * @param filePath  chemin vers le fichier .java
     * @param className nom complet de la classe principale
     * @return résultat complet de l'exécution
     */
    public ExecutionResult runFileDetailed(String filePath, String className) {
        return executionService.compileAndRun(filePath, className);
    }

    /**
     * SCRUM-33 : Extrait les exceptions runtime depuis un résultat d'exécution.
     *
     * @param result résultat retourné par runFileDetailed()
     * @return liste d'exceptions parsées (vide si aucune)
     */
    public List<RuntimeExceptionInfo> parseRuntimeExceptions(ExecutionResult result) {
        return runtimeParser.parse(result.getStderr());
    }

    /**
     * SCRUM-34 : Arrête le programme en cours d'exécution.
     * Appelé par le bouton "Arrêter" de l'interface.
     *
     * @return true si un programme a effectivement été stoppé
     */
    public boolean stopCurrentExecution() {
        return executionService.stopExecution();
    }

    /** true si un programme est actuellement en cours d'exécution. */
    public boolean isRunning() {
        return executionService.isRunning();
    }
}