# java-external-process

[Java에서 외부 프로세스를 실행할 때: JDK 25와 Linux 6.x 기준](https://blog.benelog.net/java-external-process.html) 글에서 쓴 예제와 측정 코드입니다.

| 파일 | 내용 |
|---|---|
| `DeadlockDemo.java` | 파이프 출력을 읽지 않고 `waitFor()`만 부르면 하위 프로세스가 멈추는 현상 재현 |
| `PlainJdkRunner.java` | JDK API만으로 표준 출력과 표준 오류를 동시에 읽고 시간제한과 강제 종료까지 처리하는 예 |
| `ZtExecRunner.java` | zt-exec 1.13.0으로 출력 수집, 시간제한, 종료 코드 검사를 처리하는 예 |
| `CommonsExecRunner.java` | Apache Commons Exec 1.6.0의 builder API로 같은 일을 처리하는 예 |
| `ProcessRunner.java` | `Runtime.exec()`, `ProcessBuilder`, `inheritIO()`로 `echo hello`를 실행 |
| `SpawnBench.java` | `FORK`와 `POSIX_SPAWN` 실행 방식의 프로세스 생성 지연 측정 |

실행 방법 (JDK 25):

```bash
java DeadlockDemo.java
java PlainJdkRunner.java
java ProcessRunner.java

# zt-exec 예제는 zt-exec와 slf4j-api jar가 필요
curl -sLO https://repo1.maven.org/maven2/org/zeroturnaround/zt-exec/1.13.0/zt-exec-1.13.0.jar
curl -sLO https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar
java -cp zt-exec-1.13.0.jar:slf4j-api-2.0.17.jar ZtExecRunner.java

# Commons Exec 예제는 commons-exec jar가 필요
curl -sLO https://repo1.maven.org/maven2/org/apache/commons/commons-exec/1.6.0/commons-exec-1.6.0.jar
java -cp commons-exec-1.6.0.jar CommonsExecRunner.java

# 힙 8GB를 미리 할당한 상태에서 실행 방식별로 30회 측정
java -Xms8g -Xmx8g -XX:+AlwaysPreTouch -Djdk.lang.Process.launchMechanism=POSIX_SPAWN SpawnBench.java 30
java -Xms8g -Xmx8g -XX:+AlwaysPreTouch -Djdk.lang.Process.launchMechanism=FORK SpawnBench.java 30
```

시스템 콜 확인:

```bash
strace -f -e trace=clone,clone3,vfork,execve -o trace.txt java ProcessRunner.java
grep -v CLONE_THREAD trace.txt | grep -E 'clone|vfork|execve'
```
