# Call by value vs Call by reference 글 사실 관계 검증 — 2026-09-19 (3차)

- 대상: [call-by-value-vs-call-by-reference.adoc](../src/content/call-by-value-vs-call-by-reference.adoc)
- 검토 기준 커밋: `3ceb9c3054aca07b603cac326a80f77c9a5e50c6`
- 참고: 이 기록에 적은 검토 기준 커밋과 반영 커밋은 이후 하나의 커밋으로 합쳐져 현재 이력에서는 해시로 찾을 수 없다. 검증 당시 글은 `call-by-value.adoc`이었고, 합칠 때 지금 파일명으로 바꿨다.
- 이전 기록: [2026-09-19 1차 검증](call-by-value-vs-call-by-reference-2026-09-19.md), [2026-09-19 2차 검증](call-by-value-vs-call-by-reference-2026-09-19-2.md). 2차 이후 절 순서 조정, C# 절과 MDN 인용, Ada 비교 예제, 인용 블록 전환 등 큰 수정이 있어 글 전체를 다시 봤다.
- 범위: 글 전체. 두 용어의 정의와 출처, 언어별 절의 사양 인용과 예제, 정리 표의 각 칸, 참고 자료의 절 이름.
- 방법: Codex 적대적 리뷰(`task`, 글 전체)로 의심 지점을 모은 뒤, Claude가 언어 사양·공식 문서·논문 원문을 WebFetch와 curl로 받아 대조했다. 예제는 Java 25, Node 24.6, Python 3.12.3, GCC 13.3, Go 1.27.1, Kotlin 2.4.20으로 로컬에서 실행했고, Rust 1.98.1, .NET SDK 8.0.425, GNAT 13.3은 도커 이미지로 실행했다. 이전 기록의 판정은 승계하지 않고 다시 대조했다.
- 결과: **우선 수정할 사항 6건, 표현을 보완할 사항 10건.** 핵심 결론인 "Java·JavaScript는 객체를 전달할 때도 참조값의 복사본을 전달하는 call by value이며, 객체 상태 공유는 호출자 변수의 별칭 전달과 다르다"는 타당하다. 오류는 이번 세션에 추가한 C# 절과 Flanagan 인용 블록, Go 사양의 절 이름, Ada 절의 타입 분류와 aliased 예시 문장에 몰려 있다.
- 반영 상태: **16건 모두 본문에 반영함.** 커밋 `fec7609`에 적용했다. Java swap 예제에 추가한 `int` 오버로드는 Java 25로 실행해 출력이 `Hello`, `1`로 같음을 확인했다.
- 아래 행 번호는 위 기준 커밋 기준이다.

## 우선 수정할 사항

### 1. C#의 `in`, `ref readonly`를 호출자 변수를 바꾸는 수단으로 제시함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 424행.** "호출한 쪽 변수를 바꾸려면 파라미터에 `ref`, `out`, `in`, `ref readonly` 수정자를 붙입니다"라고 적었다.

`in`과 `ref readonly`는 읽기 전용 참조다. 파라미터를 통해 대입하면 컴파일 오류 CS8331이 난다. 같은 절 444행이 이 둘을 "메서드가 값을 바꿀 수 없는 읽기 전용 참조"라고 바르게 설명하므로 본문 안에서도 모순된다. 참조로 전달하는 수정자 네 개와 그중 대입까지 허용하는 수정자 두 개를 구분해 써야 한다.

수정 제안(424행):

> 파라미터를 참조로 전달하려면 `ref`, `out`, `in`, `ref readonly` 수정자를 붙입니다. 이 가운데 호출한 쪽 변수에 대입까지 할 수 있는 것은 `ref`와 `out`입니다.

