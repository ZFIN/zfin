<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:if test="${!empty zebraShareMetadata}">
    <table class="primary-entity-attributes">
        <tr>
            <th>Submitter:</th>
            <td>${zebraShareMetadata.submitterName} (${zebraShareMetadata.submitterEmail})</td>
        </tr>
        <tr>
            <th>Editors:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty zebraShareEditors}">
                        <c:forEach items="${zebraShareEditors}" var="editor">
                            <div><zfin:link entity="${editor.person}"/></div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="no-data-tag"><i>None</i></span>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>Lab of Origin:</th>
            <td><zfin:link entity="${zebraShareMetadata.labOfOrigin}"/></td>
        </tr>
        <tr>
            <th>Figure Filenames:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty zebraShareFigures}">
                        <c:forEach items="${zebraShareFigures}" var="figure">
                            <div>${figure.img.externalName} &rarr; ${figure.label}</div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="no-data-tag"><i>None</i></span>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </table>
</c:if>


