# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Korean-language static blog ("개발수양록") by 정상혁, built with JBake 2.6.7 (via the `org.jbake.site` Gradle plugin 5.5.0). Deployed via Netlify.

## Build Commands

```bash
# Build the site (generates into output/)
./gradlew bake

# Clean and rebuild
./gradlew clean bake
```

Requires JDK 25 (configured via `.sdkmanrc` as `25-tem`). Use `sdk env` if using SDKMAN.

## Architecture

- **Content**: `src/content/` — AsciiDoc (`.adoc`) files. 2 types: `post` and `page`.
- **Templates**: `src/templates/` — FreeMarker (`.ftl`). Theme is "Future Imperfect" (ported from HTML5 UP by manikmagar).
- **Assets**: `src/assets/` — CSS, JS, fonts, images served as-is.
- **Config**: `src/jbake.properties` — site metadata, menu items, sidebar config, social links, rendering options.
- **Output**: `output/` (gitignored).
- **Fact checks**: `fact-checks/` — 글별 사실 관계 검증 기록(`<slug>-<YYYY-MM-DD>.md`). `/fact-check` 스킬이 작성한다.

## Commit Conventions

커밋 메시지 제목(첫 줄)은 공백을 포함해 50자 이내로 쓴다. Git 공식 문서(`git commit` 매뉴얼의 DISCUSSION)가 권하는 길이이고, 이 저장소 커밋 제목의 중간값(48자)에도 맞는다. 한글은 터미널에서 2칸을 차지하므로 `git log --oneline`에서 잘리지 않게 하려면 이 한도 안에서도 짧을수록 좋다.

제목에는 무엇을 바꿨는지만 적고, 바꾼 이유나 세부 항목은 빈 줄 뒤 본문에 적는다. 본문은 항목이 여러 개면 문단이나 목록으로 나눈다.

## Content Conventions

New posts go in `src/content/` as `.adoc` files with this header format:

```adoc
= Post Title
정상혁
2026-02-18
:jbake-type: post
:jbake-status: published
:jbake-tags: tag1, tag2
:description: Brief description
:jbake-last_updated: 2026-02-18
:idprefix:
```

Post images go in `src/content/img/<topic-slug>/`.

### 파일명을 바꿀 때의 redirect

글의 파일명이 곧 주소(`/<slug>.html`)이므로, 이미 게시된 글의 파일명을 바꾸면 `src/assets/_redirects`에 옛 주소를 새 주소로 넘기는 301 규칙을 추가한다.
Netlify는 확장자 없는 주소(`/<slug>`)로도 페이지를 열어 주므로, 규칙은 `.html` 주소와 확장자 없는 주소 두 줄을 한 쌍으로 넣는다.

```
/old-slug.html /new-slug.html 301
/old-slug /new-slug.html 301
```

배포 후 `curl -sI https://blog.benelog.net/old-slug`로 두 주소 모두 301과 새 `location`이 오는지 확인한다.

## Template Structure

Key layout chain: `header.ftl` → `menu.ftl` → page-specific template → `commons/sidebar.ftl` → `footer.ftl`

- `post.ftl` / `page.ftl` — main content layouts
- `index.ftl` — homepage with pagination (3 posts per page)
- `post/` subdirectory — post-specific partials (header, content, prev/next navigation)
- `commons/` subdirectory — shared components (sidebar, disqus, analytics, social links, share buttons)

## Key Config Notes

- Site menu items are defined in `jbake.properties` as `site.menus.main` entries
- Disqus comments (`blog-benelog`) and Google Analytics are configured in properties and rendered by templates in `commons/`
- Pagination is set to 3 posts per page via `index.posts_per_page=3`
