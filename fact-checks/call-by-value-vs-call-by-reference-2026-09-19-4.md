# Call by value vs Call by reference 글 사실 관계 검증 — 2026-09-19 (4차)

- 대상: [call-by-value-vs-call-by-reference.adoc](../src/content/call-by-value-vs-call-by-reference.adoc)
- 검토 기준 커밋: `b29fb9eef9b067adb618bb98b178d9b71abc5b47`
- 이전 기록: [2026-09-19 1차 검증](call-by-value-vs-call-by-reference-2026-09-19.md), [2026-09-19 2차 검증](call-by-value-vs-call-by-reference-2026-09-19-2.md), [2026-09-19 3차 검증](call-by-value-vs-call-by-reference-2026-09-19-3.md). 앞의 세 기록은 처음에 글 파일명을 바꾸기 전의 slug(`call-by-value`)로 저장했다가 이 기록과 함께 지금 이름으로 바꿨다. 3차 이후 Java 절에 JLS·JVMS의 프레임 설명과 draw.io 다이어그램(`java-stack-frames`)을 추가했고, 기존 SVG 그림 두 개를 draw.io로 다시 그렸다.
- 범위: 글 전체와 `src/content/img/call-by-value/`의 `.drawio` 3개. 중점은 새로 추가한 "메서드 호출마다 새로 만들어지는 프레임" 절(209–244행)과 그림이다.
- 방법: Codex 적대적 리뷰(`task`, 글 전체)로 의심 지점을 모은 뒤, Claude가 JLS·JVMS SE 26, Ada RM, C# 레퍼런스, Go 사양, rustc 문서, HotSpot 소스를 curl로 받아 대조했다. 글에 인용한 18개 출처의 영문 인용(JLS 4.3.1, 8.4.1, 15.12.4.5, JVMS 2.6, 2.6.1, 2장 서두, Dev.java, MDN, Python 튜토리얼, Go FAQ, C# 레퍼런스, Kotlin 사양, TypeScript 핸드북, Rust Book, N4950 [dcl.ref]·[expr.call], Flanagan, N1570)는 원문과 글자 단위로 대조했다. Java 예제는 Java 25, JavaScript는 Node 24.6, Python은 3.12.3으로 실행했고, `javap -l`로 지역 변수 슬롯을 확인했다. Go 1.27.1과 Rust 1.98.1(도커 `rust:latest`)로 지적 사항을 재현했다. 이전 기록의 판정은 승계하지 않았다. 다만 C·C++·C#·Ada 예제의 실행은 3차 기록의 결과에 기댔다.
- 결과: **우선 수정할 사항 3건, 표현을 보완할 사항 5건.** 핵심 결론인 "Java는 객체를 전달할 때도 참조값의 복사본을 전달하는 call by value"와 새로 추가한 JLS·JVMS 인용·번역은 타당하다. 오류는 그림의 실행 시점, 정리 표의 C#·Ada 행, Ada 절의 unspecified 경우 누락에 있다.
- 반영 상태: **8건 모두 본문에 반영함.** 우선 수정 1번과 표현 보완 1·2번은 커밋 `adb199ae352d4b7d2c3e7598d3d81aa26df743e9`에, 나머지 5건은 커밋 `3f9e2d3ca77004d1aafe25ded34f0bb9bef02ab8`에 적용했다. 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다.
- 아래 행 번호는 위 기준 커밋 기준이다.

## 우선 수정할 사항

### 1. 재할당 그림이 `tv = new Tv()` 직후의 객체 상태를 `"Rock"`으로 표시함 — 반영 완료

**위치: 본문 102–110행, 240–242행과 `java-reassign.drawio`, `java-stack-frames.drawio`.** `java-reassign` 그림의 두 번째 시점 제목은 "② reassign 안에서 tv = new Tv() 실행 후"인데, 새 객체를 `channel = "Rock"`으로 그렸다. `java-stack-frames` 그림도 `② tv = new Tv()` 화살표 끝의 객체를 `"Rock"`으로 표시한다.

