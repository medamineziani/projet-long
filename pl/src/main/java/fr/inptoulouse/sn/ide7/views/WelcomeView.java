package fr.inptoulouse.sn.ide7.views;

import fr.inptoulouse.sn.ide7.models.ProjectEntry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.DragEvent;
import javafx.event.ActionEvent;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Files;
import java.util.List;

public class WelcomeView {

    private final BorderPane root;
    private final ListView<ProjectEntry> projectList;
    private final Button openButton;
    private final Button newButton;
    private EventHandler<ActionEvent> removeProjectHandler;

    public WelcomeView() {
        Label title = new Label("IDE7");
        title.setFont(Font.font(28));
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(20, 0, 12, 0));

        this.projectList = new ListView<ProjectEntry>();
        this.projectList.setMaxWidth(420);
        this.projectList.setCellFactory(listView -> new ProjectCell());
        BorderPane.setAlignment(projectList, Pos.CENTER);
        BorderPane.setMargin(projectList, new Insets(0, 24, 0, 24));

        this.openButton = new Button("Ouvrir");
        this.newButton = new Button("Nouveau projet");
        this.openButton.setGraphic(createIcon(AntDesignIconsOutlined.FOLDER_OPEN, "#1d4ed8"));
        this.newButton.setGraphic(createIcon(AntDesignIconsOutlined.FILE_ADD, "#15803d"));
        this.openButton.setContentDisplay(ContentDisplay.LEFT);
        this.newButton.setContentDisplay(ContentDisplay.LEFT);
        this.openButton.setGraphicTextGap(6);
        this.newButton.setGraphicTextGap(6);

        HBox actions = new HBox(12, openButton, newButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(16, 0, 20, 0));

        this.root = new BorderPane();
        this.root.setTop(title);
        this.root.setCenter(projectList);
        this.root.setBottom(actions);
    }

    public Parent getView() {
        return this.root;
    }

    public void setProjects(List<ProjectEntry> projects) {
        this.projectList.getItems().setAll(projects);
    }

    public ProjectEntry getSelectedProject() {
        return this.projectList.getSelectionModel().getSelectedItem();
    }

    public void setOnOpenRequested(EventHandler<ActionEvent> handler) {
        this.openButton.setOnAction(handler);
    }

    public void setOnNewProjectRequested(EventHandler<ActionEvent> handler) {
        this.newButton.setOnAction(handler);
    }

    public void setOnProjectListClicked(EventHandler<MouseEvent> handler) {
        this.projectList.setOnMouseClicked(handler);
    }

    public void setOnRemoveProjectRequested(EventHandler<ActionEvent> handler) {
        this.removeProjectHandler = handler;
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

    private final class ProjectCell extends ListCell<ProjectEntry> {

        private final Label nameLabel;
        private final Label pathLabel;
        private final Button removeButton;
        private final HBox container;
        private final VBox textBox;

        private ProjectCell() {
            this.nameLabel = new Label();
            this.nameLabel.setFont(Font.font(15));
            this.nameLabel.setWrapText(true);
            this.nameLabel.setMaxWidth(Double.MAX_VALUE);

            this.pathLabel = new Label();
            this.pathLabel.setTextFill(Color.web("#64748b"));
            this.pathLabel.setWrapText(true);
            this.pathLabel.setMaxWidth(Double.MAX_VALUE);

            this.textBox = new VBox(2, nameLabel, pathLabel);
            this.textBox.setFillWidth(true);
            HBox.setHgrow(this.textBox, Priority.ALWAYS);

            Region spacer = new Region();
            spacer.setMinWidth(8);

            this.removeButton = new Button("Retirer");
            this.removeButton.setFocusTraversable(false);
            this.removeButton.setMinWidth(Region.USE_PREF_SIZE);
            this.removeButton.setOnAction(event -> {
                getListView().getSelectionModel().select(getItem());
                if (removeProjectHandler != null) {
                    removeProjectHandler.handle(new ActionEvent(getItem(), removeButton));
                }
                event.consume();
            });

            this.container = new HBox(12, this.textBox, spacer, removeButton);
            this.container.setAlignment(Pos.CENTER_LEFT);
            this.container.setPadding(new Insets(8, 12, 8, 12));

            MenuItem removeItem = new MenuItem("Retirer de la liste");
            removeItem.setOnAction(event -> {
                getListView().getSelectionModel().select(getItem());
                if (removeProjectHandler != null) {
                    removeProjectHandler.handle(new ActionEvent(getItem(), getListView()));
                }
            });
            setContextMenu(new ContextMenu(removeItem));
        }

        @Override
        protected void updateItem(ProjectEntry item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            boolean projectDirectoryExists = Files.isDirectory(item.getPath());
            double textWidth = Math.max(getListView().getWidth() - 170, 120);
            this.textBox.setMaxWidth(textWidth);
            this.textBox.setPrefWidth(textWidth);
            this.nameLabel.setText(projectDirectoryExists ? item.getName() : item.getName() + " (introuvable)");
            this.nameLabel.setTextFill(projectDirectoryExists ? Color.web("#111827") : Color.web("#b91c1c"));
            this.pathLabel.setText(projectDirectoryExists
                ? item.getPath().toString()
                : "Dossier du projet introuvable: " + item.getPath());
            setText(null);
            setGraphic(container);
        }
    }

    private FontIcon createIcon(AntDesignIconsOutlined iconCode, String color) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        icon.setIconColor(Color.web(color));
        return icon;
    }
}
