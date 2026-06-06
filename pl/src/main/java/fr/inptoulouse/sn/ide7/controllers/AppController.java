package fr.inptoulouse.sn.ide7.controllers;

import fr.inptoulouse.sn.ide7.models.ProjectEntry;
import fr.inptoulouse.sn.ide7.models.ProjectStore;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsFilled;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppController {

    private static final int WIDTH = 860;
    private static final int HEIGHT = 560;

    private final Stage stage;
    private final ProjectStore projectStore;
    private final List<ProjectEntry> projects;

    public AppController(Stage stage) {
        this.stage = stage;
        this.projectStore = new ProjectStore();
        this.projects = new ArrayList<ProjectEntry>();
        this.stage.getIcons().add(createWindowIcon());
    }

    public void start() {
        this.projects.addAll(projectStore.loadProjects());
        this.stage.setTitle("IDE7");
        showWelcomeView();
        this.stage.show();
    }

    public void showWelcomeView() {
        WelcomeController controller = new WelcomeController(this, projects);
        this.stage.setTitle("IDE7");
        showScene(new Scene(controller.getView(), WIDTH, HEIGHT), false);
    }

    public void showNewProjectView() {
        NewProjectController controller = new NewProjectController(this, stage);
        this.stage.setTitle("IDE7 - Nouveau projet");
        showScene(new Scene(controller.getView(), WIDTH, HEIGHT), false);
    }

    public void showOpenProjectView() {
        OpenProjectController controller = new OpenProjectController(this, stage);
        this.stage.setTitle("IDE7 - Ouvrir un projet");
        showScene(new Scene(controller.getView(), WIDTH, HEIGHT), false);
    }

    public void openProject(ProjectEntry project) {
        if (project == null) {
            showError("Projet introuvable", "Aucun projet n'a ete selectionne.");
            return;
        }

        if (!Files.isDirectory(project.getPath())) {
            promptRemoveMissingProject(project);
            return;
        }

        MainController controller = new MainController(this, project);
        this.stage.setTitle("IDE7 - " + project.getName());
        showScene(new Scene(controller.getView(), WIDTH, HEIGHT), true);
    }

    public void createProject(String rawName, File parentDirectory) {
        if (rawName == null) {
            showError("Nom invalide", "Le nom du projet est obligatoire.");
            return;
        }
        String projectName = rawName.trim();
        if (projectName.isEmpty() || projectName.contains("/") || projectName.contains("\\")) {
            showError("Nom invalide", "Le nom du projet est vide ou contient des caractères interdits.");
            return;
        }
        if (parentDirectory == null || !parentDirectory.isDirectory()) {
            showError("Dossier invalide", "Sélectionne un dossier parent valide.");
            return;
        }

        Path projectPath = parentDirectory.toPath().resolve(projectName).toAbsolutePath().normalize();
        if (Files.exists(projectPath)) {
            showError("Projet existant", "Le dossier du projet existe déjà: " + projectPath);
            return;
        }

        try {
            Files.createDirectories(projectPath);
        } catch (IOException e) {
            showError("Création impossible", "Impossible de créer le projet: " + e.getMessage());
            return;
        }

        ProjectEntry entry = new ProjectEntry(projectName, projectPath);
        upsertProject(entry);
        persistProjects();
        openProject(entry);
    }

    public void openProjectFromDirectory(File projectDirectory) {
        if (projectDirectory == null || !projectDirectory.isDirectory()) {
            showError("Projet invalide", "Sélectionne un dossier de projet valide.");
            return;
        }

        Path projectPath = projectDirectory.toPath().toAbsolutePath().normalize();
        ProjectEntry entry = findByPath(projectPath);
        if (entry == null) {
            entry = new ProjectEntry(projectDirectory.getName(), projectPath);
            projects.add(entry);
            persistProjects();
        }

        openProject(entry);
    }

    public void removeProject(ProjectEntry project) {
        if (project == null) {
            return;
        }

        if (projects.remove(project)) {
            persistProjects();
        }
    }

    public List<ProjectEntry> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    private ProjectEntry findByPath(Path path) {
        for (ProjectEntry project : projects) {
            if (project.getPath().equals(path)) {
                return project;
            }
        }
        return null;
    }

    private void upsertProject(ProjectEntry entry) {
        ProjectEntry existing = findByPath(entry.getPath());
        if (existing == null) {
            projects.add(entry);
            return;
        }

        if (!existing.getName().equals(entry.getName())) {
            projects.remove(existing);
            projects.add(entry);
        }
    }

    private void persistProjects() {
        try {
            projectStore.saveProjects(projects);
        } catch (IOException e) {
            showError("Sauvegarde impossible", "Impossible de sauvegarder la liste des projets: " + e.getMessage());
        }
    }

    private void showScene(Scene scene, boolean maximized) {
        this.stage.setScene(scene);
        if (maximized) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    stage.setMaximized(true);
                }
            });
            return;
        }

        this.stage.setMaximized(false);
        this.stage.setWidth(WIDTH);
        this.stage.setHeight(HEIGHT);
        this.stage.centerOnScreen();
    }

    private void showError(String title, String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(title);
        error.setHeaderText(title);
        error.setContentText(message);
        error.showAndWait();
    }

    private void promptRemoveMissingProject(ProjectEntry project) {
        ButtonType removeButton = new ButtonType("Retirer de la liste", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.WARNING, "", removeButton, cancelButton);
        alert.setTitle("Projet introuvable");
        alert.setHeaderText("Le dossier du projet n'existe plus.");
        alert.setContentText(project.getPath().toString());

        ButtonType selectedButton = alert.showAndWait().orElse(cancelButton);
        if (selectedButton == removeButton) {
            removeProject(project);
            showWelcomeView();
        }
    }

    private Image createWindowIcon() {
        InputStream iconStream = AppController.class.getResourceAsStream("/fr/inptoulouse/sn/ide7/icon.png");
        if (iconStream != null) {
            return new Image(iconStream);
        }

        FontIcon icon = new FontIcon(AntDesignIconsFilled.CODE);
        icon.setIconSize(34);
        icon.setIconColor(Color.web("#eff6ff"));

        StackPane wrapper = new StackPane(icon);
        wrapper.setPrefSize(64, 64);
        wrapper.setMinSize(64, 64);
        wrapper.setMaxSize(64, 64);
        wrapper.setPadding(new Insets(10));
        wrapper.setBackground(new Background(
            new BackgroundFill(Color.web("#1d4ed8"), new CornerRadii(14), Insets.EMPTY)
        ));
        new Scene(wrapper);
        wrapper.applyCss();
        wrapper.layout();
        return wrapper.snapshot(null, null);
    }
}
