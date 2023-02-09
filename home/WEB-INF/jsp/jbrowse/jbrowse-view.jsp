<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <i class="fas fa-spinner fa-spin" id="loading"></i>

    <iframe src="${urlPrefix}?${requestParams}" width="100%" height="1000px"
            id="jbrowseFrame" marginheight="0" frameborder="0"></iframe>

    <script>
        jQuery("#jbrowseFrame").on("load", function () {
            jQuery("#loading").hide();
            jQuery("#jbrowseFrame").show();
        });
        jQuery("#jbrowseFrame").hide();
    </script>

    <style>
        main {
            margin-bottom:0;
        }
    </style>
</z:page>