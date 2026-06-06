package fr.inptoulouse.sn.ide7;

import fr.inptoulouse.sn.ide7.controllers.AppController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Ide7Application extends Application {
    @Override
    public void start(Stage stage) {
        installErrorPopupHandler();
        AppController appController = new AppController(stage);
        appController.start();
    }

    private void installErrorPopupHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> showErrorPopup(throwable));
    }

    private void showErrorPopup(Throwable throwable) {
        if (Platform.isFxApplicationThread()) {
            showErrorAlert(throwable);
            return;
        }

        Platform.runLater(() -> showErrorAlert(throwable));
    }

    private void showErrorAlert(Throwable throwable) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Erreur");
        error.setHeaderText("Erreur");
        error.setContentText("Erreur: " + getErrorMessage(throwable));
        error.showAndWait();
    }

    private String getErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "erreur inconnue";
        }

        String message = throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }
}
