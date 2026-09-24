import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * systemd-run --scope로 명령을 전용 cgroup에서 실행하고, 시간 초과 때 scope 단위를 멈춰서
 * setsid로 프로세스 그룹을 벗어난 후손까지 정리한다. systemd의 사용자 인스턴스가 필요하다.
 */
public class SystemdScopeDemo {
    public static void main(String[] args) throws Exception {
        String unit = "job-" + UUID.randomUUID() + ".scope";
        Process process = new ProcessBuilder(
                "systemd-run", "--user", "--scope", "--quiet", "--unit=" + unit,
                "sh", "-c", "setsid sleep 60 & sleep 60")
                .start();

        if (!process.waitFor(1, TimeUnit.SECONDS)) {
            System.out.println("cgroup: " + Files.readString(Path.of("/proc/" + process.pid() + "/cgroup")).trim());
            List<ProcessHandle> descendants = process.descendants().toList();
            new ProcessBuilder("systemctl", "--user", "stop", unit).start().waitFor();
            process.waitFor();

            List<String> left = descendants.stream()
                    .filter(ProcessHandle::isAlive)
                    .map(p -> p.info().commandLine().orElse("?"))
                    .toList();
            System.out.println("descendants=" + descendants.size() + ", left=" + left);
        }
    }
}
