package UserInterfaceFX;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

/**
 * Bridges log4j into the JavaFX log drawer.
 * Registers itself as a log4j appender and appends formatted lines to an
 * ObservableList that the status bar's log view is bound to.
 */
public final class FxLog extends AbstractAppender {
    private static final int MAX_LINES = 2000;
    private static final ObservableList<String> LINES = FXCollections.observableArrayList();

    private FxLog() {
        super("FxLog", null, null, true, Property.EMPTY_ARRAY);
    }

    public static ObservableList<String> lines() {
        return LINES;
    }

    /** Installs the appender on the root logger. Call once from the FX shell. */
    public static void install() {
        FxLog appender = new FxLog();
        appender.start();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.getConfiguration().getRootLogger().addAppender(appender, Level.INFO, null);
        context.updateLoggers();
    }

    @Override
    public void append(LogEvent event) {
        String line = "[" + event.getLevel() + "] " + event.getMessage().getFormattedMessage();
        Platform.runLater(() -> {
            LINES.add(line);
            if (LINES.size() > MAX_LINES) LINES.remove(0, LINES.size() - MAX_LINES);
        });
    }
}