`new Tv()` 직후 `channel` 필드는 참조 타입의 기본값인 `null`이다. `"Rock"`은 다음 줄의 `tv.setChannel("Rock")`까지 실행해야 들어간다. 그림의 시점 설명과 객체 상태가 맞지 않는다.

수정 제안: 두 그림의 시점 표기를 "`tv = new Tv()`와 `tv.setChannel("Rock")` 실행 후"로 바꾼다.

근거: [JLS 4.12.5 Initial Values of Variables](https://docs.oracle.com/javase/specs/jls/se26/html/jls-4.html#jls-4.12.5) "For all reference types (§4.3), the default value is null."

Codex 지적. 소스 대조 판정. 커밋 `adb199a`에서 `java-reassign` 두 번째 시점 제목과 `java-stack-frames`의 ② 레이블을 위 제안대로 고쳤다.

### 2. 정리 표의 C#·Ada 행이 참조 전달 여부와 대입 가능 여부를 섞어 요약함 — 반영 완료

**위치: 본문 725행, 729행.** C# 행의 'Call by reference 지원' 칸은 "예. `ref`, `out` 파라미터에 한함"이라고 쓴 뒤 "`in`, `ref readonly`도 참조로 전달하지만 대입은 불가"를 덧붙인다. Ada 행은 "`in out`, `out` 파라미터 중 by-reference 타입이거나 ``aliased``로 선언한 것은 call by reference"라고 쓴다.

C# 레퍼런스는 `in`과 `ref readonly`도 참조로 전달되는 파라미터이며 참조로 전달된 파라미터는 referent를 가리키는 reference variable이라고 설명한다. "`ref`, `out`에 한함"과 바로 뒤의 문장이 한 칸 안에서 충돌한다. Ada RM 6.2 10/5항은 by-reference 타입의 파라미터와 명시적 `aliased` 파라미터를 모드와 관계없이 참조로 전달한다고 규정한다. 따라서 `in` 모드도 참조로 전달된다. 두 행 모두 "참조로 전달하는가"와 "파라미터에 대입해 호출자 변수를 바꿀 수 있는가"를 한 칸에서 구분하지 않았다.

수정 제안(C# 행 둘째 칸):

> 예. `ref`, `out`, `in`, `ref readonly` 파라미터. 이 중 파라미터에 대입할 수 있는 것은 `ref`, `out`

수정 제안(Ada 행 둘째 칸):

> 부분적으로. by-reference 타입이나 ``aliased``로 선언한 파라미터는 모드와 관계없이 참조로 전달. 그 밖의 타입은 명세가 정하지 않음. by-copy 타입의 `in out`, `out`은 정상 종료 시 값을 되돌려 씀(copy-restore)

근거: [C# reference - Method parameters and modifiers](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/method-parameters), [Ada RM 6.2 10/5항](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) "A parameter of a by-reference type is passed by reference, as is an explicitly aliased parameter of any type."

Codex 지적. 소스 대조 판정. 3차 기록의 표현 보완 1번(읽기 전용 참조의 취급이 C# 행과 Ada 행 사이에 다름)을 반영한 뒤에도 칸 안의 충돌이 남았다. 커밋 `3f9e2d3`에 두 칸 모두 위 제안대로 반영했다.

### 3. Ada 절이 전달 방식을 타입으로 모두 정하는 것처럼 서술함 — 반영 완료

**위치: 본문 663행.** "Ada Reference Manual 6.2는 복사로 전달할지 참조로 전달할지를 모드가 아니라 타입으로 정합니다"라고 쓴 뒤 by-copy 타입, by-reference 타입, `aliased` 파라미터만 나열한다.

Ada RM 6.2 11/3항은 그 밖의 파라미터를 복사로 전달할지 참조로 전달할지 규정하지 않는다(unspecified)고 명시한다. 현재 문장은 모든 파라미터가 타입에 따라 둘 중 하나로 정해지는 것처럼 읽힌다. 예제의 `Integer`와 `aliased Integer`는 정해진 경우라 예제 자체는 맞다.

수정 제안: 663행의 "`aliased`로 선언한 형식 파라미터는 타입과 관계없이 참조로 전달합니다." 뒤에 다음 문장을 넣는다.

> 어느 쪽에도 해당하지 않는 파라미터는 복사와 참조 중 어느 방식으로 전달할지 규정하지 않고 구현에 맡깁니다.

첫 문장의 "타입으로 정합니다"는 "주로 타입으로 정합니다"로 바꾼다.

근거: [Ada RM 6.2 11/3항](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) "For other parameters, it is unspecified whether the parameter is passed by copy or by reference."

Codex 지적. 소스 대조 판정. 커밋 `3f9e2d3`에 위 제안대로 반영했다.

## 표현을 보완할 사항

### 1. JIT 이후에도 "별개의 저장 공간"이 유지된다고 서술함 — 반영 완료

**위치: 본문 244행.** "그렇더라도 호출한 쪽의 변수와 파라미터가 별개의 저장 공간이라는 의미는 달라지지 않습니다."

앞 문장이 JIT 컴파일러가 파라미터를 레지스터에 둘 수 있다고 한 만큼, 최적화 뒤에도 저장 공간이 따로 있다고 보장하는 문장으로 읽힌다. 인라이닝이나 레지스터 할당 뒤에는 두 변수에 대응하는 저장 공간이 따로 남지 않을 수 있다. 유지되는 것은 언어 수준의 의미다.

수정 제안:

> 그렇더라도 호출한 쪽의 변수와 파라미터가 별개의 변수라는 언어의 의미는 달라지지 않습니다.

근거: [JVMS 2장 서두](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html) "the memory layout of run-time data areas, ... and any internal optimization of the Java Virtual Machine instructions ... are left to the discretion of the implementor." 레지스터 전달은 [HotSpot `sharedRuntime_x86_64.cpp`의 `java_calling_convention` (jdk-25-ga, 550–606행)](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/hotspot/cpu/x86/sharedRuntime_x86_64.cpp#L550-L606)에서 `T_OBJECT`를 포함한 인자를 `j_rarg0`~`j_rarg5` 레지스터부터 배정하는 것으로 확인했다.

Claude 발견, Codex도 같은 지적. 커밋 `adb199a`에 위 제안대로 반영했다.

### 2. 프레임 사이의 참조값 복사가 operand stack을 거친다는 점을 생략함 — 반영 완료

**위치: 본문 240행과 `java-stack-frames.drawio`의 ① 화살표.** 그림은 `main` 프레임의 `myTv` 칸에서 `reassign` 프레임의 `tv` 칸으로 바로 복사하는 것처럼 그렸다.

바이트코드에서는 `aload_1`이 참조값을 `main` 프레임의 operand stack에 올리고, `invokestatic`이 그 값을 꺼내 새 프레임의 0번 지역 변수에 넣는다. 개념도로는 충분하지만 JVMS를 인용하는 절이므로 한 문장을 덧붙이는 편이 정확하다.

수정 제안: 240행에 다음 문장을 넣고, 그림의 ① 레이블에 "(operand stack 경유)"를 덧붙인다.

> 바이트코드 수준에서는 ``aload_1`` 명령이 이 참조값을 ``main`` 프레임의 operand stack에 올리고, `invokestatic` 명령이 그 값을 꺼내 새 프레임의 0번 지역 변수에 넣습니다.

근거: [JVMS 6.5 invokestatic](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-6.html#jvms-6.5.invokestatic) "the nargs argument values are popped from the operand stack. A new frame is created ... The nargs argument values are consecutively made the values of local variables of the new frame, with arg1 in local variable 0", [JVMS 2.6.2](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.6.2) "The operand stack is also used to prepare parameters to be passed to methods". `javap -c`로 `aload_1`, `invokestatic`을 확인했다.

Claude 발견, Codex도 같은 지적. 커밋 `adb199a`에 반영했다.

### 3. `myTv`가 1번 지역 변수라는 것은 명세가 아니라 컴파일러의 배치임 — 반영 완료

**위치: 본문 240행과 `java-stack-frames.drawio`.** "``main`` 프레임의 1번 지역 변수인 ``myTv``"

static 메서드의 파라미터가 0번부터 들어간다는 것(`main`의 `args`, `reassign`의 `tv`)은 JVMS 2.6.1이 정한다. 파라미터가 아닌 지역 변수 `myTv`를 몇 번 칸에 둘지는 컴파일러가 정한다. javac 25는 1번에 배정했다(`javap -l`의 LocalVariableTable에서 `Slot 1 myTv`).

수정 제안:

> javac가 ``main`` 프레임의 1번 지역 변수에 배정한 ``myTv``에 담긴 참조값이 이 칸으로 복사됩니다.

근거: [JVMS 3.2 Use of Constants, Local Variables, and Control Constructs](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-3.html#jvms-3.2) "The use (and reuse) of local variables is the responsibility of the compiler writer."

Codex 지적. 실행 확인(Java 25, `javap -c -l -p`). 커밋 `3f9e2d3`에 위 제안 문장을 넣고, 파라미터 번호는 JVMS가, 다른 지역 변수의 번호는 컴파일러가 정한다는 문장을 덧붙였다. 그림은 javac의 배치와 같으므로 고치지 않았다.

### 4. Go에서 `append`한 요소를 호출한 쪽 `nums`로는 읽을 수 없다고 단정함 — 반영 완료

**위치: 본문 596행, 618행.** 596행은 "함수 안에서 슬라이스에 ``append``한 결과가 호출한 쪽에 보이지 않는 것도 같은 원리입니다", 618행은 "용량이 남아 있어 새 요소가 같은 배열에 쓰이더라도 ``nums``의 길이는 바뀌지 않으므로, ``nums``로는 그 요소를 읽을 수 없습니다"라고 쓴다.

슬라이스 식의 상한은 길이가 아니라 용량이므로 `nums[:4]`처럼 다시 슬라이싱하면 그 요소를 읽을 수 있다. 길이 3, 용량 4인 `nums`를 `modify`에 넘긴 뒤 `nums[:4][3]`을 출력하면 `4`가 나온다. 호출한 쪽에 반영되지 않는 것은 늘어난 길이다.

수정 제안(596행):

> 함수 안에서 슬라이스에 ``append``해도 늘어난 길이가 호출한 쪽 슬라이스에 반영되지 않는 것도 같은 원리입니다.

수정 제안(618행 마지막 문장):

> 용량이 남아 있어 새 요소가 같은 배열에 쓰이더라도 ``nums``의 길이는 3 그대로이므로 ``nums[3]``은 범위를 벗어납니다. ``nums[:4]``처럼 용량까지 다시 슬라이싱해야 그 요소가 보입니다.

근거: [Go 사양 - Slice expressions](https://go.dev/ref/spec#Slice_expressions) "For slices, the upper index bound is the slice capacity cap(a) rather than the length.", [Appending and copying slices](https://go.dev/ref/spec#Appending_and_copying_slices).

Codex 지적. 실행 확인(Go 1.27.1, 출력 `[100 2 3] 3 4 4`). 커밋 `3f9e2d3`에 두 제안 모두 반영했다.

### 5. Rust에서 `Copy`가 아닌 인자는 모두 이동한다고 읽히는 서술 — 반영 완료

**위치: 본문 632행, 636행.** "``String``처럼 ``Copy``가 아닌 타입의 값은 소유권이 파라미터로 이동"하고, 이동과 복사의 차이는 "호출 뒤에 호출자 변수를 계속 쓸 수 있느냐뿐"이라고 쓴다.

`&mut T`는 `Copy`가 아니지만 `&mut T` 타입 변수를 `&mut T` 파라미터에 넘기면 컴파일러가 암묵적으로 재대여(reborrow)하므로 호출 뒤에도 그 변수를 다시 쓸 수 있다. 글의 분류(참조도 값으로 전달)를 뒤집지는 않지만, `Copy` 여부만으로 호출 뒤 사용 가능성을 판단하는 것처럼 읽힌다.

수정 제안(632행 뒤):

> 다만 ``&mut T`` 타입의 변수를 ``&mut T`` 파라미터에 넘기면 컴파일러가 암묵적으로 재대여(reborrow)하므로, ``&mut T``가 ``Copy``가 아니어도 호출 뒤에 그 변수를 다시 쓸 수 있습니다.

근거: [rustc 문서 - `rustc_hir_typeck::coercion`, Reborrowing](https://doc.rust-lang.org/stable/nightly-rustc/rustc_hir_typeck/coercion/index.html#reborrowing) "if we are expecting a reference, we will reborrow even if the argument provided was already a reference.", [`Copy` 트레이트 문서](https://doc.rust-lang.org/std/marker/trait.Copy.html)(`&mut T`는 `Copy`가 아님).

Codex 지적. 실행 확인(Rust 1.98.1에서 `let r = &mut a; bump(r); bump(r);`가 컴파일되고 `3` 출력). rustc 문서는 컴파일러 내부 문서라 언어 사양 수준의 근거는 아니다. 커밋 `3f9e2d3`에 위 제안 문장을 632행 끝에 이어 붙였다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| JLS 15.12.4.5 인용(activation frame에 인자 값을 담고, 새로 만든 파라미터 변수에 대입)과 번역 (213–218행) | **확인.** SE 26 원문과 글자 단위로 같다. 생략 부분(구현 관리 정보, StackOverflowError, 현재 프레임 전환)은 인자 값과 파라미터 변수의 관계를 바꾸지 않는다. | [JLS 15.12.4.5](https://docs.oracle.com/javase/specs/jls/se26/html/jls-15.html#jls-15.12.4.5) |
| 대상 참조(target reference)가 새 프레임에 담겨 `this`가 됨 (220행) | **확인.** 같은 절의 "make the target reference available as this" | [JLS 15.12.4.5](https://docs.oracle.com/javase/specs/jls/se26/html/jls-15.html#jls-15.12.4.5) |
| JVMS 2.6 인용(호출마다 새 프레임, 스레드의 JVM stack에 할당, 프레임마다 지역 변수 배열)과 번역 (224–229행) | **확인.** 원문과 같다. 2.5.2가 "frames may be heap allocated"라고 덧붙이지만 인용한 "allocated from the Java Virtual Machine stack" 서술과 모순되지 않는다. | [JVMS 2.6](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.6) |
| JVMS 2.6.1 인용(static 메서드는 0번부터, 인스턴스 메서드는 0번에 `this`, 파라미터는 1번부터)과 번역 (233–238행) | **확인.** 원문과 같다. `long`·`double`이 두 칸을 쓰는 규칙은 인용하지 않았으나 `Tv` 예제에는 영향이 없다. | [JVMS 2.6.1](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.6.1) |
| `reassign`은 static이므로 `tv`는 0번 지역 변수 (240행) | **확인.** 실행 확인. `javap -l`에서 `reassign`의 `Slot 0 tv`. | [JVMS 2.6.1](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.6.1) |
| JVMS 2장 서두가 런타임 데이터 영역의 메모리 배치를 구현자에게 맡김 (244행) | **확인.** "the memory layout of run-time data areas ... are left to the discretion of the implementor" | [JVMS 2장](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html) |
| JIT 컴파일러가 만든 코드는 파라미터를 CPU 레지스터에 둘 수 있음 (244행) | **확인.** HotSpot x86_64의 compiled calling convention이 참조를 포함한 인자를 레지스터부터 배정한다. 소스 대조 판정. | [HotSpot `java_calling_convention` (jdk-25-ga)](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/hotspot/cpu/x86/sharedRuntime_x86_64.cpp#L550-L606) |
| JLS 4.3.1 "reference values ... are pointers to these objects" 인용 (190–195행) | **확인.** 원문과 같다. | [JLS 4.3.1](https://docs.oracle.com/javase/specs/jls/se26/html/jls-4.html#jls-4.3.1) |
| JVMS 2.7의 핸들 구현 설명 (197행) | **확인.** "In some of Oracle's implementations ... a reference to a class instance is a pointer to a handle that is itself a pair of pointers" | [JVMS 2.7](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.7) |
| JLS 8.4.1 인용 (202–207행) | **확인.** 원문과 같다. | [JLS 8.4.1](https://docs.oracle.com/javase/specs/jls/se26/html/jls-8.html#jls-8.4.1) |
| Java swap·재할당·상태 변경 예제의 출력 (46–135행) | **확인.** 실행 확인(Java 25). `Hello`, `1`, `Classic`, `Rock`. | 로컬 실행 |
| `java-mutation` 그림의 화살표와 객체 상태 | **확인.** `setChannel("Rock")` 실행 후 시점이 제목에 명시되어 있고 두 변수가 같은 객체를 가리킨다. | 본문 119–135행 |
| Dev.java, MDN, Python 튜토리얼, Go FAQ, C# 레퍼런스, Kotlin 사양, TypeScript 핸드북, Rust Book 인용 | **확인.** 각 원문과 글자 단위로 같다. | 본문 참고 자료의 각 링크 |
| {cpp} N4950 [dcl.ref] 4항, [expr.call] 6항, C11 N1570 6.5.2.2 4항, Flanagan 11.2 인용 | **확인.** 원문(N4950은 timsong-cpp 미러, N1570은 port70.net HTML 미러)과 같다. | [N4950 dcl.ref](https://timsong-cpp.github.io/cppwp/n4950/dcl.ref), [N4950 expr.call](https://timsong-cpp.github.io/cppwp/n4950/expr.call), [N1570 HTML](https://port70.net/~nsz/c/c11/n1570.html), [Flanagan 11.2](https://docstore.mik.ua/orelly/webprog/jscript/ch11_02.htm) |
| JavaScript 객체 전달 예제, Python swap·객체 예제의 출력 | **확인.** 실행 확인(Node 24.6, Python 3.12.3). | 로컬 실행 |
| Go 슬라이스 예제의 출력 `[100 2 3] 3` (612행) | **확인.** 실행 확인(Go 1.27.1). 표현 보완 4번과 별개로 예제 출력은 맞다. | 로컬 실행 |
| JavaScript의 Object 값과 Java 참조값의 구분 (337–339행), Python의 불변성과 전달 방식 구분 (566행) | **확인.** Codex도 적절하다고 판정. | [ECMAScript ArgumentListEvaluation](https://tc39.es/ecma262/2026/multipage/ecmascript-language-expressions.html#sec-argument-lists-runtime-semantics-argumentlistevaluation), [Python FAQ](https://docs.python.org/3/faq/programming.html#how-do-i-write-a-function-with-output-parameters-call-by-reference) |

## 한계

- The Java Programming Language 4판(Gosling 인용), Head First Java, Compilers: Principles, Techniques, and Tools 2판은 원문을 이번에 다시 보지 못했다. 이전 기록의 판정에 기댔다.
- CLU 설계 문서와 Strachey 논문의 해당 절은 이번에 다시 대조하지 않았다.
- C·C++·C#·Ada 예제는 이번에 다시 실행하지 않았다. 출력은 3차 기록(GCC 13.3, .NET SDK 8.0.425, GNAT 13.3)의 실행 결과에 기댔다.
- HotSpot의 레지스터 전달은 x86_64 소스로만 확인했다. 다른 아키텍처의 calling convention은 보지 않았다.
