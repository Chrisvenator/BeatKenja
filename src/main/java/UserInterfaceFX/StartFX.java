package UserInterfaceFX;

import AppLogic.AppController;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static DataManager.Parameters.DARK_MODE;
import static DataManager.Parameters.logger;

/**
 * Entry point of the new JavaFX UI shell.
 * Applies the AtlantaFX theme according to the configured dark mode and shows the AppShell.
 *
 * Launched via "java -jar BeatKenja.jar --fx" while the Swing UI is still the default.
 */
public class StartFX extends Application {

    private AppShell shell;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(DARK_MODE
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());

        AppController controller = new AppController();
        shell = new AppShell(controller, stage);

        Scene scene = new Scene(shell, 1280, 820);
        stage.setTitle("BeatKenja");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
        logger.info("JavaFX shell started (dark mode: {})", DARK_MODE);

        autoloadIfRequested(controller);
        takeDevScreenshotIfRequested(scene);
    }

    /**
     * Ensures the JVM terminates when the last window is closed.
     *
     * Non-daemon threads (local map zip server, running background tasks, Swing
     * spectrogram frames) would otherwise keep the process alive after pressing X.
     */
    @Override
    public void stop() {
        if (shell != null) shell.shutdown();
        logger.info("Shutting down.");
        System.exit(0);
    }

    /**
     * Dev aid: with -Dbk.autoload=<path> the shell loads a map right after startup;
     * -Dbk.autogen=<GeneratorType> additionally runs a generator on it (smoke checks).
     */
    private void autoloadIfRequested(AppController controller) {
        String autoload = System.getProperty("bk.autoload");
        if (autoload == null) return;
        try {
            controller.loadMapFileOrFolder(new java.io.File(autoload));
            String autogen = System.getProperty("bk.autogen");
            if (autogen != null) {
                controller.generateFor(AppLogic.GeneratorType.valueOf(autogen), false, java.util.List.copyOf(controller.session().diffs()));
            }
        } catch (Exception e) {
            logger.error("Autoload failed: {}", e.getMessage());
        }
    }

    /**
     * Dev aid: with -Dbk.screenshot=<path> the shell saves a PNG of its scene
     * shortly after startup and exits. Used for automated UI smoke checks.
     */
    private void takeDevScreenshotIfRequested(Scene scene) {
        String target = System.getProperty("bk.screenshot");
        if (target == null) return;

        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        delay.setOnFinished(e -> {
            try {
                javafx.scene.image.WritableImage image = scene.snapshot(null);
                java.awt.image.BufferedImage buffered = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);
                javax.imageio.ImageIO.write(buffered, "png", new java.io.File(target));
                logger.info("Screenshot saved to {}", target);
            } catch (Exception ex) {
                logger.error("Screenshot failed: {}", ex.getMessage());
            }
            javafx.application.Platform.exit();
        });
        delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}