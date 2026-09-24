import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 셸로 실행한 명령에 destroy()를 호출하면 셸만 끝나고 셸이 만든 후손 프로세스는 남는 것을 재현한다.
 * 남은 후손은 확인 후 정리한다.
 */
public class DescendantLeakDemo {
    public static void main(String[] args) throws Exception {
        run("sh", "-c", "sleep 60");
        run("sh", "-c", "sleep 60; echo done");
        run("sh", "-c", "sleep 60 | cat");
        run("bash", "-c", "sleep 60");
    }

    static void run(String... command) throws Exception {
        Process process = new ProcessBuilder(command).start();
        Thread.sleep(300);
        List<ProcessHandle> descendants = process.descendants().toList();
        process.destroy();
        process.waitFor(1, TimeUnit.SECONDS);
        Thread.sleep(100);

        List<String> left = descendants.stream()
                .filter(ProcessHandle::isAlive)
                .map(p -> name(p) + "(ppid=" + p.parent().map(ProcessHandle::pid).orElse(-1L) + ")")
                .toList();
        System.out.println(String.join(" ", command) + " -> shell alive=" + process.isAlive()
                + ", children=" + descendants.size() + ", left=" + left);
        descendants.forEach(ProcessHandle::destroyForcibly);
    }

    static String name(ProcessHandle p) {
        String command = p.info().command().orElse("?");
        return command.substring(command.lastIndexOf('/') + 1);
    }
}
