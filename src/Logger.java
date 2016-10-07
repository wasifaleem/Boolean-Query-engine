import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Wasif (wasifale@buffalo.edu).
 */
public final class Logger {

    private static final List<String> lines = new ArrayList<>();
    public static final Collector<CharSequence, ?, String> STRING_COLLECTOR = Collectors.joining(", ");

    private Logger() {
    }

    public static void log(String line) {
        lines.add(line);
    }

    public static <T> void log(String key, Collection<T> args) {
        lines.add(key + ": " +
                args.stream()
                        .map(Object::toString)
                        .collect(STRING_COLLECTOR));
    }

    public static void function(String function, Object o) {
        lines.add("FUNCTION: " + function + " " + o.toString());
    }

    public static void function(String function, Object[] args) {
        lines.add("FUNCTION: " + function + " " +
                Arrays.stream(args)
                        .map(Object::toString)
                        .collect(STRING_COLLECTOR));
    }

    public static void function(String function, Collection<Object> args) {
        lines.add("FUNCTION: " + function + " " +
                args.stream()
                        .map(Object::toString)
                        .collect(STRING_COLLECTOR));
    }

    public static void result(Collection<?> args) {
        lines.add("Result: "
                + args.stream()
                .map(Object::toString)
                .collect(STRING_COLLECTOR));
    }

    public static void write(String logFile) {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(logFile), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : lines) {
                System.out.println(line);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            System.err.println("IOException when writing log file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void found(int size) {
        lines.add(size + " documents are found");
    }

    public static void comparisons(int size) {
        lines.add(size + " comparisons are made");
    }

    public static void time(long seconds) {
        lines.add(seconds + " seconds are used");
    }

    public static void notFound() {
        lines.add("terms not found");
    }
}
