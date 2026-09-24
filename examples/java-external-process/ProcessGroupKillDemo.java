import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * setsid로 새 프로세스 그룹을 만들어 실행하고, 시간 초과 때 그룹 전체에 SIGTERM을 보내 후손까지 정리한다.
 * 후손이 다시 setsid로 그룹을 벗어나면 이 방법으로 정리되지 않는 것도 함께 보여준다.
 */
public class ProcessGroupKillDemo {
    public static void main(String[] args) throws Exception {
        run("sleep 60 | cat");
        run("setsid sleep 60 & sleep 60");
    }

    static void run(String script) throws Exception {
        Process process = new ProcessBuilder("setsid", "sh", "-c", script).start();
        if (!process.waitFor(1, TimeUnit.SECONDS)) {
            List<ProcessHandle> descendants = process.descendants().toList();
            killGroup(process);
            Thread.sleep(100);

            List<String> left = descendants.stream()
                    .filter(ProcessHandle::isAlive)
                    .map(p -> p.info().commandLine().orElse("?"))
                    .toList();
            System.out.println("[" + script + "] pgid=" + process.pid()
                    + ", shell alive=" + process.isAlive() + ", left=" + left);
            descendants.forEach(ProcessHandle::destroyForcibly);
        }
    }

    // setsid로 그룹 리더가 된 자식의 PID가 곧 프로세스 그룹 ID
    static void killGroup(Process process) throws Exception {
        long pgid = process.pid();
        new ProcessBuilder("kill", "-TERM", "--", "-" + pgid).start().waitFor();
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            new ProcessBuilder("kill", "-KILL", "--", "-" + pgid).start().waitFor();
            process.waitFor();
        }
    }
}
