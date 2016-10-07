import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Postings ordered by decreasing term frequencies.
 *
 * @author Wasif (wasifale@buffalo.edu).
 */
public class TermAtATime extends Index {

    public TermAtATime(String indexFile) {
        super(indexFile, Comparator.comparingInt(Posting::frequency).reversed()); // sort by decreasing frequency
    }

    @Override
    public void and(String[] queryTerms) {
        Logger.function("termAtATimeQueryAnd", queryTerms);

        List<PostingList> queryTermsPostings = postingLists(queryTerms);

        if (!queryTermsPostings.isEmpty() && queryTermsPostings.size() == queryTerms.length) {
            StopWatch stopWatch = StopWatch.createStarted();

            Pair<PostingList, Integer> resultPair = and(queryTermsPostings);
            PostingList result = resultPair.first(); // intersected result list
            Integer comparisons = resultPair.second(); //comparisons

            stopWatch.stop();


            Pair<PostingList, Integer> resultOptimized = andOptimized(queryTermsPostings); // optimized
            Integer optimizedComparisons = resultOptimized.second(); // optimized comparisons

            logResult(stopWatch, result, comparisons, optimizedComparisons);
        } else {
            Logger.notFound();
        }
    }

    /**
     * Performs optimized term-at-a-time AND evaluation by sorting the postings by size.
     *
     * @param queryTermsPostings query terms postingsList in order of input
     * @return a pair containing merged Postings and the number of comparisons.
     */
    private Pair<PostingList, Integer> andOptimized(List<PostingList> queryTermsPostings) {
        List<PostingList> copy = new ArrayList<>(queryTermsPostings);
        copy.sort(Comparator.comparing(List::size, Comparator.naturalOrder())); // sort the postings
        return and(copy);
    }

    /**
     * Performs term-at-a-time AND evaluation.
     *
     * @param queryTermsPostings query terms postingsList in order of input
     * @return a pair containing merged Postings and the number of comparisons.
     */
    private Pair<PostingList, Integer> and(List<PostingList> queryTermsPostings) {
        PostingList result = new PostingList(queryTermsPostings.get(0));
        int comparisons = 0;
        for (int i = 1; i < queryTermsPostings.size(); ++i) {
            PostingList tempPostingList = new PostingList(result); // intermediate result
            Iterator<Posting> iterator = tempPostingList.iterator();
            while (iterator.hasNext()) {
                Posting tempPosting = iterator.next();
                boolean exists = false;
                for (Posting posting : queryTermsPostings.get(i)) {
                    ++comparisons;
                    if (posting.documentId() == tempPosting.documentId()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    iterator.remove(); // current intermediate posting should not be part of intersection
                }
            }
            result = new PostingList(tempPostingList);
        }
        return new Pair<>(result, comparisons);
    }

    @Override
    public void or(String[] queryTerms) {
        Logger.function("termAtATimeQueryOr", queryTerms);

        List<PostingList> queryTermsPostings = postingLists(queryTerms);

        if (!queryTermsPostings.isEmpty()) {
            StopWatch stopWatch = StopWatch.createStarted();

            Pair<PostingList, Integer> resultPair = or(queryTermsPostings);
            PostingList result = resultPair.first();
            Integer comparisons = resultPair.second();

            stopWatch.stop();


            Pair<PostingList, Integer> resultOptimized = orOptimized(queryTermsPostings);
            Integer optimizedComparisons = resultOptimized.second();

            logResult(stopWatch, result, comparisons, optimizedComparisons);
        } else {
            Logger.notFound();
        }
    }


    /**
     * Performs optimized term-at-a-time OR evaluation by sorting the postings by size.
     *
     * @param queryTermsPostings query terms postingsList in order of input
     * @return a pair containing merged Postings and the number of comparisons.
     */
    private Pair<PostingList, Integer> orOptimized(List<PostingList> queryTermsPostings) {
        List<PostingList> copy = new ArrayList<>(queryTermsPostings);
        copy.sort(Comparator.comparing(List::size, Comparator.naturalOrder()));
        return or(copy);
    }

    /**
     * Performs term-at-a-time OR evaluation.
     *
     * @param queryTermsPostings query terms postingsList in order of input
     * @return a pair containing merged Postings and the number of comparisons.
     */
    private Pair<PostingList, Integer> or(List<PostingList> queryTermsPostings) {
        PostingList result = new PostingList(queryTermsPostings.get(0));
        int comparisons = 0;
        for (int i = 1; i < queryTermsPostings.size(); ++i) {
            PostingList tempPostingList = new PostingList(result); // intermediate result
            List<Posting> append = new ArrayList<>(); // new documents to add to intermediate result

            for (Posting posting : queryTermsPostings.get(i)) {
                Iterator<Posting> iterator = tempPostingList.iterator();
                boolean exists = false;

                while (iterator.hasNext()) {
                    Posting tempPosting = iterator.next();
                    ++comparisons;
                    if (posting.documentId() == tempPosting.documentId()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    append.add(posting); // current posting is not found in intermediate result, so we add it
                }
            }
            tempPostingList.addAll(append);
            result = new PostingList(tempPostingList);
        }
        return new Pair<>(result, comparisons);
    }

    private void logResult(StopWatch stopWatch, PostingList result, Integer comparisons, Integer optimizedComparisons) {
        if (!result.isEmpty()) {
            Logger.found(result.size());
            Logger.comparisons(comparisons);
            Logger.time(stopWatch.elapsedSeconds());
            Logger.log(optimizedComparisons + " comparisons are made with optimization");
            List<Integer> documentIds = result.documentIds();
            documentIds.sort(Comparator.naturalOrder());
            Logger.result(documentIds);
        } else {
            Logger.notFound();
        }
    }
}
