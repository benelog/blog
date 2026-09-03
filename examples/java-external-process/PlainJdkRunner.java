import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlainJdkRunner {
    public static void main(String[] args) throws Exception {
        run("echo", "hello");
        run("seq", "1", "100000");
        run("ls", "/no-such-dir");
        try {
            run("sleep", "60");
        } catch (TimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    static void run(String... command) throws IOException, InterruptedException, TimeoutException {
        Process process = new ProcessBuilder(command).start();
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        Thread outPump = Thread.ofVirtual().start(() -> process.inputReader().lines().forEach(l -> stdout.append(l).append('\n')));
        Thread errPump = Thread.ofVirtual().start(() -> process.errorReader().lines().forEach(l -> stderr.append(l).append('\n')));
        if (!process.waitFor(1, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new TimeoutException("timed out: " + String.join(" ", command));
        }
        outPump.join();
        errPump.join();
        System.out.println(command[0] + ": exit=" + process.exitValue()
                + ", stdout chars=" + stdout.length() + ", stderr=" + stderr.toString().trim());
    }
}
