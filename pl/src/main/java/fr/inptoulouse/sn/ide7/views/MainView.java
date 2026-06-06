package fr.inptoulouse.sn.ide7.views;

import fr.inptoulouse.sn.ide7.models.ProjectEntry;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.List;

public class MainView {
    private final BorderPane root;
    private final Label projectName;
    private final Label projectPath;
    private final Label currentFileLabel;
    private final Label dirtyLabel;
    private final Label statusLabel;
    private final TreeView<Path> projectTree;
    private final TextArea lineNumbersArea;
    private final TextArea editorArea;
    private final SyntaxHighlightLayer highlightLayer;
    private final StackPane editorStack;
    private final MenuItem homeMenuItem;
    private final MenuItem newClassMenuItem;
    private final MenuItem newPackageMenuItem;
    private final MenuItem saveMenuItem;
    private final MenuItem refreshMenuItem;
    private final MenuItem undoMenuItem;
    private final MenuItem redoMenuItem;
    private final MenuItem cutMenuItem;
    private final MenuItem copyMenuItem;
    private final MenuItem pasteMenuItem;
    private final MenuItem findMenuItem;
    private final MenuItem replaceMenuItem;
    private final MenuItem selectAllMenuItem;
    private final MenuItem generateUmlMenuItem;
    private final MenuItem generateClassFromUmlMenuItem;
    private final MenuItem debugProjectMenuItem;
    private final TextField searchField;
    private final TextField replaceField;
    private final Button undoButton;
    private final Button redoButton;
    private final Button cutButton;
    private final Button copyButton;
    private final Button pasteButton;
    private final Button selectAllButton;
    private final Button searchButton;
    private final Button replaceButton;
    private final Button replaceAllButton;
    private final Button backButton;
    private final Button refreshButton;
    private final Button newFileButton;
    private final Button newFolderButton;
    private final Button deleteButton;
    private final Button saveButton;
    private final Button generateUmlButton;
    private final Button generateClassFromUmlButton;
    private final Button debugProjectButton;
    private final ListView<String> completionList;
    private EventHandler<ActionEvent> completionHandler;
    private boolean dirty;

