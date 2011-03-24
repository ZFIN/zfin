<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="sequenceInfo" type="org.zfin.marker.presentation.SequenceInfo" rtexprvalue="true" required="true" %>

<%-- optionally allow the title to be set --%>
<%@ attribute name="title" type="java.lang.String" required="false"%>
<%@ attribute name="showAllSequences" type="java.lang.Boolean" required="true"%>

<script type="text/javascript" language="JavaScript">
    function jump(form_select){
        if (0 != form_select.selectedIndex){
            window.open(form_select.options[form_select.selectedIndex].value,parseInt(Math.random()*2000000000));
        }
        return true;
    }

</script>


<%-- set the tag to a default value if nothing is passed in --%>
<c:if test="${empty title}">
    <c:set var="title" value="Sequence Information"/>
</c:if>

<c:if test="${empty showAllSequences}">
    <c:set var="showAllSequences" value="true"/>
</c:if>

<%-- Should always have atleast one sequence, so won't ever hide --%>



<zfin2:subsection title="${title}" test="${!empty sequenceInfo}">
    <table class="summary rowstripes">
            <tr>
                <th width="25%"><a href="/action/marker/transcript-definitions#type">Type</a></th>
                <th width="25%"> Accession # </th>
                <th width="15%" style="text-align: right"> Length (bp/aa) </th>
                <th width="35%" style="text-align: center">
                    Analysis <a class="popup-link info-popup-link" href="/ZFIN/help_files/sequence_tools_help.html"></a>
                </th> <%-- filler column, just to take up space --%>
            </tr>

            <c:set var="lastType" value=""/>
            <c:set var="groupIndex" value="0"/>
            <c:forEach var="dblink" items="${sequenceInfo}" varStatus="loop">
                <c:if test="${dblink.referenceDatabase.foreignDBDataType.dataType ne lastType}">
                    <c:set var="groupIndex" value="${groupIndex + 1}"/>
                </c:if>
                <c:if test="${ (showAllSequences) || (!showAllSequences && dblink.referenceDatabase.foreignDBDataType.dataType ne lastType)  }">
                    <zfin:alternating-tr loopName="loop"
                                         groupBeanCollection="${sequenceInfo.list}"
                                         groupByBean="referenceDatabase.foreignDBDataType.dataType">
                        <td>
                            <zfin:groupByDisplay loopName="loop" groupBeanCollection="${sequenceInfo.list}"
                                                 groupByBean="referenceDatabase.foreignDBDataType.dataType">
                                ${dblink.referenceDatabase.foreignDBDataType.dataType} 
                            </zfin:groupByDisplay>
                        </td>
                        <td>
                            <zfin:link entity="${dblink}"/>
                            <zfin:attribution entity="${dblink}"/>
                        </td>
                        <td style="text-align: right">
                            <c:if test="${!empty dblink.length}"> ${dblink.length} ${dblink.units} </c:if>
                        </td>
                        <td  style="text-align: center">
                            <zfin2:externalAccessionBlastDropDown dbLink="${dblink}"/>
                        </td>
                    </zfin:alternating-tr>
                </c:if>
                <c:set var="lastType" value="${dblink.referenceDatabase.foreignDBDataType.dataType}"/>
            </c:forEach>
        </table>
</zfin2:subsection>

