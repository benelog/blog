# Java 외부 프로세스 글의 후손 프로세스 정리 절 검증 — 2026-09-25

- 대상: [java-external-process.adoc](../src/content/java-external-process.adoc)
- 검토 기준 커밋: `18bb6f414952d9da1b02205742c91ded1d42ded2`
- 이전 기록: [2026-09-13 검증](java-external-process-2026-09-13.md), [2026-09-06 검증](java-external-process-2026-09-06.md)
- 범위: 기준 커밋에서 새로 추가한 "후손 프로세스의 정리" 절(본문 388~561행)과 예제 `DescendantLeakDemo.java`, `ProcessGroupKillDemo.java`, `SystemdScopeDemo.java`. 후손이 남는 사례의 예시 프로그램, 셸·`setsid`·`kill`의 동작, cgroup v2와 systemd 설명, 예제 코드의 정리 누락을 봤다.
- 방법: Codex 적대적 리뷰(`adversarial-review --base HEAD~1`) 후 Claude가 man 페이지·커널 문서·systemd와 OpenSSH·adb 소스로 재검증했다. 예시 프로그램의 프로세스 구조와 Codex가 지적한 정리 누락은 로컬(Ubuntu 24.04, 커널 6.17, JDK 25, systemd 255)에서 실행해 확인했다.
- 결과: **우선 수정할 사항 3건, 표현을 보완할 사항 2건.** 후손이 남는 원리, 프로세스 그룹과 cgroup의 차이, systemd scope로 정리하는 방법이라는 절의 핵심 설명은 타당하다. 다만 두 정리 예제가 본문이 말한 조건(SIGTERM을 무시하는 후손, 직접 자식이 먼저 정상 종료하는 경우)을 코드로 처리하지 않고, 래퍼 사례 중 headless Chrome은 실제로 후손을 남기지 않았다.
- 반영 상태: **5건 모두 본문과 예제에 반영 완료(커밋 전).** 반영 내역은 문서 끝의 "반영 내역" 절에 있다. 검증 본문의 수정 제안은 작성 당시 그대로 둔다.
- 아래 행 번호는 위 커밋 기준이다.

## 우선 수정할 사항

### 1. 그룹 종료 예제가 셸의 종료만 보고 SIGKILL을 생략함

**반영 완료.** 아래 "반영 내역" 참고.

**위치: 본문 484~491행의 `killGroup()`, 496행.** 코드는 SIGTERM 뒤 5초 안에 직접 자식(셸)이 끝나면 SIGKILL을 보내지 않는다. 496행은 "셸이 끝나도 SIGTERM을 무시한 후손이 남을 수 있으므로, 이런 명령을 다룬다면 유예 시간 뒤에 그룹 전체에 SIGKILL을 한 번 더 보냅니다"라고 적었지만 예제 코드에는 그 처리가 없다.

`process.waitFor()`는 그룹이 아니라 직접 자식만 기다린다. 같은 그룹에 SIGTERM을 무시하는 후손이 있으면 셸만 끝나고 후손은 남는다. 이 경우는 `setsid`로 그룹을 벗어난 경우와 달리 프로세스 그룹 방식으로 정리할 수 있는데도 예제가 놓친다.

실행 확인: `killGroup()`을 그대로 옮긴 재현 코드로 `setsid sh -c "(trap '' TERM; sleep 71) & sleep 60"`을 실행했다. 1초 시간 초과 뒤 그룹에 SIGTERM을 보내자 셸은 끝났고, `sleep 71`은 SIGKILL을 받지 못해 남았다.

수정 제안: 직접 자식이 아니라 그룹에 남은 프로세스가 있는지를 `kill -0`으로 확인하면서 유예 시간을 기다리고, 그 뒤에도 남아 있으면 그룹 전체에 SIGKILL을 보낸다. 아래 코드를 수정 제안 2의 `finally` 구조와 함께 실행해, SIGTERM을 무시하는 `sleep`은 5초 유예 뒤 SIGKILL로, 백그라운드로 남은 `sleep`은 SIGTERM으로 모두 정리되는 것을 확인했다.

