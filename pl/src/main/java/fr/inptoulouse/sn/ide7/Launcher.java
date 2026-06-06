package fr.inptoulouse.sn.ide7;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        configureDesktopIntegration();
        Application.launch(Ide7Application.class, args);
    }

    private static void configureDesktopIntegration() {
        System.setProperty("apple.awt.application.name", "IDE7");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IDE7");

        if (!Taskbar.isTaskbarSupported()) {
            return;
        }

        try (InputStream iconStream = Launcher.class.getResourceAsStream("/fr/inptoulouse/sn/ide7/icon.png")) {
            if (iconStream == null) {
                return;
            }

            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(ImageIO.read(iconStream));
        } catch (UnsupportedOperationException | IOException ignored) {
        }
    }
}
