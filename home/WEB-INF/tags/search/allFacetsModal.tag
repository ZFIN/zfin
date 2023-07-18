<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="queryString" required="true" type="java.lang.String" %>
<%@attribute name="baseUrlWithoutPage" required="true" type="java.lang.String" %>


<!-- Modal -->
<div class="modal fade" id="facet-value-modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <%-- todo: need to label the modal--%>
                <span id="all-facet-modal-title"></span>
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            </div>
            <div class="modal-body">

                <zfin-search:facetAutocomplete queryString="${queryString}" baseUrlWithoutPage="${baseUrlWithoutPage}"/>

            </div>
            <div class="modal-footer">
                <button class="btn btn-outline-secondary" data-dismiss="modal" aria-hidden="true">Close</button>
            </div>
        </div>
    </div>
</div>


<script>
    jQuery(document).ready(function() {
        jQuery('.facet-value-modal-link').on('click', function() {
            var field = jQuery(this).attr('field');
            var title = jQuery(this).attr('modal-title');
            jQuery('#all-facet-modal-title').text(title);
            window.zfinEventBus.publish('facet-modal-open', field);
        });
    });

</script>