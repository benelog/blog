import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

public class ZtExecRunner {
    public static void main(String[] args) throws Exception {
        captureOutput();
        largeOutput();
        timeout();
        exitValue();
    }

    static void captureOutput() throws IOException, InterruptedException, TimeoutException {
        ProcessResult result = new ProcessExecutor()
                .command("echo", "hello")
                .readOutput(true)
                .exitValueNormal()
                .execute();
        System.out.println("output=" + result.outputUTF8().trim() + ", exit=" + result.getExitValue());
    }

    static void largeOutput() throws IOException, InterruptedException, TimeoutException {
        AtomicLong lines = new AtomicLong();
        ProcessResult result = new ProcessExecutor()
                .command("seq", "1", "100000")
                .redirectOutput(line -> lines.incrementAndGet())
                .timeout(3, TimeUnit.SECONDS)
                .execute();
        System.out.println("seq lines=" + lines.get() + ", exit=" + result.getExitValue());
    }

    static void timeout() throws IOException, InterruptedException {
        try {
            new ProcessExecutor()
                    .command("sleep", "60")
                    .timeout(1, TimeUnit.SECONDS)
                    .execute();
        } catch (TimeoutException e) {
            System.out.println("timeout: " + e.getMessage());
        }
    }

    static void exitValue() throws IOException, InterruptedException, TimeoutException {
        try {
            new ProcessExecutor()
                    .command("ls", "/no-such-dir")
                    .readOutput(true)
                    .exitValueNormal()
                    .execute();
        } catch (InvalidExitValueException e) {
            System.out.println("exit=" + e.getExitValue() + ", output=" + e.getResult().outputUTF8().trim());
        }
    }
}
