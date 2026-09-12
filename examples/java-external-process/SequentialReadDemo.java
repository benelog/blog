import java.io.IOException;

/**
 * 표준 출력을 끝까지 읽은 뒤에 표준 오류를 읽는 순차 처리가 교착에 빠지는 것을 재현한다.
 * 표준 오류 파이프가 먼저 차서 하위 프로세스가 멈추고, 부모는 오지 않는 표준 출력의 EOF를 기다린다.
 * 이 프로그램은 끝나지 않으므로 Ctrl+C로 중단해야 한다.
 */
public class SequentialReadDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("sh", "-c", "seq 1 100000 >&2; echo done").start();
        System.out.println("reading stdout...");
        long outLines = process.inputReader().lines().count();
        long errLines = process.errorReader().lines().count();
        System.out.println("stdout " + outLines + ", stderr " + errLines + ", exit=" + process.waitFor());
    }
}
