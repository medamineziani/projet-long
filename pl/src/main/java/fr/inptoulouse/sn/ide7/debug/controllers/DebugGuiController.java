package fr.inptoulouse.sn.ide7.debug.controllers;

import fr.inptoulouse.sn.ide7.debug.BreakpointDebugger;
import fr.inptoulouse.sn.ide7.debug.CompilerService;
import fr.inptoulouse.sn.ide7.debug.ExecutionResult;
import fr.inptoulouse.sn.ide7.debug.ExecutionService;
import fr.inptoulouse.sn.ide7.debug.views.DebugView;

public class DebugGuiController {

    private final DebugView view;
    private final CompilerService compilerService;
    private final ExecutionService executionService;
    private final BreakpointDebugger breakpointDebugger;

    public DebugGuiController(DebugView view) {
        this.view = view;
        this.compilerService = new CompilerService();
        this.executionService = new ExecutionService();
        this.breakpointDebugger = new BreakpointDebugger();

        initializeActions();
    }

    private void initializeActions() {
        view.getCompileButton().setOnAction(event -> compile());
        view.getRunButton().setOnAction(event -> run());
        view.getDebugButton().setOnAction(event -> debugBreakpoint());
        view.getStopButton().setOnAction(event -> stopExecution());
    }

    private String buildFullClassName(String className) {
        className = className.trim();

        if (className.isEmpty()) {
            throw new IllegalArgumentException("Le nom de classe est vide.");
        }

        if (className.contains(".")) {
            return className;
        }

        return "fr.inptoulouse.sn.ide7.debug." + className;
    }

    private void compile() {
        try {
            String filePath = view.getFilePathField().getText().trim();

            ExecutionResult result = compilerService.compile(filePath);

            view.getConsoleArea().appendText("\n--- Compilation ---\n");
            view.getConsoleArea().appendText("Fichier : " + filePath + "\n");
            view.getConsoleArea().appendText("Succès : " + result.isSuccess() + "\n");

            if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                view.getConsoleArea().appendText(result.getOutput() + "\n");
            }

        } catch (Exception e) {
            view.getConsoleArea().appendText("Erreur compilation : " + e.getMessage() + "\n");
        }
    }

    private void run() {
        try {
            String simpleName = view.getClassNameField().getText();
            String className = buildFullClassName(simpleName);

            ExecutionResult result = executionService.execute(className, "bin");

            view.getConsoleArea().appendText("\n--- Exécution ---\n");
            view.getConsoleArea().appendText("Classe : " + className + "\n");
            view.getConsoleArea().appendText("Succès : " + result.isSuccess() + "\n");

            if (result.getOutput() != null && !result.getOutput().isEmpty()) {
                view.getConsoleArea().appendText(result.getOutput() + "\n");
            }

        } catch (Exception e) {
            view.getConsoleArea().appendText("Erreur exécution : " + e.getMessage() + "\n");
        }
    }

    private void debugBreakpoint() {
        try {
            String simpleName = view.getClassNameField().getText();
            String className = buildFullClassName(simpleName);
            int line = Integer.parseInt(view.getBreakpointLineField().getText().trim());

            view.getConsoleArea().appendText("\n--- Debug Breakpoint ---\n");
            view.getConsoleArea().appendText("Classe : " + className + "\n");
            view.getConsoleArea().appendText("Breakpoint demandé à la ligne " + line + "\n");

            breakpointDebugger.debug(className, line);

            view.getConsoleArea().appendText("Debug terminé.\n");

        } catch (NumberFormatException e) {
            view.getConsoleArea().appendText("Erreur debug : la ligne doit être un nombre.\n");
        } catch (Exception e) {
            view.getConsoleArea().appendText("Erreur debug : " + e.getMessage() + "\n");
        }
    }

    private void stopExecution() {
        boolean stopped = executionService.stopExecution();

        view.getConsoleArea().appendText("\n--- Arrêt ---\n");
        view.getConsoleArea().appendText("Processus arrêté : " + stopped + "\n");
    }
}
