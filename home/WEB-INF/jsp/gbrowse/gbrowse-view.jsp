<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<i class="fa fa-spinner fa-spin" id="loading"></i>

<%-- this should be the only place that the raw gbrowse url is used --%>
<iframe src="/gb2/gbrowse/zfin_ensembl?${requestParams}"
        id="gbrowseFrame" marginheight="0" frameborder="0"></iframe>

<script>
    <%-- this keeps the iframe size in sync with the gbrowse content, on old browsers
    that don't support MutationObserver (IE <11) you just get a very tall iframe --%>
    jQuery("#gbrowseFrame").on("load", function () {
        var MutationObserver = window.MutationObserver || window.WebKitMutationObserver,
            iframe = this,
            $iframe = jQuery(this);
        $iframe.width("100%");
        jQuery("#loading").hide();
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