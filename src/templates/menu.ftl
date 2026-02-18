<header class="site-header">
    <div class="header-inner">
        <a href="${config.site_host}" class="site-title">${config.site_title}</a>
        <div class="header-nav-row">
            <nav>
                <ul>
                    <#list config.site_menus_main as menuItem>
                    <li><a href="<#if (config['site_menus_main_' + menuItem + '_url'] != "/")>${content.rootpath}${config['site_menus_main_' + menuItem + '_url']}<#else>${config.site_host}</#if>">${config['site_menus_main_' + menuItem + '_label']}</a></li>
                    </#list>
                </ul>
            </nav>
            <form class="header-search" method="get" action="//google.com/search">
                <input type="text" name="q" />
                <input type="hidden" name="q" value="site:${config.site_host}">
                <button type="submit" class="search-btn" aria-label="검색"><i class="fa fa-search"></i></button>
            </form>
        </div>
    </div>
</header>
