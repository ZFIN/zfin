<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<div class="modal-header">
    <span class="result-header search-result-name">
        <span class="genedom">${result.name}</span>
        Phenotype
    </span>
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
</div>
<div class="modal-body modal-body-scrolling">
    <table class="phenotype-data-modal">
        <tr>
            <td colspan="2" style="text-align: right;">${allPhenotypeLink}</td>
        </tr>
        <tr>
            <td colspan="2" style="text-align: right;">${allPhenotypeInvolvingLink}</td>
        </tr>
        <tr>
            <th></th>
            <th></th>
        </tr>
        <c:forEach var="entry" items="${phenotypeLinks}">
            <tr>
                <td style="padding-right: 1em;">${entry.key}</td>
                <td>${entry.value}</td>
            </tr>
        </c:forEach>
    </table>
</div>
<div class="modal-footer">
    <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
</div>