    public MainView() {
        this.projectName = new Label("Projet Java");
        this.projectName.setFont(Font.font(26));
        this.projectName.setTextFill(Color.web("#0f172a"));

        this.projectPath = new Label();
        this.projectPath.setTextFill(Color.web("#64748b"));
        this.projectPath.setWrapText(true);

        this.currentFileLabel = new Label("Aucun fichier ouvert");
        this.currentFileLabel.setFont(Font.font(14));
        this.currentFileLabel.setTextFill(Color.web("#1e293b"));

        this.dirtyLabel = new Label();
        this.dirtyLabel.setTextFill(Color.web("#b91c1c"));
        this.dirtyLabel.setStyle("-fx-font-weight: bold;");

        this.statusLabel = new Label("Pret");
        this.statusLabel.setTextFill(Color.web("#475569"));

        this.homeMenuItem = new MenuItem("Accueil");
        this.newClassMenuItem = new MenuItem("Nouvelle classe");
        this.newPackageMenuItem = new MenuItem("Nouveau package");
        this.saveMenuItem = new MenuItem("Sauvegarder");
        this.refreshMenuItem = new MenuItem("Actualiser");
        this.undoMenuItem = new MenuItem("Annuler");
        this.redoMenuItem = new MenuItem("Retablir");
        this.cutMenuItem = new MenuItem("Couper");
        this.copyMenuItem = new MenuItem("Copier");
        this.pasteMenuItem = new MenuItem("Coller");
        this.findMenuItem = new MenuItem("Rechercher");
        this.replaceMenuItem = new MenuItem("Remplacer");
        this.selectAllMenuItem = new MenuItem("Tout selectionner");
        this.generateUmlMenuItem = new MenuItem("Generer UML");
        this.generateClassFromUmlMenuItem = new MenuItem("Generer classe depuis UML");
        this.debugProjectMenuItem = new MenuItem("Debug projet");
        this.completionList = new ListView<String>();
        this.completionList.setVisible(false);
        this.completionList.setManaged(true);
        this.completionList.setMinWidth(230);
        this.completionList.setMaxWidth(380);
        this.completionList.setPrefWidth(360);
        this.completionList.setMinHeight(92);
        this.completionList.setMaxHeight(150);
        this.completionList.setPrefHeight(130);
        this.completionList.setFocusTraversable(false);
        this.completionList.setStyle(
            "-fx-background-color: white;"
                + "-fx-border-color: #cbd5e1;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
                + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 14, 0.18, 0, 3);"
        );
        this.completionList.setCellFactory(list -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setFont(Font.font("JetBrains Mono", 13));
                setTextFill(Color.web("#0f172a"));
                setStyle("-fx-padding: 7 10 7 10;");
            }
        });
        this.completionList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                fireCompletionSelection();
            }
        });
        this.completionList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                fireCompletionSelection();
                event.consume();
            }
        });

        Menu fileMenu = new Menu("Fichier");
        fileMenu.getItems().addAll(
            homeMenuItem,
            new SeparatorMenuItem(),
            newClassMenuItem,
            newPackageMenuItem,
            new SeparatorMenuItem(),
            saveMenuItem,
            refreshMenuItem
        );

        Menu editMenu = new Menu("Edition");
        editMenu.getItems().addAll(
            undoMenuItem,
            redoMenuItem,
            new SeparatorMenuItem(),
            cutMenuItem,
            copyMenuItem,
            pasteMenuItem,
            new SeparatorMenuItem(),
            findMenuItem,
            replaceMenuItem,
            selectAllMenuItem
        );

        MenuItem viewRefreshItem = new MenuItem("Actualiser");
        viewRefreshItem.setOnAction(event -> refreshMenuItem.fire());
        Menu viewMenu = new Menu("Affichage");
        viewMenu.getItems().addAll(viewRefreshItem);

        MenuItem navigateHomeItem = new MenuItem("Accueil");
        navigateHomeItem.setOnAction(event -> homeMenuItem.fire());
        Menu navigateMenu = new Menu("Navigation");
        navigateMenu.getItems().addAll(navigateHomeItem);

        MenuItem codeNewClassItem = new MenuItem("Nouvelle classe");
        codeNewClassItem.setOnAction(event -> newClassMenuItem.fire());
        MenuItem codeNewPackageItem = new MenuItem("Nouveau package");
        codeNewPackageItem.setOnAction(event -> newPackageMenuItem.fire());
        Menu codeMenu = new Menu("Code");
        codeMenu.getItems().addAll(
            codeNewClassItem,
            codeNewPackageItem,
            new SeparatorMenuItem(),
            generateUmlMenuItem,
            generateClassFromUmlMenuItem
        );

        Menu runMenu = new Menu("Execution");
        runMenu.getItems().addAll(debugProjectMenuItem);
        Menu toolsMenu = new Menu("Outils");
        toolsMenu.setDisable(true);
        Menu gitMenu = new Menu("Git");
        gitMenu.setDisable(true);
        Menu windowMenu = new Menu("Fenetre");
        windowMenu.setDisable(true);
        Menu helpMenu = new Menu("Aide");
        helpMenu.setDisable(true);

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, viewMenu, navigateMenu, codeMenu, runMenu, toolsMenu, gitMenu, windowMenu, helpMenu);
        menuBar.setUseSystemMenuBar(false);

        VBox headerText = new VBox(4, projectName, projectPath);
        headerText.setAlignment(Pos.CENTER_LEFT);

        this.backButton = createActionButton("Accueil", AntDesignIconsOutlined.LEFT_CIRCLE, "#2563eb");
        this.refreshButton = createActionButton("Sync", AntDesignIconsOutlined.RELOAD, "#2563eb");
        this.newFileButton = createActionButton("Classe", AntDesignIconsOutlined.FILE_ADD, "#15803d");
        this.newFolderButton = createActionButton("Package", AntDesignIconsOutlined.FOLDER_ADD, "#15803d");
        this.deleteButton = createActionButton("Suppr.", AntDesignIconsOutlined.DELETE, "#b91c1c");
        this.saveButton = createActionButton("Sauver", AntDesignIconsOutlined.SAVE, "#15803d");
        this.generateUmlButton = createActionButton("UML", AntDesignIconsOutlined.FORM, "#7c3aed");
        this.generateClassFromUmlButton = createActionButton("Classe UML", AntDesignIconsOutlined.FILE_ADD, "#0f766e");
        this.debugProjectButton = createActionButton("Debug", AntDesignIconsOutlined.BUG, "#dc2626");
        this.saveButton.setDisable(true);

        HBox actions = new HBox(
            8,
            backButton,
            refreshButton,
            newFileButton,
            newFolderButton,
            generateUmlButton,
            generateClassFromUmlButton,
            debugProjectButton,
            deleteButton,
            saveButton
        );
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setFillHeight(false);

        VBox topLeft = new VBox(12, headerText, actions);
        topLeft.setAlignment(Pos.CENTER_LEFT);

        Label explorerTitle = new Label("Explorer");
        explorerTitle.setFont(Font.font(16));
        explorerTitle.setTextFill(Color.web("#0f172a"));

        this.projectTree = new TreeView<Path>();
        this.projectTree.setMinWidth(240);
        this.projectTree.setPrefWidth(300);

        VBox leftPanel = new VBox(10, explorerTitle, projectTree);
        leftPanel.setPadding(new Insets(14));
        leftPanel.setStyle("-fx-background-color: #f8fafc;");
        VBox.setVgrow(projectTree, Priority.ALWAYS);

        Label editorTitle = new Label("Editeur Java");
        editorTitle.setFont(Font.font(16));
        editorTitle.setTextFill(Color.web("#0f172a"));

        this.searchField = new TextField();
        this.searchField.setPromptText("Rechercher");
        this.searchField.setPrefColumnCount(18);

        this.replaceField = new TextField();
        this.replaceField.setPromptText("Remplacer par");
        this.replaceField.setPrefColumnCount(18);

        this.undoButton = createEditorToolButton(AntDesignIconsOutlined.UNDO, "#1d4ed8", "Annuler");
        this.redoButton = createEditorToolButton(AntDesignIconsOutlined.REDO, "#1d4ed8", "Retablir");
        this.cutButton = createEditorToolButton(AntDesignIconsOutlined.SCISSOR, "#b45309", "Couper");
        this.copyButton = createEditorToolButton(AntDesignIconsOutlined.COPY, "#0f766e", "Copier");
        this.pasteButton = createEditorToolButton(AntDesignIconsOutlined.SNIPPETS, "#15803d", "Coller");
        this.selectAllButton = createEditorToolButton(AntDesignIconsOutlined.SELECT, "#475569", "Tout selectionner");

        this.searchButton = new Button("Chercher");
        this.replaceButton = new Button("Remplacer");
        this.replaceAllButton = new Button("Tout remplacer");
        this.searchButton.setGraphic(createIcon(AntDesignIconsOutlined.SEARCH, "#1d4ed8"));
        this.searchButton.setContentDisplay(ContentDisplay.LEFT);
        this.searchButton.setGraphicTextGap(6);
        this.replaceButton.setGraphic(createIcon(AntDesignIconsOutlined.SWAP, "#15803d"));
        this.replaceButton.setContentDisplay(ContentDisplay.LEFT);
        this.replaceButton.setGraphicTextGap(6);
        this.replaceAllButton.setGraphic(createIcon(AntDesignIconsOutlined.FORM, "#b45309"));
        this.replaceAllButton.setContentDisplay(ContentDisplay.LEFT);
        this.replaceAllButton.setGraphicTextGap(6);

        HBox editorToolBar = new HBox(8, undoButton, redoButton, cutButton, copyButton, pasteButton, selectAllButton);
        editorToolBar.setAlignment(Pos.CENTER_LEFT);
        editorToolBar.setPadding(new Insets(0, 0, 2, 0));

        HBox searchBar = new HBox(8, searchField, replaceField, searchButton, replaceButton, replaceAllButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(0, 0, 2, 0));

        this.lineNumbersArea = new TextArea("1");
        this.lineNumbersArea.setEditable(false);
        this.lineNumbersArea.setFocusTraversable(false);
        this.lineNumbersArea.setWrapText(false);
        this.lineNumbersArea.setMouseTransparent(true);
        this.lineNumbersArea.setPrefWidth(56);
        this.lineNumbersArea.setMinWidth(56);
        this.lineNumbersArea.setMaxWidth(56);
        this.lineNumbersArea.setStyle(
            "-fx-font-family: 'JetBrains Mono', 'Menlo', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-background-color: #f8fafc;" +
            "-fx-control-inner-background: #f8fafc;" +
            "-fx-text-fill: #64748b;" +
            "-fx-highlight-fill: transparent;" +
            "-fx-highlight-text-fill: #64748b;" +
            "-fx-padding: 10 8 10 8;"
        );

        this.highlightLayer = new SyntaxHighlightLayer();
        this.highlightLayer.setVisible(false);
        this.editorArea = new TextArea();
        this.editorArea.setWrapText(false);
        this.editorArea.setEditable(false);
        this.editorArea.setPromptText("Ouvre ou crée un fichier pour écrire ici.");
        this.editorArea.setStyle(
            "-fx-font-family: 'JetBrains Mono', 'Menlo', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-control-inner-background: white;" +
            "-fx-background-color: white;" +
            "-fx-text-fill: #0f172a;" +
            "-fx-highlight-fill: #bfdbfe;" +
            "-fx-highlight-text-fill: #0f172a;" +
            "-fx-padding: 10 12 10 12;"
        );
        this.editorArea.setPrefColumnCount(80);
        this.editorArea.setPrefRowCount(24);
        this.editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLineNumbers();
            updateHighlighting();
        });
        this.editorArea.scrollTopProperty().addListener((observable, oldValue, newValue) -> syncHighlightScroll());
        this.editorArea.scrollLeftProperty().addListener((observable, oldValue, newValue) -> syncHighlightScroll());

        this.editorStack = new StackPane(this.editorArea, completionList);
        StackPane.setAlignment(this.editorArea, Pos.TOP_LEFT);
        StackPane.setAlignment(this.completionList, Pos.BOTTOM_LEFT);
        StackPane.setMargin(this.completionList, new Insets(0, 0, 16, 72));
        this.editorStack.setStyle("-fx-background-color: white;");
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(this.editorStack.widthProperty());
        clip.heightProperty().bind(this.editorStack.heightProperty());
        this.editorStack.setClip(clip);

        HBox editorContent = new HBox(0, lineNumbersArea, editorStack);
        editorContent.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(editorStack, Priority.ALWAYS);
        HBox.setHgrow(lineNumbersArea, Priority.NEVER);

        VBox editorPanel = new VBox(10, editorTitle, currentFileLabel, dirtyLabel, editorToolBar, searchBar, editorContent);
        editorPanel.setPadding(new Insets(14));
        VBox.setVgrow(editorContent, Priority.ALWAYS);
        VBox.setVgrow(editorStack, Priority.ALWAYS);

        SplitPane split = new SplitPane(leftPanel, editorPanel);
        split.setDividerPositions(0.27);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottom = new HBox(12, statusLabel, spacer);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(8, 14, 10, 14));
        bottom.setStyle("-fx-background-color: #f8fafc;");

        BorderPane header = new BorderPane();
        header.setPadding(new Insets(14, 14, 8, 14));
        header.setLeft(topLeft);
        header.setRight(buildEditorBadge());
        VBox topContainer = new VBox(menuBar, header);

        this.root = new BorderPane();
        this.root.setTop(topContainer);
        this.root.setCenter(split);
        this.root.setBottom(bottom);
        this.root.setStyle("-fx-background-color: white;");
        updateHighlighting();
    }

    public Parent getView() {
        return root;
    }

    public void setProject(ProjectEntry project) {
        this.projectName.setText(project.getName());
        this.projectPath.setText(project.getPath().toString());
    }

    public void setProjectTreeRoot(TreeItem<Path> rootItem) {
        this.projectTree.setRoot(rootItem);
    }

    public TreeItem<Path> getSelectedTreeItem() {
        return this.projectTree.getSelectionModel().getSelectedItem();
    }

    public void setOnProjectTreeClicked(EventHandler<MouseEvent> handler) {
        this.projectTree.setOnMouseClicked(handler);
    }

    public void setOnFilesDragOver(EventHandler<DragEvent> handler) {
        this.root.setOnDragOver(handler);
    }

    public void setOnFilesDragDropped(EventHandler<DragEvent> handler) {
        this.root.setOnDragDropped(handler);
    }

    public void setOnFilesDragExited(EventHandler<DragEvent> handler) {
        this.root.setOnDragExited(handler);
    }

    public void setDropActive(boolean active) {
        String rootStyle = "-fx-background-color: white;";
        String highlightStyle = "-fx-background-color: #eff6ff; -fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-insets: 6; -fx-border-radius: 10;";
        this.root.setStyle(active ? rootStyle + highlightStyle : rootStyle);
    }

    public void setOnRefreshRequested(EventHandler<ActionEvent> handler) {
        this.refreshButton.setOnAction(handler);
        this.refreshMenuItem.setOnAction(handler);
    }

    public void setOnNewFileRequested(EventHandler<ActionEvent> handler) {
        this.newFileButton.setOnAction(handler);
        this.newClassMenuItem.setOnAction(handler);
    }

    public void setOnNewFolderRequested(EventHandler<ActionEvent> handler) {
        this.newFolderButton.setOnAction(handler);
        this.newPackageMenuItem.setOnAction(handler);
    }

    public void setOnGenerateUmlRequested(EventHandler<ActionEvent> handler) {
        this.generateUmlButton.setOnAction(handler);
        this.generateUmlMenuItem.setOnAction(handler);
    }

    public void setOnGenerateClassFromUmlRequested(EventHandler<ActionEvent> handler) {
        this.generateClassFromUmlButton.setOnAction(handler);
        this.generateClassFromUmlMenuItem.setOnAction(handler);
    }

    public void setOnDebugProjectRequested(EventHandler<ActionEvent> handler) {
        this.debugProjectButton.setOnAction(handler);
        this.debugProjectMenuItem.setOnAction(handler);
    }

    public void setOnDeleteRequested(EventHandler<ActionEvent> handler) {
        this.deleteButton.setOnAction(handler);
    }

    public void setOnSaveRequested(EventHandler<ActionEvent> handler) {
        this.saveButton.setOnAction(handler);
        this.saveMenuItem.setOnAction(handler);
    }

    public void setOnSearchRequested(EventHandler<ActionEvent> handler) {
        this.searchButton.setOnAction(handler);
        this.searchField.setOnAction(handler);
        this.findMenuItem.setOnAction(handler);
    }

    public void setOnUndoRequested(EventHandler<ActionEvent> handler) {
        this.undoButton.setOnAction(handler);
        this.undoMenuItem.setOnAction(handler);
    }

    public void setOnRedoRequested(EventHandler<ActionEvent> handler) {
        this.redoButton.setOnAction(handler);
        this.redoMenuItem.setOnAction(handler);
    }

    public void setOnCutRequested(EventHandler<ActionEvent> handler) {
        this.cutButton.setOnAction(handler);
        this.cutMenuItem.setOnAction(handler);
    }

    public void setOnCopyRequested(EventHandler<ActionEvent> handler) {
        this.copyButton.setOnAction(handler);
        this.copyMenuItem.setOnAction(handler);
    }

    public void setOnPasteRequested(EventHandler<ActionEvent> handler) {
        this.pasteButton.setOnAction(handler);
        this.pasteMenuItem.setOnAction(handler);
    }

    public void setOnSelectAllRequested(EventHandler<ActionEvent> handler) {
        this.selectAllButton.setOnAction(handler);
        this.selectAllMenuItem.setOnAction(handler);
    }

    public void setOnReplaceRequested(EventHandler<ActionEvent> handler) {
        this.replaceButton.setOnAction(handler);
        this.replaceMenuItem.setOnAction(handler);
    }

    public void setOnReplaceAllRequested(EventHandler<ActionEvent> handler) {
        this.replaceAllButton.setOnAction(handler);
    }

    public String getSearchText() {
        return this.searchField.getText();
    }

    public String getReplaceText() {
        return this.replaceField.getText();
    }

    public void setEditorText(String text) {
        this.editorArea.setText(text);
        updateLineNumbers();
        updateHighlighting();
    }

    public String getEditorText() {
        return this.editorArea.getText();
    }

    public int getEditorCaretPosition() {
        return this.editorArea.getCaretPosition();
    }

    public int getEditorSelectionStart() {
        return this.editorArea.getSelection().getStart();
    }

    public int getEditorSelectionEnd() {
        return this.editorArea.getSelection().getEnd();
    }

    public void selectEditorRange(int start, int end) {
        this.editorArea.selectRange(start, end);
        this.editorArea.requestFocus();
    }

    public void replaceEditorSelection(String replacement) {
        int start = this.editorArea.getSelection().getStart();
        int end = this.editorArea.getSelection().getEnd();
        this.editorArea.replaceText(start, end, replacement);
    }

    public void requestEditorFocus() {
        this.editorArea.requestFocus();
    }

    public void undoEditor() {
        this.editorArea.undo();
    }

    public void redoEditor() {
        this.editorArea.redo();
    }

    public void cutEditor() {
        this.editorArea.cut();
    }

    public void copyEditor() {
        this.editorArea.copy();
    }

    public void pasteEditor() {
        this.editorArea.paste();
    }

    public void selectAllEditor() {
        this.editorArea.selectAll();
        this.editorArea.requestFocus();
    }

    public void setEditorEditable(boolean editable) {
        this.editorArea.setEditable(editable);
        this.saveButton.setDisable(!editable);
        this.saveMenuItem.setDisable(!editable);
    }

    public void setOnEditorTextChanged(ChangeListener<String> listener) {
        this.editorArea.textProperty().addListener(listener);
    }

    public void setOnEditorKeyReleased(EventHandler<KeyEvent> handler) {
        this.editorArea.setOnKeyReleased(handler);
    }

    public void setOnEditorKeyPressed(EventHandler<KeyEvent> handler) {
        this.editorArea.setOnKeyPressed(handler);
    }

    public String getCurrentEditorPrefix() {
        String text = this.editorArea.getText();
        int caret = this.editorArea.getCaretPosition();
        if (text == null || caret <= 0 || caret > text.length()) {
            return "";
        }

        int start = caret;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        if (start == caret) {
            return "";
        }
        return text.substring(start, caret);
    }

    public void replaceCurrentEditorPrefix(String replacement) {
        if (replacement == null || replacement.isEmpty()) {
            return;
        }

        String text = this.editorArea.getText();
        int caret = this.editorArea.getCaretPosition();
        int start = caret;
        while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            start--;
        }
        this.editorArea.replaceText(start, caret, replacement);
        this.editorArea.requestFocus();
    }

    public boolean hasCompletionSelection() {
        return this.completionList.isVisible()
            && this.completionList.getSelectionModel().getSelectedItem() != null;
    }

    public void applySelectedCompletion() {
        fireCompletionSelection();
    }

    public void showCompletionSuggestions(List<String> suggestions, EventHandler<ActionEvent> handler) {
        this.completionList.getItems().clear();
        this.completionHandler = handler;
        if (suggestions == null || suggestions.isEmpty()) {
            hideCompletionSuggestions();
            return;
        }

        this.completionList.getItems().addAll(suggestions);
        this.completionList.getSelectionModel().selectFirst();
        this.completionList.setVisible(true);
        this.completionList.toFront();
    }

    public void hideCompletionSuggestions() {
        this.completionList.setVisible(false);
        this.completionList.getItems().clear();
        this.completionHandler = null;
    }

    private void fireCompletionSelection() {
        String selected = this.completionList.getSelectionModel().getSelectedItem();
        if (selected == null || this.completionHandler == null) {
            return;
        }

        MenuItem item = new MenuItem(selected);
        this.completionHandler.handle(new ActionEvent(item, this.completionList));
    }

    public void setCurrentFileLabel(String value) {
        if (value == null || value.trim().isEmpty()) {
            this.currentFileLabel.setText("Aucun fichier ouvert");
            return;
        }

        if (value.startsWith("Fichier: ")) {
            this.currentFileLabel.setText(value.substring("Fichier: ".length()));
            return;
        }

        this.currentFileLabel.setText(value);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        this.dirtyLabel.setText(dirty ? "Modifie" : "");
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setStatus(String value) {
        this.statusLabel.setText(value);
    }

    public void setTreeCellFactory(Callback<TreeView<Path>, javafx.scene.control.TreeCell<Path>> callback) {
        this.projectTree.setCellFactory(callback);
    }

    public void setOnBackRequested(EventHandler<ActionEvent> handler) {
        this.backButton.setOnAction(handler);
        this.homeMenuItem.setOnAction(handler);
    }

    private void updateLineNumbers() {
        String text = this.editorArea.getText();
        int lineCount = countLines(text);
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lineCount; i++) {
            builder.append(i);
            if (i < lineCount) {
                builder.append('\n');
            }
        }
        this.lineNumbersArea.setText(builder.toString());
    }

    private void updateHighlighting() {
        this.highlightLayer.setText(this.editorArea.getText());
        syncHighlightScroll();
    }

    private void syncHighlightScroll() {
        this.highlightLayer.setTranslateY(-this.editorArea.getScrollTop());
        this.highlightLayer.setTranslateX(-this.editorArea.getScrollLeft());
    }

    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }

        int count = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private Button createActionButton(String text, AntDesignIconsOutlined iconCode, String color) {
        Button button = new Button(text);
        button.setGraphic(createIcon(iconCode, color));
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(6);
        button.setMinHeight(34);
        return button;
    }

    private Button createEditorToolButton(AntDesignIconsOutlined iconCode, String color, String tooltip) {
        Button button = new Button();
        button.setGraphic(createIcon(iconCode, color));
        button.setMinWidth(34);
        button.setPrefWidth(34);
        button.setMinHeight(34);
        button.setPrefHeight(34);
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }

    private VBox buildEditorBadge() {
        Label badge = new Label("Java");
        badge.setTextFill(Color.web("#1d4ed8"));
        badge.setStyle(
            "-fx-background-color: #dbeafe;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 8 14 8 14;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;"
        );
        VBox wrapper = new VBox(badge);
        wrapper.setAlignment(Pos.TOP_RIGHT);
        return wrapper;
    }

    private FontIcon createIcon(AntDesignIconsOutlined iconCode, String color) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(color));
        return icon;
    }
}
