package fr.inptoulouse.sn.ide7.views;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

public class NewProjectView {
    private static final File DEFAULT_PROJECTS_DIRECTORY =
        new File(System.getProperty("user.home"), "IDE7");

    private final BorderPane root;
    private final TextField nameField;
    private final TextField parentField;
    private final Button browseButton;
    private final Button createButton;
    private final Button cancelButton;
    private File selectedParent;
    private Window ownerWindow;

    public NewProjectView() {
        Label title = new Label("Nouveau projet");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        this.nameField = new TextField("MonProjet");
        this.parentField = new TextField(DEFAULT_PROJECTS_DIRECTORY.getAbsolutePath());
        this.parentField.setEditable(false);
        this.selectedParent = DEFAULT_PROJECTS_DIRECTORY;

        this.browseButton = new Button("Parcourir");
        this.browseButton.setGraphic(createIcon(AntDesignIconsOutlined.SEARCH, "#1d4ed8"));
        this.browseButton.setContentDisplay(ContentDisplay.LEFT);
        this.browseButton.setGraphicTextGap(6);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        form.add(new Label("Nom"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Dossier parent"), 0, 1);
        form.add(parentField, 1, 1);
        form.add(browseButton, 2, 1);

        this.createButton = new Button("Créer le projet");
        this.cancelButton = new Button("Annuler");
        this.createButton.setGraphic(createIcon(AntDesignIconsOutlined.FILE_ADD, "#15803d"));
        this.cancelButton.setGraphic(createIcon(AntDesignIconsOutlined.CLOSE_CIRCLE, "#b91c1c"));
        this.createButton.setContentDisplay(ContentDisplay.LEFT);
        this.cancelButton.setContentDisplay(ContentDisplay.LEFT);
        this.createButton.setGraphicTextGap(6);
        this.cancelButton.setGraphicTextGap(6);
        this.createButton.setDefaultButton(true);
        this.cancelButton.setCancelButton(true);

        HBox actions = new HBox(12, cancelButton, createButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(0, 20, 20, 20));

        VBox content = new VBox(10, title, form, actions);
        content.setPadding(new Insets(18, 18, 18, 18));

        this.root = new BorderPane();
        this.root.setCenter(content);
        this.root.setStyle("-fx-background-color: white;");
    }

    public Parent getView() {
        return root;
    }

    public void setOwnerWindow(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    public Window getOwnerWindow() {
        return ownerWindow;
    }

    public String getProjectName() {
        return nameField.getText();
    }

    public File getSelectedParentDirectory() {
        return selectedParent;
    }

    public File getDefaultProjectsDirectory() {
        return DEFAULT_PROJECTS_DIRECTORY;
    }

    public void setSelectedParentDirectory(File parentDirectory) {
        this.selectedParent = parentDirectory;
        if (parentDirectory != null) {
            this.parentField.setText(parentDirectory.getAbsolutePath());
        }
    }

    public void setOnBrowseRequested(EventHandler<ActionEvent> handler) {
        this.browseButton.setOnAction(handler);
    }

    public void setOnCreateRequested(EventHandler<ActionEvent> handler) {
        this.createButton.setOnAction(handler);
    }

    public void setOnCancelRequested(EventHandler<ActionEvent> handler) {
        this.cancelButton.setOnAction(handler);
    }

    private FontIcon createIcon(AntDesignIconsOutlined iconCode, String color) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(color));
        return icon;
    }
}
