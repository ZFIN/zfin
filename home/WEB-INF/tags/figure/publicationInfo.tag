<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publication" type="org.zfin.publication.Publication" rtexprvalue="true" required="true" %>
<%@ attribute name="submitters" type="java.util.List" rtexprvalue="true" required="false" %>
<%@ attribute name="showThisseInSituLink" type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@ attribute name="showErrataAndNotes" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<div class="publication-info">
    <zfin:link entity="${publication}"/>
    - ${publication.title}.
    <c:if test="${!empty publication.journal && !empty publication.journal.name}"> ${publication.journal.name} </c:if>
    <c:if test="${!empty publication.volume}">
        &nbsp; <%-- this ends up creating two spaces...I think an output filter is messing with the spacing...--%>
        ${publication.volume}<c:if test="${!empty publication.pages}">:${publication.pages}</c:if>
    </c:if>

    <zfin-figure:journalAbbrev publication="${publication}"/>
</div>

<%-- this needs to be wrapped by the extra test because unlink other subsections,
     if there's no data, we don't want the label --%>
<c:if test="${!empty submitters}">
    <zfin2:subsection title="Submitted By" additionalCssClass="submitted-by" test="${!empty submitters}" inlineTitle="true" showNoData="false">
        <zfin:link entity="${submitters}"/>
        <a style="margin-left: 5em"
           class="citing-this-work-link"
           href=/action/publication/search/printable?zdbID=${publication.zdbID}>(Citing this work)</a>
    </zfin2:subsection>
</c:if>

<c:if test="${showErrataAndNotes}">
    <div class="summary">
        ${publication.errataAndNotes}
    </div>
</c:if>


<c:if test="${showThisseInSituLink}">
    <div class="summary">
       <a class="thisse-protocol-link" href="/ZFIN/Methods/ThisseProtocol.html"><b>Thisse <i>in situ</i> hybridization protocol</b></a>
    </div>
</c:if>

