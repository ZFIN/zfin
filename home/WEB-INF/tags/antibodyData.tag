<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibody" type="org.zfin.antibody.Antibody"
              rtexprvalue="true" required="true" %>

<%@ attribute name="antibodyStat" type="org.zfin.antibody.AntibodyService"
              rtexprvalue="true" required="true" %>

<%-- this tag is intended to be a fragment of the primary entity attributes table of the
     antibody object, so the table tag is defined in antibodyHead.tag --%>

    <tr>
        <th>
            Host Organism:
        </th>
        <td>
            <span id="host organism">${antibody.hostSpecies}</span>
        </td>
    </tr>

    <tr>
        <th>
            Immunogen Organism:
        </th>
        <td>
            <span id="immunogen organism">${antibody.immunogenSpecies}</span>
        </td>
    </tr>

    <tr>
        <th>
            Isotype:
        </th>
        <td>
            ${antibody.heavyChainIsotype}<c:if
                    test="${antibody.heavyChainIsotype != null && antibody.lightChainIsotype != null}">,
            </c:if><font face="symbol">${antibody.lightChainIsotype}</font>
        </td>
    </tr>
    <tr>
        <th>
            Type:
        </th>
        <td>
            <span id="clonal type">${antibody.clonalType}</span>
        </td>
    </tr>
    <tr>
        <th>
            Assays:
        </th>
        <td>
            <c:forEach var="assay" items="${antibodyStat.distinctAssayNames}" varStatus="loop">
                ${assay}
                <c:if test="${!loop.last}">
                    ,&nbsp;
                </c:if>
            </c:forEach>
        </td>
    </tr>
    <tr>
        <th>
            Antigen&nbsp;Genes:
        </th>
        <td>
            <c:forEach var="antigenRel" items="${antibodyStat.sortedAntigenRelationships}" varStatus="loop">
                <zfin:link entity="${antigenRel.firstMarker}"/>
                <c:if test="${antigenRel.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${antigenRel.publicationCount == 1}">
                            (<a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pubview2.apg&OID=${antigenRel.singlePublication.zdbID}">${antigenRel.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="relationship-publication-list?markerRelationship.zdbID=${antigenRel.zdbID}&orderBy=author">${antigenRel.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${!loop.last}">,&nbsp;</c:if>
            </c:forEach>
        </td>
    </tr>

