<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="formBean" type="org.zfin.marker.presentation.MarkerBean" %>
<div class="summary">
    <b>CONSTRUCTS WITH SEQUENCES FROM <i>${formBean.marker.abbreviation}</i></b>
    <c:choose>
        <c:when test="${!empty formBean.constructs}"   >
            <table class="summary">
                <tr>
                    <td align="left">
                        <zfin2:toggledPostcomposedList expressionResults="${formBean.constructs}" maxNumber="5"/>
                    </td>
                </tr>
            </table>
        </c:when>
        <c:otherwise>
            <zfin2:noDataAvailable/>
        </c:otherwise>
    </c:choose>
</div>
