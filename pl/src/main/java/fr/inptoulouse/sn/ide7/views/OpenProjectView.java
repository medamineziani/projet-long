package fr.inptoulouse.sn.ide7.views;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

public class OpenProjectView {

    private final BorderPane root;
    private final TextField pathField;
    private final Button browseButton;
    private final Button openButton;
    private final Button newProjectButton;
    private final Button cancelButton;
    private File selectedDirectory;
    private Window ownerWindow;

    public OpenProjectView() {
        Label title = new Label("Ouvrir un projet");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        this.pathField = new TextField("");
        this.pathField.setEditable(false);
        this.selectedDirectory = null;

        this.browseButton = new Button("Parcourir");
        this.browseButton.setGraphic(createIcon(AntDesignIconsOutlined.SEARCH, "#1d4ed8"));
        this.browseButton.setContentDisplay(ContentDisplay.LEFT);
        this.browseButton.setGraphicTextGap(6);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        GridPane.setHgrow(pathField, Priority.ALWAYS);
        this.pathField.setMaxWidth(Double.MAX_VALUE);
        form.add(new Label("Chemin"), 0, 0);
        form.add(pathField, 1, 0);
        form.add(browseButton, 2, 0);

        this.openButton = new Button("Ouvrir");
        this.newProjectButton = new Button("Nouveau projet");
        this.cancelButton = new Button("Annuler");
        this.openButton.setGraphic(createIcon(AntDesignIconsOutlined.FOLDER_OPEN, "#1d4ed8"));
        this.newProjectButton.setGraphic(createIcon(AntDesignIconsOutlined.FILE_ADD, "#15803d"));
        this.cancelButton.setGraphic(createIcon(AntDesignIconsOutlined.CLOSE_CIRCLE, "#b91c1c"));
        this.openButton.setContentDisplay(ContentDisplay.LEFT);
        this.newProjectButton.setContentDisplay(ContentDisplay.LEFT);
        this.cancelButton.setContentDisplay(ContentDisplay.LEFT);
        this.openButton.setGraphicTextGap(6);
        this.newProjectButton.setGraphicTextGap(6);
        this.cancelButton.setGraphicTextGap(6);
        this.openButton.setDefaultButton(true);
        this.cancelButton.setCancelButton(true);

        HBox actions = new HBox(12, cancelButton, newProjectButton, openButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(0, 20, 20, 20));

        VBox content = new VBox(10, title, form, actions);
        content.setPadding(new Insets(18, 18, 18, 18));

        this.root = new BorderPane();
        this.root.setCenter(content);
        this.root.setStyle("-fx-background-color: white;");
    }

    public Parent getView() {
        return this.root;
    }

    public void setOwnerWindow(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    public Window getOwnerWindow() {
        return this.ownerWindow;
    }

    public File getSelectedDirectory() {
        return this.selectedDirectory;
    }

    public void setSelectedDirectory(File selectedDirectory) {
        this.selectedDirectory = selectedDirectory;
        if (selectedDirectory != null) {
            this.pathField.setText(selectedDirectory.getAbsolutePath());
        } else {
            this.pathField.setText("");
        }
    }

    public void setOnBrowseRequested(EventHandler<ActionEvent> handler) {
        this.browseButton.setOnAction(handler);
    }

    public void setOnOpenRequested(EventHandler<ActionEvent> handler) {
        this.openButton.setOnAction(handler);
    }

    public void setOnNewProjectRequested(EventHandler<ActionEvent> handler) {
        this.newProjectButton.setOnAction(handler);
    }

    public void setOnCancelRequested(EventHandler<ActionEvent> handler) {
        this.cancelButton.setOnAction(handler);
    }

    public void setOnProjectDropDragOver(EventHandler<DragEvent> handler) {
        this.root.setOnDragOver(handler);
    }

    public void setOnProjectDrop(EventHandler<DragEvent> handler) {
        this.root.setOnDragDropped(handler);
    }

    public void setOnProjectDropExited(EventHandler<DragEvent> handler) {
        this.root.setOnDragExited(handler);
    }

    public void setDropActive(boolean active) {
        this.root.setStyle(active
            ? "-fx-background-color: #eff6ff; -fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-insets: 10; -fx-border-radius: 12;"
            : "-fx-background-color: white;");
    }

    private FontIcon createIcon(AntDesignIconsOutlined iconCode, String color) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(color));
        return icon;
    }
}
