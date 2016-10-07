import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Wasif (wasifale@buffalo.edu).
 */
public abstract class Index {
    private static final Pattern SPLIT_LINE_PATTERN = Pattern.compile("(\\\\c)|(\\\\m)");
    private static final Pattern SPLIT_POSTINGS_LIST_PATTERN = Pattern.compile("(, )");
    private static final Pattern SPLIT_POSTING_PATTERN = Pattern.compile("(/)");

    protected Map<String, PostingList> index;

    public Index(String indexFile, Comparator<Posting> postingComparator) {
        index = parse(indexFile, postingComparator);
    }

    public abstract void and(String[] queryTerms);

    public abstract void or(String[] queryTerms);

    public List<PostingList> postingLists(String[] queryTerms) {
        return Arrays.stream(queryTerms)
                .map(this::postingList)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public Optional<PostingList> postingList(String term) {
        return Optional.ofNullable(index.get(term));
    }

    public void topK(int k) {
        Logger.function("getTopK", k);

        List<String> topK = index.entrySet()
                .stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, PostingList> e) -> e.getValue().size()).reversed())
                .map(Map.Entry::getKey)
                .limit(k) // select only k!
                .collect(Collectors.toList());

        Logger.result(topK);
    }

    protected Map<String, PostingList> parse(String indexFile, Comparator<Posting> postingComparator) {
        StopWatch stopWatch = StopWatch.createStarted();

        Map<String, PostingList> map = new LinkedHashMap<>(24217); // NOTE: 24217 is num lines in term.idx

        try (Stream<String> lines = Files.lines(Paths.get(indexFile), StandardCharsets.UTF_8)) {
            lines.forEach(line -> {
                String[] split = SPLIT_LINE_PATTERN.split(line, 3);
                if (split.length == 3) {
                    String[] postingsArray = SPLIT_POSTINGS_LIST_PATTERN
                            .split(split[2].subSequence(1, split[2].length() - 1));

                    LinkedList<Posting> postings = new LinkedList<>();
                    for (String posting : postingsArray) {
                        String[] posting_freq = SPLIT_POSTING_PATTERN.split(posting);
                        postings.add(new Posting(Integer.parseInt(posting_freq[0]), Integer.parseInt(posting_freq[1])));
                    }

                    postings.sort(postingComparator);

                    map.put(split[0], new PostingList(postings));
                }
            });
        } catch (IOException e) {
            System.err.println("IOException when reading index file: " + e.getMessage());
            e.printStackTrace();
        }

        stopWatch.stop();
        System.out.println("Created " + this.getClass().getName() + " in " + stopWatch.elapsedSeconds() + " seconds.");

        return map;
    }
}
