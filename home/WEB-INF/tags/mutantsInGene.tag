<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="mutantsOnMarkerBean" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.MutantOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="MUTANTS AND TARGETED KNOCKDOWNS"/>
</c:if>

<zfin2:subsection title="${title}"
                        test="${!empty mutantsOnMarkerBean and (!empty mutantsOnMarkerBean.alleles or !empty mutantsOnMarkerBean.knockdownReagents)}" showNoData="true">
    <table class="summary horizontal-solidblock">

        <c:if test="${!empty mutantsOnMarkerBean.genotypeList}">
            <tr>
                <td class="data-label"><b>Mutant lines:</b> </td>
                <td>
                    <c:set var="numberOfGenotypes" value="${mutantsOnMarkerBean.genotypeList.size()}"/>
                        <c:choose>
                            <c:when test="${numberOfGenotypes == 1}">
                                <zfin:link entity="${mutantsOnMarkerBean.genotypeList.get(0)}"/>
                            </c:when>
                            <c:otherwise>
                                <a href="/action/mutant/mutant-list?zdbID=${zdbID}">${numberOfGenotypes} Genotypes</a>
                            </c:otherwise>
                        </c:choose>
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty mutantsOnMarkerBean.alleles}">
            <tr>
                <td class="data-label"><b>Alleles:</b> </td>
                <td>
                    <zfin2:toggledPostcomposedList entities="${mutantsOnMarkerBean.alleles}"
                                                   maxNumber="5"
                                                   suppressPopupLinks="true"
                                                   numberOfEntities="${fn:length(mutantsOnMarkerBean.alleles)}"
                            />
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty mutantsOnMarkerBean.knockdownReagents}">
            <tr>
                <td class="data-label"><b>Knockdown reagents:</b> </td>
                <td>
                    <zfin2:toggledProvidesLinkList collection="${mutantsOnMarkerBean.knockdownReagents}" maxNumber="5"/>
                </td>
            </tr>
        </c:if>
    </table>

</zfin2:subsection>

