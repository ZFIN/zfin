<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibody" type="org.zfin.antibody.Antibody"
              rtexprvalue="true" required="true" %>

<%@ attribute name="antibodyStat" type="org.zfin.antibody.AntibodyService"
              rtexprvalue="true" required="true" %>

<table>

    <tr>
        <td>
            <b> Host Organism: </b>
        </td>
        <td>
            <span id="host organism">${antibody.hostSpecies}</span>
        </td>
    </tr>

    <tr>
        <td>
            <b> Immunogen Organism: </b>
        </td>
        <td>
            <span id="immunogen organism">${antibody.immunogenSpecies}</span>
        </td>
    </tr>

    <tr>
        <td>
            <b> Isotype: </b>
        </td>
        <td>
            ${antibody.heavyChainIsotype}
            <c:if
                    test="${antibody.heavyChainIsotype != null && antibody.lightChainIsotype != null}">,
            </c:if>
            <font face="symbol">${antibody.lightChainIsotype}</font>
        </td>
    </tr>
    <tr>
        <td>
            <b> Type: </b>
        </td>
        <td>
            <span id="clonal type">${antibody.clonalType}</span>
        </td>
    </tr>
    <tr>
        <td>
            <b> Assays: </b>
        </td>
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
        <td width="180">
            <b><b>Antigen Genes:</b> </b>
        </td>
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
                <c:if test="${!loop.last}">
                    ,&nbsp;
                </c:if>
            </c:forEach>
        </td>
    </tr>
</table>
