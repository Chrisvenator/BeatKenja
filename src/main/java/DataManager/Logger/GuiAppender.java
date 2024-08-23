package DataManager.Logger;

import UserInterface.UserInterface;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import static DataManager.Parameters.logger;

/**
 * A custom Log4j2 appender that redirects log messages to a user interface component.
 * This appender is designed to append log messages to a GUI, specifically to the status check area of the `UserInterface` class.
 * It filters log events based on their level, only appending messages with a severity of INFO or higher.
 */
@Plugin(name = "GuiAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class GuiAppender extends AbstractAppender {
    /**
     * The reference to the `UserInterface` instance where the log messages will be displayed.
     * This should be set using the `setUserInterface` method before the appender is used.
     */
    private static UserInterface ui;

    /**
     * Constructs a new `GuiAppender` with the specified name and layout.
     *
     * @param name   The name of the appender.
     * @param layout The layout to use for formatting log messages. If no layout is provided, a default layout is used.
     */
    protected GuiAppender(String name, Layout<?> layout) {
        super(name, null, layout, true, null);
    }

    /**
     * Appends a log event to the GUI component.
     * The method checks if the log level is INFO or higher before appending the message.
     *
     * @param event The log event to append.
     */
    @Override
    public void append(LogEvent event) {
        if (ui != null && event.getLevel().isMoreSpecificThan(Level.INFO)) {
            ui.statusCheck.append(getLayout().toSerializable(event).toString());
        }
    }

    /**
     * Factory method for creating a new instance of `GuiAppender`.
     * This method is used by Log4j2 to configure the appender.
     *
     * @param name   The name of the appender.
     * @param layout The layout to use for formatting log messages. If not provided, a default layout will be used.
     * @return A new `GuiAppender` instance.
     */
    @Deprecated
    @PluginFactory
    public static GuiAppender createAppender(@PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<?> layout) {
        if (name == null) {
            LOGGER.error("No name provided for GuiAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new GuiAppender(name, layout);
    }

    /**
     * Sets the `UserInterface` instance where log messages should be displayed.
     *
     * @param ui The `UserInterface` instance to set.
     */
    public static void setUserInterface(UserInterface ui) {
        GuiAppender.ui = ui;
        logger.info("set User Interface for GuiAppender");
    }
}
