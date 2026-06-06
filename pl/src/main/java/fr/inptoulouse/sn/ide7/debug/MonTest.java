package fr.inptoulouse.sn.ide7.debug;

public class MonTest {

    public static void main(String[] args) throws Exception{

        System.out.println("Dossier : " + System.getProperty("user.dir"));

        System.out.println("=== Test du module Debug ===\n");

        // 1. Test de compilation seule
        System.out.println("--- Test 1 : Compilation de HelloWorld ---");

        CompilerService compiler = new CompilerService();

        ExecutionResult compileResult =
            compiler.compile(
                "src/main/java/fr/inptoulouse/sn/ide7/debug/Helloworld.java"
            );

        System.out.println("Succes : " + compileResult.isSuccess());
        System.out.println("Code retour : " + compileResult.getExitCode());

        if (!compileResult.getOutput().isEmpty()) {
            System.out.println("Sortie : " + compileResult.getOutput());
        }

        // Test erreur de compilation
        System.out.println("\n--- Test erreur de compilation ---");

        ExecutionResult errorResult =
            compiler.compile("test_programs/MainError.java");

        System.out.println("Succes erreur : " + errorResult.isSuccess());
        System.out.println("Code retour erreur : " + errorResult.getExitCode());
        System.out.println("Sortie brute erreur :");
        System.out.println(errorResult.getOutput());

        CompilationErrorParser parser = new CompilationErrorParser();

        for (CompilationError error : parser.parse(errorResult.getOutput())) {
            System.out.println(error);
        }

        // 2. Test d'execution seule
        System.out.println("\n--- Test 2 : Execution de HelloWorld ---");

        ExecutionService executor = new ExecutionService();

        ExecutionResult execResult =
            executor.execute(
                "fr.inptoulouse.sn.ide7.debug.Helloworld",
                "bin"
            );

        System.out.println("Succes : " + execResult.isSuccess());
        System.out.println("Sortie : " + execResult.getOutput());

        // 3. Test compileAndRun
        System.out.println("\n--- Test 3 : Compile + Run de HelloWorld ---");

        ExecutionResult fullResult =
            executor.compileAndRun(
                "src/main/java/fr/inptoulouse/sn/ide7/debug/Helloworld.java",
                "fr.inptoulouse.sn.ide7.debug.Helloworld"
            );

        System.out.println("Succes : " + fullResult.isSuccess());
        System.out.println("Sortie : " + fullResult.getOutput());

        // 4. Test MemorySnapshot
        System.out.println("\n--- Test 4 : MemorySnapshot ---");

       MemorySnapshotManager memoryManager = new MemorySnapshotManager();

       memoryManager.addSnapshot("Etape 1", "x = 5");
       memoryManager.addSnapshot("Etape 2", "y = 10");
       memoryManager.addSnapshot("Etape 3", "somme = x + y = 15");

       memoryManager.displaySnapshots();
        // 5. Test DebugController
       System.out.println("\n--- Test 5 : DebugController ---");

       DebugController debugController = new DebugController();

       String controllerOutput = debugController.runFile(
    "src/main/java/fr/inptoulouse/sn/ide7/debug/Helloworld.java",
    "fr.inptoulouse.sn.ide7.debug.Helloworld"
);

       System.out.println(controllerOutput);
       // 6. Test BreakpointManager
       System.out.println("\n--- Test 6 : Breakpoints ---");

       BreakpointManager breakpointManager = new BreakpointManager();

       breakpointManager.addBreakpoint(7);
       breakpointManager.addBreakpoint(15);
       breakpointManager.addBreakpoint(22);

       breakpointManager.displayBreakpoints();

       System.out.println(
         "Breakpoint ligne 15 : "
        + breakpointManager.hasBreakpoint(15)
       );
       // 7. Test vrai breakpoint avec JDI
       System.out.println("\n--- Test 7 : Vrai breakpoint JDI ---");

       BreakpointDebugger debugger = new BreakpointDebugger();

debugger.debug(
    "fr.inptoulouse.sn.ide7.debug.DebugTarget",
    9
);
    // 6. SCRUM-33 : Parser les exceptions runtime
       System.out.println("\n--- Test 6 : RuntimeExceptionParser (SCRUM-33) ---");
 
       RuntimeExceptionParser runtimeParser = new RuntimeExceptionParser();
 
       // Simuler une sortie stderr typique de la JVM
       String fakeStderr =
           "Exception in thread \"main\" java.lang.NullPointerException: "
           + "Cannot read field \"name\" because \"person\" is null\n"
           + "\tat fr.example.Main.run(Main.java:15)\n"
           + "\tat fr.example.Main.main(Main.java:8)\n";
 
       java.util.List<RuntimeExceptionInfo> exceptions = runtimeParser.parse(fakeStderr);
       System.out.println("Nombre d'exceptions detectees : " + exceptions.size());
 
       if (!exceptions.isEmpty()) {
           RuntimeExceptionInfo ex = exceptions.get(0);
           System.out.println("Type    : " + ex.getExceptionType());
           System.out.println("Message : " + ex.getMessage());
           System.out.println("Resume  : " + ex);
       }
 
       // Test ArrayIndexOutOfBoundsException
       String fakeStderr2 =
           "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: "
           + "Index 5 out of bounds for length 3\n"
           + "\tat fr.example.Foo.bar(Foo.java:22)\n";
 
       java.util.List<RuntimeExceptionInfo> ex2 = runtimeParser.parse(fakeStderr2);
       System.out.println("AIOOBE detectee : " + (!ex2.isEmpty()));
 
       // Test stderr vide
       System.out.println("stderr vide     : " + runtimeParser.parse("").isEmpty());
       System.out.println("stderr null     : " + runtimeParser.parse(null).isEmpty());
 
       // 7. SCRUM-34 : Arrêt d'exécution
       System.out.println("\n--- Test 7 : stopExecution / isRunning (SCRUM-34) ---");
 
       ExecutionService executorWithTimeout = new ExecutionService(5);
       System.out.println("isRunning (aucun process) : " + executorWithTimeout.isRunning());
       System.out.println("stopExecution (aucun process) : " + executorWithTimeout.stopExecution());
 
       // 8. SCRUM-34 : Vérification du champ wasInterrupted
       System.out.println("\n--- Test 8 : ExecutionResult.wasInterrupted (SCRUM-34) ---");
 
       ExecutionResult normalResult = new ExecutionResult(true, 0, "ok");
       System.out.println("wasInterrupted (normal)  : " + normalResult.wasInterrupted());
 
       ExecutionResult timeoutResult = new ExecutionResult(
           false, -1, "", "timeout",
           ExecutionResult.TerminationReason.TIMEOUT
       );
       System.out.println("wasInterrupted (timeout) : " + timeoutResult.wasInterrupted());
 
       // 9. SCRUM-33 via DebugController
       System.out.println("\n--- Test 9 : DebugController.parseRuntimeExceptions ---");
 
       ExecutionResult fakeExecResult = new ExecutionResult(
           false, 1, "sortie normale", fakeStderr,
           ExecutionResult.TerminationReason.NORMAL
       );
       java.util.List<RuntimeExceptionInfo> parsed =
           debugController.parseRuntimeExceptions(fakeExecResult);
       System.out.println("Exceptions via controller : " + parsed.size());
       
       System.out.println("\n=== Tous les tests sont passes ===");
    }
}