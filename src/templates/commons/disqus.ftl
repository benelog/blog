<#if (config.site_disqus_shortname?has_content)>
<div class="disqus">
  <div id="disqus_thread"></div>
  <script type="text/javascript">
    var disqus_config = function () {
      this.page.identifier = '${post.uri}';
    };
    var disqus_shortname = '${config.site_disqus_shortname}';
    /* * * DON'T EDIT BELOW THIS LINE * * */
    (function() {
        var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
        dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';
        dsq.setAttribute('data-timestamp', + new Date());
        (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
    })();
  </script>
  <noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
  <!--<a href="http://disqus.com" class="dsq-brlink">comments powered by <span class="logo-disqus">Disqus</span></a>-->
</div>
</#if>