<#if (config.site_google_trackingid?has_content)>
<script async src="https://www.googletagmanager.com/gtag/js?id=${config.site_google_trackingid}"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', '${config.site_google_trackingid}');
</script>
</#if>