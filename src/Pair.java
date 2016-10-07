import java.util.Objects;

/**
 * @author Wasif (wasifale@buffalo.edu).
 */
public class Pair<A, B> {
    A a;
    B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A first() {
        return a;
    }

    public B second() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(a, pair.a) &&
                Objects.equals(b, pair.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}
