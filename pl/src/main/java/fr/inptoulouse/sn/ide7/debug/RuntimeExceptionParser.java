package fr.inptoulouse.sn.ide7.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse la sortie d'erreur (stderr) de la JVM pour en extraire les exceptions.
 *
 * Format attendu de la JVM :
 *   Exception in thread "main" java.lang.NullPointerException: message
 *       at com.example.Foo.bar(Foo.java:12)
 *       at com.example.Main.main(Main.java:5)
 *
 * Gère aussi les "Caused by:" pour les exceptions chaînées.
 *
 * SCRUM-33 : Capturer et afficher proprement les exceptions Java.
 */
public class RuntimeExceptionParser {

    /**
     * Capture le type et le message d'une exception dans la sortie JVM.
     * Groupe 1 = type complet (java.lang.NullPointerException)
     * Groupe 2 = message (optionnel)
     */
    private static final Pattern EXCEPTION_HEADER = Pattern.compile(
        "(?:Exception in thread \"[^\"]*\"|Caused by:)\\s+([^:\\s]+)(?:: (.+))?"
    );

    /** Ligne de stack trace : "    at some.Class.method(File.java:42)" */
    private static final Pattern STACK_LINE = Pattern.compile(
        "\\s+at .+"
    );

    /**
     * Analyse la sortie stderr d'un programme Java et retourne la liste
     * des exceptions détectées.
     *
     * @param stderr sortie d'erreur brute produite par la JVM
     * @return liste d'exceptions parsées, vide si aucune exception
     */
    public List<RuntimeExceptionInfo> parse(String stderr) {
        List<RuntimeExceptionInfo> exceptions = new ArrayList<>();

        if (stderr == null || stderr.isBlank()) {
            return exceptions;
        }

        String[] lines = stderr.split("\\R");
        int i = 0;

        while (i < lines.length) {
            Matcher header = EXCEPTION_HEADER.matcher(lines[i]);

            if (header.find()) {
                String type    = simplifyType(header.group(1));
                String message = header.group(2) != null ? header.group(2).trim() : "";

                // Collecter les lignes "at ..." qui suivent
                StringBuilder trace = new StringBuilder();
                trace.append(lines[i]).append(System.lineSeparator());
                int j = i + 1;

                while (j < lines.length && STACK_LINE.matcher(lines[j]).matches()) {
                    trace.append(lines[j]).append(System.lineSeparator());
                    j++;
                }

                exceptions.add(
                    new RuntimeExceptionInfo(type, message, trace.toString().trim())
                );
                i = j;
            } else {
                i++;
            }
        }

        return exceptions;
    }

    /**
     * Simplifie un nom de classe qualifié en nom court.
     * "java.lang.NullPointerException" → "NullPointerException"
     */
    private String simplifyType(String fullyQualified) {
        if (fullyQualified == null) {
            return "Exception";
        }
        int lastDot = fullyQualified.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualified.substring(lastDot + 1) : fullyQualified;
    }
}