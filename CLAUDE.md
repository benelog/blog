# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Korean-language static blog ("개발수양록") by 정상혁, built with JBake 2.7.0 and Gradle. Deployed via Netlify.

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
