import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

public class CommonsExecRunner {
    public static void main(String[] args) throws IOException {
        run("echo", "hello");
        run("seq", "1", "100000");
        run("sleep", "60");
        run("ls", "/no-such-dir");
    }

    static void run(String... command) throws IOException {
        CommandLine cmdLine = new CommandLine(command[0]);
        cmdLine.addArguments(Arrays.copyOfRange(command, 1, command.length), false);
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(Duration.ofSeconds(1)).get();
        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
        executor.setWatchdog(watchdog);
        try {
            int exitValue = executor.execute(cmdLine);
            System.out.println(command[0] + ": exit=" + exitValue + ", stdout bytes=" + stdout.size());
        } catch (ExecuteException e) {
            System.out.println(command[0] + ": exit=" + e.getExitValue()
                    + ", killed by watchdog=" + watchdog.killedProcess()
                    + ", stderr=" + stderr.toString().trim());
        }
    }
}
