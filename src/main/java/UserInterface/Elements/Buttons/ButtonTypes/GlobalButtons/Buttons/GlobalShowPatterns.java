package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import DataManager.Parameters;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GlobalShowPatterns extends GlobalButton {
    public GlobalShowPatterns(UserInterface ui) {
        super(ElementTypes.GLOBAL_SHOW_PATTERNS_BUTTON, ui);
        if (ui.pattern == null || (!new File(Parameters.DEFAULT_PATTERN_PATH).exists() && !Parameters.useDatabase) ){
            setBackground(Color.RED);
        }
    }

    @Override
    public void onClick() {

        SwingUtilities.invokeLater(() -> new ButtonExample(ui).setVisible(true));
    }

    private static class ButtonExample extends JFrame {
        public ButtonExample(UserInterface ui) {
            setTitle("Visualize Patterns in different ways");
            setSize(400, 400);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            String openingString = "Pattern visualized as a ";
            String varianceString = (UserInterface.patternVariance != 0 ? (" with a variance of " + (UserInterface.patternVariance / 10)) : "");
            Pattern p = Pattern.adjustVariance(ui.pattern);

            Map<String, BiConsumer<Pattern, String>> map = new HashMap<>();
            map.put("Heatmap", Pattern::visualizeAsHeatmap);
            map.put("Truncated Heatmap", Pattern::visualizeAsHeatmapTruncated);
            map.put("Normalized Heatmap", Pattern::visualizeAsHeatmapNormalized);
            map.put("Logarithmic Normalized Heatmap", Pattern::visualizeAsHeatmapNormalizedLogarithmically);

            for (String s : map.keySet()){
                String name = openingString + s + varianceString;

                JButton heatMap = new JButton(s);
                heatMap.addActionListener(e -> map.get(s).accept(p, name));
                panel.add(heatMap);
            }

            add(panel);
        }
    }
}
