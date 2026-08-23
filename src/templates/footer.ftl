    <footer class="site-footer">
        <p>&copy; ${config.site_title}. Baked with <a href="http://jbake.org">JBake ${version}</a>.
        <#if (config.render_sitemap?boolean)><a href="/${config.sitemap_file}">Sitemap</a>.</#if></p>
    </footer>

    <a id="back-to-top" href="#" class="fa fa-arrow-up fa-border fa-2x"></a>

    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/jquery.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/skel.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/util.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/main.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/backToTop.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/highlight.pack.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/readingTime.js"></script>

    <#if (config.site_disqus_shortname?has_content)>
        <script id="dsq-count-scr" src="//${config.site_disqus_shortname}.disqus.com/count.js" async></script>
    </#if>
    <#if (config.site_google_trackingid?has_content)>
        <#include "commons/google-analytics.ftl" />
    </#if>
    <script>
        // AsciiDoc 콜아웃 마커의 괄호를 없애서 동그라미 안에 숫자만 남긴다
        document.querySelectorAll('b.conum').forEach(function (el) {
            el.textContent = el.textContent.replace(/[()]/g, '');
        });
        hljs.initHighlightingOnLoad();
    </script>

  </body>
</html>
