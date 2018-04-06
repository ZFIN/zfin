<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<i class="fas fa-spinner fa-spin" id="loading"></i>

<%-- this should be the only place that the raw gbrowse url is used --%>
<iframe src="${urlPrefix}?${requestParams}" width="100%"
        id="gbrowseFrame" marginheight="0" frameborder="0"></iframe>

<script>
    <%-- this keeps the iframe size in sync with the gbrowse content, on old browsers
    that don't support MutationObserver (IE <11) you just get a very tall iframe --%>
    jQuery("#gbrowseFrame").on("load", function () {
        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver,
            iframe = this,
            $iframe = jQuery(this);
        jQuery("#loading").hide();

        <%-- absolute urls that don't already have a target should get popup out --%>
        $iframe.contents().on("click", "a", function () {
            var $this = jQuery(this),
                url = $this.attr("href"),
                target = $this.attr("target");
            if ((url.indexOf('http://') === 0 || url.indexOf('https://') === 0) && target === undefined) {
                $this.attr("target", "_blank");
            }
        });

        if (MutationObserver) {
            new MutationObserver(function () {
                <%-- 20 is a bit of fudge to keep iframe's scroll bars to from showing up --%>
                $iframe.height($iframe.contents().find("body").height() + 20);
            }).observe(iframe.contentDocument.body, {
                attributes: true,
                childList: true,
                characterData: true,
                subtree: true
            });
        } else {
            $iframe.height(3000);
        }
    });
</script>