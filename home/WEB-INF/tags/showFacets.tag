<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="facetGroups" type="java.util.List" %>
<%@attribute name="facetQueries" type="java.util.List" %>

<div class="facet-container">
    <c:forEach var="facetGroup" items="${facetGroups}" varStatus="loop">
        <c:set var="open" value="true"/>
        <zfin2:showFacetGroup facetGroup="${facetGroup}" open="${facetGroup.open}"/>
    </c:forEach>
</div>

<script>
    $('.facet-group').each(function() {
        var $group = $(this);
        var openCloseState = window.localStorage.getItem($group.data('name'));
        if (openCloseState) {
            // only find child elements if there's word to do
            var $values = $group.find('.facet-group-values');
            var $icon = $group.find('.facet-group-label-container .icon-toggle');

            // don't do icon transition on page load
            $icon.addClass('notransition');
            setTimeout(function() {
                $icon.removeClass('notransition');
            }, 500);

            // now alter the UI based on saved state
            if (openCloseState === 'open') {
                $values.show();
                $icon.addClass('open');
            } else if (openCloseState === 'closed') {
                $values.hide();
                $icon.removeClass('open');
            }
        }
    });
    $('.facet-group-label-container').click(function(event) {
        var $target = $(event.target);
        var $group = $target.closest('.facet-group');
        var $values = $group.find('.facet-group-values');
        var $icon = $target.find('.icon-toggle');
        $icon.toggleClass('open');
        $values.slideToggle(200);
        window.localStorage.setItem($group.data('name'), $icon.hasClass('open') ? 'open' : 'closed');
    });
    $('.facet-label-container').click(function (event) {
        var $list = $(event.target).closest('.facet-value-list');
        var $icon = $list.find('.icon-toggle');
        if (!$icon.length) {
            // no icon, nothing to toggle.
            return;
        }

        $icon.toggleClass('open');
        $list.find('.facet-value-outer-box').slideToggle(200);
        $list.find('.facet-field-count').fadeToggle(200);
    });
</script>


