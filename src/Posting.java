/**
 * @author Wasif (wasifale@buffalo.edu).
 */
public class Posting {
    private final int documentId;
    private final int frequency;

    public Posting(int documentId, int frequency) {
        this.documentId = documentId;
        this.frequency = frequency;
    }

    public int documentId() {
        return documentId;
    }

    public int frequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return "Posting{" +
                "documentId='" + documentId + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
