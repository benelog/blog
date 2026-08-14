# 블로그 인용 예제 저장소 통합 검토

작성일: 2026-08-14

블로그(개발수양록) 글에서 인용하는 https://github.com/benelog 하위 예제 저장소들의 현황과 통합 방안 정리.

## 작업 기록 (2026-08-14)

아래 "제안" 중 일부를 변경하여 실행했다.

### 결정 사항 (제안과 달라진 점)

- 별도 `blog-examples` 모노레포를 새로 만들지 않고, **blog 저장소의 `examples/` 하위 폴더**로 직접 병합
- 원본 저장소는 아카이브 대신 **삭제**하기로 결정
  - 외부 사이트에서 걸어둔 옛 저장소 링크는 삭제 후 깨짐(복구 불가)을 감수
  - 블로그 글 내부의 링크는 새 경로로 갱신해 깨지지 않도록 조치
- 우선 3개 저장소만 대상으로 진행 (`quiz`, `uploader`, `dumper` 등은 이번 대상 아님)

### 완료된 작업

1. `git subtree add --prefix=examples/<이름> ... master`로 커밋 이력을 보존한 채 병합
   - `examples/beanutils-test/`
   - `examples/java-date-time/`
   - `examples/jackson-experiment/`
2. 해당 저장소를 인용하는 글 4편의 링크를 새 경로로 갱신
   - `2626007.adoc` → `examples/beanutils-test`
   - `3120317.adoc` → `examples/java-date-time` (파일 딥링크 2개 포함)
   - `jackson-with-constructor.adoc`, `missing-readme.adoc` → `examples/jackson-experiment` (`:source-repo:`, `:source-link-base:` 속성 수정)
   - 딥링크가 가리키는 파일이 병합된 폴더에 실제로 존재하는 것 확인
3. `origin/master`에 push 완료, GitHub에 `examples/` 반영 확인

### 추가 작업 (2026-08-14, 2차)

- `btrace-scripts`도 같은 방식(`git subtree add`)으로 `examples/btrace-scripts/`에 병합
  - 블로그 미인용 저장소였으나, 설명 글(`btrace-jdbc-monitoring.adoc`)을 새로 작성해 예제로 인용
- 원본 https://github.com/benelog/btrace-scripts 도 아래 수동 삭제 대상에 추가

### 남은 작업: 원본 저장소 수동 삭제

`gh` 토큰에 `delete_repo` 권한이 없어 삭제는 수동으로 진행하기로 함. 삭제 대상:

- https://github.com/benelog/beanutils-test
- https://github.com/benelog/java-date-time
- https://github.com/benelog/jackson-experiment
- https://github.com/benelog/benchmark
  - blog `examples/`로 병합하지 않고 gist로 이전: https://gist.github.com/benelog/a9db4ac3018d6222baea32d5c2f783b5 (이전 완료)
  - `delete_repo` 권한 승인 대기 중, 승인되면 `gh repo delete benelog/benchmark --yes`로 삭제

삭제 방법: GitHub 웹 (Settings → Danger Zone → Delete this repository) 또는
`gh auth refresh -h github.com -s delete_repo` 후 `gh repo delete benelog/<이름> --yes`

## 현황

블로그 글에서 인용하는 benelog 계정 저장소 (blog 자체와 gist 제외):

| 저장소 | 인용 글 | 특징 |
|---|---|---|
| quiz, beanutils-test, uploader, dumper, java-date-time, jackson-experiment | 2012~2019년경 글 6~7편 | 스타 거의 없음, 10년 안팎 미활동, 순수 예제 |
| multiline | 1편 | ★105, 라이브러리로 독자 사용자 있음 |
| one-ftpserver | 1편 | ★34, 독립 실행 도구 |
| egloos-migration | 1편 | ★11, 완결된 프로젝트 |
| hermes-modal | 최근 글 1편 (파일 단위 딥링크 12개) | 활동 중, Modal에 배포되는 실물 앱 |

