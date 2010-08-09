<%
    /*
    This tag is used to expand those sections that are marked as visible in the form bean.
    */
%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="sectionVisibility" type="org.zfin.framework.presentation.SectionVisibility" required="true" %>

<%-- Auto-display the sections that were visible when this page is called. --%>
<script type="text/javascript">
    <c:forEach var="sectionName" items="${sectionVisibility.visibleSections}">
    show_${sectionName}();
    </c:forEach>
</script>
