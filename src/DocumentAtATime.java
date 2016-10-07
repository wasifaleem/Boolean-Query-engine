import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Postings ordered by increasing document IDs
 *
 * @author Wasif (wasifale@buffalo.edu).
 */
public class DocumentAtATime extends Index {

    public DocumentAtATime(String indexFile) {
        super(indexFile, Comparator.comparing(Posting::documentId)); // sort by increasing documentId
    }

    /**
     * Performs Document-at-a-time AND evaluation
     *
     * @param queryTerms query terms in order of input
     */
    @Override
    public void and(String[] queryTerms) {
        Logger.function("docAtATimeQueryAnd", queryTerms);

        List<PostingList> queryTermsPostings = postingLists(queryTerms);

        if (!queryTermsPostings.isEmpty()  && queryTermsPostings.size() == queryTerms.length) {

            PostingList result = new PostingList();

            int[] pointers = new int[queryTermsPostings.size()]; // pointers into all the posting lists
            int comparisons = 0;

            StopWatch stopWatch = StopWatch.createStarted();

            while_loop:
            // main while loop label, we'll use this to break out
            while (true) {

                boolean isEqual = true;
                Posting first = null;

                for (int i = 0; i < pointers.length; i++) { // compare if all docid's in current step are equal
                    PostingList postings = queryTermsPostings.get(i);
                    if (pointers[i] < postings.size()) {
                        Posting currentPosting = postings.get(pointers[i]);
                        if (first == null) {
                            first = currentPosting;
                        } else {
                            ++comparisons;
                            if (first.documentId() != currentPosting.documentId()) {
                                isEqual = false;
                            }
                        }
                    } else {
                        break while_loop; // break because we exceeded a pointer by it's posting-list size
                    }
                }

                if (isEqual) {
                    result.add(first);
                    for (int i = 0; i < pointers.length; i++) { // increment all pointers
                        PostingList postings = queryTermsPostings.get(i);
                        if (pointers[i] < postings.size()) {
                            ++pointers[i];
                        }
                    }
                } else {
                    int maxDocId = Integer.MIN_VALUE; // find max docId to increment other pointers to
                    for (int i = 0; i < pointers.length; i++) {
                        PostingList postings = queryTermsPostings.get(i);
                        if (pointers[i] < postings.size()) {
                            Posting currentPosting = postings.get(pointers[i]);
                            ++comparisons;
                            if (currentPosting.documentId() > maxDocId) {
                                maxDocId = currentPosting.documentId();
                            }
                        }
                    }


                    // increment pointers if it's less than maxDocId and if it can be incremented
                    for (int i = 0; i < pointers.length; i++) {
                        PostingList postings = queryTermsPostings.get(i);
                        if (pointers[i] < postings.size()) {
                            Posting currentPosting = postings.get(pointers[i]);
                            ++comparisons;
                            if (currentPosting.documentId() < maxDocId) {
                                ++pointers[i];
                            }
                        } else {
                            break while_loop;
                        }
                    }
                }
            }


            stopWatch.stop();
            logResult(stopWatch, result, comparisons);
        } else {
            Logger.notFound();
        }
    }

    /**
     * Performs Document-at-a-time OR evaluation
     *
     * @param queryTerms query terms in order of input
     */
    @Override
    public void or(String[] queryTerms) {
        Logger.function("docAtATimeQueryOr", queryTerms);

        List<PostingList> queryTermsPostings = postingLists(queryTerms);

        if (!queryTermsPostings.isEmpty()) {

            PostingList result = new PostingList();

            int[] pointers = new int[queryTermsPostings.size()]; // pointers into all the posting lists
            int comparisons = 0;

            StopWatch stopWatch = StopWatch.createStarted();

            while_loop:
            // main while loop label, we'll use this to break out
            while (true) {
                // find min posting in current pointers
                Posting min = new Posting(Integer.MAX_VALUE, 0);
                int countExhaustedPointers = 0; // counts the number of exhausted pointer, used to break out of main loop
                for (int i = 0; i < pointers.length; i++) {
                    PostingList postings = queryTermsPostings.get(i);
                    if (pointers[i] < postings.size()) {
                        Posting currentPosting = postings.get(pointers[i]);
                        ++comparisons;
                        if (currentPosting.documentId() < min.documentId()) {
                            min = currentPosting;
                        }
                    } else {
                        if (++countExhaustedPointers == pointers.length) { // have we reached the end of all posting-lists?
                            break while_loop;
                        }
                    }
                }
                result.add(min); // add the current min
                for (int i = 0; i < pointers.length; i++) { // increment the pointers of all min doc-id's
                    PostingList postings = queryTermsPostings.get(i);
                    if (pointers[i] < postings.size()) {
                        Posting currentPosting = postings.get(pointers[i]);
                        ++comparisons;
                        if (currentPosting.documentId() == min.documentId()) {
                            ++pointers[i];
                        }
                    }
                }
            }


            stopWatch.stop();
            logResult(stopWatch, result, comparisons);
        } else {
            Logger.notFound();
        }
    }

    private void logResult(StopWatch stopWatch, PostingList result, Integer comparisons) {
        if (!result.isEmpty()) {
            Logger.found(result.size());
            Logger.comparisons(comparisons);
            Logger.time(stopWatch.elapsedSeconds());
            List<Integer> documentIds = result.documentIds();
            documentIds.sort(Comparator.naturalOrder());
            Logger.result(documentIds);
        } else {
            Logger.notFound();
        }
    }
}
