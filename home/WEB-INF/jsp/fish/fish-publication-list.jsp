<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishPublicationBean" scope="request"/>


<table width=100%>
    <tr>
        <td bgcolor=#cccccc>
            <span class="citation-heading">CITATIONS</span>&nbsp;(${formBean.numOfPublications} total)
        </td>
    </tr>
</table>

<div class="name-label">
        Fish:&nbsp;
        <a href="fish-detail/${formBean.fish.fishID}">${formBean.fish.displayName}</a>
</div>

<form:form commandName="formBean" name="Update Fish Publication List" id="Update Fish Publication List">

    <c:if test="${formBean.numOfPublishedPublications > 1}">
        <c:choose>
            <c:when test="${formBean.orderBy == 'author'}">
                <input type=button name=resultOrder
                       onClick="document.location.replace('fish-publication-list?fishID=${formBean.fish.fishID}&orderBy=date')"
                       value="Order By Date">
            </c:when>
            <c:otherwise>
                <input type=button name=resultOrder
                       onClick="document.location.replace('fish-publication-list?fishID=${formBean.fish.fishID}&orderBy=author')"
                       value="Order By Author">
            </c:otherwise>
        </c:choose>
        &nbsp;&nbsp;&nbsp;
    </c:if>

    <table class="summary rowstripes">
        <c:forEach var="publishedPublication" items="${formBean.sortedPublishedPublications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td align=left>
                    <div class="show_pubs">
                        <a href="/${publishedPublication.zdbID}"
                                id="${publishedPublication.zdbID}">${publishedPublication.authors}
                            &nbsp;(${publishedPublication.year})&nbsp;${publishedPublication.title}.&nbsp;${publishedPublication.journal.abbreviation}&nbsp;<c:if
                                    test="${publishedPublication.volume != null}">${publishedPublication.volume}:</c:if>${publishedPublication.pages}
                        </a><authz:authorize ifAnyGranted="root">&nbsp;&nbsp;&nbsp;<c:if
                            test="${publishedPublication.open}">OPEN</c:if><c:if
                            test="${!publishedPublication.open}">CLOSE</c:if><c:if
                            test="${publishedPublication.indexed}">,&nbsp;INDEXED</c:if></authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>

        <c:if test="${formBean.numOfUnpublishedPublications > 0}">
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td><b>Other Citations (${formBean.numOfUnpublishedPublications}):</b></td>
            </tr>
            <c:forEach var="unpublishedPublication" items="${formBean.sortedUnpublishedPublications}"
                       varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td align=left>
                        <div class="show_pubs">
                            <a href="/${unpublishedPublication.zdbID}">${unpublishedPublication.authors}
                                &nbsp;(${unpublishedPublication.year})&nbsp;${unpublishedPublication.title}
                            </a><authz:authorize ifAnyGranted="root">&nbsp;&nbsp;&nbsp;<c:if
                                test="${unpublishedPublication.open}">OPEN</c:if><c:if
                                test="${!unpublishedPublication.open}">CLOSE</c:if><c:if
                                test="${publishedPublication.indexed}">,&nbsp;INDEXED</c:if></authz:authorize>
                        </div>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </c:if>
    </table>
</form:form>
