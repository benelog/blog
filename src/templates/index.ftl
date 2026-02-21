<#include "header.ftl">

    <#include "menu.ftl">

    <div class="container">
        <#if (currentPageNumber == 1)>
        <div class="content-card">
            <h2 class="recent-posts-title">Recent Posts</h2>
            <ul class="recent-post-list">
                <#list db.getPublishedPosts() as recentPost>
                <#if recentPost?index < 5>
                <li>
                    <span class="recent-post-date">${recentPost.date?string("yyyy-MM-dd")}</span>
                    <a href="${content.rootpath}${recentPost.noExtensionUri!recentPost.uri}">${recentPost.title}</a>
                    <#if recentPost.tags?has_content>
                    <span class="post-tags-inline">
                        <#list recentPost.tags as tag>
                        <#if tag?has_content><a href="${content.rootpath}${config.tag_path}/${tag}${config.output_extension}">#${tag}</a></#if>
                        </#list>
                    </span>
                    </#if>
                </li>
                </#if>
                </#list>
            </ul>
            <a href="${content.rootpath}archive.html" class="all-posts-link">All Posts →</a>
        </div>
        </#if>

        <#list published_posts as post>
        <#if (post??) >
            <#include "post/content-single.ftl">
        </#if>
        </#list>

        <#include "post/prev-next.ftl">
    </div>

<#include "footer.ftl">
