<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="bean" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.MutantOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="MUTANTS AND TARGETED KNOCKDOWNS"/>
</c:if>

<zfin2:subsectionMarker title="${title}"
                        test="${!empty bean and (!empty bean.alleles or !empty bean.knockdownReagents)}" showNoData="true">
    <table class="summary horizontal-solidblock">

        <c:if test="${!empty bean.mutantLineDisplay}">
            <tr>
                <td class="data-label"><b>Mutant lines:</b> </td>
                <td>
                        ${bean.mutantLineDisplay}
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty bean.alleles}">
            <tr>
                <td class="data-label"><b>Alleles:</b> </td>
                <td>
                    <zfin2:toggledPostcomposedList expressionResults="${bean.alleles}"
                                                   maxNumber="5"
                                                   suppressPopupLinks="true"
                            />
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty bean.knockdownReagents}">
            <tr>
                <td class="data-label"><b>Knockdown reagents:</b> </td>
                <td>
                    <zfin2:toggledProvidesLinkList collection="${bean.knockdownReagents}" maxNumber="5"/>
                </td>
            </tr>
        </c:if>
    </table>

</zfin2:subsectionMarker>

