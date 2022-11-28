<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    ${formBean.size()} Publications for Fish <zfin:name entity="${fish}"/>
</div>
<div class="popup-body">
    <c:forEach var="publication" items="${formBean}" varStatus="loop">
        <table class="primary-entity-attributes">
            <tr>
                <th>
                        ${publication.authors}, ${publication.title}, <a href="/${publication.zdbID}">${publication.zdbID}</a>
                </th>
            </tr>
        </table>
    </c:forEach>
</div>
