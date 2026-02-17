<#include "header.ftl">

    <#include "menu.ftl">

    <div class="container">
        <#assign post = content />
        <#if (post??) >
            <#include "page/content-single.ftl">
        </#if>
    </div>

<#include "footer.ftl">
