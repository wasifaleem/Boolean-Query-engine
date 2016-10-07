import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Wasif (wasifale@buffalo.edu).
 */
public class CSE535Assignment {

    public static void main(String[] args) {
        if (args.length == 4) {

            DocumentAtATime documentAtATime = new DocumentAtATime(args[0]);
            TermAtATime termAtATime = new TermAtATime(args[0]);

            // getTopK
            documentAtATime.topK(Integer.parseInt(args[2]));

            parseQueryFile(args[3])
                    .forEach(queryTerms -> {
                        Arrays.stream(queryTerms).forEach(term -> {
                            getPostings(documentAtATime, termAtATime, term); // getPostings
                        });

                        termAtATime.and(queryTerms); // TAAT AND
                        termAtATime.or(queryTerms); // TAAT OR

                        documentAtATime.and(queryTerms); // DAAT AND
                        documentAtATime.or(queryTerms); // DAAT OR
                    });

            Logger.write(args[1]);
        } else {
            System.err.println("Invalid command-line arguments");
            System.exit(1);
        }
    }

    private static void getPostings(DocumentAtATime documentAtATime, TermAtATime termAtATime, String term) {
        Logger.function("getPostings", term);
        Optional<PostingList> daatPostings = documentAtATime.postingList(term);
        if (daatPostings.isPresent()) {
            Logger.log("Ordered by doc IDs", daatPostings.get().documentIds());

            termAtATime.postingList(term).ifPresent(postings -> {
                Logger.log("Ordered by TF", postings.documentIds());
            });
        } else {
            Logger.log("term not found");
        }
    }

    private static List<String[]> parseQueryFile(String arg) {
        final Pattern SPACE_PATTERN = Pattern.compile("\\s");

        try (Stream<String> lines = Files.lines(Paths.get(arg), StandardCharsets.UTF_8)) {
            return lines.map(SPACE_PATTERN::split).collect(toList());
        } catch (IOException e) {
            System.err.println("IOException when reading query file: " + e.getMessage());
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
