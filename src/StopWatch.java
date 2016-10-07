import java.util.concurrent.TimeUnit;

/**
 * @author Wasif (wasifale@buffalo.edu).
 */
public class StopWatch {
    private long startNanos;
    private long stopNanos;

    private StopWatch(long startNanos) {
        this.startNanos = startNanos;
    }

    public static StopWatch createStarted() {
        return new StopWatch(System.nanoTime());
    }

    public void stop() {
        this.stopNanos = System.nanoTime();
    }

    public long elapsed(TimeUnit timeUnit) {
        if (stopNanos != 0L) {
            return timeUnit.convert(stopNanos - startNanos, TimeUnit.NANOSECONDS);
        } else {
            throw new RuntimeException("Call stop() before elapsed");
        }
    }

    public long elapsedSeconds() {
        return elapsed(TimeUnit.SECONDS);
    }

}
