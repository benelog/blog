# Call by value vs Call by reference 글 사실 관계 검증 — 2026-09-20

- 대상: [call-by-value-vs-call-by-reference.adoc](../src/content/call-by-value-vs-call-by-reference.adoc)
- 검토 기준 커밋: `3ab8ef33a0b2197c75e37b9c36161f5855f9e192`
- 이전 기록: [2026-09-19 4차 검증](call-by-value-vs-call-by-reference-2026-09-19-4.md)과 그 안에서 링크한 1~3차 기록. 이번 검증은 4차 이후의 변경분만 본다.
- 범위: 기준 커밋이 바꾼 부분. JVMS 2.7의 핸들 설명을 보완한 문단(197행, 199행)과 참고 자료에 추가한 링크 두 개(729행, 730행). 글의 다른 부분은 다시 보지 않았다.
- 방법: Codex 적대적 리뷰(`adversarial-review --base HEAD~1`)로 의심 지점을 모은 뒤, Claude가 JVMS SE 26 2.7절, JVMS SE 6판 3.7절과 각주 8, Wayback Machine에 보존된 JVMS 초판(1997)과 2판 HTML, HotSpot 백서, OpenJDK HotSpot 용어집을 curl로 받아 원문과 대조했다. 실행으로 재현할 항목은 없다. 모두 소스 대조 판정이다.
- 결과: **우선 수정할 사항 0건, 표현을 보완할 사항 1건.** "JVMS의 핸들 설명은 Sun 시절 구현을 가리키며, Oracle JDK와 OpenJDK가 함께 쓰는 HotSpot은 핸들 없이 직접 포인터를 쓴다"는 이번 수정의 결론은 타당하다. 보완할 곳은 "Classic VM을 염두에 둔 것"이라는 한 문장으로, 근거 사슬을 본문에 드러내면 추론이 아니라 확인된 사실로 읽힌다.
- 반영 상태: **표현 보완 1건을 본문에 반영함.** 커밋 `6181e2ff4f40454171ef6383ecebcbe28d437b1c`에서 199행을 제안 문장으로 바꾸고, 참고 자료에 Wayback Machine의 JVMS 초판 3.7절 링크를 추가했다. 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다.
- 아래 행 번호는 위 기준 커밋 기준이다.

## 우선 수정할 사항

없음.

## 표현을 보완할 사항

### 1. "이 문장은 Sun 시절의 Classic VM을 염두에 둔 것"이 본문에 적은 근거만으로는 추론에 머묾 — 반영 완료

**위치: 본문 199행.** "이 문장은 Sun 시절의 Classic VM을 염두에 둔 것입니다. Java SE 6판 JVMS의 같은 내용은 "Sun의 일부 구현"이라고 적혀 있었고, 이후 판에서 회사 이름만 Oracle로 바뀌었습니다."라고 서술한다.

Codex는 SE 6판 각주가 "Sun의 일부 구현"이라고만 하고, HotSpot 백서는 Classic VM을 핸들을 쓴 이전 VM의 예로 들 뿐이므로, 두 자료를 합쳐도 JVMS 문장이 바로 Classic VM을 가리킨다는 단정까지는 입증되지 않는다고 지적했다. 본문에 적은 근거만 보면 맞는 지적이다.

재검증에서 JVMS 초판(1997)의 3.7절 원문을 확인했다. 초판은 "In Sun's current implementation of the Java Virtual Machine, a reference to a class instance is a pointer to a handle that is itself a pair of pointers ..."라고 쓴다. "일부 구현"이 아니라 "현재 구현"이다. 초판이 나온 1997년 Sun의 JVM은 JDK 1.0·1.1의 Classic VM이고(HotSpot은 1999년 출시), HotSpot 백서는 그 Classic VM이 간접 핸들을 썼다고 설명한다. 따라서 문장의 유래를 Classic VM으로 보는 것은 근거가 있다. 다만 초판도 "Classic VM"이라는 이름을 쓰지는 않으므로, 본문은 판별 변천을 그대로 보여 주고 Classic VM은 백서의 설명으로 연결하는 편이 정확하다.

수정 제안:

> 이 문장은 JVMS 초판(1997)에서 "Sun의 현재 구현"을 설명하던 것입니다. 2판에서 "Sun의 일부 구현"으로, Oracle 인수 뒤의 판에서 "Oracle의 일부 구현"으로 바뀌었을 뿐 내용은 같습니다. 초판 당시 Sun의 JVM은 JDK 1.0·1.1의 Classic VM이었고, HotSpot 백서는 이 Classic VM이 간접 핸들을 썼다고 설명합니다. 현재 Oracle JDK와 OpenJDK가 공통으로 쓰는 HotSpot은 핸들 없이 객체를 직접 가리키는 포인터를 씁니다.

