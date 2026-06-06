package fr.inptoulouse.sn.ide7.controllers;

import fr.inptoulouse.sn.ide7.autocomplete.CompletionService;
import fr.inptoulouse.sn.ide7.models.ProjectEntry;
import fr.inptoulouse.sn.ide7.uml.JavaAttribut;
import fr.inptoulouse.sn.ide7.uml.JavaClass;
import fr.inptoulouse.sn.ide7.uml.JavaMethode;
import fr.inptoulouse.sn.ide7.uml.JavaParamettre;
import fr.inptoulouse.sn.ide7.uml.JavaParser;
import fr.inptoulouse.sn.ide7.uml.JavaRelation;
import fr.inptoulouse.sn.ide7.uml.UMLClassGenerator;
import fr.inptoulouse.sn.ide7.uml.UMLFormatter;
import fr.inptoulouse.sn.ide7.views.MainView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javafx.util.Duration;

public class MainController {
    private static final Duration AUTO_SAVE_DELAY = Duration.seconds(1);
    private static final String JAVA_CLASS = "Classe Java";
    private static final String JAVA_ABSTRACT_CLASS = "Classe abstraite Java";
    private static final String JAVA_INTERFACE = "Interface Java";
    private static final String JAVA_ENUM = "Enumeration Java";
    private static final String JAVA_ANNOTATION = "Annotation Java";
    private static final String TEXT_FILE = "Fichier texte";
    private static final String CUSTOM_FILE = "Personnalise";
    private static final int COMPLETION_LIMIT = 8;
    private static final Set<String> JAVA_KEYWORDS = new HashSet<String>(Arrays.asList(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
        "null", "exports", "module", "non-sealed", "open", "opens", "permits", "provides",
        "record", "requires", "sealed", "to", "transitive", "uses", "var", "with", "yield"
    ));

    private final MainView view;
    private final ProjectEntry project;
    private final PauseTransition autoSaveDelay;
    private final CompletionService completionService;
    private Path openedFile;
    private boolean suppressAutoSave;
    private String lastSavedContent;
    private String lastGeneratedUmlText;
    private List<JavaClass> lastGeneratedUmlClasses;
    private Process debugProcess;
    private boolean dirty;

    public MainController(final AppController appController, ProjectEntry project) {
        this.view = new MainView();
        this.project = project;
        this.autoSaveDelay = new PauseTransition(AUTO_SAVE_DELAY);
        this.completionService = new CompletionService();
        this.completionService.indexProject(project.getPath());
        this.view.setProject(project);
        this.view.setTreeCellFactory(new Callback<TreeView<Path>, TreeCell<Path>>() {
            @Override
            public TreeCell<Path> call(TreeView<Path> param) {
                return new TreeCell<Path>() {
                    @Override
                    protected void updateItem(Path item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }
                        TreeItem<Path> currentItem = getTreeItem();
                        boolean isRoot = currentItem != null && currentItem.getParent() == null;
                        boolean isDirectory = Files.isDirectory(item);
                        if (currentItem != null && currentItem.getParent() == null) {
                            setText(MainController.this.project.getName());
                        } else {
                            Path fileName = item.getFileName();
                            setText(fileName == null ? item.toString() : fileName.toString());
                        }
                        setGraphic(createTreeIcon(isRoot || isDirectory));
                        setStyle(isDirectory || isRoot ? "-fx-font-weight: bold;" : "");
                    }
                };
            }
        });

