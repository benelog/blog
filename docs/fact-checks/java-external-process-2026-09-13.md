# Java 외부 프로세스 글 재검증 — 2026-09-13

- 대상: [java-external-process.adoc](../../src/content/java-external-process.adoc)
- 검토 기준 커밋: `42e16fee393ca22ae6b7558d24f310ac89798e22`
- 이전 기록: [2026-09-06 검증](java-external-process.md). 반영 과정과 후속 재검토는 같은 문서의 7절과 8절에 있다.
- 범위: 현재 본문 전체의 논리 전개와 기술적 주장. 이전 기록의 판정을 그대로 승계하지 않고 변경된 설명을 중심으로 공식 문서·배포 소스·실행 결과를 대조했다.
- 결과: **우선 수정할 사항 4건, 표현을 보완할 사항 2건.** 기본 POSIX_SPAWN 유지, 출력 소비와 입력 EOF 처리, 시간제한과 종료 정책을 함께 설계한다는 핵심 결론은 타당하다.
- 반영 상태: **6건 모두 본문에 반영 완료.** 검증 당일 패치로 만들어 검토를 거쳐 커밋 `fc14993`에 적용했다. 반영 후 재검토에서 나온 보완 2건(INHERIT 리다이렉트의 파이프 대기 조건, 시스템 콜 설명의 아키텍처 한정)도 커밋 `f8859cb`에 적용했다. 패치 파일은 반영 뒤 삭제했다.
- 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다. 예제 소스는 변경하지 않았다. 아래 행 번호는 위 커밋 기준이다.

## 우선 수정할 사항

### 1. glibc 2.24 미만에서의 JDK 실행 방식 설명이 잘못됨

**반영 완료.** 오래된 glibc를 이유로 VFORK를 지정할 필요가 없었다는 문장과 glibc 2.4~2.23의 선택 조건, 2.24의 clone 기반 구현 설명으로 교체했다.


**위치: 본문 777~779행.** glibc 2.24 미만에서는 `posix_spawn()`이 내부적으로 `fork()`를 사용했으므로 JDK 13에서 `VFORK`를 지정할 이유가 있었다고 설명한다.

glibc 2.4~2.23은 호출 인자에 따라 `fork()` 또는 `vfork()`를 선택한다. **JDK 13의 호출 방식은 `vfork()`를 선택하는 조건에 해당한다.** JDK 13 소스의 `spawnChild()`는 file actions와 attributes에 모두 NULL을 전달하며, 파일 상단 주석도 이 경우 `vfork()`를 사용한다고 명시한다. 같은 설명이 JDK 25 소스에도 남아 있다. 항상 `fork()`를 사용했다는 구분은 glibc 2.4 이전에 해당한다.

따라서 과거 VFORK 설정의 성능상 필요성을 설명하는 근거가 잘못됐다. 설정을 제거하고 기본값을 유지하라는 현재의 권고 자체는 타당하다.

수정 제안:

> glibc 2.4~2.23에서도 JDK의 posix_spawn() 호출은 내부적으로 vfork()를 사용했습니다. glibc 2.24부터는 별도 스택과 시그널 처리를 갖춘 clone 기반 구현으로 바뀌었습니다. 따라서 glibc가 2.24 미만이라는 이유만으로 VFORK를 직접 지정해야 했던 것은 아닙니다.

