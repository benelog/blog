import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * setsid로 새 프로세스 그룹을 만들어 실행하고, 작업이 끝나면 그룹 전체에 SIGTERM을 보내 후손까지 정리한다.
 * 유예 시간 뒤에도 그룹에 프로세스가 남아 있으면 SIGKILL을 보낸다.
 * 후손이 다시 setsid로 그룹을 벗어나면 이 방법으로 정리되지 않는 것도 함께 보여준다.
 */
public class ProcessGroupKillDemo {
    public static void main(String[] args) throws Exception {
        run("sleep 60 | cat");
        run("(trap '' TERM; sleep 60) & sleep 60");
        run("setsid sleep 60 & sleep 60");
    }

    static void run(String script) throws Exception {
        Process process = new ProcessBuilder("setsid", "sh", "-c", script).start();
        List<ProcessHandle> descendants = List.of();
        try {
            if (!process.waitFor(1, TimeUnit.SECONDS)) {
                descendants = process.descendants().toList(); // 결과 확인용 스냅샷
            }
        } finally {
            killGroup(process.pid()); // setsid로 그룹 리더가 된 자식의 PID가 곧 프로세스 그룹 ID
            process.waitFor();
        }

        List<String> left = descendants.stream()
                .filter(ProcessHandle::isAlive)
                .map(p -> p.info().commandLine().orElse("?"))
                .toList();
        System.out.println("[" + script + "] pgid=" + process.pid() + ", left=" + left);
        descendants.forEach(ProcessHandle::destroyForcibly);
    }

    static void killGroup(long pgid) throws Exception {
        signalGroup("TERM", pgid);
        if (!awaitGroupExit(pgid, 5)) {
            signalGroup("KILL", pgid);
            awaitGroupExit(pgid, 5);
        }
    }

    static boolean awaitGroupExit(long pgid, long seconds) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (signalGroup("0", pgid)) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            Thread.sleep(100);
        }
        return true;
    }

    // kill의 종료 코드 0은 그룹에 시그널을 받은 프로세스가 하나 이상 있다는 뜻
    static boolean signalGroup(String signal, long pgid) throws Exception {
        Process kill = new ProcessBuilder("kill", "-" + signal, "--", "-" + pgid)
                .redirectErrorStream(true)
                .redirectOutput(Redirect.DISCARD)
                .start();
        return kill.waitFor() == 0;
    }
}
