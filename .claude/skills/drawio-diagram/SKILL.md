---
name: drawio-diagram
description: draw.io(.drawio) 다이어그램을 작성하고 PNG로 내보내 블로그 글에 넣는 워크플로. 사용자가 "다이어그램", "draw.io", "도식", "그림으로 표현", "구조도"를 언급하거나, .drawio 파일을 만들고·수정하고·PNG로 내보내거나, 글에 다이어그램을 삽입할 때 반드시 이 스킬을 사용한다. 아키텍처·파이프라인·흐름 설명을 시각화해 달라는 요청이면 draw.io를 명시하지 않아도 이 스킬을 참고한다.
---

# draw.io 다이어그램 작성과 PNG 내보내기

## CLI 환경

draw.io desktop이 사용자 레벨로 설치되어 있다 (sudo 불필요).

- 실행 파일: `~/.local/bin/drawio` (PATH에 있음)
- 실체: `~/.local/opt/drawio/opt/drawio/drawio` (GitHub 릴리스 .deb를 dpkg-deb로 풀어둔 것, `--no-sandbox`가 래퍼에 포함)
- 업데이트: jgraph/drawio-desktop 릴리스에서 새 .deb를 받아 `dpkg-deb -x`로 같은 위치에 다시 푼다

내보내기 명령:

```bash
drawio -x -f png --scale 2 -o <출력.png> <입력.drawio>
```

- `--scale 2`: 고해상도(레티나) 대응. 블로그 본문 폭 기준으로 충분하다
- SVG가 필요하면 `-f svg`
- Electron이라 디스플레이가 필요하다. 데스크톱 세션이면 `DISPLAY=:0`으로 동작하고, headless 환경이면 `xvfb-run -a drawio ...`를 쓴다
- stderr의 Vulkan/GTK/dbus 경고는 무해하므로 무시한다

## 파일 위치 규칙

- `.drawio` 원본과 내보낸 `.png` 모두 `src/content/img/<topic-slug>/`에 둔다 (원본도 커밋해 나중에 수정할 수 있게 한다)
- 글 삽입: `image::img/<topic-slug>/<이름>.png[<alt 텍스트>,width=800]`

## .drawio XML 직접 작성 요령

draw.io 파일은 mxGraph XML이다. 골격:

```xml
<mxfile host="app.diagrams.net">
  <diagram id="..." name="...">
    <mxGraphModel dx="1200" dy="700" grid="1" gridSize="10" page="1" pageWidth="1480" pageHeight="560">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
        <!-- vertex/edge mxCell들 -->
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
```

작성할 때 지킬 것:

- 노드: `vertex="1"` + `<mxGeometry x= y= width= height= as="geometry"/>`. 스타일은 `rounded=1;whiteSpace=wrap;html=1;fontSize=12;`를 기본으로 한다
- `value` 안의 줄바꿈은 `html=1`과 함께 `&lt;br&gt;`로 넣는다 (XML 이스케이프 필수)
- 간선: `edge="1"` + `source`/`target`에 노드 id. 스타일 `edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;`
- 간선 레이블이 다른 요소와 겹치면 `labelBackgroundColor=#ffffff`를 넣고, 위치는 `<mxGeometry relative="1"><mxPoint as="offset" x="0" y="-24"/></mxGeometry>`로 조정한다
- 간선의 출발·도착 지점 고정: `exitX/exitY/entryX/entryY` (0~1 비율). 경유점은 `<Array as="points"><mxPoint x= y=/></Array>`
- 그룹(구역) 표현: 실제 컨테이너 대신 점선 배경 사각형을 먼저 선언하고(먼저 선언한 셀이 뒤에 깔림) 그 위에 노드를 절대 좌표로 배치하는 편이 XML이 단순하다. 스타일: `fillColor=#fafafa;strokeColor=#b3b3b3;dashed=1;verticalAlign=top;align=left;fontStyle=1;spacingLeft=10;spacingTop=6;`
- 색은 draw.io 기본 팔레트를 쓴다: 파랑 `#dae8fc/#6c8ebf`, 초록 `#d5e8d4/#82b366`, 주황 `#ffe6cc/#d79b00`, 노랑 `#fff2cc/#d6b656`, 빨강 `#f8cecc/#b85450`, 보라 `#e1d5e7/#9673a6` (fillColor/strokeColor 쌍)
- 한글 텍스트는 기본 폰트로 잘 렌더링되므로 fontFamily를 지정하지 않는다

## 검증 절차

파일을 만들거나 고친 뒤 반드시:

1. XML 파싱 확인: `python3 -c "import xml.etree.ElementTree as ET; ET.parse('<파일>')"`
2. PNG로 내보낸 뒤 Read 도구로 이미지를 직접 열어 배치·글자 겹침·화살표 경로를 눈으로 확인한다. 겹침이 보이면 좌표를 고치고 다시 내보낸다
