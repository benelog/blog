# java-external-process.adoc 사실 관계 검증

- 최초 검증일: 2026-09-06 (Asia/Seoul)
- 추가 검증일: 2026-09-13 (Asia/Seoul). 아래 1~6절은 최초 검증 당시의 기록이며, 현재 원고에 대한 재검증 결과와 패치 상태는 7절에 기록했다.
- 대상: [src/content/java-external-process.adoc](../../src/content/java-external-process.adoc)
- 범위: 본문의 JDK API, 라이브러리 동작과 릴리스, Linux/glibc 구현, 예제 실행 결과, 벤치마크 해석, 운영상 결론.
- 방법: 공식 API 문서, OpenJDK 이슈와 소스, Maven Central 배포물, 라이브러리 소스, Linux 소스를 대조하고 로컬에서 재현했다. 웹으로 읽히지 않은 OpenJDK 이슈는 공개 REST API `/rest/api/2/issue/JDK-번호`로 조회했다.
- 판정: **확인**은 출처 또는 실행으로 뒷받침됨, **수정**은 오류나 과도한 일반화를 고침, **한계**는 이번 검증으로 확정할 수 없는 내용이다.

## 핵심 수정

`fork의 메모리 문제, 그 후` 절은 다음 결론을 먼저 제시하도록 바꿨다. Linux의 JDK 25에서는 `jdk.lang.Process.launchMechanism`을 지정하지 않고 기본 `POSIX_SPAWN`을 유지한다. 실행 중인 JDK 파일을 덮어쓰지 않고 JDK 갱신과 JVM 재시작을 함께 관리한다. 기존 `VFORK` 지정은 제거한다. 이후에 JDK, glibc, 커널의 변경 이력을 설명한다.

“메모리 문제가 사라졌다”, “파이프는 항상 64KiB”, “시간 초과면 자식이 종료된다”는 단정을 제거했다. Commons Exec의 종료 코드 설정 가능 여부, 공식 릴리스 날짜, zt-exec의 메모리 사용과 SLF4J 버전 설명도 바로잡았다.

## 1. JDK API와 파이프

