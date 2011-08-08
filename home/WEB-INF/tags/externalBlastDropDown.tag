<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dbLink" type="org.zfin.sequence.DBLink" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="minWidth" type="java.lang.String" rtexprvalue="true" required="false" %>


<c:choose>
    <c:when test="${fn:length(dbLink.referenceDatabase.relatedBlastDbs) == 1}">
        <%--almost certain that we never have these (would be megablast) but always possible)--%>
        <c:set var="blastDB" value="${dbLink.blastableDatabases[0]}"/>
        <c:set var="blastLinkString"
               value="/action/blast/external-blast?accession=${dbLink.accessionNumber}&refDB=${dbLink.referenceDatabase.zdbID}&blastDB=${blastDB.zdbID}"/>
        <c:choose>
            <c:when test="${dbLink.referenceDatabase.foreignDBDataType.dataType eq 'Polypeptide'}">
                <c:set var="blastLinkString" value="${blastLinkString}&sequenceType=pt"/>
                <c:set var="blastLinkString" value="${blastLinkString}&program=blastp"/>
            </c:when>
            <c:otherwise>
                <c:set var="blastLinkString" value="${blastLinkString}&sequenceType=nt"/>
                <c:set var="blastLinkString" value="${blastLinkString}&program=blastn"/>
            </c:otherwise>
        </c:choose>

        <a href="${blastLinkString}"
                >Blast at ${blastDB.displayName}</a>
    </c:when>
    <c:when test="${fn:length(dbLink.referenceDatabase.relatedBlastDbs) > 1}">
        <c:set var="blastLink" value="blast-popup${dbLink.accessionNumber}"/>
        <c:set var="blastLinkPopup" value="blast-links${dbLink.accessionNumber}"/>

        <c:set var="style" value="vertical-align: top; text-align: center;"/>
        <c:if test="${!empty minWidth}">
            <c:set var="style" value="${style} min-width: ${minWidth};"/>
        </c:if>

        <div class="analysis_tools_box" style="${style}">
            <div id="${blastLink}" class="analysis_tools_box_header">
                    ${!empty instructions? instructions : "Select Tool"}
            </div>

            <div id="${blastLinkPopup}" class="analysis_tools_box_popup_box">
                <c:forEach var="blastDB" items="${dbLink.blastableDatabases}">
                    <div class="analysis_tools_box_popup_entry">
                            <%--for some reason I couldn't use class here--%>
                        <c:set var="blastLinkString" value=""/>
                        <c:choose>
                            <c:when test="${!empty blastDB.location}">
                                <c:set var="blastLinkString"
                                       value="/action/blast/external-blast?accession=${dbLink.accessionNumber}&refDB=${dbLink.referenceDatabase.zdbID}&blastDB=${blastDB.zdbID}"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="blastLinkString"
                                       value="/action/blast/blast?sequenceID=${dbLink.accessionNumber}&queryType=SEQUENCE_ID&dataLibraryString=${blastDB.abbrev}"/>
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when test="${dbLink.referenceDatabase.foreignDBDataType.dataType eq 'Polypeptide'}">
                                <c:set var="blastLinkString" value="${blastLinkString}&sequenceType=pt"/>
                                <c:set var="blastLinkString" value="${blastLinkString}&program=blastp"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="blastLinkString" value="${blastLinkString}&sequenceType=nt"/>
                                <c:set var="blastLinkString" value="${blastLinkString}&program=blastn"/>
                            </c:otherwise>
                        </c:choose>

                        <a href="${blastLinkString}">${blastDB.displayName}</a>
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
