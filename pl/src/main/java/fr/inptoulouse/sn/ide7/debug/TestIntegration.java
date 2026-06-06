package fr.inptoulouse.sn.ide7.debug;

import java.util.List;

/**
 * Test d'intégration : compile et exécute de vrais programmes buggés.
 *   - MainNPE.java      → NullPointerException (SCRUM-33)
 *   - MainInfini.java    → boucle infinie, tuée par timeout (SCRUM-34)
 */
public class TestIntegration {

    public static void main(String[] args) {
        System.out.println("=== Test d'integration SCRUM-33 / SCRUM-34 ===\n");

        testExceptionReelle();
        testTimeout();

        System.out.println("\n=== Tests d'integration termines ===");
    }

    /**
     * SCRUM-33 : Compile et exécute MainBug.java,
     * puis parse l'exception depuis stderr.
     */
    static void testExceptionReelle() {
        System.out.println("--- SCRUM-33 : Programme avec NullPointerException ---");

        DebugController controller = new DebugController();

        ExecutionResult result = controller.runFileDetailed(
            "test_programs/MainBug.java", "MainBug"
        );

        System.out.println("Succes       : " + result.isSuccess());
        System.out.println("Stdout       : " + result.getOutput().trim());
        System.out.println("Stderr brut  : " + result.getStderr().trim());

        List<RuntimeExceptionInfo> exceptions = controller.parseRuntimeExceptions(result);
        System.out.println("Exceptions   : " + exceptions.size());

        for (RuntimeExceptionInfo ex : exceptions) {
            System.out.println("  -> " + ex);
        }

        System.out.println();
    }

    /**
     * SCRUM-34 : Compile et exécute MainInfini.java avec un timeout de 3s.
     * Le programme doit être tué automatiquement.
     */
    static void testTimeout() {
        System.out.println("--- SCRUM-34 : Programme en boucle infinie (timeout 3s) ---");

        // Timeout court pour le test
        DebugController controller = new DebugController(3);

        long start = System.currentTimeMillis();

        ExecutionResult result = controller.runFileDetailed(
            "test_programs/MainInfini.java", "MainInfini"
        );

        long duree = System.currentTimeMillis() - start;

        System.out.println("Succes         : " + result.isSuccess());
        System.out.println("wasInterrupted : " + result.wasInterrupted());
        System.out.println("Raison         : " + result.getTerminationReason());
        System.out.println("Duree          : " + duree + " ms");
        System.out.println("Stderr         : " + result.getStderr().trim());
        System.out.println();
    }
}