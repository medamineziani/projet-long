package fr.inptoulouse.sn.ide7.controllers;

import fr.inptoulouse.sn.ide7.models.ProjectEntry;
import fr.inptoulouse.sn.ide7.views.WelcomeView;
import javafx.scene.Parent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.util.List;

public class WelcomeController {

    private final WelcomeView view;

    public WelcomeController(final AppController appController, List<ProjectEntry> projects) {
        this.view = new WelcomeView();
        this.view.setProjects(projects);

        this.view.setOnNewProjectRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.showNewProjectView();
            }
        });

        this.view.setOnOpenRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.showOpenProjectView();
            }
        });

        this.view.setOnProjectListClicked(new javafx.event.EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (event.getClickCount() == 2) {
                    ProjectEntry selectedProject = view.getSelectedProject();
                    if (selectedProject != null) {
                        appController.openProject(selectedProject);
                    }
                }
            }
        });

        this.view.setOnRemoveProjectRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                ProjectEntry selectedProject = view.getSelectedProject();
                if (selectedProject != null) {
                    appController.removeProject(selectedProject);
                    view.setProjects(appController.getProjects());
                }
            }
        });

        this.view.setOnProjectDropDragOver(new javafx.event.EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles() && containsDirectory(dragboard)) {
                    event.acceptTransferModes(TransferMode.COPY);
                    view.setDropActive(true);
                }
                event.consume();
            }
        });

        this.view.setOnProjectDrop(new javafx.event.EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                boolean completed = false;
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles()) {
                    File directory = firstDirectory(dragboard);
                    if (directory != null) {
                        appController.openProjectFromDirectory(directory);
                        completed = true;
                    }
                }
                view.setDropActive(false);
                event.setDropCompleted(completed);
                event.consume();
            }
        });

        this.view.setOnProjectDropExited(new javafx.event.EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                view.setDropActive(false);
            }
        });
    }

    public Parent getView() {
        return this.view.getView();
    }

    private boolean containsDirectory(Dragboard dragboard) {
        return firstDirectory(dragboard) != null;
    }

    private File firstDirectory(Dragboard dragboard) {
        for (File file : dragboard.getFiles()) {
            if (file != null && file.isDirectory()) {
                return file;
            }
        }
        return null;
    }
}
