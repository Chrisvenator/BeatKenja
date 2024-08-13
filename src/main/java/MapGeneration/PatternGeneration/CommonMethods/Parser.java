package MapGeneration.PatternGeneration.CommonMethods;

import java.util.function.Function;

import static DataManager.Parameters.logger;

public class Parser {
    public static <T extends Number> T parseValue(String input, String loggerLogAs, Function<String, T> parser, T defaultValue) {
        try {
            T parsedValue = parser.apply(input);
            logger.info("Current {}: {}", loggerLogAs, parsedValue);
            System.out.println("Current " + loggerLogAs + ": " + parsedValue);

            return parsedValue;
        } catch (NumberFormatException ex) {
            String errorMessage = loggerLogAs + ": " + input + " is not a number!";
            logger.error(errorMessage);
            System.err.println(errorMessage);
        }

        return defaultValue;
    }
}
