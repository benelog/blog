<#include "header.ftl">

    <#include "menu.ftl">

    <div class="container">
        <div class="content-card">
            <h1>Archive</h1>
            <div class="archive-section">
                <#list published_posts as post>
                    <#if (last_month)??>
                        <#if post.date?string("MMMM yyyy") != last_month>
                            </ul>
                            <h4>${post.date?string("MMMM yyyy")}</h4>
                            <ul>
                        </#if>
                    <#else>
                        <h4>${post.date?string("MMMM yyyy")}</h4>
                        <ul>
                    </#if>
                    <li>
                        <span class="archive-date">${post.date?string("dd")}</span>
                        <a href="${content.rootpath}${post.noExtensionUri!post.uri}"><#escape x as x?xml>${post.title}</#escape></a>
                        <#if ((config.site_includeReadTime!'true')?boolean == true)><span class="eta"></span></#if>
                    </li>
                    <#assign last_month = post.date?string("MMMM yyyy")>
                </#list>
            </ul>
            </div>
        </div>
    </div>

<#include "footer.ftl">