근거: [JDK 13+33 ProcessImpl_md.c](https://github.com/openjdk/jdk/blob/jdk-13%2B33/src/java.base/unix/native/libjava/ProcessImpl_md.c), [JDK 25+36 ProcessImpl_md.c](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/native/libjava/ProcessImpl_md.c), [posix_spawn(3)](https://man7.org/linux/man-pages/man3/posix_spawn.3.html).

JDK 13 바이너리와 구버전 glibc의 조합을 실행한 결과가 아니라, 해당 버전의 소스와 libc의 분기 조건을 대조한 판정이다.

### 2. fork 뒤 준비 작업을 “제약 없이” 할 수 있다는 설명이 잘못됨

**반영 완료.** 표의 장점 칸에서 “제약 없이”를 빼고, 표 뒤에 fork(2)의 async-signal-safe 제약 문단을 추가했다.


**위치: 본문 631~633행의 비교 표.** JVM처럼 멀티스레드인 프로세스에서 `fork()`한 자식은 `execve()`까지 async-signal-safe 함수만 안전하게 호출할 수 있다. 다른 스레드는 자식에 복제되지 않지만 그 스레드가 잡고 있던 잠금 상태는 남을 수 있기 때문이다. 자식의 주소 공간이 분리된다는 사실이 모든 함수 호출의 안전성을 보장하지는 않는다.

파일 디스크립터 정리나 작업 디렉터리 변경이라는 예시는 가능하지만, 이를 “제약 없이”로 일반화하면 잘못된 기준을 전달한다.

수정 제안:

> 자식의 주소 공간이 분리되어 부모 메모리를 훼손하지 않고 준비 작업을 할 수 있음. 단, 멀티스레드 프로그램에서는 execve() 전 호출을 async-signal-safe 함수로 제한해야 함.

근거: [fork(2)의 멀티스레드 제약](https://man7.org/linux/man-pages/man2/fork.2.html), [signal-safety(7)](https://man7.org/linux/man-pages/man7/signal-safety.7.html).

### 3. 기본 문자 집합과 inputReader의 정확성을 과도하게 일반화함

**반영 완료.** COMPAT 설정과 inputReader()가 자식의 인코딩을 감지하지 않는다는 설명, Charset 지정 예를 두 문단으로 반영했다.


**위치: 본문 167행.** 다음 두 부분을 구분해 고쳐야 한다.

- JDK 18부터 기본 문자 집합은 UTF-8이지만 “고정”은 아니다. `-Dfile.encoding=COMPAT`으로 이전 방식의 선택 규칙을 적용할 수 있다.
- `inputReader()`는 부모 JVM의 `native.encoding`을 사용한다. 자식 프로그램의 실제 출력 인코딩을 감지하지 않는다. 자식이 UTF-8을 명시적으로 쓰거나 별도 로캘을 사용하면 기본 `inputReader()`가 더 부정확할 수 있다.

로컬 JDK 25에서 `LC_ALL=C`로 실행하고, Python 자식이 UTF-8의 `é`에 해당하는 바이트 `C3 A9`를 출력하도록 했다. 기본 reader는 대체 문자 두 개를 반환했고, `inputReader(StandardCharsets.UTF_8)`는 정상적으로 U+00E9를 반환했다.

```text
native=ANSI_X3.4-1968, default=UTF-8
explicitUTF8=false, codepoints=[fffd, fffd]
explicitUTF8=true, codepoints=[e9]

# 같은 프로그램에 -Dfile.encoding=COMPAT 지정
native=ANSI_X3.4-1968, default=US-ASCII
explicitUTF8=false, codepoints=[fffd, fffd]
explicitUTF8=true, codepoints=[e9]
```

수정 제안:

> JDK 18부터 기본 문자 집합은 원칙적으로 UTF-8이며, 호환성 설정으로 바꿀 수 있습니다. 자식이 native.encoding과 같은 인코딩으로 출력한다면 inputReader()가 적합합니다. 출력 인코딩을 알고 있다면 inputReader(Charset)으로 직접 지정합니다.

근거: [Process API의 inputReader](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html#inputReader()), [JEP 400](https://openjdk.org/jeps/400), [Charset.defaultCharset](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/charset/Charset.html#defaultCharset()).

### 4. jspawnhelper 버전 불일치의 설명을 예고하지만 본문에 없음

**반영 완료.** 예고 문장을 지우고 POSIX_SPAWN 절 끝에 helper 갱신 문단을 복원해 FORK 임시 우회로 연결했다.


**위치: 본문 689행, 715~717행, 769행.** “뒤에서 살펴볼 JDK 갱신 시의 버전 불일치”라고 예고하지만 이후에는 FORK의 비용과 VFORK 설정 제거만 설명한다. 앞뒤의 FORK 우회 권고에서도 helper 오류가 무엇인지 충분히 설명하지 않는다.

이전 검증 기록에는 실행 중 JDK 파일 교체, helper의 버전 검사, JVM 재시작과 설치 경로 보존에 관한 상세 검증이 있다. 현재 본문에서는 이 설명이 빠졌으므로 이전 기록의 “반영했다”는 판정을 현재 원고에도 적용할 수 없다.

수정 제안: POSIX_SPAWN 설명 뒤에 짧은 운영 문단을 복원한다. 실행 중인 JVM의 JDK 파일을 덮어쓰면 메모리에 적재된 JVM 코드와 디스크의 helper가 달라질 수 있다는 원인, JDK 갱신과 JVM 재시작을 함께 관리한다는 대응을 먼저 설명하고 FORK를 임시 대안으로 연결한다. 해당 내용을 제외할 의도라면 예고 문장과 맥락 없는 우회 권고도 함께 정리한다.

또한 “버전 불일치와 VFORK 제거는 모두 이 설계에서 나온 결과”는 인과관계가 너무 압축되어 있다. helper 버전 불일치는 별도 실행 파일의 교체 문제이고, VFORK 제거는 직접 vfork 경로의 안전성 문제이므로 각각 설명하는 편이 명확하다.

근거: 현재 본문의 절 구성, [이전 검증의 jspawnhelper 항목](java-external-process.md#3-jdk-실행-방식과-jspawnhelper), [JDK 25 ProcessImpl_md.c의 helper 실패 진단](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/native/libjava/ProcessImpl_md.c).

## 표현을 보완할 사항

### 5. 후손이 파이프를 열어 두면 join이 반드시 끝나지 않는다는 단정

**반영 완료.** 지연 가능성으로 표현하고, 직접 자식 종료 처리와 진행 중인 읽기의 순서에 따라 달라진다는 문장을 덧붙였다.


**위치: 본문 379행.** 후손이 출력 파이프를 열어 둔 상황에서 읽기와 `join()`이 지연될 위험은 실제로 있다. 다만 JDK 25의 `ProcessPipeInputStream.processExited()`는 직접 자식의 종료를 확인하면 남은 데이터를 회수하고 OS 스트림을 닫는다. 이미 진행 중인 읽기와 이 종료 처리의 순서에 따라 결과가 달라진다.

로컬에서 Python 직접 자식이 `sleep` 후손을 만들고 종료하도록 해 두 경우를 확인했다. 후손은 두 경우 모두 살아 있었다.

```text
# 직접 자식 종료 전에 읽기 스레드를 시작하지 않음
reading=false, parentExited=true, childAlive=true
EOF=true

# 직접 자식 종료 전에 가상 스레드가 파이프 읽기를 시작함
reading=true, parentExited=true, childAlive=true
pumpAlive=true
```

두 번째 경우 pump는 300ms의 join 대기 뒤에도 살아 있었다. 검증 뒤 후손을 강제 종료하고 pump 종료를 기다렸다. “EOF를 받지 못하고 join도 끝나지 않습니다”를 “진행 중인 읽기가 후손의 파이프 닫기를 기다리면 join도 지연될 수 있습니다”로 바꾸면 위험을 유지하면서 조건을 정확히 전달한다.

근거: [JDK 25 ProcessImpl.java의 ProcessPipeInputStream](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/unix/classes/java/lang/ProcessImpl.java), 위 재현 결과.

### 6. SIGKILL의 “즉시 종료”와 종료 확인의 구분

**반영 완료.** “커널이 즉시 끝내므로”를 빼고 destroyForcibly() 반환 뒤 waitFor()로 종료를 확인해야 한다는 문장을 추가했다.


**위치: 본문 334행.** SIGKILL로는 애플리케이션의 종료 핸들러가 실행되지 않는다는 취지는 맞다. 다만 `destroyForcibly()` 호출이 돌아왔다는 사실이 프로세스 종료 완료를 뜻하지는 않는다. Javadoc도 호출 직후 잠시 `isAlive()`가 true일 수 있으므로 필요하면 `waitFor()`로 확인하라고 설명한다.

“SIGKILL은 핸들러를 통한 마무리 기회 없이 강제 종료합니다. 종료 완료가 필요하면 별도로 기다려 확인해야 합니다” 정도로 쓰면 아래 예제의 `destroyForcibly().waitFor()`와도 연결된다. 본문 예제 자체는 이미 이 대기를 수행한다.

근거: [Process.destroyForcibly API Note](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Process.html#destroyForcibly()).

## 이번에 재확인한 내용

| 항목 | 결과와 검증 방법 |
|---|---|
| 기본 실행 방식과 VFORK 변화 | JDK 25 소스의 POSIX_SPAWN 기본값과 VFORK 경고 확인. JDK-8357089 REST 응답은 Fixed, Fix Version 27, 출시 예정일 2026-09-15이다. “개발 버전에 반영”이라는 본문 표현과 일치한다. |
| glibc 2.24 이후 생성 경로 | CLONE_VM·CLONE_VFORK 및 별도 스택에 관한 매뉴얼과 JDK 소스의 설명이 일치한다. 이번에는 strace를 다시 실행하지 않았다. |
| overcommit 및 mmap_lock | Linux 6.17 `__vm_enough_memory()`의 모드 0 조건, `dup_mmap()`의 `mmap_write_lock_killable(oldmm)`와 `copy_page_range()`를 재확인했다. |
| 라이브러리 버전과 릴리스 날짜 | Maven Central의 최신 버전은 여전히 zt-exec 1.13.0, Commons Exec 1.6.0이다. 공식 변경 기록의 날짜도 본문과 일치한다. |
| 라이브러리의 기본값 | 배포 sources JAR에서 출력 처리, 기본 종료 코드, 입력 미지정 시 stdin 닫기, 기본 destroy 사용을 재확인했다. |
| zt-exec timeout | Future의 시간 초과와 작업 스레드 인터럽트, stopper 호출 경로 확인. 기본 stopper는 강제 종료를 보장하지 않는다. |
| Commons Exec watchdog | 1.6.0은 destroy를 호출한다. 허용 종료 코드로 종료하면 정상 반환할 수 있다는 기존 보완은 유효하다. SIGTERM 무시·정상 종료 반례는 이전 기록을 참고했고 이번에는 다시 실행하지 않았다. |
| 본문 예제 | 아래 9개 프로그램 정상 종료. 별도 교착 예제 1개는 외부 시간제한으로 대기 상태를 확인했다. |

소스: [JDK-8357089](https://bugs.openjdk.org/browse/JDK-8357089), [Linux 6.17 util.c](https://github.com/torvalds/linux/blob/v6.17/mm/util.c), [Linux 6.17 mmap.c](https://github.com/torvalds/linux/blob/v6.17/mm/mmap.c), [zt-exec 배포 소스](https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/1.13.0/zt-exec-1.13.0-sources.jar), [Commons Exec 배포 소스](https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.6.0/commons-exec-1.6.0-sources.jar), [zt-exec 버전 목록](https://repo.maven.apache.org/maven2/org/zeroturnaround/zt-exec/maven-metadata.xml), [Commons Exec 버전 목록](https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/maven-metadata.xml), [zt-exec 변경 기록](https://github.com/zeroturnaround/zt-exec/blob/main/CHANGELOG.md), [Commons Exec 변경 기록](https://commons.apache.org/proper/commons-exec/changes.html).

## 실행 기록과 한계

이번 환경은 Ubuntu **24.04.5** LTS, Linux `6.17.0-1032-oem`, glibc 2.39, Temurin `25+36-LTS`였다. 본문의 Ubuntu 24.04.4는 과거 측정 환경으로 해석하며 이번 환경과 혼동하지 않는다.

다음 프로그램을 각각 `java examples/java-external-process/이름.java`로 실행했다. zt-exec은 1.13.0과 SLF4J API 2.0.17, Commons Exec은 1.6.0을 classpath에 추가했다.

| 프로그램 | 결과 |
|---|---|
| DeadlockDemo | 3초 후 false/생존, 읽은 뒤 100,000줄·종료 코드 0·생존 false |
| MergedReadDemo | 100,001줄, 종료 코드 0 |
| StdinEofDemo | stdin 개방 시 대기, 닫기·빈 입력·입력 후 닫기 모두 본문과 일치 |
| PlainJdkRunner | echo·seq·ls 결과 및 sleep 시간 초과가 본문과 일치 |
| ProcessRunner | hello 3회 |
| ZtExecRunner | PID를 제외하고 본문과 일치. SLF4J provider 부재 경고도 확인 |
| CommonsExecRunner | sleep 143/watchdog true, ls 2/watchdog false 등 본문과 일치 |
| CommandLineQuotingDemo | 배열 quoting 기본값과 false 지정의 차이가 예제 설명과 일치 |
| RedirectDemo | 정상 종료, 부모로 전달한 출력 총 200,000줄, 파일 출력 588,895바이트 |
| SequentialReadDemo | 3초 뒤에도 `reading stdout...` 상태. 검증용 프로세스 그룹에 SIGKILL을 보내 정리 |

임시 검증 코드·다운로드 소스·실행 로그는 `/tmp/java-process-review-0913/`에 보관했다. 이 경로의 파일은 영구 보존되는 저장소 산출물이 아니다.

벤치마크의 과거 절대 수치, 원래 서버의 장애 원인, helper 교체 실험, JDK 27 바이너리 실행은 이번에 재현하지 않았다. 벤치마크 코드는 측정 구간과 해석을 검토했으며 이전 검증의 한계가 그대로 적용된다. 검증 시점에는 본문·예제·사이트 설정을 바꾸지 않았으므로 사이트 빌드를 실행하지 않았다. 반영 시점의 빌드와 렌더링 확인 결과는 [2026-09-06 검증 문서의 7절과 8절](java-external-process.md)에 있다.
