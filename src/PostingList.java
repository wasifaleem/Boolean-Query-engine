import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Convenience wrapper
 * Delegates to LinkedList
 *
 * @author Wasif (wasifale@buffalo.edu).
 */
public class PostingList extends AbstractSequentialList<Posting> {
    private final LinkedList<Posting> postings;

    public PostingList() {
        postings = new LinkedList<>();
    }

    public PostingList(LinkedList<Posting> postings) {
        this.postings = postings;
    }

    public PostingList(PostingList postings) {
        this.postings = new LinkedList<>(postings);
    }

    public PostingList intersect(PostingList other) {
        PostingList answer = new PostingList();
        int p1Index = 0, p2Index = 0;
        int compares = 0;
        while (p1Index < postings.size() && p2Index < other.size()) {
            Posting p1 = postings.get(p1Index);
            Posting p2 = other.get(p2Index);
            if (p1.documentId() == p2.documentId()) {
                answer.add(p1);
                ++p1Index;
                ++p2Index;
            } else if (p1.documentId() < p2.documentId()) {
                ++p1Index;
            } else {
                ++p2Index;
            }
            ++compares;
        }
        return answer;
    }

    @Override
    public int size() {
        return postings.size();
    }

    @Override
    public String toString() {
        return "PostingList{" +
                "postings=" + postings +
                '}';
    }

    public List<Integer> documentIds() {
        return postings.stream().map(Posting::documentId).collect(toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostingList that = (PostingList) o;
        return Objects.equals(postings, that.postings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postings);
    }

    @Override
    public ListIterator<Posting> listIterator(int index) {
        return postings.listIterator(index);
    }

    @Override
    public Spliterator<Posting> spliterator() {
        return postings.spliterator();
    }
}
