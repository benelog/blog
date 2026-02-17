<#include "header.ftl">

    <#include "menu.ftl">

    <div class="container">
        <div class="content-card">
            <h1>Tag: ${tag}</h1>
        </div>

        <#list tag_posts as post>
            <#include "post/content-list.ftl">
        </#list>
    </div>

<#include "footer.ftl">
