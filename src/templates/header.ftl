<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape></#if> - ${config.site_title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="${(content.description)!}">
    <meta name="author" content="${content.author!config.site_author}">
    <meta name="keywords" content="<#if content.tags?has_content><#list content.tags as tag>${tag}<#sep>, </#list></#if>">
    <meta name="generator" content="JBake">
    <meta property="og:url" content="${config.site_host}${content.noExtensionUri!content.uri!""}"/>
    <meta property="og:type" content="<#if (content.type)?? && content.type == 'post'>article<#else>website</#if>"/>
    <meta property="og:title" content="<#escape x as x?xml><#if (content.title)??>${content.title} - </#if>${config.site_title}</#escape>"/>
    <meta property="og:description" content="<#escape x as x?xml>${(content.description)!config.sidebar_intro_summary}</#escape>"/>
    <meta property="og:image" content="${config.site_host}<#if (content.og)??>${content.og.image}<#else>${config.sidebar_intro_pic_src}</#if>"/>

    <link rel="stylesheet" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/google-font.css" />
    <link rel="stylesheet" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/font-awesome.min.css" />
    <link rel="stylesheet" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/main.css" />
    <link rel="stylesheet" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/add-on.css" />
    <link rel="stylesheet" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/monokai-sublime.css">

    <link rel="shortcut icon" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>img/favicon/favicon.png">
  </head>
  <body>
