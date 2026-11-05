package ing.prometheusreengineer.minecraft.util.java.unchecked;

@FunctionalInterface
public interface UncheckedRunnable extends Runnable {
    void checkedRun() throws Throwable;
    @Override
    default void run() {
        try {
            checkedRun();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
