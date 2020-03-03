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
            <c:when test="${dbLink.referenceDatabase.foreignDBDataType.dataType.toString() eq 'Polypeptide'}">
                <c:set var="blastLinkString" value="${blastLinkString}&sequenceType=pt"/>
                <c:set var="blastLinkString" value="${blastLinkString}&program=blastp"/>
            </c:when>
            <c:otherwise>
                <c:set var="blastLinkString" value="${blastLinkString}&sequenceType=nt"/>
                <c:set var="blastLinkString" value="${blastLinkString}&program=blastn"/>
            </c:otherwise>
        </c:choose>

        ${dbLink.referenceDatabase.foreignDBDataType.dataType.toString()}
        <a href="${blastLinkString}"
        >Blast at ${blastDB.displayName}</a>
    </c:when>
    <c:when test="${fn:length(dbLink.referenceDatabase.relatedBlastDbs) > 1}">
        <c:set var="domID" value="${zfn:generateRandomDomID()}"/>
        <c:set var="blastLink" value="blast-popup${domID}"/>
        <c:set var="blastLinkPopup" value="blast-links${domID}"/>

        <c:set var="style" value="vertical-align: top; text-align: center;"/>
        <c:if test="${!empty minWidth}">
            <c:set var="style" value="${style} min-width: ${minWidth};"/>
        </c:if>


        <div class="dropdown">
            <a
                    className='btn btn-outline-secondary btn-sm dropdown-toggle'
                    href='#'
                    role='button'
                    data-toggle='dropdown'
                    aria-haspopup='true'
                    aria-expanded='false'
            >
                Select Tool
            </a>
            <div className='dropdown-menu'>
                {dbLink.blastableDatabases.map((blast) => (
                <a className='dropdown-item' href={blast.urlPrefix + dbLink.accessionNumber} key={blast.zdbID}>
                    {blast.displayName}
                </a>
                ))}
            </div>
        </div>


        <script type="text/javascript">
            jQuery(document).ready(function () {
                jQuery('#${blastLink}').click(function () {
                    jQuery("#${blastLinkPopup}").slideToggle(70);
                });
            });
        </script>


    </c:when>
</c:choose>