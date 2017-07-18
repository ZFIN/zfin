<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="phenotypeOnMarkerBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.PhenotypeOnMarkerBean" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="webdriverRoot" required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="title" required="false" %>


<c:if test="${empty title}">
    <c:set var="title"
           value="PHENOTYPE <a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a>"/>
</c:if>

<a name="phenotype"></a>

<div class="summary">
    <span class="summaryTitle">${title}
                    <c:if test="${(empty phenotypeOnMarkerBean || phenotypeOnMarkerBean.numPublications == 0)}">
                        <span class="no-data-tag">No data available</span>
                    </c:if>
                </span>
    <c:if test="${!empty phenotypeOnMarkerBean and phenotypeOnMarkerBean.numPublications>0}">
        <table class="summary horizontal-solidblock">
            <tr>
                <td class="data-label"><b>Data:</b></td>
                <td>
                    <zfin2:markerPhenotypeLink phenotypeOnMarkerBean="${phenotypeOnMarkerBean}" marker="${marker}"/>
                </td>
            </tr>
            <tr>
                <td class="data-label"><b>Observed&nbsp;in:</b></td>
                <td>
                    <zfin2:toggledPostcomposedList entities="${phenotypeOnMarkerBean.anatomy}"
                                                   maxNumber="4"
                                                   numberOfEntities="${fn:length(phenotypeOnMarkerBean.anatomy)}"
                            />
                </td>
            </tr>
        </table>
    </c:if>

</div>

