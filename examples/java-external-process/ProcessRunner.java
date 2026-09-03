import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public class ProcessRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] command = {"echo", "hello"};
        ProcessRunner runner = new ProcessRunner();
        runner.byRuntime(command);
        runner.byProcessBuilder(command);
        runner.byInheritIO(command);
    }

    public void byRuntime(String[] command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        printStream(process);
    }

    public void byProcessBuilder(String[] command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        printStream(process);
    }

    public void byInheritIO(String[] command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).inheritIO().start();
        process.waitFor();
    }

    private void printStream(Process process) throws IOException, InterruptedException {
        try (BufferedReader reader = process.inputReader()) {
            reader.lines().forEach(System.out::println);
        }
        process.waitFor();
    }
}
