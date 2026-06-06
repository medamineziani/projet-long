package fr.inptoulouse.sn.ide7.debug.views;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DebugView extends BorderPane {

    private final TextField filePathField;
    private final TextField classNameField;
    private final TextField breakpointLineField;

    private final Button compileButton;
    private final Button runButton;
    private final Button debugButton;
    private final Button stopButton;

    private final TextArea consoleArea;

    public DebugView() {
        filePathField = new TextField("src/main/java/fr/inptoulouse/sn/ide7/debug/Helloworld.java");
        classNameField = new TextField("fr.inptoulouse.sn.ide7.debug.Helloworld");
        breakpointLineField = new TextField("9");

        compileButton = new Button("Compiler");
        runButton = new Button("Exécuter");
        debugButton = new Button("Debug breakpoint");
        stopButton = new Button("Arrêter");

        consoleArea = new TextArea();
        consoleArea.setEditable(false);
        consoleArea.setPrefHeight(400);

        buildLayout();
    }

    private void buildLayout() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        HBox fileBox = new HBox(10);
        fileBox.getChildren().addAll(
                new Label("Fichier :"),
                filePathField
        );

        HBox classBox = new HBox(10);
        classBox.getChildren().addAll(
                new Label("Classe :"),
                classNameField
        );

        HBox breakpointBox = new HBox(10);
        breakpointBox.getChildren().addAll(
                new Label("Ligne breakpoint :"),
                breakpointLineField
        );

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(
                compileButton,
                runButton,
                debugButton,
                stopButton
        );

        root.getChildren().addAll(
                fileBox,
                classBox,
                breakpointBox,
                buttonBox,
                new Label("Console :"),
                consoleArea
        );

        setCenter(root);
    }

    public TextField getFilePathField() {
        return filePathField;
    }

    public TextField getClassNameField() {
        return classNameField;
    }

    public TextField getBreakpointLineField() {
        return breakpointLineField;
    }

    public Button getCompileButton() {
        return compileButton;
    }

    public Button getRunButton() {
        return runButton;
    }

    public Button getDebugButton() {
        return debugButton;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public TextArea getConsoleArea() {
        return consoleArea;
    }
}
