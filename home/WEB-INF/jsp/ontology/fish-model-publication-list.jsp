<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:citationList pubListBean="${citationList}" url="/action/ontology/fish-model-publication-list/${term.oboID}/${fish.zdbID}?">
    <div class="name-label">
        Disease Name: <a href="/${term.oboID}">${term.termName}</a>
    </div>
    <div class="name-label">
        Fish Name: <a href="/${fish.zdbID}">${fish.name}</a>
    </div>
</zfin2:citationList>
