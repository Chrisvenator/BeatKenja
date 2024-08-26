package MapGeneration.PatternGeneration.CommonMethods;

import java.util.function.Function;

import static DataManager.Parameters.logger;

/**
 * The `Parser` class provides utility methods for parsing string inputs into numeric values.
 * It uses functional programming concepts to allow for flexible parsing of different numeric types,
 * with error handling and logging integrated into the process.
 */
public class Parser extends MapGeneratorCommons {

    /**
     * Parses a string input into a numeric value of the specified type.
     * If the input cannot be parsed due to a `NumberFormatException`, a default value is returned.
     * The method logs the parsed value or an error message depending on the success of the parsing.
     *
     * @param <T>          The type of the numeric value to be parsed, extending `Number`.
     * @param input        The string input to parse.
     * @param loggerLogAs  A description of the value being parsed, used in logging.
     * @param parser       A `Function` that takes a string and returns a value of type `T`.
     * @param defaultValue The default value to return if parsing fails.
     * @return The parsed numeric value, or the default value if parsing fails.
     */
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
