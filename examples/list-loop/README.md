# list-loop

[java의 List와 반복문(loop), 그리고 변수 선언 위치에 대해서](https://blog.benelog.net/1382604.html) 글에서 쓴 측정 코드입니다. 2007년에 작성한 코드를 그대로 옮겼습니다.

| 파일 | 내용 |
|---|---|
| `looptest/ListReader1.java` | 반복문 밖에서 변수를 선언하는 방식 |
| `looptest/ListReader2.java` | 반복문 안에서 변수를 선언하는 방식 |
| `looptest/LoopTester.java` | 두 방식을 1,2,2,1,1,2 순서로 실행해 걸린 시간을 밀리초로 출력 |

실행 방법:

```bash
javac -d out looptest/*.java
java -cp out looptest.LoopTester
```