```java
static void killGroup(long pgid) throws Exception {
    signalGroup("TERM", pgid);
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (signalGroup("0", pgid) && System.nanoTime() < deadline) {
        Thread.sleep(100);
    }
    if (signalGroup("0", pgid)) {
        signalGroup("KILL", pgid);
    }
}

// kill의 종료 코드 0은 그룹에 시그널을 받은 프로세스가 하나 이상 있다는 뜻
static boolean signalGroup(String signal, long pgid) throws Exception {
    Process kill = new ProcessBuilder("kill", "-" + signal, "--", "-" + pgid)
            .redirectErrorStream(true)
            .redirectOutput(Redirect.DISCARD)
            .start();
    return kill.waitFor() == 0;
}
```

496행은 코드와 맞춰 다음처럼 고친다.

> ``waitFor()``는 직접 자식인 셸만 기다리므로, 셸이 끝났다고 그룹 전체가 끝났다고 볼 수 없습니다. 그래서 ``kill -0``으로 그룹에 남은 프로세스가 있는지 확인하면서 유예 시간을 기다리고, 그 뒤에도 남아 있으면 그룹 전체에 SIGKILL을 보냅니다.

근거: [kill(2)](https://man7.org/linux/man-pages/man2/kill.2.html)의 음수 PID와 시그널 0 설명, [ProcessHandle.descendants()](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ProcessHandle.html#descendants())의 스냅샷 계약.

실행 확인. Codex 지적을 재현해 채택했다.

### 2. 두 정리 예제가 시간 초과 때만 정리해서, 직접 자식이 먼저 끝나면 후손이 남음

**반영 완료.** 아래 "반영 내역" 참고.

**위치: 본문 468행(`ProcessGroupKillDemo`), 527행(`SystemdScopeDemo`).** 두 예제 모두 `if (!process.waitFor(1, TimeUnit.SECONDS))` 안에서만 정리한다. 본문 401행이 사례로 든 "``server &`` 뒤에 ``wait``가 없는 스크립트"처럼 셸이 바로 정상 종료하면 `waitFor()`가 `true`를 돌려주므로 정리 코드가 실행되지 않는다.

실행 확인:

- `setsid sh -c "sleep 72 &"`를 `ProcessGroupKillDemo`와 같은 구조로 실행하자 셸은 1초 안에 끝났고, Java 프로그램이 끝난 뒤에도 `sleep 72`가 남았다.
- `systemd-run --user --scope sh -c 'sleep 75 &'`를 실행한 뒤 셸이 끝나도 scope는 `active` 상태로 남았다. `systemctl stop`을 호출한 뒤에야 `inactive`가 되고 `sleep 75`도 끝났다. [systemd.scope(5)](https://www.freedesktop.org/software/systemd/man/latest/systemd.scope.html)는 scope의 수명이 특정 프로세스가 아니라 "scope 안에 프로세스가 하나 이상 있는지"에 묶인다고 설명한다.

수정 제안: 일회성 작업의 후손을 모두 회수하는 정책이라면 정상 종료, 실패, 시간 초과 모두에서 정리하도록 `try`/`finally`에 둔다.

```java
Process process = new ProcessBuilder("setsid", "sh", "-c", script).start();
try {
    if (!process.waitFor(1, TimeUnit.SECONDS)) {
        System.out.println("timed out: " + script);
    }
} finally {
    killGroup(process.pid());
    process.waitFor();
}
```

본문에는 코드 앞이나 뒤에 다음 문장을 덧붙인다.

> 직접 자식이 정상 종료해도 백그라운드로 띄운 후손은 남을 수 있으므로, 작업의 후손을 모두 회수하려면 시간 초과 때만이 아니라 ``finally``에서 항상 그룹이나 scope를 정리합니다.

근거: 위 실행 결과, [systemd.scope(5)](https://www.freedesktop.org/software/systemd/man/latest/systemd.scope.html).

실행 확인. Codex 지적을 재현해 채택했다.

### 3. headless Chrome은 브라우저 프로세스가 끝나면 자식도 함께 끝나 사례로 맞지 않음

**반영 완료.** 아래 "반영 내역" 참고.

**위치: 본문 399행.** "headless Chrome은 renderer와 GPU 프로세스를 따로 띄우고"라고 적어 후손이 남는 사례로 들었다.

Chrome이 zygote, GPU, utility, renderer 프로세스를 따로 띄우는 것은 맞다. `google-chrome --headless=new --user-data-dir=<임시 경로> --remote-debugging-port=0 about:blank`로 실행하자 프로세스가 12개 생겼다. 하지만 브라우저 프로세스(`google-chrome` 스크립트가 exec한 프로세스)에 SIGTERM을 보내자 2초 안에, SIGKILL을 보내자 1초 안에 나머지 프로세스가 모두 끝났다. `destroy()`나 `destroyForcibly()`로 정리되므로 "후손이 남는 사례"로 들기에 맞지 않다.

Selenium의 ChromeDriver처럼 Chrome을 다시 띄우는 드라이버를 종료할 때 Chrome이 남는 문제가 알려져 있지만, 이번에는 실행 확인하지 않았다.

수정 제안: Chrome을 예시에서 뺀다. 표현 보완 1의 npm·LibreOffice 문장과 합쳐 399행을 고친다.

실행 확인. Codex가 지적하지 않았고 Claude가 발견했다.

## 표현을 보완할 사항

### 1. `npm run`의 실행 순서와 LibreOffice가 후손을 남기는 조건

**반영 완료.** 아래 "반영 내역" 참고.

**위치: 본문 399행.** "``npm run``은 셸과 node를 거쳐 명령을 실행합니다"는 셸이 먼저이고 node가 그다음인 것처럼 읽힌다. 실제 순서는 node로 실행되는 npm이 `sh -c`를 띄우고, 셸이 다시 명령을 실행하는 구조다. `npm run`에 SIGTERM을 보내자 `sh`는 끝났지만 `sleep 30`은 `systemd --user`(PID 1693)로 재부모화되어 남았다.

"LibreOffice의 ``soffice``는 실제 작업을 하는 ``soffice.bin``을 실행합니다"도 중간 단계와 조건이 빠졌다. `/usr/bin/soffice`는 셸 스크립트이고 `oosplash`를 exec하며, `oosplash`가 `soffice.bin`을 자식으로 실행한다. `oosplash`에 SIGTERM을 보내면 `soffice.bin`도 함께 끝났고, SIGKILL을 보냈을 때만 `soffice.bin`이 남았다.

수정 제안:

> * **래퍼나 런처를 거쳐 실행되는 프로그램**: ``npm run``은 node로 실행되는 npm이 ``sh -c``로 스크립트를 실행하므로, npm 프로세스를 끝내도 셸이 실행한 명령이 남을 수 있습니다. LibreOffice의 ``soffice``는 ``oosplash``를 거쳐 실제 작업을 하는 ``soffice.bin``을 실행하며, ``oosplash``를 SIGKILL로 끝내면 ``soffice.bin``이 남습니다. ``git``도 fetch나 clone 중에 ``ssh``나 ``git-remote-https``를 자식으로 실행합니다.

근거: 로컬 실행(npm 11.16.0, Node.js 24.6.0, Ubuntu 24.04의 LibreOffice 패키지).

실행 확인.

### 2. scope 예제가 `systemd-run`과 `systemctl`의 실패를 확인하지 않음

**반영 완료.** 아래 "반영 내역" 참고.

**위치: 본문 521~538행의 `SystemdScopeDemo`.** `systemd-run`이 1초 안에 실패하면 `waitFor()`가 `true`를 돌려주므로 예제는 아무것도 출력하지 않고 끝난다. `systemctl stop`이 실패해도 종료 코드를 확인하지 않고 제한 없는 `process.waitFor()`로 넘어간다.

실행 확인: 사용자 버스에 접속할 수 없는 환경 변수로 실행하면 `systemd-run`은 `Failed to connect to bus`를 출력하고 종료 코드 1로 끝났다. 없는 단위를 멈추면 `systemctl stop`은 종료 코드 5로 끝났다.

Codex 지적 중 두 가지는 재검증 결과 기각했다.

- "실제 사용자 버스 접근 실패에서 출력 없이 종료 코드 0이 나왔다": 이 환경에서는 재현되지 않았다. Codex 샌드박스 환경의 결과로 보인다.
- "scope 등록 전에 stop이 실행되는 경쟁": systemd v255 [`run.c`](https://github.com/systemd/systemd/blob/v255/src/run/run.c)의 `start_transient_scope()`는 `StartTransientUnit` 요청 뒤 `bus_wait_for_jobs_one()`으로 scope 시작 작업을 기다린 다음에 `execvpe()`로 명령을 실행한다. 명령이 실행 중이라면 scope는 이미 등록되어 있다. 다만 등록 자체가 1초 제한보다 오래 걸리면 아직 시작되지 않은 단위에 stop을 호출할 수는 있다.

수정 제안: 예제에 종료 코드 확인을 넣거나, 본문에 예제가 오류 처리를 생략했다고 밝힌다.

> 이 예제는 흐름을 보여 주기 위해 ``systemd-run``과 ``systemctl``의 실패 처리를 생략했습니다. 실제 코드에서는 두 명령의 종료 코드와 표준 오류를 확인하고, 정리가 실패했을 때 무한정 기다리지 않도록 ``waitFor()``에 제한 시간을 둡니다.

근거: 위 실행 결과, [systemctl(1)의 Exit status 절](https://www.freedesktop.org/software/systemd/man/latest/systemctl.html).

실행 확인과 소스 대조.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| 본문 436~442행: `sh -c`로 실행한 세 명령은 `destroy()` 뒤 셸만 끝나고 `sleep`, `cat`이 PID 1693으로 재부모화되어 남는다. `bash -c "sleep 60"`은 자식이 없다 | **확인.** `DescendantLeakDemo`를 실행해 같은 결과를 얻었다. PID 1693이 `/usr/lib/systemd/systemd --user`인 것을 `ps`로 확인했다 | 실행 |
| 443행: Ubuntu 24.04의 `/bin/sh`인 dash 0.5.12는 `-c`의 마지막 명령도 자식으로 실행한다 | **확인.** `/bin/sh`가 `/usr/bin/dash`(패키지 0.5.12-6ubuntu5)이고, `sh -c "sleep 304"`의 자식이 1개, `bash -c "sleep 304"`는 0개였다. 본문은 이 버전의 관측으로 한정해 서술했다 | 실행 |
| 391~392행: 부모를 잃은 후손은 PID 1이나 가장 가까운 subreaper로 재부모화된다 | **확인** | [PR_SET_CHILD_SUBREAPER(2const)](https://man7.org/linux/man-pages/man2/PR_SET_CHILD_SUBREAPER.2const.html) |
| 398행: 비대화형 셸은 받은 SIGTERM을 자식에게 전달하지 않는다 | **확인.** dash로 실행한 세 경우 모두 셸만 끝났다 | 실행 |
| 399행: `git`은 fetch나 clone 중에 `git-remote-https`를 자식으로 실행한다 | **확인.** `GIT_TRACE=1 git ls-remote https://...`에서 `run_command: git-remote-https`를 확인했다 | 실행 |
| 400행: Gradle daemon은 호출한 쪽이 끝나도 남고 `setsid()`로 세션을 벗어나기도 한다 | **확인.** 실행 중인 Gradle daemon 3개 모두 PID, PGID, SID가 같고 부모가 `systemd --user`였다 | 실행 |
| 400행: `ssh`의 ControlPersist 연결은 데몬으로 남는다 | **확인.** `control_persist_detach()`가 `fork()` 뒤 `daemon(1, 1)`을 호출한다. `daemon(3)`은 `setsid()`를 호출한다 | [OpenSSH ssh.c](https://github.com/openssh/openssh-portable/blob/master/ssh.c) |
| 400행: `adb start-server`는 데몬으로 남는다 | **확인.** 서버 시작 경로에서 `setsid()`를 호출한다 | [adb client/main.cpp](https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/main/client/main.cpp) |
| 494행: util-linux `setsid`는 이미 그룹 리더일 때만 `fork()`하고, 그렇지 않으면 자기 프로세스에서 실행한다 | **확인.** `ProcessGroupKillDemo`의 첫 명령이 `process.pid()`를 PGID로 써서 정리됐다 | [setsid(1)](https://man7.org/linux/man-pages/man1/setsid.1.html), 실행 |
| 495행: `kill`에 음수 PID를 넘기면 그룹 전체에 시그널이 간다 | **확인** | [kill(2)](https://man7.org/linux/man-pages/man2/kill.2.html) |
| 506행: `setsid`로 그룹을 벗어난 후손은 그룹에 보낸 시그널을 받지 않는다 | **확인.** `setsid sleep 60`이 남았다 | [setsid(2)](https://man7.org/linux/man-pages/man2/setsid.2.html), 실행 |
| 510행: Docker 컨테이너의 메모리 제한은 cgroup으로 동작한다 | **확인.** Docker 문서가 자원 제한 설정이 호스트의 컨테이너 cgroup 설정을 바꾼다고 설명한다 | [Docker Resource constraints](https://docs.docker.com/engine/containers/resource_constraints/) |
| 512행: cgroup v2에서 프로세스를 옮기려면 원래 cgroup과 옮길 cgroup의 공통 조상에 있는 `cgroup.procs`에 쓸 권한이 필요하다 | **확인.** 커널 문서의 위임 제약(Delegation Containment) 절 문장과 일치한다 | [Control Group v2](https://docs.kernel.org/admin-guide/cgroup-v2.html) |
| 515행: systemd의 `KillMode` 기본값은 `control-group`이다 | **확인** | [systemd.kill(5)](https://www.freedesktop.org/software/systemd/man/latest/systemd.kill.html) |
| 548행: `--scope`로 실행하면 `systemd-run`이 scope에 들어간 뒤 명령을 실행해 같은 PID를 유지한다 | **확인.** `start_transient_scope()`의 `execvpe()`를 확인했고, `systemd-run --scope sh -c 'echo $$'`의 PID가 `systemd-run`의 PID와 같았다 | [systemd v255 run.c](https://github.com/systemd/systemd/blob/v255/src/run/run.c), 실행 |
| 549행: `systemctl stop`으로 `setsid`로 벗어난 후손까지 정리된다 | **확인.** `SystemdScopeDemo` 실행 결과가 본문과 같았다 | 실행 |
| 550행: 유예 시간은 `-p TimeoutStopSec=5s`처럼 지정할 수 있다 | **확인.** SIGTERM을 무시하는 명령을 `-p TimeoutStopSec=2s`로 실행하고 `systemctl stop`을 호출하자 약 2.2초 뒤 SIGKILL로 끝났다. `systemd.scope(5)`의 scope 전용 옵션 목록에는 없지만, `systemd.kill(5)`의 종료 절차가 `TimeoutStopSec`을 참조한다 | [systemd.kill(5)](https://www.freedesktop.org/software/systemd/man/latest/systemd.kill.html), 실행 |
| 554행: `loginctl enable-linger`로 로그아웃 뒤에도 사용자 인스턴스를 유지한다 | **확인** | [loginctl(1)](https://www.freedesktop.org/software/systemd/man/latest/loginctl.html) |
| 556행: `cgroup.kill`은 Linux 5.14부터 지원하며 하위 cgroup까지 SIGKILL을 보낸다 | **확인.** v5.13 커널의 `cgroup-v2.rst`에는 없고 v5.14에 처음 나온다. 설명 문장도 일치한다 | [Linux v5.14 cgroup-v2.rst](https://github.com/torvalds/linux/blob/v5.14/Documentation/admin-guide/cgroup-v2.rst) |

## 한계

- Kotlin 컴파일 데몬은 실행 중인 프로세스가 없어 세션과 그룹을 확인하지 못했다. "호출한 쪽이 끝나도 남아 있도록 만들어졌다"는 설명은 설계상 사실로 두었고, `setsid()` 사용 여부는 확인하지 않았다. 본문도 "벗어나기도 합니다"로 서술해 모든 예시가 `setsid()`를 쓴다고 주장하지 않는다.
- ssh ControlPersist와 adb는 소스 대조로 판정했고 실행하지 않았다. adb 소스는 main 브랜치 기준이다.
- 수정 제안 1의 `killGroup()`에는 그룹이 비는 순간과 SIGKILL 사이에 PGID가 재사용될 수 있는 경쟁이 남는다. 유예 시간 5초 안의 재사용 가능성은 낮지만 없다고 할 수는 없다. 이 경쟁까지 없애려면 그룹 리더를 유지하는 감독 프로세스나 cgroup이 필요하다(Codex 권고).
- 재현 코드는 세션 scratchpad의 `PgProbe.java`, `PgFix.java`에 있고 저장소 산출물이 아니다.

## 반영 내역

2026-09-25에 본문과 예제에 반영했다. 아직 커밋하지 않았다.

| 항목 | 반영 내용 |
|---|---|
| 우선 1 | `ProcessGroupKillDemo`의 `killGroup(long pgid)`가 `kill -0`으로 그룹에 남은 프로세스를 확인하며 최대 5초 기다린 뒤 SIGKILL을 보낸다. 제안 코드와 달리 SIGKILL 뒤에도 `awaitGroupExit()`로 그룹이 빌 때까지 최대 5초 기다린다. 제안 코드대로 실행하면 SIGKILL 직후 확인한 스냅샷에 아직 정리되지 않은 프로세스가 `left=[?]`로 나왔기 때문이다. 예제에 `(trap '' TERM; sleep 60) & sleep 60` 사례를 추가하고, 496행 설명을 코드와 맞췄다. |
| 우선 2 | 두 예제의 정리를 `finally`로 옮겼다. 본문에 직접 자식이 정상 종료해도 후손이 남는 이유와 scope 수명 규칙을 적었다. `sleep 61 &`만 실행하는 스크립트를 `run()`으로 실행해 `finally`에서 정리되는 것을 확인했다. |
| 우선 3 | 399행에서 headless Chrome을 뺐다. |
| 보완 1 | 399행을 제안 문장대로 고쳤다. |
| 보완 2 | `SystemdScopeDemo`가 `systemd-run`과 `systemctl`의 표준 오류를 부모로 넘기고 종료 코드를 출력하며, `systemctl stop` 뒤 `waitFor()`에 10초 제한을 둔다. 명령이 먼저 끝나 scope가 사라진 뒤의 `systemctl stop`이 종료 코드 5를 돌려준다는 설명을 본문에 추가했다(실행 확인). `systemd-run`이 scope 시작을 기다린 뒤 명령을 실행한다는 설명도 548행에 반영했다. |

반영 후 확인:

- 두 예제를 다시 실행했고, 본문의 실행 결과는 그 출력으로 바꿨다. `ProcessGroupKillDemo`는 두 번 실행해 같은 결과를 얻었다. 실행 뒤 남은 `sleep` 프로세스는 없었다.
- 본문 코드 블록의 모든 줄이 예제 파일에 있는지 대조했다.
- `./gradlew bake` 성공. 새 절의 본문 텍스트에 `*`나 백틱이 그대로 남은 곳이 없었다.
