package fr.inptoulouse.sn.ide7.debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ExecutionService {
	
	/** Durée maximale d'exécution par défaut (secondes). */
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private final long timeoutSeconds;

    /** Référence au processus en cours, pour l'arrêt depuis un autre thread. */
    private final AtomicReference<Process> runningProcess = new AtomicReference<>(null);

    public ExecutionService() {
        this(DEFAULT_TIMEOUT_SECONDS);
    }

    /** Constructeur avec timeout personnalisé (utile pour les tests). */
    public ExecutionService(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }


    /**
     * Execute une classe Java deja compilee.
     * @param className le nom complet de la classe (ex: "MonProgramme")
     * @param classPath le chemin vers le dossier contenant les .class (ex: "bin")
     * @return ExecutionResult contenant la sortie, le code retour et le succes
     */
    public ExecutionResult execute(String className, String classPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", classPath, className);
            // SCRUM-33 : on ne mélange plus stdout et stderr
            pb.redirectErrorStream(false);

            Process process = pb.start();
            runningProcess.set(process);

            // Lire stdout et stderr dans deux threads séparés
            StringBuilder stdoutBuf = new StringBuilder();
            StringBuilder stderrBuf = new StringBuilder();

            Thread convergingStdout = new Thread(
                () -> readStream(process.getInputStream(), stdoutBuf)
            );
            Thread convergingStderr = new Thread(
                () -> readStream(process.getErrorStream(), stderrBuf)
            );
            convergingStdout.start();
            convergingStderr.start();

            // SCRUM-34 : attente avec timeout
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                // Timeout dépassé : on tue le processus
                process.destroyForcibly();
                runningProcess.set(null);
                convergingStdout.interrupt();
                convergingStderr.interrupt();

                return new ExecutionResult(
                    false, -1,
                    stdoutBuf.toString(),
                    "Programme arrêté : délai de " + timeoutSeconds + "s dépassé.",
                    ExecutionResult.TerminationReason.TIMEOUT
                );
            }

            // Attendre que les threads de lecture aient fini
            convergingStdout.join(2000);
            convergingStderr.join(2000);

            int exitCode = process.exitValue();
            runningProcess.set(null);

            return new ExecutionResult(
                exitCode == 0, exitCode,
                stdoutBuf.toString(),
                stderrBuf.toString(),
                ExecutionResult.TerminationReason.NORMAL
            );

        } catch (IOException e) {
            runningProcess.set(null);
            return new ExecutionResult(false, -1, "Erreur IO : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            runningProcess.set(null);
            return new ExecutionResult(false, -1, "Processus interrompu : " + e.getMessage());
        }
    }
    
    /**
     * SCRUM-34 : Arrête immédiatement le programme en cours d'exécution.
     * Appelé depuis le bouton "Arrêter" de l'interface.
     * Thread-safe grâce à AtomicReference.
     *
     * @return true si un programme a été stoppé, false si aucun ne tournait
     */
    public boolean stopExecution() {
        Process process = runningProcess.getAndSet(null);
        if (process == null || !process.isAlive()) {
            return false;
        }
        process.destroyForcibly();
        return true;
    }

    /** true si un programme est actuellement en cours d'exécution. */
    public boolean isRunning() {
        Process p = runningProcess.get();
        return p != null && p.isAlive();
    }


    /**
     * Compile puis execute un fichier Java.
     * @param javaFilePath chemin vers le fichier .java
     * @param className nom de la classe principale
     * @return ExecutionResult de l'execution (ou de la compilation si elle echoue)
     */
    public ExecutionResult compileAndRun(String javaFilePath, String className) {
        CompilerService compiler = new CompilerService();
        ExecutionResult compilationResult = compiler.compile(javaFilePath);

        if (!compilationResult.isSuccess()) {
            return new ExecutionResult(false, compilationResult.getExitCode(),
                    "Erreur de compilation :\n" + compilationResult.getOutput());
        }

        return execute(className, "bin");
    }
    
    
    /** Lit intégralement un flux ligne par ligne dans un StringBuilder. */
    private void readStream(InputStream stream, StringBuilder target) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                target.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            // Flux fermé lors d'un arrêt forcé : comportement normal
        }
    }

}