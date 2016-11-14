<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="orthologyPresentationBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.OrthologyPresentationBean" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="showTitle" required="false" type="java.lang.Boolean" %>
<%@ attribute name="hideCounts" required="false" type="java.lang.Boolean" %>
<%@ attribute name="hideDownloadLink" required="false" type="java.lang.Boolean" %>

<c:if test="${empty title && showTitle}">
    <c:set var="title">
        ORTHOLOGY for <zfin:abbrev entity="${marker}"/> (<zfin2:displayLocation entity="${marker}" hideLink="true"/>)
    </c:set>
</c:if>

<c:set var="hideCounts" value="${empty hideCounts ? false : hideCounts}"/>

<zfin2:subsection title="${title}"
                  anchor="orthology"
                  test="${!empty orthologyPresentationBean.orthologs || !empty orthologyPresentationBean.note}"
                  showNoData="true">

    <div ortho-edit gene="${marker.zdbID}" edit="editMode"></div>

</zfin2:subsection>

