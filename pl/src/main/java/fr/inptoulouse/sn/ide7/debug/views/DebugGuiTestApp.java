package fr.inptoulouse.sn.ide7.debug.views;

import fr.inptoulouse.sn.ide7.debug.controllers.DebugGuiController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DebugGuiTestApp extends Application {

    @Override
    public void start(Stage stage) {
        DebugView view = new DebugView();
        new DebugGuiController(view);

        Scene scene = new Scene(view, 900, 600);

        stage.setTitle("Module Debug - Interface graphique");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
