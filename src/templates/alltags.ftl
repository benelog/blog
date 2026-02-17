<#include "header.ftl">

    <#include "menu.ftl">

    <div class="container">
        <div class="content-card">
            <h1>Tags</h1>
            <ul class="tags-list">
                <#list alltags as tag>
                <li>
                    <a href="${content.rootpath}${config.tag_path}/${tag}${config.output_extension}">
                        ${tag}
                        <span class="tag-count">${db.getPublishedPostsByTag(tag).size()}</span>
                    </a>
                </li>
                </#list>
            </ul>
        </div>
    </div>

<#include "footer.ftl">
