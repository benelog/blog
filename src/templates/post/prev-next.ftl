<ul class="pagination">
    <li><a href="${content.rootpath}${previousFileName!'#'}"
            class="<#if (previousFileName?? == false) >disabled</#if>">PREVIOUS</a></li>

    <li><span class="page-info">${currentPageNumber} / ${numberOfPages}</span></li>

    <li><a href="${content.rootpath}${nextFileName!'#'}"
            class="<#if (nextFileName?? == false) >disabled</#if>">NEXT</a></li>
</ul>
