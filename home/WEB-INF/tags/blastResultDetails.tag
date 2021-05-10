<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="blastResult" type="org.zfin.sequence.blast.results.view.BlastResultBean" rtexprvalue="true" required="true" %>
<strong>expect:</strong> ${blastResult.xmlBlastBean.expectValue} <br>
<c:if test="${!empty blastResult.xmlBlastBean.matrix}">
    <strong>matrix:</strong> ${blastResult.xmlBlastBean.matrix } <br>
</c:if>
<strong>word length:</strong> ${blastResult.xmlBlastBean.wordLength} <br>
<c:if test="${!empty blastResult.filter}">
    <strong>filter:</strong> ${blastResult.filterView} <br>
</c:if>

