package fr.inptoulouse.sn.ide7.debug;

/**
 * Résultat d'une compilation ou d'une exécution de programme Java.
 *
 * Conserve la compatibilité avec le code existant (constructeur à 3 params)
 * et ajoute le support de :
 *   - la séparation stdout / stderr (SCRUM-33)
 *   - la raison d'arrêt du processus (SCRUM-34)
 */
public class ExecutionResult {

    /** Raison de la fin du processus. */
    public enum TerminationReason {
        /** Fin normale. */
        NORMAL,
        /** Arrêté par l'utilisateur (bouton Stop). */
        KILLED_BY_USER,
        /** Délai d'exécution maximal dépassé. */
        TIMEOUT
    }

    private final boolean success;
    private final int exitCode;
    private final String output;
    private final String stderr;
    private final TerminationReason terminationReason;

    /**
     * Constructeur d'origine, conservé pour la compatibilité avec CompilerService.
     * stderr est vide et la raison d'arrêt est NORMAL.
     */
    public ExecutionResult(boolean success, int exitCode, String output) {
        this(success, exitCode, output, "", TerminationReason.NORMAL);
    }

    /**
     * Constructeur complet avec séparation stdout/stderr et raison d'arrêt.
     */
    public ExecutionResult(boolean success, int exitCode,
                           String output, String stderr,
                           TerminationReason terminationReason) {
        this.success           = success;
        this.exitCode          = exitCode;
        this.output            = output  != null ? output  : "";
        this.stderr            = stderr  != null ? stderr  : "";
        this.terminationReason = terminationReason;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getExitCode() {
        return exitCode;
    }

    /** Sortie standard (stdout) du programme. */
    public String getOutput() {
        return output;
    }

    /** Sortie d'erreur (stderr) : stack traces, exceptions JVM. */
    public String getStderr() {
        return stderr;
    }

    /** Raison de la fin du processus. */
    public TerminationReason getTerminationReason() {
        return terminationReason;
    }

    /** true si le programme a été interrompu (timeout ou bouton Stop). */
    public boolean wasInterrupted() {
        return terminationReason == TerminationReason.KILLED_BY_USER
            || terminationReason == TerminationReason.TIMEOUT;
    }
}