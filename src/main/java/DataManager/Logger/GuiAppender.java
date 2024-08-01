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

@Plugin(name = "GuiAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class GuiAppender extends AbstractAppender {

    private static UserInterface ui;

    protected GuiAppender(String name, Layout<?> layout) {
        super(name, null, layout, true, null);
    }

    @Override
    public void append(LogEvent event) {
        if (ui != null && event.getLevel().isMoreSpecificThan(Level.INFO)) {
            ui.statusCheck.append(getLayout().toSerializable(event).toString());
        }
    }

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

    public static void setUserInterface(UserInterface ui) {
        GuiAppender.ui = ui;
    }
}
