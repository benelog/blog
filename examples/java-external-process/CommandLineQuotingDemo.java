import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Commons Exec의 CommandLine이 공백을 포함한 인자에 따옴표를 붙이면
 * 셸을 거치지 않는 Runtime.exec(String[])에서는 그 따옴표가 인자의 일부가 되는 것을 보여준다.
 */
public class CommandLineQuotingDemo {
    public static void main(String[] args) throws IOException {
        CommandLine parsed = new CommandLine("printf").addArguments("[%s]\\n \"hello world\"");
        run("문자열을 나눔", parsed);

        CommandLine quoted = new CommandLine("printf").addArgument("[%s]\\n").addArgument("hello world");
        run("배열, quoting 기본값", quoted);

        CommandLine raw = new CommandLine("printf").addArgument("[%s]\\n").addArgument("hello world", false);
        run("배열, quoting 끔", raw);
    }

    static void run(String label, CommandLine cmdLine) throws IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setStreamHandler(new PumpStreamHandler(stdout));
        executor.execute(cmdLine);
        System.out.println(label + ": args=" + Arrays.toString(cmdLine.getArguments())
                + " -> printf 출력=" + stdout.toString().trim());
    }
}
