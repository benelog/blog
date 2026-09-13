import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * JDK 17의 outputWriter()가 getOutputStream()을 감싸는 코드를 대신하는 것과,
 * BufferedWriter와 OutputStream에 번갈아 쓸 때 flush()를 빠뜨리면 순서가 뒤바뀌는 것을 보여준다.
 */
public class OutputWriterDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process wrapped = new ProcessBuilder("cat").start();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(wrapped.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write("hello\n");
        }
        System.out.println("직접 감싼 Writer: echoed=" + wrapped.inputReader().readLine()
                + ", exit=" + wrapped.waitFor());

        Process unflushed = new ProcessBuilder("cat").start();
        BufferedWriter writer = unflushed.outputWriter();
        writer.write("1 writer\n");
        unflushed.getOutputStream().write("2 stream\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        System.out.println("flush 없이 섞어 씀: echoed=" + unflushed.inputReader().lines().toList()
                + ", exit=" + unflushed.waitFor());

        Process flushed = new ProcessBuilder("cat").start();
        writer = flushed.outputWriter();
        writer.write("1 writer\n");
        writer.flush();
        flushed.getOutputStream().write("2 stream\n".getBytes(StandardCharsets.UTF_8));
        writer.close();
        System.out.println("flush 후 섞어 씀: echoed=" + flushed.inputReader().lines().toList()
                + ", exit=" + flushed.waitFor());
    }
}
