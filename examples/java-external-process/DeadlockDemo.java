import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DeadlockDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("seq", "1", "100000").start();
        boolean finished = process.waitFor(3, TimeUnit.SECONDS);
        System.out.println("finished within 3s: " + finished + ", alive: " + process.isAlive());
        long lines = process.inputReader().lines().count();
        int exitCode = process.waitFor();
        System.out.println("read " + lines + " lines, exit=" + exitCode + ", alive: " + process.isAlive());
    }
}
