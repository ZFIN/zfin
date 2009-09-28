<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="clone" type="org.zfin.marker.Clone"
              rtexprvalue="true" required="true" %>

<b>${clone.markerType.name} Name:</b> <zfin:name entity="${clone}"/> <br>
<zfin2:previousNames entity="${clone}"/>

<c:if test="${!empty clone.problem}">
    <b>Clone Problem Type:</b> ${clone.problem}</br>
</c:if>


<zfin2:notes hasNotes="${formBean.marker}"/>


