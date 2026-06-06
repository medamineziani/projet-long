package fr.inptoulouse.sn.ide7.controllers;

import fr.inptoulouse.sn.ide7.views.OpenProjectView;
import javafx.scene.Parent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class OpenProjectController {

    private final OpenProjectView view;

    public OpenProjectController(final AppController appController, Stage stage) {
        this.view = new OpenProjectView();
        this.view.setOwnerWindow(stage);

        this.view.setOnBrowseRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Choisir un projet");

                File initial = view.getSelectedDirectory();
                if (initial != null && initial.isDirectory()) {
                    chooser.setInitialDirectory(initial);
                } else {
                    chooser.setInitialDirectory(new File(System.getProperty("user.home")));
                }

                File selected = chooser.showDialog(view.getOwnerWindow());
                if (selected != null) {
                    view.setSelectedDirectory(selected);
                }
            }
        });

        this.view.setOnOpenRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.openProjectFromDirectory(view.getSelectedDirectory());
            }
        });

        this.view.setOnNewProjectRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.showNewProjectView();
            }
        });

        this.view.setOnCancelRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.showWelcomeView();
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
