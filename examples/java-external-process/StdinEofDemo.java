import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

/**
 * 표준 입력 파이프의 쓰기 끝을 열어 둔 채로 두면 cat이 EOF를 받지 못해 끝나지 않는 것을 재현하고,
 * 스트림을 닫거나 빈 입력을 지정해서 끝내는 방법을 함께 보여준다.
 */
public class StdinEofDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process opened = new ProcessBuilder("cat").start();
        System.out.println("stdin 열어 둠: finished within 1s=" + opened.waitFor(1, TimeUnit.SECONDS)
                + ", alive=" + opened.isAlive());
        opened.destroy();
        opened.waitFor();

        Process closed = new ProcessBuilder("cat").start();
        closed.getOutputStream().close();
        System.out.println("stdin 닫음: finished within 1s=" + closed.waitFor(1, TimeUnit.SECONDS)
                + ", exit=" + closed.exitValue());

        Process devNull = new ProcessBuilder("cat")
                .redirectInput(new File("/dev/null"))
                .start();
        System.out.println("/dev/null 입력: finished within 1s=" + devNull.waitFor(1, TimeUnit.SECONDS)
                + ", exit=" + devNull.exitValue());

        Process fed = new ProcessBuilder("cat").start();
        try (Writer writer = fed.outputWriter()) {
            writer.write("hello\n");
        }
        String echoed = fed.inputReader().readLine();
        System.out.println("입력 후 닫음: echoed=" + echoed + ", exit=" + fed.waitFor());
    }
}