인용된 gist: `2922437`(한 글에서 24회 링크), `7655764`, `4582041`, `aee89ac5b6ff896b2e0f`

### 핵심 제약

글에서 인용하는 링크 중 절반 가까이가 `blob/master/...` 형태의 **파일 단위 딥링크**다.
저장소를 합치면 이 링크들이 깨진다. GitHub은 저장소 이름 변경은 리다이렉트해 주지만,
다른 저장소로의 병합은 리다이렉트가 없다.

### 링크 인용처 (글 파일 기준)

- `2626007.adoc` → beanutils-test
- `2864739.adoc`, `2875999.adoc`, `2930401.adoc` → uploader
- `2874354.adoc`, `2875999.adoc` → dumper
- `2930401.adoc` → one-ftpserver
- `2960128.adoc`, `2962391.adoc` → quiz
- `2999108.adoc` → multiline
- `3120317.adoc` → java-date-time
- `jackson-with-constructor.adoc`, `missing-readme.adoc` → jackson-experiment
- `migration-to-static-site.adoc` → egloos-migration, blog
- `hermes-modal-telegram-bot.adoc` → hermes-modal
- `2937766.adoc`, `2879657.adoc`, `3007743.adoc`, `robolectric.adoc` → gist

## 제안: 선별 통합 + 원본 아카이브

### 1. `blog-examples` (또는 `examples`) 모노레포 신설, "죽은 예제" 저장소만 흡수

대상: `quiz`, `beanutils-test`, `uploader`, `dumper`, `java-date-time`, `jackson-experiment`
(블로그 미인용이지만 성격이 같은 후보: `string-replace-test`, `benchmark`, `batch-experiments`, `btrace-scripts`)

- 토픽별 하위 디렉터리로 배치: `blog-examples/uploader/`, `blog-examples/quiz/` …
- `git subtree add --prefix=<이름> <원본URL> master` 방식으로 커밋 이력을 보존한 채 병합
- 루트 README에 각 디렉터리가 어느 블로그 글의 예제인지 표로 정리
  — 이것 자체가 예제들의 목차 역할을 해서 통합의 실익이 생김

### 2. 원본 저장소는 삭제하지 말고 아카이브

- 각 원본에 "이 저장소는 `blog-examples/<이름>`으로 이동했습니다" README 안내를 커밋한 뒤 archive 처리
- 아카이브 저장소는 읽기 전용으로 계속 접근 가능 → 블로그의 기존 딥링크와 외부 링크가 전부 유지됨 (링크 깨짐 위험 0)
- 계정 첫 화면에서도 아카이브 저장소는 시각적으로 구분되어 정리 효과 충분

### 3. 독자적 정체성이 있는 저장소는 유지

`multiline`(★105), `one-ftpserver`(★34), `egloos-migration`, `hermes-modal`은 통합하지 않음.

- 스타·포크·외부 링크가 붙은 라이브러리/도구는 URL 자체가 자산
- hermes-modal은 배포 단위라 합치면 오히려 불편

### 4. gist는 그대로 유지

- 글에 임베드/링크가 촘촘히 박혀 있음 (특히 `2922437`은 한 글에서 24회)
- gist → 저장소 이전은 리다이렉트가 없어 실익 대비 작업량 큼

### 5. (선택) 블로그 글의 링크를 새 경로로 갱신

- 아카이브 덕분에 필수는 아님
- 신규 독자를 새 모노레포로 보내려면 저장소 루트 링크(약 15곳)만 새 경로로 바꾸고
  파일 딥링크는 그대로 두는 절충이 편함

## 대안 (참고)

- **전부 한 모노레포로**: 계정이 가장 깔끔해지지만 스타 있는 저장소의 정체성과 외부 링크를 잃음. 비추천.
- **통합 없이 GitHub Lists/topics로 묶기만**: 작업량 최소지만 저장소 수가 줄지 않음.
  정리 목적이 '찾기 쉽게'라면 이것으로도 충분할 수 있음.
