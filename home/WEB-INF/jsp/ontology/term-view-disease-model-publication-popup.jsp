<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    ${formBean.size()} Publications for Fish <zfin:name entity="${fish}"/> with <zfin:name entity="${experiment}"/>
</div>
<div class="popup-body">
    <div class="data-list-container">
        <ul>
            <c:forEach var="publication" items="${formBean}" varStatus="loop">
                <li>
                    <a href="/${publication.zdbID}">${publication.citation}</a>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>
