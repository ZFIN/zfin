<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dbLink" type="org.zfin.sequence.DBLink" rtexprvalue="true" required="true" %>

<c:choose>
    <c:when test="${fn:length(dbLink.blastableDatabases)==1}">
        <zfin2:blastAccessionURL dbLink="${dbLink}" blastDB="${dbLink.blastableDatabases[0]}"/>
    </c:when>
    <c:when test="${fn:length(dbLink.blastableDatabases)>1}">
        <c:set var="blastLink" value="blast-popup${dbLink.accessionNumber}"/>
        <c:set var="blastLinkPopup" value="blast-links${dbLink.accessionNumber}"/>
        <div class="analysis_tools_box">
            <div id="${blastLink}" class="analysis_tools_box_header">
                Select Tool
            </div>

            <div id="${blastLinkPopup}" class="analysis_tools_box_popup_box">
                <c:forEach var="blastDB" items="${dbLink.blastableDatabases}">
                    <div class="analysis_tools_box_popup_entry">
                        <%--for some reason I couldn't use class here--%>
                        <a
                           href="<zfin:blastAccessionURL dbLink="${dbLink}" blastDB="${blastDB}"/>">${blastDB.displayName}</a>
                    </div>
                </c:forEach>
            </div>
        </div>


        <script>
            jQuery(document).ready(function() {
                jQuery('#${blastLink}').click(function() {
                    jQuery("#${blastLinkPopup}").slideToggle(70);
                });
            });
        </script>

    </c:when>
</c:choose>