        reloadTree();
        this.view.setStatus("Project loaded.");
        this.view.setDirty(false);
        this.autoSaveDelay.setOnFinished(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                autoSaveCurrentFile();
            }
        });

        this.view.setOnBackRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.showWelcomeView();
            }
        });
        this.view.setOnRefreshRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                reloadTree();
                view.setStatus("Tree refreshed.");
            }
        });
        this.view.setOnProjectTreeClicked(new javafx.event.EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (event.getClickCount() >= 1) {
                    openSelectedFile();
                }
            }
        });
        this.view.setOnNewFileRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                createFile();
            }
        });
        this.view.setOnNewFolderRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                createFolder();
            }
        });
        this.view.setOnGenerateUmlRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                generateUmlFromProject();
            }
        });
        this.view.setOnGenerateClassFromUmlRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                openUmlDesigner();
            }
        });
        this.view.setOnDebugProjectRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                openDebugWindow();
            }
        });
        this.view.setOnDeleteRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                deleteSelected();
            }
        });
        this.view.setOnSaveRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                saveCurrentFile();
            }
        });
        this.view.setOnSearchRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                findNextOccurrence();
            }
        });
        this.view.setOnUndoRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                performEditorAction(EditorAction.UNDO);
            }
        });
        this.view.setOnRedoRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                performEditorAction(EditorAction.REDO);
            }
        });
        this.view.setOnCutRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                performEditorAction(EditorAction.CUT);
            }
        });
        this.view.setOnCopyRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                performEditorAction(EditorAction.COPY);
            }
        });
        this.view.setOnPasteRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                performEditorAction(EditorAction.PASTE);
            }
        });
        this.view.setOnSelectAllRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                performEditorAction(EditorAction.SELECT_ALL);
            }
        });
        this.view.setOnReplaceRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                replaceCurrentOccurrence();
            }
        });
        this.view.setOnReplaceAllRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                replaceAllOccurrences();
            }
        });
        this.view.setOnFilesDragOver(new javafx.event.EventHandler<javafx.scene.input.DragEvent>() {
            @Override
            public void handle(javafx.scene.input.DragEvent event) {
                javafx.scene.input.Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles()) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                    view.setDropActive(true);
                    view.setStatus("Drop files or folders to import them into the project.");
                }
                event.consume();
            }
        });
        this.view.setOnFilesDragDropped(new javafx.event.EventHandler<javafx.scene.input.DragEvent>() {
            @Override
            public void handle(javafx.scene.input.DragEvent event) {
                boolean completed = false;
                javafx.scene.input.Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles()) {
                    completed = importDroppedFiles(dragboard.getFiles());
                }
                view.setDropActive(false);
                event.setDropCompleted(completed);
                event.consume();
            }
        });
        this.view.setOnFilesDragExited(new javafx.event.EventHandler<javafx.scene.input.DragEvent>() {
            @Override
            public void handle(javafx.scene.input.DragEvent event) {
                view.setDropActive(false);
            }
        });
        this.view.setOnEditorTextChanged(new javafx.beans.value.ChangeListener<String>() {
            @Override
            public void changed(
                javafx.beans.value.ObservableValue<? extends String> observable,
                String oldValue,
                String newValue
            ) {
                onEditorTextChanged();
            }
        });
        this.view.setOnEditorKeyReleased(new javafx.event.EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                updateCompletionSuggestions(event);
            }
        });
        this.view.setOnEditorKeyPressed(new javafx.event.EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.TAB && view.hasCompletionSelection()) {
                    view.applySelectedCompletion();
                    event.consume();
                }
            }
        });
    }

    public Parent getView() {
        return view.getView();
    }

    private void reloadTree() {
        TreeItem<Path> root = buildTree(project.getPath());
        root.setExpanded(true);
        view.setProjectTreeRoot(root);
    }

    private TreeItem<Path> buildTree(Path path) {
        TreeItem<Path> node = new TreeItem<Path>(path);
        File asFile = path.toFile();
        if (!asFile.isDirectory()) {
            return node;
        }

        File[] children = asFile.listFiles();
        if (children == null) {
            return node;
        }

        Arrays.sort(children, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if (a.isDirectory() && !b.isDirectory()) {
                    return -1;
                }
                if (!a.isDirectory() && b.isDirectory()) {
                    return 1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        for (int i = 0; i < children.length; i++) {
            node.getChildren().add(buildTree(children[i].toPath()));
        }
        return node;
    }

    private void openSelectedFile() {
        TreeItem<Path> selected = view.getSelectedTreeItem();
        if (selected == null) {
            return;
        }

        Path selectedPath = selected.getValue();
        if (selectedPath == null || Files.isDirectory(selectedPath)) {
            return;
        }
        openFile(selectedPath);
    }

    private FontIcon createTreeIcon(boolean directory) {
        FontIcon icon = new FontIcon(directory ? AntDesignIconsOutlined.FOLDER : AntDesignIconsOutlined.FILE);
        icon.setIconSize(14);
        icon.setIconColor(javafx.scene.paint.Color.web(directory ? "#2563eb" : "#475569"));
        return icon;
    }

    private FontIcon createIcon(AntDesignIconsOutlined iconCode, String color) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(color));
        return icon;
    }

    private void saveCurrentFile() {
        saveCurrentFile(false);
    }

    private void autoSaveCurrentFile() {
        saveCurrentFile(true);
    }

    private void saveCurrentFile(boolean autoSave) {
        if (openedFile == null) {
            if (!autoSave) {
                showError("No file", "Open a file before saving.");
            }
            return;
        }

        String content = view.getEditorText();
        if (content.equals(lastSavedContent)) {
            if (!autoSave) {
                view.setStatus("No changes to save.");
            }
            return;
        }

        try {
            Files.write(openedFile, content.getBytes(StandardCharsets.UTF_8));
            lastSavedContent = content;
            dirty = false;
            view.setDirty(false);
            view.setStatus((autoSave ? "Auto-saved " : "Saved ") + openedFile.getFileName());
        } catch (IOException e) {
            if (autoSave) {
                view.setStatus("Auto-save failed for " + openedFile.getFileName());
            } else {
                showError("Save failed", "Cannot save file: " + e.getMessage());
            }
        }
    }

    private void createFile() {
        Path parent = getSelectedDirectoryOrParent();
        if (parent == null) {
            showError("No target", "Select a folder or file first.");
            return;
        }
        if (!isInsideProject(parent)) {
            parent = project.getPath();
        }

        String fileType = askFileType();
        if (fileType == null) {
            return;
        }

        String defaultName;
        if (isJavaSourceType(fileType)) {
            defaultName = getDefaultJavaSourceName(fileType);
        } else if (TEXT_FILE.equals(fileType)) {
            defaultName = "new_file.txt";
        } else {
            defaultName = "new_file";
        }

        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("New File");
        dialog.setHeaderText("Create file in: " + parent);
        dialog.setContentText("Name:");
        dialog.showAndWait();
        String fileName = dialog.getResult();
        if (fileName == null) {
            return;
        }
        fileName = fileName.trim();
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("\\")) {
            showError("Invalid name", "File name is invalid.");
            return;
        }

        if (isJavaSourceType(fileType)) {
            if (fileName.endsWith(".java")) {
                fileName = stripExtension(fileName);
            }
            if (!isValidJavaClassName(fileName)) {
                showError(
                    "Invalid Java name",
                    "Le nom doit etre un identifiant Java valide et ne pas etre un mot-cle."
                );
                return;
            }
            fileName = fileName + ".java";
        } else if (TEXT_FILE.equals(fileType)) {
            if (!fileName.endsWith(".txt")) {
                fileName = fileName + ".txt";
            }
        }

        Path file = parent.resolve(fileName).toAbsolutePath().normalize();
        if (!isInsideProject(file)) {
            showError("Invalid location", "File must be inside the current project.");
            return;
        }
        if (Files.exists(file)) {
            showError("Already exists", "File already exists.");
            return;
        }

        try {
            Files.createFile(file);
            if (isJavaSourceType(fileType)) {
                String className = stripExtension(file.getFileName().toString());
                String packageName = getPackageNameForDirectory(parent);
                String template = buildJavaSourceTemplate(fileType, packageName, className);
                Files.write(file, template.getBytes(StandardCharsets.UTF_8));
            }
            reloadTree();
            view.setStatus("Created file " + fileName);
            openFile(file);
        } catch (IOException e) {
            showError("Create failed", "Cannot create file: " + e.getMessage());
        }
    }

    private void openDebugWindow() {
        if (dirty) {
            saveCurrentFile();
        }

        Stage stage = new Stage();
        stage.setTitle("IDE7 - Debug");
        stage.setMinWidth(840);
        stage.setMinHeight(560);

        Label title = new Label("Debug du projet");
        title.setFont(Font.font(24));
        title.setTextFill(Color.web("#1a1a1a"));

        Label targetLabel = new Label("Classe cible: " + getDebugMainClassLabel());
        targetLabel.setTextFill(Color.web("#555555"));

        // Label chrono — mis à jour pendant l'exécution
        Label chronoLabel = new Label("");
        chronoLabel.setTextFill(Color.web("#555555"));

        VBox steps = new VBox(8);
        steps.setPadding(new Insets(12));
        steps.setStyle(
            "-fx-background-color: #fafafa;"
                + "-fx-border-color: #d0d0d0;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
        );

        TextArea console = new TextArea();
        console.setEditable(false);
        console.setWrapText(false);
        console.setStyle(
            "-fx-font-family: 'Consolas', 'Menlo', monospace;"
                + "-fx-font-size: 13px;"
                + "-fx-control-inner-background: #ffffff;"
                + "-fx-background-color: #ffffff;"
                + "-fx-text-fill: #1a1a1a;"
                + "-fx-border-color: #d0d0d0;"
        );

        VBox memoryPanel = new VBox(8);
        memoryPanel.setPadding(new Insets(12));
        memoryPanel.setMinHeight(110);
        memoryPanel.setStyle(
            "-fx-background-color: #fafafa;"
                + "-fx-border-color: #d0d0d0;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
        );
        addMemorySnapshot(memoryPanel, "Memoire", "Lance l'execution pour suivre l'utilisation memoire.", "#555555");

        Button compileButton = new Button("Compiler");
        compileButton.setGraphic(createIcon(AntDesignIconsOutlined.CODE, "#555555"));
        compileButton.setStyle(primaryButtonStyle("#f0f0f0", "#1a1a1a"));

        Button runButton = new Button("Compiler + executer");
        runButton.setGraphic(createIcon(AntDesignIconsOutlined.PLAY_CIRCLE, "#555555"));
        runButton.setStyle(primaryButtonStyle("#f0f0f0", "#1a1a1a"));

        Button stopButton = new Button("Arreter");
        stopButton.setGraphic(createIcon(AntDesignIconsOutlined.PAUSE_CIRCLE, "#555555"));
        stopButton.setStyle(primaryButtonStyle("#f0f0f0", "#1a1a1a"));

        Button clearButton = new Button("Effacer");
        clearButton.setStyle(primaryButtonStyle("#f0f0f0", "#1a1a1a"));
        clearButton.setOnAction(event -> {
            console.clear();
            steps.getChildren().clear();
            memoryPanel.getChildren().clear();
            chronoLabel.setText("");
            addMemorySnapshot(memoryPanel, "Memoire", "Lance l'execution pour suivre l'utilisation memoire.", "#555555");
        });

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(primaryButtonStyle("#f0f0f0", "#1a1a1a"));
        closeButton.setOnAction(event -> stage.close());

        compileButton.setOnAction(event -> runDebugWorkflow(false, steps, memoryPanel, console, chronoLabel, stage));
        runButton.setOnAction(event -> runDebugWorkflow(true, steps, memoryPanel, console, chronoLabel, stage));
        stopButton.setOnAction(event -> stopDebugProcess(steps, console));

        HBox actions = new HBox(10, compileButton, runButton, stopButton, clearButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        ScrollPane memoryScrollPane = new ScrollPane(memoryPanel);
        memoryScrollPane.setFitToWidth(true);
        memoryScrollPane.setPannable(true);
        memoryScrollPane.setMinWidth(360);
        memoryScrollPane.setPrefWidth(520);
        memoryScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

       HBox debugPanels = new HBox(12, steps, memoryScrollPane);

       HBox.setHgrow(steps, Priority.ALWAYS);
       HBox.setHgrow(memoryScrollPane, Priority.ALWAYS);

       debugPanels.setPrefHeight(330);
       debugPanels.setMinHeight(260);
       debugPanels.setMaxHeight(330);

       console.setPrefHeight(220);
       console.setMinHeight(180);
       console.setMaxHeight(220);

       VBox header = new VBox(3, title, targetLabel, chronoLabel);
       VBox content = new VBox(14, header, actions, debugPanels, console);
       content.setPadding(new Insets(18));
       content.setPrefSize(920, 640);
       content.setMinSize(800, 520);
       content.setStyle("-fx-background-color: white;");

       VBox.setVgrow(debugPanels, Priority.NEVER);
       VBox.setVgrow(console, Priority.NEVER);

       stage.setScene(new Scene(content, 920, 640));
       stage.show();
       stage.toFront();
    }
    
    private String getDebugMainClassLabel() {
        if (openedFile == null || !openedFile.toString().endsWith(".java")) {
            return "aucun fichier Java ouvert";
        }
        try {
            return getMainClassName(openedFile);
        } catch (IOException e) {
            return openedFile.getFileName().toString();
        }
    }

    private void runDebugWorkflow(boolean executeAfterCompile, VBox steps, VBox memoryPanel, TextArea console, Label chronoLabel, Stage stage) {
        if (openedFile == null || !openedFile.toString().endsWith(".java")) {
            showError("Debug impossible", "Ouvre d'abord le fichier Java contenant la methode main.");
            return;
        }

        steps.getChildren().clear();
        memoryPanel.getChildren().clear();
        console.clear();
        chronoLabel.setText("");
        addDebugStep(steps, "Preparation", "Compilation du projet ouvert", "#888888");
        addMemorySnapshot(memoryPanel, "Memoire", "En attente de l'execution du programme.", "#888888");

        long startTime = System.currentTimeMillis();

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Path outputDirectory = project.getPath().resolve(".ide7-debug").resolve("bin");
                    Files.createDirectories(outputDirectory);

                    List<Path> sources = collectJavaSources();
                    if (sources.isEmpty()) {
                        appendDebugConsole(console, "[INFO] Aucun fichier Java trouve dans le projet.\n");
                        addDebugStepAsync(steps, "Erreur", "Aucune source Java", "#888888");
                        return;
                    }

                    addDebugStepAsync(steps, "Compilation", sources.size() + " fichier(s) Java", "#888888");
                    ExecutionResultInfo compileResult = compileProjectSources(sources, outputDirectory);

                    if (!compileResult.output.isEmpty()) {
                        appendDebugConsole(console, "[COMPILATION]\n" + compileResult.output + "\n");
                    }

                    if (compileResult.exitCode != 0) {
                        addDebugStepAsync(steps, "Compilation echouee", "Code retour " + compileResult.exitCode, "#888888");
                        updateChrono(chronoLabel, startTime);
                        return;
                    }

                    addDebugStepAsync(steps, "Compilation reussie", outputDirectory.toString(), "#888888");

                    if (!executeAfterCompile) {
                        updateChrono(chronoLabel, startTime);
                        return;
                    }

                    String mainClass = getMainClassName(openedFile);
                    addDebugStepAsync(steps, "Execution", mainClass, "#888888");

                    // Lance l'exécution avec stdout ET stderr séparés
                    ExecutionResultInfoFull runResult = executeMainClassFull(mainClass, outputDirectory, memoryPanel);

                    // Affiche stdout
                    if (!runResult.stdout.isEmpty()) {
                        appendDebugConsole(console, "[SORTIE]\n" + runResult.stdout + "\n");
                    }

                    // Affiche stderr avec préfixe clair
                    if (!runResult.stderr.isEmpty()) {
                        appendDebugConsole(console, "[ERREURS]\n" + runResult.stderr + "\n");

                        // Résumé d'exception dans les cartes
                        fr.inptoulouse.sn.ide7.debug.RuntimeExceptionParser parser =
                            new fr.inptoulouse.sn.ide7.debug.RuntimeExceptionParser();
                        java.util.List<fr.inptoulouse.sn.ide7.debug.RuntimeExceptionInfo> exceptions =
                            parser.parse(runResult.stderr);

                        for (fr.inptoulouse.sn.ide7.debug.RuntimeExceptionInfo ex : exceptions) {
                            String detail = ex.getMessage().isEmpty()
                            ? ex.getExceptionType()
                            : ex.getExceptionType() + " : " + ex.getMessage();
                            addDebugStepAsync(steps, "Exception detectee", detail, "#888888");

                            // Erreur cliquable : si la stack trace contient un fichier:ligne
                            String trace = ex.getStackTrace();
                            java.util.regex.Matcher m = java.util.regex.Pattern
                                .compile("at .+\\((.+\\.java):(\\d+)\\)")
                                .matcher(trace);
                            if (m.find()) {
                                String fileName = m.group(1);
                                int lineNumber = Integer.parseInt(m.group(2));
                                addClickableErrorStep(steps, stage,
                                    "Ligne " + lineNumber + " dans " + fileName,
                                    "Cliquer pour ouvrir dans l'editeur",
                                    fileName, lineNumber);
                            }
                        }
                    }

                    updateChrono(chronoLabel, startTime);

                    if (runResult.exitCode == 0) {
                        addDebugStepAsync(steps, "Execution terminee",
                            "Succes · pic memoire " + formatMemory(runResult.peakMemoryKb), "#888888");
                    } else {
                        addDebugStepAsync(steps, "Execution echouee",
                            "Code retour " + runResult.exitCode + " · pic memoire " + formatMemory(runResult.peakMemoryKb),
                            "#888888");
                    }

                } catch (Exception e) {
                    appendDebugConsole(console, "[ERREUR INTERNE] " + e.getMessage() + "\n");
                    addDebugStepAsync(steps, "Erreur", e.getMessage(), "#888888");
                    updateChrono(chronoLabel, startTime);
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private List<Path> collectJavaSources() throws IOException {
        List<Path> sources = new ArrayList<Path>();
        try (Stream<Path> paths = Files.walk(project.getPath())) {
            java.util.Iterator<Path> iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (Files.isRegularFile(path) && path.toString().endsWith(".java") && !isIgnoredForUml(path)) {
                    sources.add(path);
                }
            }
        }
        return sources;
    }

    private ExecutionResultInfo compileProjectSources(List<Path> sources, Path outputDirectory) throws IOException, InterruptedException {
        List<String> command = new ArrayList<String>();
        command.add("javac");
        command.add("-encoding");
        command.add("UTF-8");
        command.add("-d");
        command.add(outputDirectory.toString());
        for (Path source : sources) {
            command.add(source.toString());
        }
        return runProcess(command, project.getPath());
    }

    private ExecutionResultInfo executeMainClass(String mainClass, Path outputDirectory) throws IOException, InterruptedException {
        return executeMainClass(mainClass, outputDirectory, null);
    }

    private ExecutionResultInfo executeMainClass(String mainClass, Path outputDirectory, VBox memoryPanel)
        throws IOException, InterruptedException {
        List<String> command = new ArrayList<String>();
        command.add("java");
        command.add("-cp");
        command.add(outputDirectory.toString());
        command.add(mainClass);
        return runProcess(command, project.getPath(), memoryPanel);
    }

    private ExecutionResultInfo runProcess(List<String> command, Path directory) throws IOException, InterruptedException {
        return runProcess(command, directory, null);
    }

    private ExecutionResultInfo runProcess(List<String> command, Path directory, VBox memoryPanel)
        throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory.toFile());
        builder.redirectErrorStream(true);
        Process process = builder.start();
        debugProcess = process;
        AtomicBoolean monitorRunning = new AtomicBoolean(true);
        AtomicLong peakMemoryKb = new AtomicLong(-1L);

        Thread memoryMonitor = null;
        if (memoryPanel != null) {
            memoryMonitor = new Thread(new Runnable() {
                @Override
                public void run() {
                    monitorProcessMemory(process, memoryPanel, monitorRunning, peakMemoryKb);
                }
            });
            memoryMonitor.setDaemon(true);
            memoryMonitor.start();
        }

        String output = readProcessOutput(process.getInputStream());
        int exitCode = process.waitFor();
        monitorRunning.set(false);
        if (memoryMonitor != null) {
            memoryMonitor.join(1000);
            addMemorySnapshotAsync(
                memoryPanel,
                "Résumé mémoire",
                "Pic observe: " + formatMemory(peakMemoryKb.get()),
                exitCode == 0 ? "#15803d" : "#dc2626"
            );
        }
        if (debugProcess == process) {
            debugProcess = null;
        }
        return new ExecutionResultInfo(exitCode, output, peakMemoryKb.get());
    }

    private void monitorProcessMemory(
        Process process,
        VBox memoryPanel,
        AtomicBoolean running,
        AtomicLong peakMemoryKb
    ) {
        long pid = process.pid();
        int sample = 1;

        addMemorySnapshotAsync(memoryPanel, "Processus", "PID " + pid, "#1d4ed8");

        while (running.get() && process.isAlive()) {
            long rssKb = readResidentMemoryKb(pid);
            if (rssKb >= 0) {
                updatePeak(peakMemoryKb, rssKb);
                if (sample == 1 || sample % 4 == 0) {
                    addMemorySnapshotAsync(
                        memoryPanel,
                        "Snapshot " + sample,
                        "RSS " + formatMemory(rssKb) + " · pic " + formatMemory(peakMemoryKb.get()),
                        "#7c3aed"
                    );
                }
                sample++;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        long finalRssKb = readResidentMemoryKb(pid);
        if (finalRssKb >= 0) {
            updatePeak(peakMemoryKb, finalRssKb);
            addMemorySnapshotAsync(memoryPanel, "Fin", "Derniere mesure RSS " + formatMemory(finalRssKb), "#475569");
        }
    }

    private void updatePeak(AtomicLong peakMemoryKb, long rssKb) {
        long currentPeak = peakMemoryKb.get();
        while (rssKb > currentPeak && !peakMemoryKb.compareAndSet(currentPeak, rssKb)) {
            currentPeak = peakMemoryKb.get();
        }
    }

    private long readResidentMemoryKb(long pid) {
        try {
            ProcessBuilder builder = new ProcessBuilder("ps", "-o", "rss=", "-p", Long.toString(pid));
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String output = readProcessOutput(process.getInputStream()).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0 || output.isEmpty()) {
                return -1L;
            }
            return Long.parseLong(output.split("\\s+")[0]);
        } catch (Exception e) {
            return -1L;
        }
    }

    private String formatMemory(long memoryKb) {
        if (memoryKb < 0) {
            return "indisponible";
        }
        double memoryMb = memoryKb / 1024.0;
        return String.format(java.util.Locale.US, "%.1f Mo", memoryMb);
    }

    private String readProcessOutput(InputStream stream) throws IOException {
        byte[] bytes = stream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String getMainClassName(Path javaFile) throws IOException {
        String source = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
        String className = stripExtension(javaFile.getFileName().toString());
        java.util.regex.Matcher packageMatcher = java.util.regex.Pattern
            .compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;")
            .matcher(source);
        if (packageMatcher.find()) {
            return packageMatcher.group(1) + "." + className;
        }
        return className;
    }
    
    /** Met à jour le label chrono dans le thread JavaFX. */
    private void updateChrono(Label chronoLabel, long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        Platform.runLater(() ->
            chronoLabel.setText(String.format("Duree : %.1f s", elapsed / 1000.0))
        );
    }

    /**
     * Ajoute une carte cliquable dans les étapes.
     * Au clic, ouvre le fichier Java à la ligne indiquée dans l'éditeur.
     */
    private void addClickableErrorStep(VBox steps, Stage debugStage,
                                       String title, String detail,
                                       String fileName, int lineNumber) {
        Platform.runLater(() -> {
            Label titleLabel = new Label(">>> " + title);
            titleLabel.setTextFill(Color.web("#1a1a1a"));
            titleLabel.setStyle("-fx-font-weight: bold; -fx-underline: true; -fx-cursor: hand;");

            Label detailLabel = new Label(detail);
            detailLabel.setTextFill(Color.web("#555555"));
            detailLabel.setWrapText(true);

            VBox card = new VBox(2, titleLabel, detailLabel);
            card.setPadding(new Insets(9, 12, 9, 12));
            card.setStyle(
                "-fx-background-color: #f5f5f5;"
                    + "-fx-border-color: #888888;"
                    + "-fx-border-width: 0 0 0 4;"
                    + "-fx-background-radius: 4;"
                    + "-fx-border-radius: 4;"
                    + "-fx-cursor: hand;"
            );

            card.setOnMouseClicked(event -> {
                // Cherche le fichier dans le projet
                try {
                    java.util.Optional<Path> found;
                    try (java.util.stream.Stream<Path> walk = Files.walk(project.getPath())) {
                        found = walk
                            .filter(p -> p.getFileName().toString().equals(fileName))
                            .findFirst();
                    }
                    if (found.isPresent()) {
                        Path target = found.get();
                        // Ouvre le fichier dans l'éditeur principal
                        openFile(target);
                        // Calcule la position du caret à la ligne voulue
                        Platform.runLater(() -> {
                            String text = view.getEditorText();
                            String[] lines = text.split("\n", -1);
                            int pos = 0;
                            int targetLine = Math.min(lineNumber - 1, lines.length - 1);
                            for (int i = 0; i < targetLine; i++) {
                                pos += lines[i].length() + 1;
                            }
                            int lineEnd = pos + (targetLine < lines.length ? lines[targetLine].length() : 0);
                            view.selectEditorRange(pos, lineEnd);
                            // Ferme la fenêtre debug pour voir l'éditeur
                            debugStage.close();
                        });
                    } else {
                        showError("Fichier introuvable", "Impossible de trouver " + fileName + " dans le projet.");
                    }
                } catch (IOException ex) {
                    showError("Erreur", ex.getMessage());
                }
            });

            // Effet hover
            card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #ebebeb;"
                    + "-fx-border-color: #555555;"
                    + "-fx-border-width: 0 0 0 4;"
                    + "-fx-background-radius: 4;"
                    + "-fx-border-radius: 4;"
                    + "-fx-cursor: hand;"
            ));
            card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #f5f5f5;"
                    + "-fx-border-color: #888888;"
                    + "-fx-border-width: 0 0 0 4;"
                    + "-fx-background-radius: 4;"
                    + "-fx-border-radius: 4;"
                    + "-fx-cursor: hand;"
            ));

            steps.getChildren().add(card);
        });
    }

    /**
     * Exécute une classe Java en capturant stdout ET stderr dans des buffers séparés.
     * Remplace executeMainClass() pour le workflow debug.
     */
    private ExecutionResultInfoFull executeMainClassFull(String mainClass, Path outputDirectory, VBox memoryPanel)
        throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(outputDirectory.toString());
        command.add(mainClass);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(project.getPath().toFile());
        builder.redirectErrorStream(false); // NE PAS fusionner stdout/stderr

        Process process = builder.start();
        debugProcess = process;

        AtomicBoolean monitorRunning = new AtomicBoolean(true);
        AtomicLong peakMemoryKb = new AtomicLong(-1L);

        // Lecture stdout
        StringBuilder stdoutBuf = new StringBuilder();
        Thread stdoutThread = new Thread(() -> {
            try {
                byte[] bytes = process.getInputStream().readAllBytes();
                stdoutBuf.append(new String(bytes, StandardCharsets.UTF_8));
            } catch (IOException e) { /* ignore */ }
        });

        // Lecture stderr
        StringBuilder stderrBuf = new StringBuilder();
        Thread stderrThread = new Thread(() -> {
            try {
                byte[] bytes = process.getErrorStream().readAllBytes();
                stderrBuf.append(new String(bytes, StandardCharsets.UTF_8));
            } catch (IOException e) { /* ignore */ }
        });

        // Suivi mémoire
        Thread memoryMonitor = null;
        if (memoryPanel != null) {
            memoryMonitor = new Thread(() -> monitorProcessMemory(process, memoryPanel, monitorRunning, peakMemoryKb));
            memoryMonitor.setDaemon(true);
            memoryMonitor.start();
        }

        stdoutThread.start();
        stderrThread.start();

        int exitCode = process.waitFor();
        stdoutThread.join();
        stderrThread.join();

        monitorRunning.set(false);
        if (memoryMonitor != null) {
            memoryMonitor.join(1000);
            addMemorySnapshotAsync(
                memoryPanel,
                "Resume memoire",
                "Pic observe: " + formatMemory(peakMemoryKb.get()),
                "#888888"
            );
        }

        if (debugProcess == process) {
            debugProcess = null;
        }

        return new ExecutionResultInfoFull(exitCode, stdoutBuf.toString(), stderrBuf.toString(), peakMemoryKb.get());
    }
    
    private void stopDebugProcess(VBox steps, TextArea console) {
        Process process = debugProcess;
        if (process == null || !process.isAlive()) {
            addDebugStep(steps, "Arret", "Aucun processus en cours", "#475569");
            return;
        }
        process.destroyForcibly();
        appendDebugConsole(console, "\nProcessus arrete par l'utilisateur.\n");
        addDebugStep(steps, "Arret", "Processus termine", "#dc2626");
    }

    private void addDebugStepAsync(VBox steps, String title, String detail, String color) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                addDebugStep(steps, title, detail, color);
            }
        });
    }

    private void addDebugStep(VBox steps, String title, String detail, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#1a1a1a"));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label detailLabel = new Label(detail == null ? "" : detail);
        detailLabel.setTextFill(Color.web("#475569"));
        detailLabel.setWrapText(true);

        VBox card = new VBox(2, titleLabel, detailLabel);
        card.setPadding(new Insets(9, 12, 9, 12));
        card.setStyle(
            "-fx-background-color: white;"
            	+ "-fx-border-color: #888888;"
            	+ "-fx-border-width: 0 0 0 4;"
                + "-fx-background-radius: 6;"
                + "-fx-border-radius: 6;"
        );
        steps.getChildren().add(card);
    }

    private void addMemorySnapshotAsync(VBox memoryPanel, String title, String detail, String color) {
        if (memoryPanel == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                addMemorySnapshot(memoryPanel, title, detail, color);
            }
        });
    }

    private void addMemorySnapshot(VBox memoryPanel, String title, String detail, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#1a1a1a"));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label detailLabel = new Label(detail == null ? "" : detail);
        detailLabel.setTextFill(Color.web("#475569"));
        detailLabel.setWrapText(true);

        VBox card = new VBox(2, titleLabel, detailLabel);
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setStyle(
            "-fx-background-color: white;"
            	+ "-fx-border-color: #888888;"
            	+ "-fx-border-width: 0 0 0 4;"
                + "-fx-background-radius: 6;"
                + "-fx-border-radius: 6;"
        );
        memoryPanel.getChildren().add(card);
    }

    private void appendDebugConsole(TextArea console, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                console.appendText(text);
                if (!text.endsWith("\n")) {
                    console.appendText("\n");
                }
            }
        });
    }

    private void createFolder() {
        Path parent = getSelectedDirectoryOrParent();
        if (parent == null) {
            showError("No target", "Select a folder or file first.");
            return;
        }
        if (!isInsideProject(parent)) {
            parent = project.getPath();
        }

        TextInputDialog dialog = new TextInputDialog("com.example");
        dialog.setTitle("New Package");
        dialog.setHeaderText("Create package in: " + parent);
        dialog.setContentText("Package:");
        dialog.showAndWait();
        String folderName = dialog.getResult();
        if (folderName == null) {
            return;
        }
        folderName = folderName.trim();
        if (!isValidPackageName(folderName)) {
            showError(
                "Invalid package",
                "Le nom du package doit utiliser des identifiants Java valides separes par des points."
            );
            return;
        }

        Path folder = parent.resolve(folderName.replace('.', File.separatorChar)).toAbsolutePath().normalize();
        if (!isInsideProject(folder)) {
            showError("Invalid location", "Folder must be inside the current project.");
            return;
        }
        if (Files.exists(folder)) {
            showError("Already exists", "Folder already exists.");
            return;
        }

        try {
            Files.createDirectories(folder);
            reloadTree();
            view.setStatus("Created package " + folderName);
        } catch (IOException e) {
            showError("Create failed", "Cannot create folder: " + e.getMessage());
        }
    }

    private void generateUmlFromProject() {
        if (dirty) {
            saveCurrentFile();
        }

        List<JavaClass> classes = new ArrayList<JavaClass>();
        JavaParser parser = new JavaParser();
        int skippedFiles = 0;

        try (Stream<Path> paths = Files.walk(project.getPath())) {
            java.util.Iterator<Path> iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (!Files.isRegularFile(path) || !path.toString().endsWith(".java") || isIgnoredForUml(path)) {
                    continue;
                }

                try {
                    String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                    classes.addAll(parser.parseAll(source));
                } catch (RuntimeException e) {
                    skippedFiles++;
                }
            }
        } catch (IOException e) {
            showError("Generation UML impossible", "Impossible de lire le projet: " + e.getMessage());
            return;
        }

        if (classes.isEmpty()) {
            showError("Generation UML impossible", "Aucune classe Java analysable n'a ete trouvee dans le projet.");
            return;
        }

        String umlText = new UMLFormatter().format(classes);
        if (skippedFiles > 0) {
            umlText = "// " + skippedFiles + " fichier(s) Java ignore(s) car non analysable(s).\n\n" + umlText;
        }

        lastGeneratedUmlText = umlText;
        lastGeneratedUmlClasses = new ArrayList<JavaClass>(classes);
        showUmlWindow(umlText, classes, skippedFiles);
        view.setStatus("UML genere depuis le projet et affiche dans une fenetre dediee.");
    }

    private void generateClassFromUml() {
        if (lastGeneratedUmlClasses != null && !lastGeneratedUmlClasses.isEmpty()) {
            generateClassesFromUmlModel(lastGeneratedUmlClasses);
            return;
        }

        String umlText = view.getEditorText();
        if ((umlText == null || umlText.trim().isEmpty()) && lastGeneratedUmlText != null) {
            umlText = lastGeneratedUmlText;
        }
        generateClassFromUml(umlText);
    }

    private void generateClassFromUml(String umlText) {
        if (umlText == null || umlText.trim().isEmpty()) {
            showError("UML manquant", "Genere un UML ou colle un diagramme UML textuel avant de generer une classe.");
            return;
        }

        List<JavaClass> classes;
        try {
            classes = new UMLClassGenerator().parseAll(umlText);
        } catch (IllegalArgumentException e) {
            showError("UML invalide", e.getMessage());
            return;
        }

        if (classes.isEmpty()) {
            showError("UML invalide", "Aucune classe UML exploitable n'a ete trouvee.");
            return;
        }

        generateClassesFromUmlModel(classes);
    }

    private void openUmlDesigner() {
        final Stage stage = new Stage();
        stage.setTitle("IDE7 - Designer UML vers Java");
        stage.setMinWidth(900);
        stage.setMinHeight(620);

        final List<DesignerClass> designerClasses = new ArrayList<DesignerClass>();
        final List<JavaRelation> designerRelations = new ArrayList<JavaRelation>();
        final int[] selectedIndex = new int[] {-1};

        Label title = new Label("Creer un diagramme UML");
        title.setFont(Font.font(24));
        title.setTextFill(Color.web("#0f172a"));

        Label subtitle = new Label("Ajoute des rectangles, complete leurs attributs/methodes, puis cree les fichiers Java.");
        subtitle.setTextFill(Color.web("#64748b"));

        FlowPane classGrid = new FlowPane();
        classGrid.setHgap(18);
        classGrid.setVgap(18);
        classGrid.setPadding(new Insets(4));
        classGrid.setMinWidth(620);

        VBox relationsList = new VBox(8);
        VBox relationsPanel = createDesignerRelationsPanel(designerClasses, designerRelations, relationsList);

        Runnable[] refresh = new Runnable[1];
        refresh[0] = new Runnable() {
            @Override
            public void run() {
                classGrid.getChildren().clear();
                for (int i = 0; i < designerClasses.size(); i++) {
                    classGrid.getChildren().add(createDesignerClassCard(
                        designerClasses,
                        designerRelations,
                        i,
                        selectedIndex,
                        refresh[0]
                    ));
                }

                relationsList.getChildren().clear();
                for (JavaRelation relation : designerRelations) {
                    relationsList.getChildren().add(createUmlRelationCard(relation));
                }
            }
        };

        Button addClassButton = new Button("Ajouter un rectangle");
        addClassButton.setGraphic(createIcon(AntDesignIconsOutlined.PLUS, "#1d4ed8"));
        addClassButton.setStyle(primaryButtonStyle("#eff6ff", "#1d4ed8"));
        addClassButton.setOnAction(event -> {
            DesignerClass designerClass = askDesignerClass();
            if (designerClass == null) {
                return;
            }
            designerClasses.add(designerClass);
            selectedIndex[0] = designerClasses.size() - 1;
            refresh[0].run();
        });

        Button generateButton = new Button("Generer les classes Java");
        generateButton.setGraphic(createIcon(AntDesignIconsOutlined.FILE_ADD, "#0f766e"));
        generateButton.setStyle(primaryButtonStyle("#ecfdf5", "#065f46"));
        generateButton.setOnAction(event -> {
            List<JavaClass> classes = toJavaClasses(designerClasses, designerRelations);
            if (classes.isEmpty()) {
                showError("Diagramme vide", "Ajoute au moins une classe, interface ou classe abstraite.");
                return;
            }
            generateClassesFromUmlModel(classes);
            stage.close();
        });

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(primaryButtonStyle("#f1f5f9", "#334155"));
        closeButton.setOnAction(event -> stage.close());

        HBox topActions = new HBox(10, addClassButton, generateButton, closeButton);
        topActions.setAlignment(Pos.CENTER_RIGHT);

        VBox diagram = new VBox(18, classGrid, relationsPanel);
        diagram.setPadding(new Insets(14));
        diagram.setMinWidth(760);

        ScrollPane scrollPane = new ScrollPane(diagram);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinViewportWidth(760);
        scrollPane.setMinViewportHeight(430);
        classGrid.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(70));
        scrollPane.setStyle(
            "-fx-background-color: #f8fafc;"
                + "-fx-background: #f8fafc;"
                + "-fx-border-color: #e2e8f0;"
                + "-fx-border-radius: 8;"
        );

        VBox content = new VBox(14, new VBox(3, title, subtitle), topActions, scrollPane);
        content.setPadding(new Insets(18));
        content.setMinSize(860, 580);
        content.setPrefSize(980, 680);
        content.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        refresh[0].run();

        stage.setScene(new Scene(content, 980, 680));
        stage.show();
        stage.toFront();
    }

    private DesignerClass askDesignerClass() {
        TextInputDialog nameDialog = new TextInputDialog("NewClass");
        nameDialog.setTitle("Nouveau rectangle UML");
        nameDialog.setHeaderText("Nom du rectangle");
        nameDialog.setContentText("Nom:");
        nameDialog.showAndWait();
        String name = nameDialog.getResult();
        if (name == null) {
            return null;
        }
        name = name.trim();
        if (!isValidJavaClassName(name)) {
            showError("Nom invalide", "Le nom doit etre un identifiant Java valide.");
            return null;
        }

        List<String> choices = Arrays.asList("class", "abstract class", "interface", "enum");
        ChoiceDialog<String> kindDialog = new ChoiceDialog<String>("class", choices);
        kindDialog.setTitle("Type UML");
        kindDialog.setHeaderText("Type du rectangle " + name);
        kindDialog.setContentText("Type:");
        kindDialog.showAndWait();
        String kind = kindDialog.getResult();
        if (kind == null) {
            return null;
        }

        return new DesignerClass(name, kind);
    }

    private VBox createDesignerClassCard(
        List<DesignerClass> classes,
        List<JavaRelation> relations,
        int index,
        int[] selectedIndex,
        Runnable refresh
    ) {
        DesignerClass designerClass = classes.get(index);
        boolean selected = selectedIndex[0] == index;

        Label kind = new Label(designerClass.kind);
        kind.setTextFill(Color.web("#0369a1"));
        kind.setStyle("-fx-font-weight: bold;");

        Label name = new Label(designerClass.name);
        name.setFont(Font.font("JetBrains Mono", 17));
        name.setTextFill(Color.web("#0f172a"));
        name.setMaxWidth(Double.MAX_VALUE);
        name.setAlignment(Pos.CENTER);

        VBox header = new VBox(2, kind, name);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 12, 10, 12));
        header.setStyle("-fx-background-color: #e0f2fe; -fx-border-color: #0369a1; -fx-border-width: 0 0 1 0;");

        VBox attributes = createDesignerSection("Attributs", designerClass.attributes);
        VBox methods = createDesignerSection("Methodes", designerClass.methods);

        Button addAttribute = new Button("+ attribut");
        addAttribute.setStyle(primaryButtonStyle("#eff6ff", "#1d4ed8"));
        addAttribute.setOnAction(event -> {
            String value = askLine("Ajouter un attribut", "Format: - nom : String", "- name : String");
            if (value == null) {
                return;
            }
            if (toAttribute(value) == null) {
                showError("Attribut invalide", "Format attendu: + nom : Type, - nom : Type ou # nom : Type.");
                return;
            }
            designerClass.attributes.add(value.trim());
            refresh.run();
        });

        Button addMethod = new Button("+ methode");
        addMethod.setStyle(primaryButtonStyle("#ecfdf5", "#065f46"));
        addMethod.setOnAction(event -> {
            String value = askLine("Ajouter une methode", "Format: + getName() : String", "+ method() : void");
            if (value == null) {
                return;
            }
            if (toMethod(value) == null) {
                showError("Methode invalide", "Format attendu: + nom(param : Type) : Retour.");
                return;
            }
            designerClass.methods.add(value.trim());
            refresh.run();
        });

        Button remove = new Button("Supprimer");
        remove.setStyle(primaryButtonStyle("#fee2e2", "#991b1b"));
        remove.setOnAction(event -> {
            String removedName = designerClass.name;
            classes.remove(index);
            for (int i = relations.size() - 1; i >= 0; i--) {
                JavaRelation relation = relations.get(i);
                if (removedName.equals(relation.getSource()) || removedName.equals(relation.getTarget())) {
                    relations.remove(i);
                }
            }
            selectedIndex[0] = -1;
            refresh.run();
        });

        HBox cardActions = new HBox(8, addAttribute, addMethod, remove);
        cardActions.setAlignment(Pos.CENTER_LEFT);
        cardActions.setPadding(new Insets(10, 12, 12, 12));

        VBox card = new VBox(header, attributes, methods, cardActions);
        card.setMinWidth(360);
        card.setPrefWidth(430);
        card.setMaxWidth(520);
        card.setStyle(
            "-fx-background-color: white;"
                + "-fx-border-color: " + (selected ? "#7c3aed" : "#0369a1") + ";"
                + "-fx-border-width: " + (selected ? "3" : "2") + ";"
                + "-fx-border-radius: 3;"
                + "-fx-background-radius: 3;"
        );
        card.setOnMouseClicked(event -> {
            selectedIndex[0] = index;
            refresh.run();
        });
        return card;
    }

    private VBox createDesignerSection(String title, List<String> lines) {
        Label sectionTitle = new Label(title);
        sectionTitle.setTextFill(Color.web("#64748b"));
        sectionTitle.setStyle("-fx-font-weight: bold;");

        VBox content = new VBox(4, sectionTitle);
        content.setMinHeight(52);
        content.setPadding(new Insets(8, 12, 8, 12));
        content.setStyle("-fx-border-color: #94a3b8; -fx-border-width: 0 0 1 0;");

        if (lines.isEmpty()) {
            content.getChildren().add(createUmlLine("    "));
        } else {
            for (String line : lines) {
                content.getChildren().add(createUmlLine(line));
            }
        }
        return content;
    }

    private VBox createDesignerRelationsPanel(
        List<DesignerClass> classes,
        List<JavaRelation> relations,
        VBox relationList
    ) {
        Label title = new Label("Liaisons");
        title.setFont(Font.font(18));
        title.setTextFill(Color.web("#0f172a"));
        title.setStyle("-fx-font-weight: bold;");

        Button addRelation = new Button("Ajouter une liaison");
        addRelation.setGraphic(createIcon(AntDesignIconsOutlined.PLUS, "#7c3aed"));
        addRelation.setStyle(primaryButtonStyle("#f3e8ff", "#6d28d9"));
        addRelation.setOnAction(event -> {
            JavaRelation relation = askDesignerRelation(classes);
            if (relation == null) {
                return;
            }
            relations.add(relation);
            relationList.getChildren().add(createUmlRelationCard(relation));
        });

        VBox panel = new VBox(12, new HBox(12, title, addRelation), relationList);
        panel.setPadding(new Insets(14));
        panel.setMinWidth(700);
        panel.setStyle(
            "-fx-background-color: white;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
        );
        return panel;
    }

    private JavaRelation askDesignerRelation(List<DesignerClass> classes) {
        if (classes.size() < 2) {
            showError("Liaison impossible", "Ajoute au moins deux rectangles.");
            return null;
        }

        List<String> names = new ArrayList<String>();
        for (DesignerClass designerClass : classes) {
            names.add(designerClass.name);
        }

        ChoiceDialog<String> sourceDialog = new ChoiceDialog<String>(names.get(0), names);
        sourceDialog.setTitle("Nouvelle liaison");
        sourceDialog.setHeaderText("Classe source");
        sourceDialog.setContentText("Source:");
        sourceDialog.showAndWait();
        String source = sourceDialog.getResult();
        if (source == null) {
            return null;
        }

        ChoiceDialog<String> targetDialog = new ChoiceDialog<String>(names.get(1), names);
        targetDialog.setTitle("Nouvelle liaison");
        targetDialog.setHeaderText("Classe cible");
        targetDialog.setContentText("Cible:");
        targetDialog.showAndWait();
        String target = targetDialog.getResult();
        if (target == null || target.equals(source)) {
            return null;
        }

        List<String> types = Arrays.asList("héritage", "implémentation", "association", "agrégation", "composition", "dépendance");
        ChoiceDialog<String> typeDialog = new ChoiceDialog<String>("association", types);
        typeDialog.setTitle("Nouvelle liaison");
        typeDialog.setHeaderText(source + " vers " + target);
        typeDialog.setContentText("Type:");
        typeDialog.showAndWait();
        String type = typeDialog.getResult();
        if (type == null) {
            return null;
        }

        return new JavaRelation(source, target, type);
    }

    private String askLine(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Valeur:");
        dialog.showAndWait();
        String result = dialog.getResult();
        if (result == null) {
            return null;
        }
        result = result.trim();
        return result.isEmpty() ? null : result;
    }

    private List<JavaClass> toJavaClasses(List<DesignerClass> designerClasses, List<JavaRelation> relations) {
        List<JavaClass> javaClasses = new ArrayList<JavaClass>();

        for (DesignerClass designerClass : designerClasses) {
            JavaClass javaClass = new JavaClass(designerClass.name);
            javaClass.setKind(designerClass.kind);

            for (String attributeLine : designerClass.attributes) {
                JavaAttribut attribute = toAttribute(attributeLine);
                if (attribute != null) {
                    javaClass.addAttribute(attribute);
                }
            }

            for (String methodLine : designerClass.methods) {
                JavaMethode method = toMethod(methodLine);
                if (method != null) {
                    javaClass.addMethod(method);
                }
            }

            for (JavaRelation relation : relations) {
                if (designerClass.name.equals(relation.getSource())) {
                    javaClass.addRelation(relation);
                }
            }

            javaClasses.add(javaClass);
        }

        return javaClasses;
    }

    private JavaAttribut toAttribute(String line) {
        if (line == null) {
            return null;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("^\\s*([+\\-#~])\\s+(\\w+)\\s*:\\s*([\\w<>\\[\\]]+)\\s*$")
            .matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        return new JavaAttribut(
            matcher.group(2),
            matcher.group(3),
            visibilityFromSymbol(matcher.group(1))
        );
    }

    private JavaMethode toMethod(String line) {
        if (line == null) {
            return null;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("^\\s*([+\\-#~])\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?::\\s*([\\w<>\\[\\]]+))?\\s*$")
            .matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        JavaMethode method = new JavaMethode(
            matcher.group(2),
            matcher.group(4) == null || matcher.group(4).trim().isEmpty() ? "void" : matcher.group(4).trim(),
            visibilityFromSymbol(matcher.group(1))
        );

        String params = matcher.group(3);
        if (params != null && !params.trim().isEmpty()) {
            String[] parts = params.split(",");
            for (String part : parts) {
                String[] tokens = part.trim().split("\\s*:\\s*");
                if (tokens.length == 2 && !tokens[0].trim().isEmpty() && !tokens[1].trim().isEmpty()) {
                    method.addParameter(new JavaParamettre(tokens[0].trim(), tokens[1].trim()));
                }
            }
        }

        return method;
    }

    private fr.inptoulouse.sn.ide7.uml.Visibility visibilityFromSymbol(String symbol) {
        if ("-".equals(symbol)) {
            return fr.inptoulouse.sn.ide7.uml.Visibility.PRIVATE;
        }
        if ("#".equals(symbol)) {
            return fr.inptoulouse.sn.ide7.uml.Visibility.PROTECTED;
        }
        return fr.inptoulouse.sn.ide7.uml.Visibility.PUBLIC;
    }

    private String primaryButtonStyle(String background, String color) {
        return "-fx-background-color: " + background + ";"
            + "-fx-text-fill: " + color + ";"
            + "-fx-border-color: #c0c0c0;"
            + "-fx-border-width: 1;"
            + "-fx-background-radius: 4;"
            + "-fx-border-radius: 4;"
            + "-fx-padding: 8 12 8 12;";
    }

    private void generateClassesFromUmlModel(List<JavaClass> classes) {
        if (classes == null || classes.isEmpty()) {
            showError("UML manquant", "Aucune classe UML a generer.");
            return;
        }

        Path parent = getSelectedDirectoryOrParent();
        if (parent == null || !isInsideProject(parent)) {
            parent = project.getPath();
        }

        UMLClassGenerator generator = new UMLClassGenerator();
        int createdCount = 0;
        int skippedCount = 0;
        Path firstCreatedFile = null;

        for (JavaClass javaClass : classes) {
            if (!isValidJavaClassName(javaClass.getName())) {
                skippedCount++;
                continue;
            }

            Path javaFile = parent.resolve(javaClass.getName() + ".java").toAbsolutePath().normalize();
            if (!isInsideProject(javaFile) || Files.exists(javaFile)) {
                skippedCount++;
                continue;
            }

            String packageName = getPackageNameForDirectory(parent);
            String source = generator.generateSource(
                javaClass,
                packageName,
                relationsFor(javaClass, classes),
                classes
            );

            try {
                Files.write(javaFile, source.getBytes(StandardCharsets.UTF_8));
                if (firstCreatedFile == null) {
                    firstCreatedFile = javaFile;
                }
                createdCount++;
            } catch (IOException e) {
                skippedCount++;
            }
        }

        if (createdCount == 0) {
            showError(
                "Generation impossible",
                "Aucune classe n'a ete creee. Les fichiers existent peut-etre deja ou les noms sont invalides."
            );
            return;
        }

        reloadTree();
        if (firstCreatedFile != null) {
            openFile(firstCreatedFile);
        }
        view.setStatus(
            createdCount + " fichier(s) Java genere(s) depuis l'UML"
                + (skippedCount > 0 ? " · " + skippedCount + " ignore(s)" : "")
        );
    }

    private void generateSingleClassFromUml(JavaClass javaClass) {
        if (!isValidJavaClassName(javaClass.getName())) {
            showError("Nom invalide", "Le nom de classe UML n'est pas un identifiant Java valide.");
            return;
        }

        Path parent = getSelectedDirectoryOrParent();
        if (parent == null || !isInsideProject(parent)) {
            parent = project.getPath();
        }

        Path javaFile = parent.resolve(javaClass.getName() + ".java").toAbsolutePath().normalize();
        if (!isInsideProject(javaFile)) {
            showError("Emplacement invalide", "La classe doit etre creee dans le projet ouvert.");
            return;
        }
        if (Files.exists(javaFile)) {
            showError("Classe existante", "Le fichier existe deja: " + javaFile.getFileName());
            return;
        }

        String packageName = getPackageNameForDirectory(parent);
        String source = new UMLClassGenerator().generateSource(javaClass, packageName);

        try {
            Files.write(javaFile, source.getBytes(StandardCharsets.UTF_8));
            reloadTree();
            openFile(javaFile);
            view.setStatus("Classe generee depuis UML: " + javaFile.getFileName());
        } catch (IOException e) {
            showError("Generation impossible", "Impossible d'ecrire la classe Java: " + e.getMessage());
        }
    }

    private List<JavaRelation> relationsFor(JavaClass javaClass, List<JavaClass> classes) {
        List<JavaRelation> relations = new ArrayList<JavaRelation>();
        if (javaClass == null || classes == null) {
            return relations;
        }

        for (JavaClass sourceClass : classes) {
            for (JavaRelation relation : sourceClass.getRelations()) {
                if (javaClass.getName().equals(relation.getSource())) {
                    relations.add(relation);
                }
            }
        }
        return relations;
    }

    private void showUmlWindow(String umlText, List<JavaClass> classes, int skippedFiles) {
        Stage stage = new Stage();
        stage.setTitle("IDE7 - UML genere");

        Label title = new Label("Diagramme UML");
        title.setFont(Font.font(24));
        title.setTextFill(Color.web("#0f172a"));

        Label subtitle = new Label(
            classes.size() + " classe(s) analysee(s)"
                + (skippedFiles > 0 ? " · " + skippedFiles + " fichier(s) ignore(s)" : "")
        );
        subtitle.setTextFill(Color.web("#64748b"));

        VBox heading = new VBox(3, title, subtitle);
        heading.setAlignment(Pos.CENTER_LEFT);

        FlowPane classGrid = new FlowPane();
        classGrid.setHgap(18);
        classGrid.setVgap(18);
        classGrid.setPadding(new Insets(4));
        for (JavaClass javaClass : classes) {
            classGrid.getChildren().add(createUmlClassBox(javaClass));
        }

        VBox diagram = new VBox(22);
        diagram.setPadding(new Insets(12));
        diagram.getChildren().add(classGrid);

        VBox relationBox = createUmlRelationsPanel(classes);
        if (relationBox != null) {
            diagram.getChildren().add(relationBox);
        }

        ScrollPane diagramScroll = new ScrollPane(diagram);
        diagramScroll.setFitToWidth(true);
        diagramScroll.setStyle(
            "-fx-background-color: #f8fafc;"
                + "-fx-background: #f8fafc;"
                + "-fx-border-color: #e2e8f0;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
        );

        Button generateButton = new Button("Generer une classe depuis cet UML");
        generateButton.setGraphic(createIcon(AntDesignIconsOutlined.FILE_ADD, "#0f766e"));
        generateButton.setStyle(
            "-fx-background-color: #ecfdf5;"
                + "-fx-text-fill: #065f46;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 9 14 9 14;"
        );
        generateButton.setOnAction(event -> {
            lastGeneratedUmlText = umlText;
            lastGeneratedUmlClasses = new ArrayList<JavaClass>(classes);
            generateClassesFromUmlModel(lastGeneratedUmlClasses);
        });

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
            "-fx-background-color: #f1f5f9;"
                + "-fx-text-fill: #334155;"
                + "-fx-background-radius: 6;"
                + "-fx-padding: 9 14 9 14;"
        );
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(10, generateButton, closeButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(14, heading, diagramScroll, actions);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color: white;");
        VBox.setVgrow(diagramScroll, Priority.ALWAYS);

        Scene scene = new Scene(content, 860, 620);
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    private VBox createUmlClassBox(JavaClass javaClass) {
        Label name = new Label(javaClass.getName());
        name.setFont(Font.font("JetBrains Mono", 17));
        name.setTextFill(Color.web("#0f172a"));
        name.setMaxWidth(Double.MAX_VALUE);
        name.setAlignment(Pos.CENTER);
        name.setStyle(
            "-fx-background-color: #e0f2fe;"
                + "-fx-border-color: #0369a1;"
                + "-fx-border-width: 0 0 1 0;"
                + "-fx-padding: 10 12 10 12;"
                + "-fx-font-weight: bold;"
        );

        VBox attributes = createUmlSection(javaClass.getAttributes().isEmpty() ? "    " : null);
        for (int i = 0; i < javaClass.getAttributes().size(); i++) {
            JavaAttribut attribut = javaClass.getAttributes().get(i);
            attributes.getChildren().add(createUmlLine(
                symbol(attribut.getVisibility()) + " " + attribut.getName() + " : " + attribut.getType()
            ));
        }

        VBox methods = createUmlSection(javaClass.getMethods().isEmpty() ? "    " : null);
        for (int i = 0; i < javaClass.getMethods().size(); i++) {
            JavaMethode methode = javaClass.getMethods().get(i);
            methods.getChildren().add(createUmlLine(formatMethodForBox(methode)));
        }

        VBox box = new VBox(name, attributes, methods);
        box.setMaxWidth(520);
        box.setStyle(
            "-fx-background-color: white;"
                + "-fx-border-color: #0369a1;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 2;"
                + "-fx-background-radius: 2;"
        );

        Rectangle marker = new Rectangle(8, 8, Color.web("#0369a1"));
        HBox wrapper = new HBox(10, marker, box);
        wrapper.setAlignment(Pos.TOP_LEFT);

        VBox result = new VBox(wrapper);
        result.setAlignment(Pos.CENTER_LEFT);
        return result;
    }

    private VBox createUmlSection(String emptyText) {
        VBox section = new VBox(2);
        section.setMinHeight(42);
        section.setPadding(new Insets(8, 12, 8, 12));
        section.setStyle("-fx-border-color: #94a3b8; -fx-border-width: 0 0 1 0;");
        if (emptyText != null) {
            section.getChildren().add(createUmlLine(emptyText));
        }
        return section;
    }

    private Label createUmlLine(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("JetBrains Mono", 13));
        label.setTextFill(Color.web("#1e293b"));
        label.setWrapText(false);
        return label;
    }

    private VBox createUmlRelationsPanel(List<JavaClass> classes) {
        VBox relationList = new VBox(10);
        int relationCount = 0;

        for (JavaClass javaClass : classes) {
            for (JavaRelation relation : javaClass.getRelations()) {
                relationList.getChildren().add(createUmlRelationCard(relation));
                relationCount++;
            }
        }

        if (relationCount == 0) {
            return null;
        }

        Label title = new Label("Liens entre classes");
        title.setFont(Font.font(18));
        title.setTextFill(Color.web("#0f172a"));
        title.setStyle("-fx-font-weight: bold;");

        Label subtitle = new Label(relationCount + " relation(s) detectee(s)");
        subtitle.setTextFill(Color.web("#64748b"));

        VBox header = new VBox(2, title, subtitle);

        VBox panel = new VBox(12, header, relationList);
        panel.setPadding(new Insets(14));
        panel.setStyle(
            "-fx-background-color: white;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
        );
        return panel;
    }

    private HBox createUmlRelationCard(JavaRelation relation) {
        String color = relationColor(relation.getType());

        Label source = createRelationEndpoint(relation.getSource(), "#eff6ff", "#1d4ed8");
        Label arrow = new Label(relationArrow(relation.getType()));
        arrow.setFont(Font.font("JetBrains Mono", 18));
        arrow.setTextFill(Color.web(color));
        arrow.setStyle("-fx-font-weight: bold;");

        Label target = createRelationEndpoint(relation.getTarget(), "#f8fafc", "#334155");
        Label type = new Label(relation.getType());
        type.setTextFill(Color.web(color));
        type.setStyle(
            "-fx-background-color: " + relationBackground(relation.getType()) + ";"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 5 10 5 10;"
                + "-fx-font-weight: bold;"
        );

        HBox row = new HBox(10, source, arrow, target, type);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 10, 9, 10));
        row.setStyle(
            "-fx-background-color: #f8fafc;"
                + "-fx-border-color: #e2e8f0;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 7;"
                + "-fx-background-radius: 7;"
        );
        return row;
    }

    private Label createRelationEndpoint(String text, String background, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("JetBrains Mono", 13));
        label.setTextFill(Color.web(color));
        label.setMinWidth(120);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
            "-fx-background-color: " + background + ";"
                + "-fx-border-color: " + color + ";"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 4;"
                + "-fx-background-radius: 4;"
                + "-fx-padding: 7 10 7 10;"
                + "-fx-font-weight: bold;"
        );
        return label;
    }

    private String relationArrow(String type) {
        if ("héritage".equals(type)) {
            return "--|>";
        }
        if ("implémentation".equals(type)) {
            return "..|>";
        }
        if ("dépendance".equals(type)) {
            return "..>";
        }
        if ("agrégation".equals(type)) {
            return "o-->";
        }
        if ("composition".equals(type)) {
            return "*-->";
        }
        return "-->";
    }

    private String relationColor(String type) {
        if ("héritage".equals(type)) {
            return "#7c3aed";
        }
        if ("implémentation".equals(type)) {
            return "#0f766e";
        }
        if ("association".equals(type)) {
            return "#b45309";
        }
        if ("agrégation".equals(type)) {
            return "#2563eb";
        }
        if ("composition".equals(type)) {
            return "#be123c";
        }
        if ("dépendance".equals(type)) {
            return "#475569";
        }
        return "#334155";
    }

    private String relationBackground(String type) {
        if ("héritage".equals(type)) {
            return "#f3e8ff";
        }
        if ("implémentation".equals(type)) {
            return "#ccfbf1";
        }
        if ("association".equals(type)) {
            return "#ffedd5";
        }
        if ("agrégation".equals(type)) {
            return "#dbeafe";
        }
        if ("composition".equals(type)) {
            return "#ffe4e6";
        }
        if ("dépendance".equals(type)) {
            return "#e2e8f0";
        }
        return "#f1f5f9";
    }

    private String formatMethodForBox(JavaMethode methode) {
        StringBuilder builder = new StringBuilder();
        builder.append(symbol(methode.getVisibility()))
            .append(' ')
            .append(methode.getName())
            .append('(');

        for (int i = 0; i < methode.getParameters().size(); i++) {
            JavaParamettre parameter = methode.getParameters().get(i);
            builder.append(parameter.getName()).append(" : ").append(parameter.getType());
            if (i < methode.getParameters().size() - 1) {
                builder.append(", ");
            }
        }

        builder.append(')');
        if (methode.getReturnType() != null && !methode.getReturnType().isEmpty()) {
            builder.append(" : ").append(methode.getReturnType());
        }
        return builder.toString();
    }

    private String symbol(fr.inptoulouse.sn.ide7.uml.Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return "+";
            case PRIVATE:
                return "-";
            case PROTECTED:
                return "#";
            default:
                return "~";
        }
    }

    private void deleteSelected() {
        TreeItem<Path> selected = view.getSelectedTreeItem();
        if (selected == null || selected.getValue() == null) {
            showError("No selection", "Select a file or folder.");
            return;
        }
        if (selected.getParent() == null) {
            showError("Not allowed", "Cannot delete the project root.");
            return;
        }

        Path target = selected.getValue();
        if (!isInsideProject(target)) {
            showError("Invalid selection", "Cannot delete outside of project.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete");
        confirm.setHeaderText("Delete selected item?");
        confirm.setContentText(target.toString());
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            deleteRecursively(target);
            if (openedFile != null && (openedFile.equals(target) || openedFile.startsWith(target))) {
                openedFile = null;
                view.setEditorText("");
                view.setEditorEditable(false);
                view.setCurrentFileLabel(null);
                view.setDirty(false);
                dirty = false;
                lastSavedContent = null;
            }
            reloadTree();
            view.setStatus("Deleted " + target.getFileName());
        } catch (IOException e) {
            showError("Delete failed", "Cannot delete: " + e.getMessage());
        }
    }

    private Path getSelectedDirectoryOrParent() {
        TreeItem<Path> selected = view.getSelectedTreeItem();
        if (selected == null || selected.getValue() == null) {
            return project.getPath();
        }

        Path selectedPath = selected.getValue();
        if (Files.isDirectory(selectedPath)) {
            return selectedPath;
        }
        Path parent = selectedPath.getParent();
        if (parent == null) {
            return project.getPath();
        }
        return parent;
    }

    private void openFile(Path file) {
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            autoSaveDelay.stop();
            openedFile = file;
            suppressAutoSave = true;
            view.setEditorText(content);
            suppressAutoSave = false;
            view.setEditorEditable(true);
            view.requestEditorFocus();
            lastSavedContent = content;
            dirty = false;
            view.setDirty(false);
            view.setCurrentFileLabel("Fichier: " + file);
            view.setStatus("Opened " + file.getFileName());
        } catch (IOException e) {
            showError("Open failed", "Cannot open file: " + e.getMessage());
        }
    }

    private String askFileType() {
        List<String> choices = Arrays.asList(
            JAVA_CLASS,
            JAVA_ABSTRACT_CLASS,
            JAVA_INTERFACE,
            JAVA_ENUM,
            JAVA_ANNOTATION,
            TEXT_FILE,
            CUSTOM_FILE
        );
        ChoiceDialog<String> dialog = new ChoiceDialog<String>(JAVA_CLASS, choices);
        dialog.setTitle("File Type");
        dialog.setHeaderText("Choose the file type");
        dialog.setContentText("Type:");
        dialog.showAndWait();
        return dialog.getResult();
    }

    private boolean isJavaIdentifier(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (JAVA_KEYWORDS.contains(value)) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(value.charAt(0))) {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            if (!Character.isJavaIdentifierPart(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidJavaClassName(String value) {
        return isJavaIdentifier(value);
    }

    private boolean isValidPackageName(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (value.startsWith(".") || value.endsWith(".") || value.contains("..")) {
            return false;
        }

        String[] parts = value.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (!isJavaIdentifier(parts[i])) {
                return false;
            }
        }
        return true;
    }

    private String getPackageNameForDirectory(Path directory) {
        if (directory == null) {
            return "";
        }

        Path root = project.getPath().toAbsolutePath().normalize();
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        if (!normalizedDirectory.startsWith(root) || normalizedDirectory.equals(root)) {
            return "";
        }

        Path relativePath = root.relativize(normalizedDirectory);
        StringBuilder packageName = new StringBuilder();
        for (Path segment : relativePath) {
            String part = segment.toString();
            if (!isJavaIdentifier(part)) {
                return "";
            }
            if (packageName.length() > 0) {
                packageName.append('.');
            }
            packageName.append(part);
        }
        return packageName.toString();
    }

    private boolean isJavaSourceType(String fileType) {
        return JAVA_CLASS.equals(fileType)
            || JAVA_ABSTRACT_CLASS.equals(fileType)
            || JAVA_INTERFACE.equals(fileType)
            || JAVA_ENUM.equals(fileType)
            || JAVA_ANNOTATION.equals(fileType);
    }

    private String getDefaultJavaSourceName(String fileType) {
        if (JAVA_ABSTRACT_CLASS.equals(fileType)) {
            return "NewAbstractClass";
        }
        if (JAVA_INTERFACE.equals(fileType)) {
            return "NewInterface";
        }
        if (JAVA_ENUM.equals(fileType)) {
            return "NewEnum";
        }
        if (JAVA_ANNOTATION.equals(fileType)) {
            return "NewAnnotation";
        }
        return "NewClass";
    }

    private String buildJavaSourceTemplate(String fileType, String packageName, String className) {
        StringBuilder builder = new StringBuilder();
        if (packageName != null && !packageName.isEmpty()) {
            builder.append("package ").append(packageName).append(";\n\n");
        }
        if (JAVA_ABSTRACT_CLASS.equals(fileType)) {
            builder.append("public abstract class ").append(className).append(" {\n\n}\n");
        } else if (JAVA_INTERFACE.equals(fileType)) {
            builder.append("public interface ").append(className).append(" {\n\n}\n");
        } else if (JAVA_ENUM.equals(fileType)) {
            builder.append("public enum ").append(className).append(" {\n    VALUE\n}\n");
        } else if (JAVA_ANNOTATION.equals(fileType)) {
            builder.append("public @interface ").append(className).append(" {\n\n}\n");
        } else {
            builder.append("public class ").append(className).append(" {\n\n}\n");
        }
        return builder.toString();
    }

    private String stripExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index <= 0) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    private boolean isInsideProject(Path path) {
        if (path == null) {
            return false;
        }
        Path root = project.getPath().toAbsolutePath().normalize();
        Path normalized = path.toAbsolutePath().normalize();
        return normalized.startsWith(root);
    }

    private boolean isIgnoredForUml(Path path) {
        Path root = project.getPath().toAbsolutePath().normalize();
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(root)) {
            return true;
        }

        Path relative = root.relativize(normalized);
        for (Path segment : relative) {
            String name = segment.toString();
            if ("target".equals(name)
                || "build".equals(name)
                || ".git".equals(name)
                || ".idea".equals(name)
                || ".ide7-debug".equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean importDroppedFiles(List<File> droppedFiles) {
        Path targetDirectory = getSelectedDirectoryOrParent();
        if (targetDirectory == null || !Files.isDirectory(targetDirectory)) {
            targetDirectory = project.getPath();
        }

        int importedCount = 0;
        Path firstImportedFile = null;

        for (File droppedFile : droppedFiles) {
            if (droppedFile == null) {
                continue;
            }

            Path source = droppedFile.toPath().toAbsolutePath().normalize();
            Path target = targetDirectory.resolve(source.getFileName()).toAbsolutePath().normalize();

            if (source.startsWith(project.getPath().toAbsolutePath().normalize())) {
                view.setStatus("Skipped " + source.getFileName() + ": already inside project.");
                continue;
            }
            if (!isInsideProject(target)) {
                showError("Import failed", "Cannot import outside the current project.");
                continue;
            }
            if (Files.exists(target)) {
                showError("Import failed", "Target already exists: " + target.getFileName());
                continue;
            }

            try {
                copyRecursively(source, target);
                if (firstImportedFile == null) {
                    firstImportedFile = Files.isDirectory(target) ? firstReadableFile(target) : target;
                }
                importedCount++;
            } catch (IOException e) {
                showError("Import failed", "Cannot import " + source.getFileName() + ": " + e.getMessage());
            }
        }

        if (importedCount == 0) {
            return false;
        }

        reloadTree();
        if (firstImportedFile != null) {
            openFile(firstImportedFile);
        } else {
            view.setStatus("Imported " + importedCount + " item(s).");
        }
        return true;
    }

    private void copyRecursively(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(target);
            File[] children = source.toFile().listFiles();
            if (children == null) {
                return;
            }
            for (int i = 0; i < children.length; i++) {
                Path childSource = children[i].toPath();
                copyRecursively(childSource, target.resolve(childSource.getFileName()));
            }
            return;
        }

        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
    }

    private Path firstReadableFile(Path directory) throws IOException {
        if (directory == null || !Files.isDirectory(directory)) {
            return null;
        }

        File[] children = directory.toFile().listFiles();
        if (children == null) {
            return null;
        }

        Arrays.sort(children, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if (a.isDirectory() && !b.isDirectory()) {
                    return -1;
                }
                if (!a.isDirectory() && b.isDirectory()) {
                    return 1;
                }
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        for (int i = 0; i < children.length; i++) {
            Path child = children[i].toPath();
            if (Files.isDirectory(child)) {
                Path nested = firstReadableFile(child);
                if (nested != null) {
                    return nested;
                }
            } else {
                return child;
            }
        }
        return null;
    }

    private void deleteRecursively(Path target) throws IOException {
        if (Files.isDirectory(target)) {
            File[] children = target.toFile().listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteRecursively(children[i].toPath());
                }
            }
        }

        try {
            Files.delete(target);
        } catch (DirectoryNotEmptyException e) {
            File[] children = target.toFile().listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteRecursively(children[i].toPath());
                }
            }
            Files.delete(target);
        }
    }

    private void showError(String title, String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(title);
        error.setHeaderText(title);
        error.setContentText(message);
        error.showAndWait();
    }

    private void onEditorTextChanged() {
        if (suppressAutoSave || openedFile == null) {
            if (openedFile == null) {
                dirty = false;
                view.setDirty(false);
            }
            return;
        }

        String content = view.getEditorText();
        dirty = !content.equals(lastSavedContent);
        view.setDirty(dirty);

        if (!dirty) {
            autoSaveDelay.stop();
            view.setStatus("No changes to save.");
            return;
        }

        view.setStatus("Editing " + openedFile.getFileName() + "...");
        autoSaveDelay.playFromStart();
    }

    private void updateCompletionSuggestions(KeyEvent event) {
        if (openedFile == null || !view.isDirty() && view.getEditorText().isEmpty()) {
            view.hideCompletionSuggestions();
            return;
        }
        if (event.getCode() == KeyCode.ESCAPE
            || event.getCode() == KeyCode.SPACE
            || event.getCode() == KeyCode.BACK_SPACE
            || event.getCode() == KeyCode.DELETE) {
            view.hideCompletionSuggestions();
            return;
        }

        String memberTarget = getMemberCompletionTarget();
        if (memberTarget != null) {
            String type = resolveVariableType(memberTarget);
            List<String> memberSuggestions = completionService.memberSuggestions(type, COMPLETION_LIMIT);
            view.showCompletionSuggestions(memberSuggestions, new javafx.event.EventHandler<javafx.event.ActionEvent>() {
                @Override
                public void handle(javafx.event.ActionEvent event) {
                    if (event.getSource() instanceof javafx.scene.control.MenuItem) {
                        javafx.scene.control.MenuItem item = (javafx.scene.control.MenuItem) event.getSource();
                        applyCompletion(item.getText());
                        view.hideCompletionSuggestions();
                        onEditorTextChanged();
                    }
                }
            });
            return;
        }

        completionService.indexText(view.getEditorText());
        String prefix = view.getCurrentEditorPrefix();
        if (prefix.length() < 2) {
            view.hideCompletionSuggestions();
            return;
        }

        List<String> suggestions = completionService.suggestions(prefix, COMPLETION_LIMIT);
        view.showCompletionSuggestions(suggestions, new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                if (event.getSource() instanceof javafx.scene.control.MenuItem) {
                    javafx.scene.control.MenuItem item = (javafx.scene.control.MenuItem) event.getSource();
                    applyCompletion(item.getText());
                    view.hideCompletionSuggestions();
                    onEditorTextChanged();
                }
            }
        });
    }

    private void applyCompletion(String completion) {
        if (completionService.isSnippet(completion)) {
            view.replaceCurrentEditorPrefix(
                completionService.snippet(completion, currentLineIndent())
            );
            return;
        }
        view.replaceCurrentEditorPrefix(completion);
    }

    private String currentLineIndent() {
        String text = view.getEditorText();
        int caret = view.getEditorCaretPosition();
        if (text == null || caret < 0 || caret > text.length()) {
            return "";
        }

        int lineStart = text.lastIndexOf('\n', Math.max(0, caret - 1)) + 1;
        StringBuilder indent = new StringBuilder();
        for (int i = lineStart; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == ' ' || current == '\t') {
                indent.append(current);
            } else {
                break;
            }
        }
        return indent.toString();
    }

    private String getMemberCompletionTarget() {
        String text = view.getEditorText();
        int caret = view.getEditorCaretPosition();
        if (text == null || caret < 2 || caret > text.length()) {
            return null;
        }

        int dotIndex = caret - 1;
        while (dotIndex >= 0 && Character.isJavaIdentifierPart(text.charAt(dotIndex))) {
            dotIndex--;
        }
        if (dotIndex < 1 || text.charAt(dotIndex) != '.') {
            return null;
        }

        int start = dotIndex - 1;
        while (start >= 0 && Character.isJavaIdentifierPart(text.charAt(start))) {
            start--;
        }
        if (start == dotIndex - 1) {
            return null;
        }
        return text.substring(start + 1, dotIndex);
    }

    private String resolveVariableType(String variableName) {
        if (variableName == null || variableName.isEmpty()) {
            return null;
        }

        String text = view.getEditorText();
        java.util.regex.Pattern declarationPattern = java.util.regex.Pattern.compile(
            "\\b([A-Z][A-Za-z0-9_<>\\[\\]]*|String|int|long|double|float|boolean|char|byte|short)\\s+"
                + java.util.regex.Pattern.quote(variableName)
                + "\\b\\s*(?:=|;|,|\\))"
        );
        java.util.regex.Matcher matcher = declarationPattern.matcher(text);
        String foundType = null;
        while (matcher.find()) {
            if (matcher.start() <= view.getEditorCaretPosition()) {
                foundType = matcher.group(1);
            }
        }
        return normalizeCompletionType(foundType);
    }

    private String normalizeCompletionType(String type) {
        if (type == null) {
            return null;
        }
        int genericIndex = type.indexOf('<');
        if (genericIndex >= 0) {
            type = type.substring(0, genericIndex);
        }
        while (type.endsWith("[]")) {
            type = type.substring(0, type.length() - 2);
        }
        return type;
    }

    private void findNextOccurrence() {
        if (openedFile == null) {
            showError("No file", "Open a file before searching.");
            return;
        }

        String query = view.getSearchText();
        if (query == null || query.trim().isEmpty()) {
            showError("Search", "Enter text to search.");
            return;
        }

        String content = view.getEditorText();
        int selectionStart = view.getEditorSelectionStart();
        int selectionEnd = view.getEditorSelectionEnd();
        int caret = Math.max(0, selectionEnd > selectionStart ? selectionEnd : view.getEditorCaretPosition());
        int index = content.indexOf(query, caret);
        if (index < 0) {
            index = content.indexOf(query);
        }
        if (index < 0) {
            view.setStatus("No result for '" + query + "'.");
            return;
        }

        view.selectEditorRange(index, index + query.length());
        view.setStatus("Found occurrence on line " + countLine(content, index) + ".");
    }

    private void replaceCurrentOccurrence() {
        if (openedFile == null) {
            showError("No file", "Open a file before replacing.");
            return;
        }

        String query = view.getSearchText();
        if (query == null || query.isEmpty()) {
            showError("Replace", "Enter text to search.");
            return;
        }

        if (!selectionMatches(query)) {
            findNextOccurrence();
        }
        if (!selectionMatches(query)) {
            return;
        }

        String replacement = view.getReplaceText();
        suppressAutoSave = true;
        view.replaceEditorSelection(replacement == null ? "" : replacement);
        suppressAutoSave = false;
        onEditorTextChanged();
        view.setStatus("Replaced one occurrence.");
    }

    private void replaceAllOccurrences() {
        if (openedFile == null) {
            showError("No file", "Open a file before replacing.");
            return;
        }

        String query = view.getSearchText();
        if (query == null || query.isEmpty()) {
            showError("Replace", "Enter text to search.");
            return;
        }

        String replacement = view.getReplaceText();
        String content = view.getEditorText();
        int count = 0;
        int index = content.indexOf(query);
        while (index >= 0) {
            content = content.substring(0, index)
                + (replacement == null ? "" : replacement)
                + content.substring(index + query.length());
            count++;
            index = content.indexOf(query, index + (replacement == null ? 0 : replacement.length()));
        }

        if (count == 0) {
            view.setStatus("No result for '" + query + "'.");
            return;
        }

        suppressAutoSave = true;
        view.setEditorText(content);
        suppressAutoSave = false;
        onEditorTextChanged();
        view.setStatus("Replaced " + count + " occurrence(s).");
    }

    private boolean selectionMatches(String query) {
        if (query == null) {
            return false;
        }

        String content = view.getEditorText();
        int start = view.getEditorSelectionStart();
        int endSelection = view.getEditorSelectionEnd();
        if (endSelection <= start) {
            return false;
        }
        int end = start + query.length();
        if (start < 0 || end > content.length() || endSelection != end) {
            return false;
        }

        return query.equals(content.substring(start, end));
    }

    private int countLine(String content, int index) {
        int line = 1;
        for (int i = 0; i < index && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private void performEditorAction(EditorAction action) {
        if (openedFile == null) {
            showError("No file", "Open a file before editing.");
            return;
        }

        if (action == EditorAction.UNDO) {
            view.undoEditor();
        } else if (action == EditorAction.REDO) {
            view.redoEditor();
        } else if (action == EditorAction.CUT) {
            view.cutEditor();
        } else if (action == EditorAction.COPY) {
            view.copyEditor();
        } else if (action == EditorAction.PASTE) {
            view.pasteEditor();
        } else if (action == EditorAction.SELECT_ALL) {
            view.selectAllEditor();
        }
        view.requestEditorFocus();
    }

    private enum EditorAction {
        UNDO,
        REDO,
        CUT,
        COPY,
        PASTE,
        SELECT_ALL
    }

    private static class DesignerClass {
        private final String name;
        private final String kind;
        private final List<String> attributes;
        private final List<String> methods;

        private DesignerClass(String name, String kind) {
            this.name = name;
            this.kind = kind;
            this.attributes = new ArrayList<String>();
            this.methods = new ArrayList<String>();
        }
    }
    
    
    private static class ExecutionResultInfo {
        private final int exitCode;
        private final String output;
        private final long peakMemoryKb;

        private ExecutionResultInfo(int exitCode, String output) {
            this(exitCode, output, -1L);
        }

        private ExecutionResultInfo(int exitCode, String output, long peakMemoryKb) {
            this.exitCode = exitCode;
            this.output = output == null ? "" : output;
            this.peakMemoryKb = peakMemoryKb;
        }
    }
    
    
    private static class ExecutionResultInfoFull {
        final int exitCode;
        final String stdout;
        final String stderr;
        final long peakMemoryKb;

        ExecutionResultInfoFull(int exitCode, String stdout, String stderr, long peakMemoryKb) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.peakMemoryKb = peakMemoryKb;
        }
    }
}
