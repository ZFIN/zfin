<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="bean" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.PhenotypeOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="webdriverRoot" required="true" rtexprvalue="true" type="java.lang.String" %>

<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="PHENOTYPE <a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a>"/>
</c:if>

<zfin2:subsection title="${title}"
                        test="${!empty bean and bean.numPublications>0}" showNoData="true">

    <table class="summary horizontal-solidblock">
        <tr>
            <td class="data-label"><b>Data:</b> </td>
            <td>
                <c:choose>
                    <c:when test="${bean.numFigures==1}">
                        ${bean.singleFigureLink.link}
                    </c:when>
                    <c:otherwise>
                        <a
                                href="
        /${webdriverRoot}?MIval=aa-pheno_summary.apg&OID=${marker.zdbID}
    "
                                >
                                ${bean.numFigures} figures</a>
                    </c:otherwise>
                </c:choose>
                 from
                <c:choose>
                    <c:when test="${bean.numPublications==1}">
                        ${bean.singlePublicationLink.link}
                    </c:when>
                    <c:otherwise>
                        ${bean.numPublications} publications
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td class="data-label"><b>Observed&nbsp;in:</b></td>
            <td>
                <zfin2:toggledPostcomposedList expressionResults="${bean.anatomy}"
                                               maxNumber="4"
                        />
            </td>
        </tr>
    </table>

</zfin2:subsection>

