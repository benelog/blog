# benchmark-test

[junit-benchmarks로 HTTP API의 응답 시간 측정하기](https://blog.benelog.net/junit-benchmarks.html) 글에서 설명한 예제 코드입니다.

2012년경 `benelog/benchmark` 저장소에 있던 코드로, JUnit 4 확장인 junit-benchmarks로 HTTP API에 동시 요청을 보내 응답 시간을 측정합니다.

- `SampleBenchmarkTest.java` : `@BenchmarkOptions`로 동시 요청 수와 반복 횟수를 지정한 측정 코드
- `SimpleRequestTest.java` : junit-benchmarks 없이 단건 요청만 보내는 테스트

실행 방법:

```bash
mvn test -Djub.consumers=CONSOLE,H2 -Djub.db.file=.benchmarks
```

JUnit 4.8, HttpClient 4.1, junit-benchmarks 0.3 시절의 코드라 지금 그대로 빌드되지는 않습니다.
