    <#if (config.render_tags?boolean == true) && post.tags??>
        <div class="post-tags">
            <#list post.tags as tag>
            <a href='${content.rootpath}${config.tag_path}/${tag}${config.output_extension}'>#${tag}</a>
            </#list>
        </div>
    </#if>
