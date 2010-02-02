<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker"
              rtexprvalue="true" required="true" %>

<div style="float: right">
    <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:put name="subjectName" value="${marker.name}"/>
        <tiles:put name="subjectID" value="${marker.zdbID}"/>
    </tiles:insert>
</div>
