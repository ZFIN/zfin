<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="journal" class="org.zfin.publication.Journal" scope="request"/>

<z:attributeList>

    <z:attributeListItem label="ID">
        ${journal.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Abbreviation">
        ${journal.abbreviation}
    </z:attributeListItem>

    <z:attributeListItem label="Synonyms">
        <c:forEach var="alias" items="${journal.aliases}" varStatus="loop">
            ${alias}<c:if test="${!loop.last}">, </c:if>
        </c:forEach>
    </z:attributeListItem>

    <z:attributeListItem label="Publisher">
        <c:if test="${!empty journal.publisher}">${journal.publisher}</c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Print Issn">
        <c:if test="${!empty journal.printIssn}">${journal.printIssn}</c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Online Issn">
        <c:if test="${!empty journal.onlineIssn}">${journal.onlineIssn}</c:if>
    </z:attributeListItem>

    <z:attributeListItem label="NLM ID">
        <c:if test="${!empty journal.nlmID}">${journal.nlmID}</c:if>
    </z:attributeListItem>

    <z:attributeListItem label="Can Reproduce Images">
        ${journal.nice ? "yes" : "no"}
    </z:attributeListItem>

</z:attributeList>