| 주장 | 판정 및 반영 | 근거 |
|---|---|---|
| `Runtime.exec()`와 `ProcessBuilder`의 실행 구현은 같다 | **확인·보완.** `Runtime`은 `ProcessBuilder`에 위임하지만 `exec(String)`의 공백 토큰화와 배열 인자 전달은 다르다. | [Runtime API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Runtime.html), [Runtime 소스](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Runtime.java) |
| 출력 파이프를 비우지 않고 종료만 기다리면 교착이 가능하다 | **확인.** `DeadlockDemo`를 재실행했다. `getInputStream()` 호출 자체가 아니라 실제 읽기가 필요하다. | [Process API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html), 아래 실행 기록 |
| Linux 파이프 용량은 항상 64KiB다 | **수정.** 16페이지는 기본값이며 페이지 크기, 사용자별 한도, 시스템 설정, `F_SETPIPE_SZ`에 따라 달라진다. Linux 4.5 이후 사용자별 soft limit 영향이 있고, 4.9 이후 `pipe-max-size`가 기본 용량의 상한에도 관여한다. 로컬 새 파이프는 65,536바이트였다. | [pipe(7), Pipe capacity](https://man7.org/linux/man-pages/man7/pipe.7.html) |
| 출력을 전부 읽은 다음에만 `waitFor()`를 호출해야 한다 | **수정.** 별도 스레드에서 출력을 읽으면서 종료를 기다려도 된다. 두 출력 스트림은 동시에 비워야 한다. | [Process API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html), `PlainJdkRunner` 재실행 |
| `inputReader()`는 JDK 17, timed `waitFor()`와 `destroyForcibly()`는 JDK 8에 추가됐다 | **확인.** 각 메서드의 `Since` 확인. | [Process API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html) |
| `inheritIO()`는 JDK 7, `DISCARD`는 JDK 9에 추가됐다 | **확인.** `DISCARD`는 플랫폼별 폐기 대상으로 연결되며 본문의 `/dev/null` 설명은 Linux에 해당한다. | [ProcessBuilder API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ProcessBuilder.html), [Redirect API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ProcessBuilder.Redirect.html) |
| timed `waitFor()`가 실패하면 프로세스가 종료된다 | **수정.** 이 API는 기다림만 끝낸다. 종료 요청과 강제 종료를 구분했다. Linux OpenJDK의 일반 종료 요청은 SIGTERM, 강제 종료 요청은 SIGKILL이다. | [Process API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html), [ProcessHandleImpl_unix.c](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/native/libjava/ProcessHandleImpl_unix.c) |
| JDK 예제에 전체 실행 시간제한과 자원 정리가 완비되어 있다 | **수정.** 예제는 네 개의 단순 명령을 대상으로 한다. `start()`, 종료 대기, pump `join()`은 1초 제한 밖이며, 입력 EOF, 인터럽트, 읽기 실패 전파, 스트림 정리를 별도로 고려해야 함을 명시했다. | [PlainJdkRunner.java](../../examples/java-external-process/PlainJdkRunner.java), 코드 흐름 검토 |
| `descendants()`로 후손을 모두 확실하게 종료할 수 있다 | **수정.** 조회 결과는 스냅샷이며 프로세스 생성·종료·재부모화와 경합한다. 후손이 파이프를 열고 있으면 EOF 대기도 길어질 수 있다. | [ProcessHandle API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ProcessHandle.html), [pipe(7)](https://man7.org/linux/man-pages/man7/pipe.7.html) |

## 2. zt-exec / Apache Commons Exec

검증은 개발 브랜치의 API를 배포 버전으로 오인하지 않도록 Maven Central의 **zt-exec 1.13.0 및 Commons Exec 1.6.0 sources JAR**를 내려받아 수행했다.

| 주장 | 판정 및 반영 | 근거 |
|---|---|---|
| 두 라이브러리가 직접 `ProcessBuilder`를 사용한다 | **수정.** zt-exec은 직접 사용하지만 Commons Exec의 `Java13CommandLauncher`는 `Runtime.exec(String[], String[], File)`을 호출한다. 최종 JDK 구현과 pump 방식은 공통이다. | [zt-exec sources JAR](https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/1.13.0/zt-exec-1.13.0-sources.jar), [Commons Exec sources JAR](https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.6.0/commons-exec-1.6.0-sources.jar) |
| zt-exec의 기원은 ZeroTurnaround 내부 공통 코드이며 2013년 공개됐다 | **확인.** README의 기원 설명과 변경 기록의 2013년 릴리스를 확인했다. | [v1.13.0 README](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/README.md), [CHANGELOG](https://github.com/zeroturnaround/zt-exec/blob/main/CHANGELOG.md) |
| zt-exec은 기본적으로 stderr를 합쳐 버리고 모든 종료 코드를 허용한다 | **확인.** `ProcessExecutor` 생성자, 기본 필드, `exitValueNormal()`/`exitValues()` 확인. 출력 수집을 켰을 때 실패 결과에서도 출력을 얻는 예제 확인. | [ProcessExecutor.java](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/src/main/java/org/zeroturnaround/exec/ProcessExecutor.java), `ZtExecRunner` 재실행 |
| zt-exec은 시간 초과 시 자식을 종료한다 | **수정.** 기본 `DestroyProcessStopper`는 `destroy()`만 호출한다. SIGTERM을 무시하는 자식은 `TimeoutException` 이후에도 살아 있음을 재현했다. | [DestroyProcessStopper.java](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/src/main/java/org/zeroturnaround/exec/stop/DestroyProcessStopper.java), 아래 경계 조건 검증 |
| `destroyOnExit()`가 모든 JVM 종료에서 자식 종료를 보장한다 | **보완.** shutdown hook의 종료 요청이다. SIGKILL 등 hook이 실행되지 않는 종료나 후손 전체 종료의 보장은 아니다. | [ShutdownHookProcessDestroyer.java](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/src/main/java/org/zeroturnaround/exec/listener/ShutdownHookProcessDestroyer.java) |
| `redirectOutputAsInfo()`가 현재 권장 API다 | **수정.** deprecated이며 `redirectOutput(Slf4jStream.of(logger).asInfo())`로 대체한다. | 배포 sources JAR의 `ProcessExecutor`, `Slf4jStream` |
| `readOutput(true)`의 메모리는 출력 크기의 두 배 이하다 | **수정.** `ByteArrayOutputStream` 증가 여유 공간, `WaitForProcess.getCurrentOutput()`의 `toByteArray()` 복사, 문자열 생성이 있으므로 두 배라는 상한은 성립하지 않는다. 실제 최대치는 GC와 인코딩 등에 따라 달라진다. | [WaitForProcess.java](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/src/main/java/org/zeroturnaround/exec/WaitForProcess.java), [ProcessOutput.java](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/src/main/java/org/zeroturnaround/exec/ProcessOutput.java) |
| 줄 단위 callback은 출력 크기와 무관한 메모리를 쓴다 | **수정.** `LogOutputStream`은 줄을 버퍼링한다. `reset()`은 배열 용량을 줄이지 않으므로 가장 긴 줄이 중요하다. callback 자체의 저장·처리 속도에도 영향받는다. | [LogOutputStream.java](https://github.com/zeroturnaround/zt-exec/blob/v1.13.0/src/main/java/org/zeroturnaround/exec/stream/LogOutputStream.java) |
| zt-exec의 의존성은 SLF4J 하나이고 최소 Java는 8이다 | **확인·보완.** POM은 `slf4j-api:1.7.32`를 선언한다. 저장소 예제는 2.0.17을 명시적으로 사용하므로 provider 경고를 1.7의 기본 경고와 혼동하지 않도록 했다. | [1.13.0 POM](https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/1.13.0/zt-exec-1.13.0.pom), CHANGELOG, [예제 README](../../examples/java-external-process/README.md) |
| Commons Exec 1.4.0부터 Java 8, builder 및 `Duration` API를 쓴다 | **확인.** 예전 생성자 등의 deprecation과 builder 추가 이력을 확인했다. 런타임 전이 의존성은 없으며 POM의 commons-lang3 등은 test scope다. | [공식 변경 기록](https://commons.apache.org/proper/commons-exec/changes.html), [1.6.0 POM](https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.6.0/commons-exec-1.6.0.pom) |
| Commons Exec은 0 이외의 종료 코드에 무조건 예외를 던진다 | **수정.** Linux의 기본 정책이며 `setExitValue`, `setExitValues`로 변경 가능하다. null이면 검사하지 않는다. 종료 코드 7을 허용하는 실행을 확인했다. | 배포 sources JAR의 `DefaultExecutor.isFailure()`와 `setExitValues()`, 아래 경계 조건 검증 |
| Commons Exec의 시간 초과는 항상 `ExecuteException`이다 | **수정.** watchdog은 `destroy()`를 호출한다. 자식이 SIGTERM 처리 후 0으로 종료하면 기본 설정에서도 정상 반환한다. SIGTERM을 무시하면 기다림이 계속될 수 있다. `killedProcess()`는 실제 종료 보장이 아니라 watchdog의 종료 요청 표시다. | 배포 sources JAR의 `ExecuteWatchdog.timeoutOccured()`, `DefaultExecutor.executeInternal()`, 아래 경계 조건 검증 |
| `CommandLine`의 인자 배열은 추가 quoting을 주의해야 한다 | **확인·보완.** `addArguments(String)`의 quote-aware 토큰화와 배열의 `handleQuoting` 처리를 구분했다. Linux의 배열 기반 실행은 셸이 따옴표를 제거하지 않는다. | 배포 sources JAR의 `CommandLine`, `StringUtils`, `Java13CommandLauncher` |

### 릴리스 기록

검증일 현재 Maven Central의 최신 버전은 원문대로 zt-exec 1.13.0과 Commons Exec 1.6.0이다. 날짜는 아티팩트 업로드나 발표 날짜를 섞지 않고 프로젝트 변경 기록으로 통일했다.

| 라이브러리 | 확인한 릴리스 날짜 | 원문 반영 |
|---|---|---|
| zt-exec | 1.13.0: 2026-07-10 / 1.12: 2020-09-02 / 1.11: 2019-07-05 | 유지 |
| Commons Exec | 1.6.0: 2025-11-25 / 1.5.0: 2025-05-16 / 1.4.0: 2024-01-01 / 1.3: 2014-11-02 | 1.6.0의 11-27을 11-25로, 1.3의 11-03을 11-02로 수정 |

근거: [zt-exec Maven metadata](https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/maven-metadata.xml), [Commons Exec Maven metadata](https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/maven-metadata.xml), [zt-exec CHANGELOG](https://github.com/zeroturnaround/zt-exec/blob/main/CHANGELOG.md), [Commons Exec Changes](https://commons.apache.org/proper/commons-exec/changes.html).

2021-09-06~2026-09-06의 릴리스 수는 각각 1회, 3회다. 2024-09-06 이후로도 각각 1회, 2회다. 이 횟수는 유지보수 품질 전체를 뜻하지 않는다. zt-exec 선택 의견은 저자의 API 선호로 남겼다. Commons Exec의 2024년 builder/Duration 기여자는 공식 Changes의 Gary Gregory 표기로 확인했다. zt-exec 1.13.0의 Gradle 이전, Java 8 상향, JPMS multi-release descriptor도 CHANGELOG로 확인했다. 불필요한 최신 커밋 추세 단정은 삭제했다.

## 3. JDK 실행 방식과 jspawnhelper

| 항목 | 판정과 근거 |
|---|---|
| JDK 7 Linux 기본 `vfork` | **확인.** [JDK-6868160](https://bugs.openjdk.org/browse/JDK-6868160)의 Fix Version은 7이다. |
| JDK 12 POSIX_SPAWN 옵션 도입 | **확인.** [JDK-8212828](https://bugs.openjdk.org/browse/JDK-8212828)은 기본값을 바꾸지 않는 옵션 도입이다. |
| OpenJDK 11u 백포트 | **확인.** [JDK-8220360](https://bugs.openjdk.org/browse/JDK-8220360)의 Fix Version은 11.0.4다. 별도 Oracle 백포트 [JDK-8249955](https://bugs.openjdk.org/browse/JDK-8249955)는 11.0.10-oracle이므로 모든 벤더를 같은 시점으로 일반화하지 않았다. |
| JDK 13 Linux 기본 POSIX_SPAWN | **확인.** [JDK-8213192](https://bugs.openjdk.org/browse/JDK-8213192)의 Fix Version은 13이다. JDK 25의 `ProcessImpl.launchMechanism()`에서도 미지정 시 POSIX_SPAWN을 확인했다. |
| JDK 25 VFORK deprecation 경고 | **확인.** [JDK-8357180](https://bugs.openjdk.org/browse/JDK-8357180), JDK 25 소스 및 로컬 실행의 경고가 일치한다. 경고는 JVM의 단순 시작이 아니라 해당 프로세스 구현 초기화 시 출력된다. |
| JDK 27 VFORK 제거와 FORK 대체 | **확인·시점 보완.** [JDK-8357089](https://bugs.openjdk.org/browse/JDK-8357089)의 Fix Version 27과 [CSR JDK-8357090](https://bugs.openjdk.org/browse/JDK-8357090), [2026-03-30 구현 커밋 ca3fe721](https://github.com/openjdk/jdk/commit/ca3fe721ba23a1304089b71c1b58940f16a0d053)을 확인했다. 검증일에는 개발 버전 반영 사항으로 표현한다. CSR 초반에 남은 “JDK 26” 문구보다 실제 Fix Version과 구현을 우선했다. |
| JDK 8에는 POSIX_SPAWN이 없다 | **범위 보완.** [OpenJDK 8u UNIXProcess](https://github.com/openjdk/jdk8u/blob/master/jdk/src/solaris/classes/java/lang/UNIXProcess.java)의 Linux 플랫폼 허용 목록은 VFORK와 FORK다. 다른 OS의 POSIX_SPAWN 구현까지 없다는 뜻은 아니다. |
| POSIX_SPAWN은 libc 함수이고 helper와 실제 명령으로 두 번 exec한다 | **확인.** [JDK 25 ProcessImpl_md.c](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/native/libjava/ProcessImpl_md.c)의 설명과 로컬 strace가 일치한다. |
| 버전 검사 도입 및 백포트 | **확인.** [JDK-8325621](https://bugs.openjdk.org/browse/JDK-8325621): 23, [JDK-8331949](https://bugs.openjdk.org/browse/JDK-8331949): 22.0.2, [JDK-8332133](https://bugs.openjdk.org/browse/JDK-8332133): 21.0.4, [JDK-8334407](https://bugs.openjdk.org/browse/JDK-8334407): 17.0.13. 도입 버전을 운영 권장 패치 버전으로 읽지 않도록 보완했다. |
| 실행 중 JDK 교체가 버전 불일치를 일으킨다 | **확인.** JDK-8325621에 unattended upgrade 사례가 명시되어 있다. 별도 JDK 복사본에서 helper를 바꿔 동일 오류를 재현했다. |
| 재시작만이 helper 오류의 유일한 해결책이다 | **수정.** JDK 25 오류 자체가 재시작, 로그 조사, 권한/설치 문제 해결, FORK 대안을 열거한다. 버전 불일치와 그 밖의 원인을 구분했다. |
| 심볼릭 링크 교체 또는 자동 업데이트 제외만으로 예방된다 | **보완.** 기존 JVM이 사용하는 `java.home`의 파일을 보존하고 교체·재시작을 관리해야 한다. [ProcessImpl](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/classes/java/lang/ProcessImpl.java)의 helper 경로와 static launchMechanism 초기화에서 도출한 운영상 권고다. 실행 중 속성을 바꿔도 즉시 실행 방식이 바뀌지 않는다. |

JDK 개발 브랜치 확인 시 HEAD는 `ef54ca1c775881843f0710a8148f379a6e209816`이었다. [이 시점의 ProcessImpl.java](https://github.com/openjdk/jdk/blob/ef54ca1c775881843f0710a8148f379a6e209816/src/java.base/unix/classes/java/lang/ProcessImpl.java)에서 enum에 FORK/POSIX_SPAWN만 있고 Linux의 VFORK 문자열은 경고 후 FORK로 치환됨을 확인했다. 과거 리뷰 PR #25768의 GitHub `merged=false`만으로 미반영이라고 판정하지 않았다. 실제 반영 여부는 JBS changeset과 소스로 확인했다.

[JDK 27 공식 프로젝트 페이지](https://openjdk.org/projects/jdk/27/)의 검증일 상태는 Release Candidate이며 GA 예정일은 2026-09-15다. 따라서 본문의 “개발 버전”은 정식 출시 전 RC를 포함하는 표현이다.

## 4. glibc, Linux, 보안

| 주장 | 판정 및 반영 | 근거 |
|---|---|---|
| glibc 2.24 이후 posix_spawn은 CLONE_VM/CLONE_VFORK를 사용한다 | **확인.** 별도 스택과 시그널 처리로 직접 vfork 후 준비 작업을 하는 위험을 줄인다. “위험이 전혀 없다”는 표현과 정확한 인용으로 오인할 수 있는 요약 문장을 삭제했다. | [posix_spawn(3)](https://man7.org/linux/man-pages/man3/posix_spawn.3.html), [glibc 2.39 spawni.c](https://github.com/bminor/glibc/blob/glibc-2.39/sysdeps/unix/sysv/linux/spawni.c) |
| glibc 2.39에서 clone3가 보인다 | **환경 내 확인.** 로컬 strace로 확인. libc/커널/허용 syscall에 따라 clone 경로도 가능하므로 모든 Linux에 보장되는 trace는 아니다. | 위 glibc 소스와 아래 trace |
| musl도 clone 기반이다 | **확인 범위 명시.** JDK 25 소스 주석이 설명하는 내용으로 표현했다. musl 모든 과거 버전의 소스를 별도로 전수 조사한 것은 아니다. | [JDK 25 ProcessImpl_md.c](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/native/libjava/ProcessImpl_md.c) |
| vfork가 Linux/glibc에서 제거됐다 | **수정.** POSIX.1-2008 명세에서 제거된 것과 libc 내부 사용 변경, JDK VFORK 옵션 제거를 구분한다. Linux syscall 자체는 남아 있다. | [vfork(2)](https://man7.org/linux/man-pages/man2/vfork.2.html) |
| Linux 5.2에서 overcommit 모드 0 판정이 단순화됐다 | **확인.** v5.1과 v5.2의 `__vm_enough_memory()`를 직접 비교했다. v5.2는 요청 페이지 수가 전체 RAM+swap 페이지 수보다 큰지 검사한다. | [v5.1 mm/util.c](https://github.com/torvalds/linux/blob/v5.1/mm/util.c), [v5.2 mm/util.c](https://github.com/torvalds/linux/blob/v5.2/mm/util.c), [패치](https://lkml.iu.edu/hypermail/linux/kernel/1904.1/05420.html) |
| 현대 기본 설정이면 fork의 메모리 실패가 사라진다 | **수정.** 휴리스틱 완화는 실제 페이지 테이블 등 커널 메모리 할당 실패를 없애지 않는다. POSIX_SPAWN의 주소 공간 복제 회피와 커널 모드 0의 완화를 별개로 설명했다. | 위 커널 소스, [JDK 25 ProcessImpl_md.c](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/native/libjava/ProcessImpl_md.c) |
| 모드 2에서 부모의 전체 크기를 그대로 commit에 더한다 | **수정.** 익명/private writable 매핑 등 회계 규칙에 따라 계산된다. 큰 Java 힙 매핑은 부담이 되지만 주소 공간 전체와 동의어는 아니다. | [Overcommit Accounting](https://www.kernel.org/doc/html/latest/mm/overcommit-accounting.html), v5.2 `vm_commit_limit()`/`__vm_enough_memory()` |
| 2015년의 실제 장애 원인을 특정 휴리스틱으로 확정할 수 있다 | **한계·수정.** 원래 서버의 overcommit 설정, 메모리 상태, syscall 기록이 없다. 가능한 실패 경로로 한정했다. | 당시 글의 요약과 커널 구현을 대조한 추론 |
| fork 시 부모 mmap_lock을 쓰기 모드로 잡는다 | **확인.** Linux 6.17 `dup_mmap()`의 `mmap_write_lock_killable(oldmm)` 뒤에 `copy_page_range()`가 있다. 관련 잠금을 필요로 하는 다른 스레드의 대기 가능성으로 표현했다. | [v6.17 mm/mmap.c](https://github.com/torvalds/linux/blob/v6.17/mm/mmap.c) |
| 실행 지연은 힙에 정확히 비례하며 항상 100배 차이다 | **수정.** 본문의 표 자체도 정확한 선형 비례는 아니다. 초기 힙 pre-touch, GC, 페이지 크기, 부하 등에 따른 관측값으로 한정했다. | [SpawnBench.java](../../examples/java-external-process/SpawnBench.java), 아래 재측정 |
| Security Manager가 사라져 어떤 JDK 수단도 없다고 단정한다 | **범위 보완.** JDK 24부터 영구 비활성화된 Security Manager의 기존 checkExec/FilePermission 경로를 사용할 수 없다는 구체적 사실로 바꿨다. OS 격리는 위협 모델에 따른 운영 권고다. | [JEP 486](https://openjdk.org/jeps/486) |
| java_posix_spawn 마지막 커밋은 2014년 8월이다 | **확인.** 기본 브랜치 최신 커밋은 `28e723fdcfd55910f2bd1c87c188cfbbcd7a47f5`, committer 시각은 2014-08-07T13:27:36Z였다. | [해당 커밋](https://github.com/axiak/java_posix_spawn/commit/28e723fdcfd55910f2bd1c87c188cfbbcd7a47f5) |

## 5. 로컬 재현 기록

### 환경과 원문 예제

Ubuntu 24.04.4 LTS, Linux `6.17.0-1032-oem`, glibc 2.39, Temurin `25+36-LTS`, `vm.overcommit_memory=0`을 확인했다. RAM 약 31.6GiB, swap 약 8GiB 환경이며 일반 실행은 도구의 샌드박스 안에서 수행했다. strace는 ptrace 제한으로 실패한 뒤 승인된 샌드박스 밖 실행으로 재확인했다.

JDK 경로는 `/home/benelog/.sdkman/candidates/java/25-tem/bin/java`였다. 라이브러리는 Maven Central의 해당 버전 JAR이며 zt-exec 예제에는 README대로 `slf4j-api:2.0.17`을 함께 사용했다. 아래 명령은 저장소 루트에서 실행하며 `java`가 해당 JDK라고 가정한다.

```bash
java examples/java-external-process/DeadlockDemo.java
java examples/java-external-process/PlainJdkRunner.java
java examples/java-external-process/ProcessRunner.java
java -cp zt-exec-1.13.0.jar:slf4j-api-2.0.17.jar examples/java-external-process/ZtExecRunner.java
java -cp commons-exec-1.6.0.jar examples/java-external-process/CommonsExecRunner.java
```

다섯 프로그램 모두 종료 코드 0이었다. PID를 제외하고 본문의 예제 결과와 일치했다.

```text
DeadlockDemo:
finished within 3s: false, alive: true
read 100000 lines, exit=0

PlainJdkRunner:
echo: exit=0, stdout chars=6, stderr=
seq: exit=0, stdout chars=588895, stderr=
ls: exit=2, stdout chars=0, stderr=ls: cannot access '/no-such-dir': No such file or directory
timed out: sleep 60

ProcessRunner:
hello
hello
hello

ZtExecRunner:
output=hello, exit=0
seq lines=100000, exit=0
timeout: Timed out waiting for Process[pid=126, exitValue="not exited"] to finish, timeout: 1 second, executed command [sleep, 60]
exit=2, output=ls: cannot access '/no-such-dir': No such file or directory

CommonsExecRunner:
echo: exit=0, stdout bytes=6
seq: exit=0, stdout bytes=588895
sleep: exit=143, killed by watchdog=true, stderr=
ls: exit=2, killed by watchdog=false, stderr=ls: cannot access '/no-such-dir': No such file or directory
```

SLF4J 2.0.17에서는 provider 부재, NOP fallback, 설명 링크로 구성된 초기화 경고 3줄도 확인했다. 각 외부 명령을 실행할 때마다 나오는 경고는 아니다.

### 종료 정책의 경계 조건

임시 Java 프로그램으로 다음을 추가 확인했다. 남은 검증용 프로세스는 PID를 얻어 `destroyForcibly()`를 호출하고 종료까지 기다렸다.

1. Commons Exec에 `setExitValue(7)`을 설정하고 `/bin/sh -c 'exit 7'`을 실행했다. 예외 없이 7을 반환했다.
2. Python 자식 프로세스에 `signal.signal(SIGTERM, lambda *a: sys.exit(0))`을 등록하고 `time.sleep(30)`을 호출했다. Commons Exec의 watchdog을 1초로 설정하자 종료 코드 0으로 정상 반환하고 `killedProcess()`는 true였다.
3. 같은 자식 프로세스에 `signal.signal(SIGTERM, SIG_IGN)`을 등록했다. watchdog이 동작했어도 3초 후 `execute()`는 기다리는 중이었다.
4. zt-exec으로 같은 SIGTERM 무시 프로세스를 실행하고 `timeout(1, SECONDS)`를 설정했다. 검증 프로그램의 정리 대기를 제한하려고 `closeTimeout(1, SECONDS)`도 지정했다. `TimeoutException`을 잡은 뒤에도 자식이 살아 있었다. stopper는 변경하지 않았다.

```text
commons accepted exit=7
commons ignore=false, exit=0, watchdog=true
commons ignore=true, still waiting after 3s, watchdog=true
zt timeout, child alive=true
```

이를 통해 “Commons Exec의 시간 초과는 반드시 예외”, “killedProcess()가 true면 종료 완료”, “zt-exec의 timeout이면 자식이 반드시 사라짐”의 반례를 확인했다.

### 실행 방식과 helper

```bash
strace -f -e trace=clone,clone3,vfork,execve -o trace.txt java examples/java-external-process/ProcessRunner.java
strace -f -e trace=clone,clone3,vfork,execve -o trace-fork.txt java -Djdk.lang.Process.launchMechanism=FORK examples/java-external-process/ProcessRunner.java
java -Djdk.lang.Process.launchMechanism=VFORK examples/java-external-process/ProcessRunner.java
```

일반 스레드 생성을 제외하면 기본 설정에서 다음 순서를 확인했다. 경로, 환경 변수 개수, 주소 등을 생략한 요약이다.

```text
clone3(flags=CLONE_VM|CLONE_VFORK|CLONE_CLEAR_SIGHAND, ...)
execve(".../25-tem/lib/jspawnhelper", [".../jspawnhelper", "25+36-LTS", "10:11:13"], ...)
execve("/usr/bin/echo", ["echo", "hello"], ...)
```

FORK 지정에서는 CLONE_VM 없는 clone과 대상 명령의 exec를 확인했고 helper 실행은 없었다. VFORK 지정에서는 원문의 `VFORK MODE DEPRECATED`와 후속 경고가 나왔다.

helper 불일치는 JDK 25 디렉터리를 `/tmp`에 **독립된 복사본**으로 만들고 그 Java로 표준 입력을 기다리는 검증 프로그램을 실행해 재현했다. 기다리는 동안 복사본의 `lib/jspawnhelper`만 Temurin 17.0.16의 파일로 교체하고, 이후 `new ProcessBuilder("echo", "hello").start()`를 호출했다. 원래 설치된 JDK는 변경하지 않았다.

```text
Incorrect Java version: 25+36-LTS
jspawnhelper version 17.0.16+8
Exception in thread "main" java.io.IOException: Cannot run program "echo": Failed to exec spawn helper: pid: 27, exit code: 1, error: 0 (none)
```

원문에 실린 `Possible reasons`와 `Possible solutions`의 모든 항목도 확인했다. 검증 프로그램의 종료 코드는 1이었다. 서로 다른 JDK 메이저 버전의 helper 교환으로 불일치를 재현했으며, Ubuntu의 자동 업데이트 자체를 실행한 실험은 아니다.

### 벤치마크 재측정

`SpawnBench.java`를 변경하지 않고 힙 크기별로 각 모드를 2회씩 순차 실행했다. 한 번 실행할 때 5회 예열 후 30회의 `start().waitFor()`를 측정한다. 명령 예시는 다음과 같다.

```bash
java -Xms8g -Xmx8g -XX:+AlwaysPreTouch -Djdk.lang.Process.launchMechanism=POSIX_SPAWN examples/java-external-process/SpawnBench.java 30
java -Xms8g -Xmx8g -XX:+AlwaysPreTouch -Djdk.lang.Process.launchMechanism=FORK examples/java-external-process/SpawnBench.java 30
```

| 힙 지정 | POSIX_SPAWN, ms/회 | FORK, ms/회 |
|---|---|---|
| 256m | 1.03, 0.93 | 10.63, 11.83 |
| 2g | 1.00, 1.30 | 45.67, 49.17 |
| 8g | 1.03, 1.20 | 116.23, 118.20 |

12회 실행 모두 종료 코드 0이었다. 원래 표의 경향은 재현했지만 절대 시간은 달랐다. 본문의 수치를 이번 값으로 교체하지 않고 작성 당시의 관측 결과라고 명시했다. 원래 측정 당시의 원시 로그, CPU 상태, THP 설정, 부하 정보가 없으므로 기존 소수점 수치 자체를 독립적으로 재검증할 수는 없다.

이 프로그램은 합계 시간을 정수 ms로 버림한 다음 횟수로 나눈다. 또한 `true`의 시작과 종료 대기를 측정하며, 실제 웹 요청 응답 시간이나 모든 스레드의 정지 시간, 순수 fork 시스템 콜 시간을 측정한 것은 아니다. 따라서 고정 배율이나 선형 비례를 주장하는 근거로 쓰지 않는다.

## 6. 문서 검증과 남은 한계

- `./gradlew bake`: **성공**. JDK IO subsystem의 `--add-opens`에 관한 JRuby 경고는 나왔지만 문서 생성은 완료했다.
- `git diff --check`: **성공**.
- 예제 소스는 변경하지 않았다. 한계가 있는 예제를 완성된 범용 runner로 설명하지 않도록 본문의 적용 범위를 보충했다.
- 2015년의 개별 장애, 원문 벤치마크의 과거 절대 수치, 모든 배포판과 JDK 벤더의 백포트 상황은 전수 검증하지 않았다.
- 호스트의 `vm.overcommit_memory`는 변경하지 않았으며, 모드 2의 실패는 공식 문서와 커널 코드로 검증했다.
- JDK 27은 소스와 변경 기록으로 확인했고 JDK 27 바이너리로 실행하지는 않았다.
- 참고 자료에 남긴 2000년의 글은 현재 API나 구현의 근거로 사용하지 않았다. 이번 기술 판정은 위의 일차 자료에 근거한다.

## 7. 2026-09-13 재검증

검토 기준 커밋은 `42e16fee393ca22ae6b7558d24f310ac89798e22`이다. 이전 검증 이후 편집된 본문 전체를 다시 읽고, 공식 문서와 버전이 고정된 소스, 배포 JAR, 로컬 실행으로 대조했다. **우선 수정할 사항 4건과 표현을 보완할 사항 2건**을 확인했다. 기본 POSIX_SPAWN 유지, 출력 소비와 입력 EOF 처리, 시간제한과 종료 정책을 함께 설계한다는 핵심 결론은 타당하다.

### 확인한 문제와 수정 방향

행 번호는 검토 기준 커밋의 본문을 가리킨다. 아래 6건은 같은 날 패치를 검토한 뒤 본문에 모두 적용했다. 적용 과정과 패치 원안에서 달라진 문장은 아래 “패치 검토와 적용 결과”에 있다.

| 위치 | 판정 | 검증 결과와 수정 방향 |
|---|---|---|
| 777~779행, 구버전 glibc | **오류** | glibc 2.4~2.23에서도 JDK 13의 `posix_spawn()` 호출 조건은 내부 `vfork()` 경로를 선택한다. “glibc 2.24 미만에서는 fork를 썼으므로 VFORK를 명시할 이유가 있었다”는 근거를 바로잡는다. |
| 632행, fork 비교 표 | **오류** | 멀티스레드 프로그램에서 fork한 자식은 execve까지 async-signal-safe 함수만 안전하게 호출할 수 있다. 준비 작업을 “제약 없이” 할 수 있다는 표현을 고치고 잠금 상태 상속의 제약을 설명한다. |
| 167행, 문자 인코딩 | **과도한 일반화** | JDK 18 이후 UTF-8 기본값은 `-Dfile.encoding=COMPAT`으로 변경할 수 있다. `inputReader()`는 자식의 인코딩을 감지하지 않으므로 항상 더 정확하지는 않다. 출력 인코딩을 알면 Charset을 명시하도록 설명한다. |
| 689행 및 715~717행, helper 운영 설명 | **논리 연결 누락** | 뒤에서 설명한다고 예고한 JDK 갱신 시 helper 버전 불일치 내용이 현재 본문에는 없다. 별도 실행 파일의 교체 문제와 VFORK의 안전성 문제를 구분하고, JDK 경로 보존·갱신·JVM 재시작을 설명한 뒤 FORK 임시 우회로 연결한다. |
| 379행, 후손의 출력 파이프 | **표현 보완** | 후손이 파이프를 열고 있어도 JDK의 직접 자식 종료 처리와 진행 중인 읽기의 순서에 따라 EOF 도달 여부가 달라진다. join이 반드시 끝나지 않는다고 단정하지 않고 지연 가능성으로 표현한다. |
| 334행, SIGKILL | **표현 보완** | 핸들러를 통한 마무리 기회가 없다는 설명은 맞다. 다만 `destroyForcibly()` 반환이 종료 완료를 보장하지 않으므로 필요하면 `waitFor()`로 확인한다는 점을 명시한다. |

주요 근거: [JDK 13 ProcessImpl_md.c](https://github.com/openjdk/jdk/blob/jdk-13%2B33/src/java.base/unix/native/libjava/ProcessImpl_md.c), [fork(2)](https://man7.org/linux/man-pages/man2/fork.2.html), [JEP 400](https://openjdk.org/jeps/400), [Process API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html), [JDK 25 ProcessImpl.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/classes/java/lang/ProcessImpl.java). 항목별 설명과 수정 제안은 [당일 상세 검증 기록](java-external-process-2026-09-13.md)에 있다.

### 실행 결과와 재확인 범위

이번 환경은 Ubuntu **24.04.5** LTS, Linux `6.17.0-1032-oem`, glibc 2.39, Temurin `25+36-LTS`였다. 최초 검증의 Ubuntu 24.04.4 환경과 구분한다.

- `DeadlockDemo`, `MergedReadDemo`, `StdinEofDemo`, `PlainJdkRunner`, `ProcessRunner`, `ZtExecRunner`, `CommonsExecRunner`, `CommandLineQuotingDemo`, `RedirectDemo`의 **9개 프로그램이 정상 종료**했다. 본문에 실린 출력은 PID를 제외하고 일치했다.
- `SequentialReadDemo`는 3초 뒤에도 `reading stdout...` 상태였다. 검증용 프로세스 그룹을 종료해 정리했다.
- `LC_ALL=C`에서 Python 자식이 UTF-8 바이트 `C3 A9`를 출력하게 했다. 기본 `inputReader()`는 U+FFFD 두 개를 반환하고, UTF-8을 명시하면 U+00E9를 반환했다. 같은 환경의 `-Dfile.encoding=COMPAT` 실행에서는 기본 문자 집합이 UTF-8에서 US-ASCII로 바뀌었다.
- 직접 자식이 종료하고 후손이 출력 파이프를 열고 있는 상황에서, 읽기를 미리 시작하지 않은 경우에는 EOF에 도달했다. 가상 스레드에서 읽기를 먼저 시작한 경우에는 직접 자식 종료 후에도 pump가 300ms의 join 대기를 넘겼다. 후손을 종료한 뒤 pump 종료까지 확인했다.
- Maven Central 최신 버전과 릴리스 날짜는 zt-exec 1.13.0, Commons Exec 1.6.0으로 본문과 일치했다. 배포 sources JAR에서 출력·종료 코드 기본값, 입력 미지정 시 stdin 닫기, 기본 destroy 사용을 재확인했다.
- JDK 25의 기본 POSIX_SPAWN 및 VFORK 경고, JDK 27의 VFORK 제거 반영 상태, Linux 6.17의 overcommit 모드 0 판정과 mmap_lock 사용도 소스·변경 기록으로 재확인했다.

벤치마크 과거 수치, 과거 서버의 장애 원인, helper 교체 실험, strace, SIGTERM 무시·정상 종료의 라이브러리 경계 조건, JDK 27 바이너리 실행은 이번에 재현하지 않았다. 이전 검증 기록과 이번에 직접 확인한 범위를 구분한다.

### 패치 검토와 적용 결과

위 6건을 반영한 [java-external-process-2026-09-13.patch](java-external-process-2026-09-13.patch)를 생성했다. 패치 대상은 `src/content/java-external-process.adoc` 한 파일이다. 생성 시점에는 `git apply --check --whitespace=error-all`이 성공했고, 임시 복사본에 적용한 결과가 의도한 수정문과 바이트 단위로 일치했다.

같은 날 패치의 8개 hunk를 항목별로 다시 검토한 뒤 `git apply`로 본문에 적용했다. 패치 파일은 검토 기록으로 남긴다. 검토 결과는 다음과 같다.

| hunk | 판정 | 검토 내용 |
|---|---|---|
| 167행, 인코딩 두 문단 | **채택** | JEP 400의 `COMPAT` 설명과 `Process.inputReader(Charset)`의 존재를 확인했다. |
| 334행, SIGKILL과 `waitFor()` | **채택** | `destroyForcibly()` API Note의 “isAlive()가 잠시 true일 수 있다”는 설명과 일치한다. |
| 379행, 후손의 파이프와 `join()` | **채택·표현 조정** | `ProcessPipeInputStream.processExited()`의 동작과 위 재현 결과에 부합한다. “JDK 25는 출력 스트림을 정리하지만”이 무엇을 하는지 드러나지 않아 “파이프에 남은 출력을 회수하고 스트림을 닫지만”으로 풀었다. |
| 632행, fork 표의 장점 칸 | **채택** | 주소 공간 분리를 장점으로 적고 “제약 없이”를 뺐다. |
| 표 뒤, async-signal-safe 문단 | **채택** | fork(2) 매뉴얼의 멀티스레드 제약과 일치한다. 표 바로 뒤에 두어 장점 칸의 한계를 바로 설명한다. |
| 689행, 앞 참조 문장 삭제 | **채택** | helper 버전 불일치와 VFORK 제거를 한 문장에 묶은 인과를 없앴다. 두 주제는 아래 helper 문단과 VFORK 절에서 따로 설명한다. |
| 715행 뒤, helper 갱신 문단 | **채택·표현 조정** | 원안의 첫 문장 “별도 실행 파일인 jspawnhelper는 JDK 갱신 때도 함께 관리해야 합니다”는 문제의 원인이 드러나지 않아 “별도 실행 파일이라는 점은 JDK를 갱신할 때 문제가 됩니다”로 바꿨다. JDK-8325621 설명은 “불일치를 다룹니다”에서 “자동 업데이트로 생긴 불일치를 배경으로 helper의 버전 검사를 보강했습니다”로 구체화했다. 3절의 기존 확인 내용과 일치한다. |
| 777행, 구버전 glibc | **채택·표현 조정** | JDK 13+33 소스에서 `posix_spawn(&resultPid, helperpath, 0, 0, ...)` 호출과 상단 주석의 “Due to the way the JDK calls posix_spawn(3), it would therefore call vfork(2)”를 직접 확인했다. 원안의 “다만 glibc 2.24 미만이라는 이유만으로”는 바로 앞 문장이 glibc를 언급하지 않아 이어지지 않으므로 “당시의 오래된 glibc를 이유로 지정할 필요도 없었습니다”로 시작하고, glibc 2.4~2.23이 호출 인자에 따라 `fork()`와 `vfork()` 중 하나를 고른다는 조건을 덧붙였다. “앞 절에서 본”은 해당 설명이 두 절 앞에 있으므로 “앞에서 본”으로 고쳤다. |

마크업은 두 곳을 손봤다. 원안의 `` `-Dfile.encoding=COMPAT`을 ``과 `` `native.encoding`과 ``는 단일 백틱 바로 뒤에 조사가 붙어 렌더링이 깨지므로 이중 백틱으로 바꿨다. 블로그 관례에 맞춰 새로 넣은 두 문단은 문장 단위로 줄을 나눴다.

적용 후 검증 결과는 다음과 같다.

- `git apply`: 성공. 적용 대상은 본문 한 파일이며 예제 코드와 사이트 설정은 바꾸지 않았다.
- `git diff --check`: 성공.
- `./gradlew bake`: **성공**. 최초 검증 때와 같은 JRuby의 `--add-opens` 경고만 나왔다.
- 생성된 `output/java-external-process.html`에서 수정한 여덟 문단을 찾아 백틱이나 별표가 노출되지 않았음을 확인했다.
- 이번 적용에서 새 실행 실험은 하지 않았다. JDK 13 소스 확인은 GitHub의 `jdk-13+33` 태그 파일을 내려받아 대조했다.
