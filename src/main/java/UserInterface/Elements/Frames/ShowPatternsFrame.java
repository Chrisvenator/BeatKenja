package UserInterface.Elements.Frames;

import MapGeneration.GenerationElements.Pattern;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static DataManager.Parameters.logger;

public class ShowPatternsFrame extends JFrame {
    public ShowPatternsFrame(UserInterface ui) {
        setTitle("Visualize Patterns in different ways");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        logger.info("Pattern visualization window initialized.");

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
        map.put("Dirichlet-multinomial distribution", Pattern::visualizeDirichletMultinomialDistribution);

        for (String s : map.keySet()) {
            String name = openingString + s + varianceString;

            JButton heatMap = new JButton(s);
            heatMap.addActionListener(e -> {
                logger.info("Visualizing pattern as: {}", name);
                map.get(s).accept(p, name);
            });
            panel.add(heatMap);
        }

        add(panel);
        logger.debug("Buttons for pattern visualization added to the panel.");
    }
}
