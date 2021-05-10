<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="showName" type="java.lang.String" %>
<%@attribute name="hideName" type="java.lang.String" %>
<%@attribute name="classNamePatternShow" type="java.lang.String" %>
<%@attribute name="classNamePatternHide" type="java.lang.String" %>


<span id="${classNamePatternShow}-show-link" style="float: right">
<a href="javascript:showAll();">${showName}</a>
</span>
<span id="${classNamePatternShow}-hide-link" style="float: right; display: none;font-size:small; font-weight:normal;">
    <a href="javascript:hideAll();">${hideName}</a>
</span>

<script type="text/javascript">

    function showAll() {
        jQuery('.${classNamePatternShow}').each(function () {
            jQuery(this).click();
        });
        jQuery('#${classNamePatternShow}-show-link').hide();
        jQuery('#${classNamePatternShow}-hide-link').show();
    }
    function hideAll() {
        jQuery('.${classNamePatternHide}').each(function () {
            jQuery(this).click();
        });
        jQuery('#${classNamePatternShow}-show-link').show();
        jQuery('#${classNamePatternShow}-hide-link').hide();
    }

</script>

