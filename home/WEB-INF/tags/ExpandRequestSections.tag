<%@ tag import="org.zfin.anatomy.AnatomyItem" %>
<%@ tag import="org.zfin.anatomy.DevelopmentStage" %>
<%@ tag import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ tag import="org.zfin.anatomy.presentation.StagePresentation" %>
<%@ tag import="org.zfin.framework.presentation.SectionVisibility" %>
<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ tag import="static org.zfin.framework.presentation.SectionVisibility.Action.SHOW_ALL" %>
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