근거: [JVMS 초판 3.7 Representation of Objects(Wayback Machine, 2000-12-13 스냅샷)](http://web.archive.org/web/20001213164500/http://java.sun.com/docs/books/vmspec/html/Overview.doc.html), [JVMS 2판 3.7절 각주 8(Wayback Machine, 2004-12-29 스냅샷)](http://web.archive.org/web/20041229191147/http://java.sun.com/docs/books/vmspec/2nd-edition/html/Overview.doc.html), [HotSpot 백서 Handleless Objects](https://www.oracle.com/java/technologies/whitepaper.html).

Codex 지적, 재검증 결과 부분 수용. 소스 대조 판정. 커밋 `6181e2f`에서 위 제안 문장을 본문에 넣었다. 본문에는 초판 링크를 Wayback Machine 주소로 걸었고, 백서의 "Java 코드가 핸들을 쓰지 않는다"는 설명 문장은 제안 문장 뒤에 그대로 이어 두었다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| JVMS 2.7은 객체의 내부 구조를 특정하지 않으며, 일부 구현에서는 참조가 핸들을 가리킬 수 있다고 설명한다(197행) | **확인.** SE 26 원문 "The Java Virtual Machine does not mandate any particular internal structure for objects. In some of Oracle's implementations of the Java Virtual Machine, a reference to a class instance is a pointer to a handle that is itself a pair of pointers: one to a table containing the methods of the object and a pointer to the Class object that represents the type of the object, and the other to the memory allocated from the heap for the object data."와 대조. 핸들이 메서드 테이블·Class 객체 포인터와 객체 데이터 포인터의 쌍이라는 본문 설명도 일치 | [JVMS SE 26 2.7](https://docs.oracle.com/en/java/javase/26/docs/specs/jvms/jvms-2.html#jvms-2.7) |
| Java SE 6판 JVMS의 같은 내용은 "Sun의 일부 구현"이라고 적혀 있었다(199행) | **확인.** SE 6판 3.7절 각주 8 "In some of Sun's implementations of the Java virtual machine, a reference to a class instance is a pointer to a handle ..."와 대조. 본문은 SE 6판에서 각주였던 문장이 SE 26에서 본문으로 옮겨진 점은 적지 않았으나, 회사 이름 외의 문구는 대소문자(virtual machine)만 다르다 | [JVMS SE 6 3.7](https://docs.oracle.com/javase/specs/jvms/se6/html/Overview.doc.html#16066) |
| 이후 판에서 회사 이름만 Oracle로 바뀌었다(199행) | **확인.** 초판(1997) "In Sun's current implementation", 2판 "In some of Sun's implementations", SE 26 "In some of Oracle's implementations"로 이어지며 핸들 구조 설명은 같다. 위 표현 보완 1번에 적은 대로 초판은 "현재 구현"이라 하므로 본문에서 초판까지 언급하면 더 정확하다 | 위 표현 보완 1번의 Wayback 링크 |
| HotSpot 백서 'Handleless Objects' 절이 Classic VM은 간접 핸들을 썼고 HotSpot은 핸들 없이 직접 포인터를 쓴다고 설명한다(199행, 730행) | **확인.** 원문 "In previous versions of the Java virtual machine, such as the Classic VM, indirect handles are used to represent object references. ... In the Java HotSpot VM, no handles are used by Java code. Object references are implemented as direct pointers."와 대조. 절 제목은 `<h4>Handleless Objects`로 실재한다 | [HotSpot 백서](https://www.oracle.com/java/technologies/whitepaper.html) |
| HotSpot의 객체 참조는 핸들이 아니라 직접 포인터다(199행) | **확인.** 백서 외에 OpenJDK HotSpot 용어집 oop 항목 "Implemented as a native machine address, not a handle."로 재확인. 같은 용어집의 handle 항목은 C/C++ 코드가 safepoint를 넘길 때 쓰는 GC 루트용 간접 참조를 뜻하며, Java 코드의 객체 참조와는 다른 개념이다. Codex도 문제 없다고 봤다 | [HotSpot Glossary](https://openjdk.org/groups/hotspot/docs/HotSpotGlossary.html) |
| 참고 자료의 SE 6판 링크 앵커 `#16066`이 3.7절을 가리킨다(729행) | **확인.** 페이지 HTML에서 `<h2>3.7 Representation of Objects</h2>` 바로 앞의 앵커가 `<a name="16066">`임을 curl로 확인. Codex는 앵커 실재를 확인하지 못했다고 했으나 재검증으로 확정 | [JVMS SE 6 3.7](https://docs.oracle.com/javase/specs/jvms/se6/html/Overview.doc.html#16066) |

## 한계

- "Oracle JDK와 OpenJDK가 공통으로 HotSpot을 쓴다"(199행)는 Oracle 블로그 "Oracle JDK Releases for Java 11 and Later"(2018, Donald Smith)의 "From Java 11 forward, therefore, Oracle JDK builds and OpenJDK builds will be essentially identical."로 뒷받침되지만, 해당 페이지는 curl과 WebFetch 모두 본문을 내려 주지 않아 검색 결과 스니펫으로만 확인했다. 원문 대조가 필요하면 브라우저에서 직접 본다. 본문에는 이 출처를 링크하지 않았으므로 수정 요구 사항은 아니다.
- HotSpot 백서 페이지에는 절 단위 앵커가 없어 참고 자료 링크(730행)는 문서 전체로 연결된다. 링크 표시 문구에 절 이름을 적어 두었으므로 그대로 둔다.
- JVMS 초판·2판 원문은 Sun 사이트가 사라져 Wayback Machine 스냅샷으로 확인했다. 인쇄본(Lindholm, Yellin, 1997·1999)과는 대조하지 않았다.
