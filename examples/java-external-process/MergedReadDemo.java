import java.io.IOException;

/**
 * redirectErrorStream(true)로 표준 오류를 표준 출력에 합쳐서
 * SequentialReadDemo와 같은 명령을 파이프 하나로 읽는다.
 */
public class MergedReadDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("sh", "-c", "seq 1 100000 >&2; echo done")
                .redirectErrorStream(true)
                .start();
        long lines = process.inputReader().lines().count();
        System.out.println("read " + lines + " lines, exit=" + process.waitFor());
    }
}