근거: [C# 레퍼런스 Method parameters](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/method-parameters) "`in`: ... The method can't assign a new value to the parameter", "`ref readonly`: ... The method can't assign a new value to the parameter". .NET SDK 8.0.425에서 `void F(in int x) { x = 1; }`와 `void G(ref readonly int x) { x = 1; }`를 컴파일해 CS8331 "Cannot assign to variable 'x' ... because it is a readonly variable"를 확인했다.

Codex 지적을 실행으로 확인했다.

### 2. C# 예제의 callout이 `ref` 없는 호출이 컴파일되는 것처럼 설명함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 468행.** `Replace(ref Tv tv)`로 선언한 메서드에 대해 "`ref` 없이 ``Replace(myTv)``로 넘기면 Java의 ``reassign(myTv)``와 같아서 ``Classic``이 출력됩니다"라고 적었다.

선언에 `ref`가 있으면 호출 지점에서 `ref`를 빼는 것만으로는 컴파일되지 않는다. 오류 CS1620이 난다. `Classic`이 출력되는 상황은 선언과 호출 양쪽에서 `ref`를 뺀 경우다.

수정 제안(468행):

> `Replace(Tv tv)`처럼 `ref` 없이 선언하고 ``Replace(myTv)``로 호출하면 Java의 ``reassign(myTv)``와 같아서 ``Classic``이 출력됩니다. 선언에 `ref`가 있는 상태에서 호출 지점의 `ref`만 빼면 컴파일 오류입니다.

근거: [C# 레퍼런스 Method parameters, `ref` parameter modifier](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/method-parameters#ref-parameter-modifier) "both the method definition and the calling method must explicitly use the `ref` keyword". .NET SDK 8.0.425에서 `Replace(myTv)` 호출을 컴파일해 CS1620 "Argument 1 must be passed with the 'ref' keyword"를 확인했다.

Codex 지적을 실행으로 확인했다.

### 3. Flanagan 인용 블록이 연속되지 않은 원문을 한 인용문으로 합침

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 315행부터의 인용 블록.** 인용 블록의 마지막 문장 "References themselves are passed by value."는 앞 두 문장에 바로 이어지는 본문이 아니라 Example 11-3의 제목이다. 원문에서는 앞 두 문장 뒤에 "Readers familiar with the other meaning of this term may prefer to say that objects and arrays are passed by value, but the value that is passed is actually a reference rather than the object itself. Example 11-3 illustrates this issue."라는 문장이 이어지고, 그다음에 예제 제목이 나온다.

같은 세션의 앞선 수정에서 WebFetch 요약이 두 문장을 연속으로 보여 준 것을 믿고 합쳤다. curl로 받은 HTML에서 `<h4 class="objtitle">Example 11-3. References themselves are passed by value</h4>`를 확인했다. 인용 블록에는 연속된 문장만 넣고, 예제 제목은 본문에서 따로 언급해야 한다. 원문의 셋째 문장은 이 글의 논지를 직접 뒷받침하므로 인용에 포함하는 편이 낫다.

수정 제안(315행 인용 블록):

> A function can use the reference to modify properties of the object or elements of the array. But if the function overwrites the reference with a reference to a new object or array, that modification is not visible outside of the function. Readers familiar with the other meaning of this term may prefer to say that objects and arrays are passed by value, but the value that is passed is actually a reference rather than the object itself.
>
> 함수는 그 참조로 객체의 프로퍼티나 배열의 원소를 수정할 수 있다. 그러나 함수가 그 참조를 새 객체나 새 배열의 참조로 덮어쓰면, 그 변경은 함수 밖에서 보이지 않는다. 이 용어의 다른 뜻에 익숙한 독자라면 객체와 배열이 값으로 전달되며 다만 전달되는 값이 객체 자체가 아니라 참조라고 말하고 싶을 것이다.

수정 제안(인용 블록 다음 문단 첫 문장):

> 이 설명에 이어지는 예제의 제목도 'References themselves are passed by value', 즉 참조 자체는 값으로 전달된다는 것입니다.

근거: [JavaScript: The Definitive Guide 4판 11.2](https://docstore.mik.ua/orelly/webprog/jscript/ch11_02.htm) 11.2.1절 본문과 Example 11-3 제목.

Codex 지적을 원문 HTML 구조로 확인했다.

### 4. Go 사양의 메서드 축약 규칙을 Selectors 절로 잘못 귀속함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 552행, 참고 자료 718행.** "Go 사양의 Selectors 절은 ``x``가 주소를 구할 수 있는 값이고 ``&x``의 메서드 집합에 ``m``이 있으면 ``x.m()``이 ``(&x).m()``의 축약 표기라고 규정합니다"라고 적고 `#Selectors`로 링크했다.

이 문장은 사양의 Calls 절에 있다. Selectors 절에 있는 축약 규칙은 포인터를 통한 필드 접근(`x.f`가 `(*x).f`의 축약)에 관한 것이다. 사양 HTML에서 해당 문장 앞의 가장 가까운 제목이 `<h3 id="Calls">`임을 확인했다.

수정 제안(552행): "Go 사양의 Selectors 절은"을 "Go 사양의 Calls 절은"으로, 링크를 `https://go.dev/ref/spec#Calls`로 바꾼다. 참고 자료 718행도 "The Go Programming Language Specification - Calls"로 고치거나, 이미 있는 Calls 항목과 합친다.

근거: [Go 사양 Calls](https://go.dev/ref/spec#Calls) "If x is addressable and &x's method set contains m, x.m() is shorthand for (&x).m()".

Claude가 먼저 찾았고 Codex도 같은 지적을 했다. 소스 대조 판정이다.

### 5. `aliased` 파라미터 예시 문장이 실제 인자 조건을 빠뜨림

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 614행.** "위 예제의 ``X``를 ``X : aliased in out Integer``로 선언하면 ``Integer``가 elementary type이어도 참조로 전달됩니다"라고 적었다.

형식 파라미터만 `aliased`로 바꾸면 앞 Swap 예제는 컴파일되지 않는다. Ada RM 6.4.1 6/3항은 explicitly aliased 파라미터의 실제 인자가 tagged 타입이거나 aliased view여야 한다고 규정하는데, 앞 예제의 `A : Integer := 1`은 aliased view가 아니다. 뒤에 추가한 Demo 예제는 `N : aliased Integer`로 선언해 이 조건을 만족한다.

수정 제안(614행):

> 위 예제의 ``X``를 ``X : aliased in out Integer``로 선언하고 실제 인자 ``A``도 ``A : aliased Integer``로 선언하면, ``Integer``가 elementary type이어도 참조로 전달됩니다.

근거: [Ada Reference Manual 6.4.1](https://ada-lang.io/docs/arm/AA-6/AA-6.4) 6/3항 "If the formal parameter is an explicitly aliased parameter, the type of the actual parameter shall be tagged or the actual parameter shall be an aliased view of an object."

Codex 지적을 매뉴얼 원문으로 확인했다. 소스 대조 판정이다.

### 6. Ada의 by-reference 타입 목록이 불완전한데 나머지를 모두 unspecified로 서술함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 614행.** "tagged type과 task·protected type, 명시적으로 limited인 record type은 by-reference type이라 참조로 전달합니다. 그 밖의 타입은 복사인지 참조인지 명시하지 않습니다(unspecified)"라고 적었다.

RM 6.2는 by-reference 타입을 다섯 갈래로 규정한다. 위 셋에 더해 by-reference 타입의 subcomponent를 가진 composite 타입과 full type이 by-reference인 private 타입도 by-reference다. 두 갈래를 빼고 "그 밖의 타입은 unspecified"라고 하면 이들이 unspecified로 읽힌다.

수정 제안(614행):

> tagged type과 task·protected type, 명시적으로 limited인 record type, 그리고 by-reference 타입을 구성 요소로 가진 composite type과 full type이 by-reference인 private type은 by-reference type이라 참조로 전달합니다. 어느 쪽에도 속하지 않는 타입은 복사인지 참조인지 명시하지 않습니다(unspecified).

근거: [Ada Reference Manual 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) 4~9항(by-reference 타입 목록), 11항(unspecified).

Codex 지적을 매뉴얼 원문으로 확인했다. 소스 대조 판정이다.

## 표현을 보완할 사항

### 1. 정리 표에서 읽기 전용 참조의 취급이 C# 행과 Ada 행 사이에 다름

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 678행(C# 행), 682행(Ada 행).** C# 행은 "예. `ref`, `out`, `in`, `ref readonly` 파라미터에 한함"으로 읽기 전용 참조를 call by reference 지원에 포함했다. 반면 Ada 행은 `in out`, `out` 모드로 한정해, by-reference 타입이나 `aliased` 파라미터가 `in` 모드로 참조 전달되는 경우를 뺐다. 2차 검증에서 Ada 행은 이 글의 판별 기준(파라미터에 대입하면 호출자 변수가 바뀌는가)에 맞춰 대입 가능한 모드만 적기로 했는데, C# 행이 같은 기준을 따르지 않아 일관성이 깨졌다.

또한 Ada 행의 "by-reference 타입이거나 `aliased`로 선언한 것만"은 unspecified 타입을 구현이 참조로 전달할 수도 있다는 점(6.2 11항)을 배제하는 표현이다. "by-copy 타입은 정상 종료 시 되돌려 쓰는 copy-restore"도 `out` 모드에는 정확하지 않다. 6.4.1 12~15항에 따르면 by-copy `out` 파라미터는 access 타입 등 일부를 빼면 호출 시 값을 복사해 오지 않고, 17항에 따라 정상 종료 시 되돌려 쓰기만 한다.

수정 제안(678행 C# 행의 지원 칸):

> 예. `ref`, `out` 파라미터에 한함. `in`, `ref readonly`도 참조로 전달하지만 대입은 불가

수정 제안(682행 Ada 행의 지원 칸):

> 부분적으로. `in out`, `out` 파라미터 중 by-reference 타입이거나 `aliased`로 선언한 것은 call by reference. by-copy 타입은 `in out`이면 copy-in·copy-out, `out`이면 정상 종료 시 되돌려 쓰기만 함. 그 밖의 타입은 unspecified라 구현이 정함. `in` 모드는 참조로 전달되더라도 상수 뷰라 대입 불가

근거: [Ada Reference Manual 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) 10~11항, NOTE 2 "A formal parameter of mode in is a constant view; it cannot be updated within the subprogram_body"; [6.4.1](https://ada-lang.io/docs/arm/AA-6/AA-6.4) 11~17항; [C# 레퍼런스 Method parameters](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/method-parameters).

Codex 지적을 매뉴얼로 확인했다. Ada `in` 모드를 지원 칸에 넣자는 부분은 2차 검증의 결정과 같은 이유로 받지 않고, 대신 두 행의 기준을 맞추는 쪽으로 제안한다.

### 2. Call by value 정의의 '복사본'이 Rust 절의 이동 설명과 어긋남

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 19행, 558행 이하.** 19행은 call by value를 "인자로 넘긴 값의 복사본을 파라미터에 대입하는 방식"으로 정의하고, Rust 절은 `Copy`가 아닌 타입은 소유권이 이동한다면서 이를 call by value에 넣는다. C++ 값 파라미터도 이동 생성으로 초기화될 수 있다.

정의의 중심을 '별도의 파라미터를 인자의 값으로 초기화한다'에 두고 복사와 이동을 그 방법으로 설명하면 논리가 이어진다.

수정 제안(19행):

> *Call by value*는 인자로 넘긴 값으로 파라미터라는 별도의 변수를 초기화하는 방식입니다. 대개 값을 복사하며, Rust처럼 소유권을 옮기는 언어도 있습니다. 함수 안에서 파라미터에 다른 값을 대입해도 호출한 쪽의 변수는 영향을 받지 않습니다.

근거: [Rust Book 4.1](https://doc.rust-lang.org/book/ch04-01-what-is-ownership.html#ownership-and-functions) "Passing a variable to a function will move or copy, just as assignment does"; [C++ N4950 \[expr.call\]](https://timsong-cpp.github.io/cppwp/n4950/expr.call) 6항 "each parameter is initialized with its corresponding argument".

Codex 지적이며 타당하다.

### 3. 판별 기준에 파라미터가 대입 가능하다는 조건이 빠짐

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 25행.** "값을 다시 대입할 수 있는 변수를 인자로 넘긴다고 가정하면"이라고 인자 쪽 조건만 두었다. C++의 `const T&`나 C#의 `in T`처럼 참조 파라미터가 읽기 전용이면 별칭이어도 대입 실험 자체를 할 수 없다.

수정 제안(25행): "값을 다시 대입할 수 있는 변수를 인자로 넘기고 파라미터에도 대입할 수 있다고 가정하면"으로 조건을 보탠다.

근거: [C# 레퍼런스 Method parameters](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/method-parameters) `in` 항목; .NET SDK 8.0.425에서 CS8331 확인.

Codex 지적이며 타당하다.

### 4. Rust가 call by reference를 지원하지 않는다는 서술은 이 글의 정의에 따른 해석임을 밝혀야 함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 558행, 정리 표 681행.** Rust 공식 자료인 Rust By Example은 `&T`를 넘기는 것을 "passed by reference"라고 부른다. 이 글은 호출자 변수의 별칭이 생기는지를 기준으로 삼아 "지원하지 않는다"고 했으므로, 그 기준에 따른 분류임을 문장에서 드러내야 공식 용례와 충돌하지 않는다.

수정 제안(558행 첫 문장):

> Rust는 이 글에서 정의한 의미의 call by reference, 즉 파라미터가 호출자 변수의 별칭이 되는 전달 방식을 지원하지 않습니다. Rust 공식 자료가 ``&T``를 넘기는 것을 'passed by reference'라고 부르기는 하지만, 그 참조는 값으로 전달되는 하나의 값입니다.

근거: [Rust By Example - Borrowing](https://doc.rust-lang.org/rust-by-example/scope/borrow.html) "Instead of passing objects by value (`T`), objects can be passed by reference (`&T`)."

Codex 지적을 원문으로 확인했다. Codex가 함께 든 interior mutability(`&Cell<T>`로 공유 참조를 통해 값을 바꿀 수 있음)는 인자 전달 방식이 아니라 타입의 성질이라 이 글의 범위 밖으로 보고 제안에 넣지 않았다.

### 5. "일반 함수 호출에서는 호출 지점에 `&`나 `&mut`가 드러난다"는 일반화

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 688행.** 이미 포인터나 가변 참조를 담은 변수를 넘기면 C·Go의 `f(p)`, Rust의 `f(r)`처럼 호출 지점에 아무 표시가 없다. C에서는 배열 이름을 넘길 때도 `&` 없이 포인터로 변환된다. 예외가 Go·Rust의 메서드 호출뿐인 것처럼 읽히지 않게 범위를 좁혀야 한다.

수정 제안(688행): "일반 함수 호출에서는 호출 지점에 ``&``나 ``&mut``가 드러나지만"을 "변수의 주소나 참조를 그 자리에서 구해 넘기는 호출에서는 ``&``나 ``&mut``가 드러나지만, 이미 포인터나 참조를 담은 변수를 넘기거나 Go와 Rust의 메서드 호출처럼 컴파일러가 주소 취득과 대여를 대신하는 경우에는 호출식의 모양만으로 판단할 수 없습니다"로 바꾼다.

근거: [C11 N1570](https://www.open-std.org/jtc1/sc22/wg14/www/docs/n1570.pdf) 6.3.2.1 3항(배열의 포인터 변환); [Go 사양 Calls](https://go.dev/ref/spec#Calls); [Rust Reference - Method-call expressions](https://doc.rust-lang.org/reference/expressions/method-call-expr.html).

Codex 지적이며 타당하다.

### 6. 정리 표 열 제목 "호출한 쪽 변수를 바꾸는 방법"의 범위

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 671행, 677행.** Kotlin의 클로저 캡처나 Python의 `nonlocal`처럼 인자 전달과 무관하게 바깥 변수를 바꾸는 수단이 있으므로, "없음"이 그런 방법까지 없다고 읽힐 수 있다. C++ 행에는 본문에서 설명한 포인터 파라미터가 빠져 있다.

수정 제안: 열 제목을 "파라미터를 통해 호출한 쪽 변수를 바꾸는 방법"으로 바꾸고, C++ 행의 방법 칸을 "참조 파라미터 선언. C처럼 포인터를 넘길 수도 있음"으로 보탠다.

근거: [Kotlin - Closures](https://kotlinlang.org/docs/lambdas.html#closures), [Python - nonlocal](https://docs.python.org/3/reference/simple_stmts.html#the-nonlocal-statement).

Codex 지적이며 타당하다.

### 7. Go FAQ 인용의 "C 계열의 모든 언어"를 보편 명제로 읽지 않도록 해야 함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 536행.** 인용은 원문과 번역 모두 정확하다. 그러나 C++과 C#은 참조 파라미터를 지원하므로 "C 계열의 모든 언어처럼 모든 것이 값으로 전달된다"를 이 글 전체의 명제로 받으면 앞뒤가 어긋난다.

수정 제안(인용 블록 다음 문장): "FAQ의 'C 계열의 모든 언어'라는 표현은 Go의 규칙을 강조하려는 것이고, 이 글에서 살펴본 {cpp}과 C#은 참조 파라미터를 따로 지원합니다"를 덧붙인다.

근거: [Go FAQ](https://go.dev/doc/faq#pass_by_value).

Codex 지적이며 타당하다.

### 8. swap 테스트만으로 primitive type의 전달 방식을 검증했다고 결론짓기 어려움

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 67행, 70행.** `swap(Object, Object)`에 int를 넘기면 오토박싱되어 참조값이 전달되므로, 이 실험은 두 호출 모두 참조값 전달을 관찰한 것이다. 70행의 "primitive type이든 객체든 call by value로 동작합니다"는 결론 자체는 JLS 8.4.1로 뒷받침되지만, 이 예제만으로 primitive를 검증했다고 연결하면 약하다.

수정 제안: `static void swap(int x, int y)` 오버로드를 하나 더 두어 두 번째 호출이 primitive 파라미터로 가게 하거나, 67행 callout에 "이 실험은 오토박싱된 참조값을 넘기므로 primitive 파라미터의 동작은 8.4.1 절의 규정으로 확인합니다"를 덧붙인다.

근거: [JLS 5.3 Invocation Contexts](https://docs.oracle.com/javase/specs/jls/se26/html/jls-5.html#jls-5.3), [JLS 8.4.1](https://docs.oracle.com/javase/specs/jls/se26/html/jls-8.html#jls-8.4.1).

Codex 지적이며 타당하다.

### 9. Kotlin 예제는 그대로 컴파일되지 않으므로 출력 조건을 밝혀야 함

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 224행.** `println(myTv.channel) // Rock`이라고 적었지만, 같은 파일의 `reassign` 함수가 컴파일 오류를 내므로 파일 전체가 컴파일되지 않는다. 오류 줄이나 `reassign` 함수를 지운 뒤의 출력이라는 조건이 필요하다.

수정 제안(callout 2번 끝): "``reassign`` 함수를 지우고 실행하면 ``Rock``이 출력됩니다"로 조건을 적는다.

근거: kotlinc 2.4.20으로 컴파일해 `'val' cannot be reassigned.` 오류를 확인하고, `reassign`을 지운 파일을 실행해 `Rock` 출력을 확인했다.

Codex 지적을 실행으로 확인했다.

### 10. Go의 맵·슬라이스 설명에 경계 조건 보충

**반영 완료.** 커밋 `fec7609`.

**위치: 본문 554행, 정리 표 680행.** "맵이나 슬라이스 값을 복사해도 그 값이 가리키는 데이터는 복사되지 않습니다"와 "포인터처럼 동작"은 맞지만, nil 맵에는 원소를 넣을 수 없고, 슬라이스의 길이와 용량은 각 슬라이스 값에 속하므로 함수 안의 `append`가 호출자의 슬라이스 길이를 바꾸지 않는다. 독자가 "모든 변경이 공유된다"로 넓혀 읽지 않도록 한 문장을 보탠다.

수정 제안(554행 끝): "다만 슬라이스의 길이와 용량은 슬라이스 값 자체에 속하므로, 함수 안에서 ``append``한 결과는 반환해서 다시 대입하지 않으면 호출한 쪽에 반영되지 않습니다."

근거: [Go 사양 Map types](https://go.dev/ref/spec#Map_types) "A nil map is equivalent to an empty map except that no elements may be added", [Slice types](https://go.dev/ref/spec#Slice_types), [Appending to and copying slices](https://go.dev/ref/spec#Appending_to_and_copying_slices).

Codex 지적이며 타당하다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| Strachey, Fundamental Concepts in Programming Languages 3.4.2절 'Parameter calling modes'가 R-value 전달을 call by value, L-value 전달을 call by reference라고 부름(23행) | **확인.** HOSC 13권(2000) 판 PDF를 pdftotext로 풀어 3.4.2절 본문 대조. FORTRAN은 모두 by reference, ALGOL call by value는 by R-value에 해당한다는 서술도 확인 | [Strachey 1967/2000](https://www.cs.cmu.edu/~crary/819-f09/Strachey67.pdf) |
| Dragon book 2판 1.6.6절의 call-by-value·call-by-reference 정의와 Java가 call-by-value만 쓴다는 서술(23행) | **확인.** PDF 34~35쪽 원문 대조. 판권면 2007년 | Aho, Lam, Sethi, Ullman, Compilers 2판 1.6.6 |
| ALGOL 60 개정 보고서 4.7.3.1 'Value assignment (call by value)', 4.7.3.2 'Name replacement (call by name)'(29행) | **확인.** masswerk 텍스트 원문 대조. 원본은 CACM 1960년 5월호 | [Revised Report](https://www.masswerk.at/algol60/report.htm) |
| CLU 설계 문서(Memo-112)가 1970년대 중반의 것(33행) | **확인.** MIT CSG 출판 목록에서 Memo-112 "A Note on CLU", Barbara Liskov, November 1974 확인. 문서 본문에 call by sharing이 쓰였는지는 아래 한계 참조 | [CSG Publications](https://csg.csail.mit.edu/pubs/publications.html) |
| MDN Functions의 "Arguments are always passed by value ... object arguments are passed by sharing" 인용(287행) | **확인.** Description > Passing arguments 단락 원문 대조 | [MDN Functions](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Functions#passing_arguments) |
| Gosling 외, The Java Programming Language 인용(143행) | **한계.** 아래 참조 | |
| Dev.java 인용 "Reference data type parameters, such as objects, are also passed into methods by value", "the passed-in reference still references the same object as before"(152행) | **확인.** WebFetch 원문 대조 | [Dev.java](https://dev.java/learn/classes-objects/calling-methods-constructors/) |
| JLS 4.3.1 "The reference values (often just references) are pointers to these objects, and a special null reference, which refers to no object"(184행) | **확인.** JLS SE 26 원문 대조 | [JLS 4.3.1](https://docs.oracle.com/javase/specs/jls/se26/html/jls-4.html#jls-4.3.1) |
| JVMS 2.7: Oracle의 일부 구현에서 참조가 핸들을 가리키고 핸들이 메서드 테이블과 객체 데이터를 가리킴(191행) | **확인.** curl로 받은 JVMS SE 26 2.7절 원문 대조. "a reference to a class instance is a pointer to a handle that is itself a pair of pointers" | [JVMS 2.7](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.7) |
| JLS 8.4.1 "When the method or constructor is invoked (§15.12), the values of the actual argument expressions initialize newly created parameter variables ..."(196행) | **확인.** curl로 받은 원문 대조 | [JLS 8.4.1](https://docs.oracle.com/javase/specs/jls/se26/html/jls-8.html#jls-8.4.1) |
| Kotlin 사양 Function declaration "parameters are final and cannot be changed inside the function"과 인용 블록의 앞부분(207행) | **확인.** WebFetch로 완전한 문장 대조 | [Kotlin Spec](https://kotlinlang.org/spec/declarations.html#function-declaration) |
| ECMAScript ArgumentListEvaluation이 `GetValue`로 값을 얻어 목록을 만들고, FunctionDeclarationInstantiation이 그 목록으로 파라미터 바인딩을 초기화함(294행) | **확인.** ES2026 multipage HTML을 curl로 받아 "Let arg be ? GetValue(ref)"와 "Perform ? IteratorBindingInitialization of formals with arguments iteratorRecord and usedEnv" 대조 | [ArgumentListEvaluation](https://tc39.es/ecma262/2026/multipage/ecmascript-language-expressions.html#sec-argument-lists-runtime-semantics-argumentlistevaluation), [FunctionDeclarationInstantiation](https://tc39.es/ecma262/2026/multipage/ordinary-and-exotic-objects-behaviours.html#sec-functiondeclarationinstantiation) |
| ECMAScript 언어 타입 여덟 가지와 language value·specification type의 구분(296행) | **확인.** 6.1절 "The ECMAScript language types are Undefined, Null, Boolean, String, Symbol, Number, BigInt, and Object", 6.2절 specification types 목록 대조 | [ES 6.1](https://tc39.es/ecma262/2026/multipage/ecmascript-data-types-and-values.html) |
| TypeScript 핸드북 "As a principle, TypeScript never changes the runtime behavior of JavaScript code"(300행) | **확인.** Runtime Behavior 절 원문 대조 | [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/typescript-from-scratch.html#runtime-behavior) |
| Flanagan 4판 11.2 요약 표에서 Object가 by reference로 복사·전달·비교됨(311행) | **확인.** Table 11-3의 Object 행 "Reference / Reference / Reference" 대조 | [Definitive Guide 11.2](https://docstore.mik.ua/orelly/webprog/jscript/ch11_02.htm) |
| C11 N1570 6.5.2.2 4항 인용과 각주 93의 내용(328행) | **확인.** port70 HTML 미러를 curl로 받아 4항 본문과 각주 93 "A function may change the values of its parameters, but these changes cannot affect the values of the arguments. On the other hand, it is possible to pass a pointer to an object, and the function may change the value of the object pointed to" 대조 | [N1570 HTML](https://port70.net/~nsz/c/c11/n1570.html#6.5.2.2p4) |
| C++ N4950 [dcl.ref] 1항 주석 "A reference can be thought of as a name of an object", 4항 "It is unspecified whether or not a reference requires storage"(389행) | **확인.** WebFetch 원문 대조 | [\[dcl.ref\]](https://timsong-cpp.github.io/cppwp/n4950/dcl.ref) |
| C++ N4950 전문에 'call by value', 'call by reference', 'pass by value'는 0회, 'passed by reference'는 4회 출현하며 모두 주석이나 라이브러리 절(404행) | **확인.** N4950 단일 페이지 HTML(36MB)을 curl로 받아 태그를 벗기고 대소문자 구분 없이 집계. 4회는 [dcl.fct.def.coroutine] Note, [syserr.errcat.overview] Note, [container.assoc.reqmts] 본문, Annex C 호환성 절. Codex는 이 집계를 검증하지 않았다고 했으나 Claude가 확인함 | [N4950 full](https://timsong-cpp.github.io/cppwp/n4950/full) |
| C++ N4950 [expr.call] 6항 "When a function is called, each parameter ([dcl.fct]) is initialized ([dcl.init], [class.copy.ctor]) with its corresponding argument"(402행) | **확인.** WebFetch 원문과 항 번호 대조 | [\[expr.call\]](https://timsong-cpp.github.io/cppwp/n4950/expr.call) |
| C# 레퍼런스 인용 "By default, C# passes arguments to functions by value. ... For reference (class) types, the method gets a copy of the reference"(415행) | **확인.** 문서 첫 단락 원문 대조 | [C# Method parameters](https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/keywords/method-parameters) |
| C#에서 `ref`는 선언과 호출 양쪽에 써야 하며 참조 파라미터는 referent를 가리키는 reference variable(424행, 444행) | **확인.** 문서의 "both the method definition and the calling method must explicitly use the `ref` keyword", "A parameter that's passed by reference is a reference variable ... refers to a different variable called its referent" 대조 | 위와 같음 |
| Python 튜토리얼 4.8 인용과 각주의 call by object reference, FAQ의 passed by assignment(477행 이하) | **확인.** WebFetch로 완전한 문장과 각주 원문 대조 | [Python Tutorial 4.8](https://docs.python.org/3/tutorial/controlflow.html#defining-functions) |
| Go 사양 Calls: 새 저장 공간을 할당하고 인자를 파라미터에 대입(530행) | **확인.** curl로 받은 원문 "new storage is allocated for the function's variables, which includes its parameters and results. Then, the arguments of the call are passed to the function, which means that they are assigned to their corresponding function parameters" 대조 | [Go Spec Calls](https://go.dev/ref/spec#Calls) |
| Go FAQ "As in all languages in the C family, everything in Go is passed by value. That is, a function always gets a copy of the thing being passed, as if there were an assignment statement assigning the value to the parameter"(532행 인용 블록)와 맵·슬라이스가 포인터를 담은 descriptor라는 설명 | **확인.** WebFetch 원문 대조 | [Go FAQ](https://go.dev/doc/faq#pass_by_value) |
| Rust Book "Passing a variable to a function will move or copy, just as assignment does"(560행) | **확인.** 원문 대조 | [Rust Book 4.1](https://doc.rust-lang.org/book/ch04-01-what-is-ownership.html#ownership-and-functions) |
| Rust Reference: `&mut T`는 `Copy`가 아님(586행) | **확인.** "A mutable reference (that hasn't been borrowed) is the only way to access the value it points to, so is not `Copy`" 대조 | [Rust Reference Pointer types](https://doc.rust-lang.org/reference/types/pointer.html#mutable-references-mut) |
| Rust 메서드 호출이 수신자를 자동으로 역참조·대여함(588행) | **확인.** "for each candidate T, add &T and &mut T to the list immediately after T" 대조 | [Method-call expressions](https://doc.rust-lang.org/reference/expressions/method-call-expr.html) |
| Ada RM 6.2: elementary type은 by-copy, tagged·task·protected·explicitly limited record는 by-reference, `aliased` 파라미터는 타입과 관계없이 by-reference, 그 밖은 unspecified(614행) | **확인.** 3, 4~9, 10, 11항 원문 대조. 단, by-reference 목록 누락은 우선 수정 6번 | [Ada RM 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) |
| Ada RM 6.4.1 17항: by-copy `in out`·`out` 파라미터는 정상 종료 시 실제 인자에 되돌려 씀(614행) | **확인.** "After normal completion and leaving of a subprogram, for each in out or out parameter that is passed by copy, the value of the formal parameter is converted ... and assigned to it" 대조 | [Ada RM 6.4.1](https://ada-lang.io/docs/arm/AA-6/AA-6.4) |
| `aliased` 형식 파라미터는 Ada 2012부터(663행) | **확인.** 6.1절 "Extensions to Ada 2005: Parameters can now be explicitly aliased" 대조 | [Ada RM 6.1](https://ada-lang.io/docs/arm/AA-6/AA-6.1/) |
| Java ReferenceTest 출력 `Hello`, `1`; ReassignTest 출력 `Classic`; ChangeStateTest 출력 `Rock` | **확인.** Java 25로 실행. ChangeStateTest는 앞 예제의 `Tv` 클래스를 같이 컴파일 | 로컬 실행 |
| JavaScript swap 출력 `Hello`, 객체 예제 출력 `Classic`, `Rock` | **확인.** Node 24.6.0으로 실행 | 로컬 실행 |
| Python 예제 출력 `1`, `Classic`, `Rock` | **확인.** Python 3.12.3으로 실행 | 로컬 실행 |
| Kotlin `reassign`의 컴파일 오류 메시지 "'val' cannot be reassigned"와 `changeState` 출력 `Rock` | **확인.** kotlinc 2.4.20. 출력은 `reassign`을 지운 파일로 확인(보완 9번) | 로컬 실행 |
| C·C++·Go·Rust swap 결과 a=2, b=1 | **확인.** GCC 13.3, G++ 13.3, Go 1.27.1은 로컬, Rust 1.98.1은 도커 `rust:1-slim` | 로컬·도커 실행 |
| C# Swap 결과 a=2, b=1과 Replace 예제 출력 `Rock` | **확인.** 도커 `mcr.microsoft.com/dotnet/sdk:8.0`(8.0.425)으로 실행 | 도커 실행 |
| Ada Demo 출력 네 줄(inside By_Copy: N = 1 / after By_Copy: N = 2 / inside By_Reference: N = 2 / after By_Reference: N = 2) | **확인.** 도커 `ubuntu:24.04`에 GNAT 13.3.0을 설치해 실행 | 도커 실행 |
| Ada `in` 모드를 정리 표의 call by reference 지원 칸에 넣어야 한다는 Codex 지적 | **Codex 지적, 재검증 결과 기각.** RM 6.2 NOTE 2에 따라 `in` 파라미터는 상수 뷰라 대입할 수 없으므로, 이 글의 판별 기준(파라미터 대입이 호출자 변수에 반영되는가)으로는 지원 칸에 넣지 않는다. 2차 검증과 같은 결정이다. 대신 C# 행과의 일관성 문제로 보완 1번에 반영 | [Ada RM 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) NOTE 2 |
| Rust의 interior mutability(`&Cell<T>`)를 호출자 값 변경 수단으로 추가하자는 Codex 지적 | **Codex 지적, 재검증 결과 기각.** 공유 참조로 내부 값을 바꾸는 것은 타입의 성질이지 인자 전달 방식이 아니며, 이 글의 Rust 절은 파라미터가 별칭이 되는지를 다룬다 | [Rust Reference Interior mutability](https://doc.rust-lang.org/reference/interior-mutability.html) |

## 한계

- Gosling 외 The Java Programming Language 4판의 인용문(143행)과 Head First Java 2판의 리모컨 비유(137행)는 종이책이라 원문을 확보하지 못했다. 1차·2차 검증에서도 대조하지 않았다. 본문은 판과 장만 밝히고 쪽 번호를 적지 않은 상태를 유지한다.
- CLU Memo-112는 스캔 이미지 PDF라 본문에서 'call by sharing'이라는 용어가 실제로 쓰였는지 확인하지 못했다. 문서의 제목·저자·날짜만 MIT CSG 출판 목록으로 확인했다. Liskov의 A History of CLU(MIT-LCS-TR-561) 텍스트에서는 이 용어를 찾지 못했다.
- Kotlin 사양 인용 블록의 수식 표기(p~i~: P~i~ = v~i~)는 원문의 수식 조판을 AsciiDoc 아래 첨자로 옮긴 것이라 글자 단위로 같지는 않다.
- Codex 리뷰가 든 C# 언어 사양(§15.6.2) 링크는 직접 열지 않았고, 같은 내용을 C# 레퍼런스 문서와 컴파일 결과로 확인했다.
