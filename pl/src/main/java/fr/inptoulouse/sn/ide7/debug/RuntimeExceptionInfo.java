package fr.inptoulouse.sn.ide7.debug;

/**
 * Représente une exception Java détectée dans la sortie d'erreur (stderr)
 * d'un programme en cours d'exécution.
 *
 * Encapsule le type (ex: NullPointerException), le message associé,
 * et la trace complète de la pile d'appels.
 *
 * SCRUM-33 : Capturer et afficher proprement les exceptions Java.
 */
public class RuntimeExceptionInfo {

    private final String exceptionType;
    private final String message;
    private final String stackTrace;

    public RuntimeExceptionInfo(String exceptionType, String message, String stackTrace) {
        this.exceptionType = exceptionType;
        this.message       = message;
        this.stackTrace    = stackTrace;
    }

    /** Nom simple de l'exception, ex : "NullPointerException". */
    public String getExceptionType() {
        return exceptionType;
    }

    /** Message de l'exception (peut être vide). */
    public String getMessage() {
        return message;
    }

    /** Stack trace complète telle que produite par la JVM. */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * Résumé lisible pour l'affichage dans la console IDE7.
     * Ex : "NullPointerException : Cannot read field 'x' of null"
     */
    @Override
    public String toString() {
        if (message != null && !message.isEmpty()) {
            return exceptionType + " : " + message;
        }
        return exceptionType;
    }
}
