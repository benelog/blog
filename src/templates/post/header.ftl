<div class="post-meta">
    <span class="post-date">
        <time datetime='${post.date?string("yyyy-MM-dd")}'>
            ${post.date?string("yyyy-MM-dd")}</time>
    </span>
    <#if ((config.site_includeReadTime!'true')?boolean == true)><span class="eta"></span></#if>
</div>
<#if (titleH1 == true)??>
    <h1><a href="${content.rootpath}${post.noExtensionUri!post.uri}">${post.title}</a></h1>
    <#assign titleH1 = false />
<#else>
    <h2><a href="${content.rootpath}${post.noExtensionUri!post.uri}">${post.title}</a></h2>
</#if>
<#if (post.description?has_content)>
    <p class="post-description">${post.description}</p>
</#if>
