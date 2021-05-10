<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <div class="search-form-top-bar">
        <span class="search-form-title">
            CITATIONS
        </span>
    </div>

    <p/>
    <table class="summary rowstripes">
        <caption>Term:
            <small><zfin:link entity="${term}"/></small>
        </caption>
        <c:forEach var="reference" items="${term.definitionReferences}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td align=left>
                    <zfin2:termDefinitionReference term="${term}" reference="${reference}"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</z:page>