package fr.inptoulouse.sn.ide7.controllers;

import fr.inptoulouse.sn.ide7.views.NewProjectView;
import javafx.scene.Parent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class NewProjectController {
    private final NewProjectView view;

    public NewProjectController(final AppController appController, final Stage stage) {
        this.view = new NewProjectView();
        this.view.setOwnerWindow(stage);
        File defaultProjectsDirectory = ensureDefaultProjectsDirectory();
        this.view.setSelectedParentDirectory(defaultProjectsDirectory);

        this.view.setOnBrowseRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Choisir le dossier parent");

                File initial = view.getSelectedParentDirectory();
                if (initial != null && initial.isDirectory()) {
                    chooser.setInitialDirectory(initial);
                } else {
                    chooser.setInitialDirectory(ensureDefaultProjectsDirectory());
                }

                File selected = chooser.showDialog(view.getOwnerWindow());
                if (selected != null) {
                    view.setSelectedParentDirectory(selected);
                }
            }
        });

        this.view.setOnCreateRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.createProject(view.getProjectName(), view.getSelectedParentDirectory());
            }
        });

        this.view.setOnCancelRequested(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                appController.showWelcomeView();
            }
        });
    }

    public Parent getView() {
        return view.getView();
    }

    private File ensureDefaultProjectsDirectory() {
        File defaultDirectory = this.view.getDefaultProjectsDirectory();
        if (!defaultDirectory.exists()) {
            defaultDirectory.mkdirs();
        }
        return defaultDirectory;
    }
}
