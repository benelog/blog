<#include "header.ftl">

    <#include "menu.ftl">

    <div class="container">
        <#assign post = content />
        <#assign titleH1 = true />
        <#if (post??) >
            <#include "post/content-single.ftl">
        </#if>

        <#include "post/prev-next-post.ftl">

        <#include "commons/disqus.ftl">
    </div>

<#include "footer.ftl">
