<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataList hasData="${!empty formBean.constructs}">
    <c:forEach var="construct" items="${formBean.constructs}">
        <li>
            <zfin:link entity="${construct}"/>
        </li>
    </c:forEach>
</z:dataList>


