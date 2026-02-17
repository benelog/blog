<div class="post-nav">
    <#if (post.previousContent)??>
    <span class="nav-prev">
        <a href="${content.rootpath}${post.previousContent.noExtensionUri!post.previousContent.uri}">${post.previousContent.title}</a>
    </span>
    </#if>
    <#if (post.nextContent)??>
    <span class="nav-next">
        <a href="${content.rootpath}${post.nextContent.noExtensionUri!post.nextContent.uri}">${post.nextContent.title}</a>
    </span>
    </#if>
</div>
