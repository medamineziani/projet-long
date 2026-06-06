package fr.inptoulouse.sn.ide7.debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CompilerService {

    public ExecutionResult compile(String javaFilePath) {
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder("javac", "-d", "bin", javaFilePath);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
            boolean success = (exitCode == 0);

            return new ExecutionResult(success, exitCode, output.toString());

        } catch (IOException e) {
            return new ExecutionResult(false, -1, "Erreur IO : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ExecutionResult(false, -1, "Processus interrompu : " + e.getMessage());
        }
    }
}