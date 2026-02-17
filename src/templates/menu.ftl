<header class="site-header">
    <div class="header-inner">
        <a href="${config.site_host}" class="site-title">${config.site_title}</a>
        <nav>
            <ul>
                <#list config.site_menus_main as menuItem>
                <li><a href="<#if (config['site_menus_main_' + menuItem + '_url'] != "/")>${content.rootpath}${config['site_menus_main_' + menuItem + '_url']}<#else>${config.site_host}</#if>">${config['site_menus_main_' + menuItem + '_label']}</a></li>
                </#list>
            </ul>
        </nav>
    </div>
</header>
