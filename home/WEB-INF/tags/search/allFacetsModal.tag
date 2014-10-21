<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<div id="facet-value-modal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <%-- todo: need to label the modal--%>
        <span id="all-facet-modal-title"></span>
    </div>
    <div class="modal-body">

        <zfin-search:facetAutocomplete/>

    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div>

<script>
    jQuery(document).ready(function() {

        jQuery('.facet-value-modal-link').on('click', function() {
            var category = jQuery(this).attr('category');
            var field = jQuery(this).attr('field');
            var title = jQuery(this).attr('modal-title');
            jQuery('#all-facet-modal-title').text(title);
            angular.element(jQuery('#facet-list-controller')).scope().fetchValues(category, field, title);
        });
    });

</script>