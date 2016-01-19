<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:citationList pubListBean="${citationList}"
                    url="/action/ontology/fish-model-publication-list/${term.oboID}/${fish.zdbID}?">

    <table class="primary-entity-attributes">
        <tr>
            <th>Disease Name</th>
            <td><a href="/${term.oboID}">${term.termName}</a></td>
        </tr>
        <tr>
            <th>Fish Name</th>
            <td><a href="/${fish.zdbID}">${fish.name}</a></td>
        </tr>
        <c:if test="${environmentKey != null}">
            <tr>
                <th>Environment</th>
                <td>${environmentKey}</td>
            </tr>
        </c:if>
    </table>

</zfin2:citationList>
