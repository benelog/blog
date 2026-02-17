<div class="content-card post-list-item">
    <#include "header.ftl">

    <#include "../commons/featured.ftl">

    <p>${post.summary!''}</p>

    <a href="${content.rootpath}${post.noExtensionUri!post.uri}" class="continue-reading">Continue Reading</a>

    <footer>
        <#include "../commons/footer-tags.ftl">
    </footer>
</div>
