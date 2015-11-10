<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="pubListBean" type="org.zfin.publication.presentation.PublicationListBean" required="true" %>
<%@ attribute name="url" type="java.lang.String" rtexprvalue="true" required="true" %>

<table width=100%>
  <tr>
    <td bgcolor=#cccccc>
      <span class="citation-heading">CITATIONS</span> (${pubListBean.numOfPublications} total)
    </td>
  </tr>
</table>

<jsp:doBody/>

<c:if test="${pubListBean.numOfPublishedPublications > 1}">
  <hr>
  <c:choose>
    <c:when test="${pubListBean.orderBy == 'author'}">
      <input type=button name=resultOrder
             onClick="document.location.replace('${url}orderBy=date')"
             value="Order By Date">
    </c:when>
    <c:otherwise>
      <input type=button name=resultOrder
             onClick="document.location.replace('${url}orderBy=author')"
             value="Order By Author">
    </c:otherwise>
  </c:choose>
</c:if>

<c:if test="${pubListBean.numOfPublishedPublications > 0}">
  <hr>
  <table class="summary rowstripes">
    <c:forEach var="pub" items="${pubListBean.sortedPublishedPublications}" varStatus="loop">
      <zfin:alternating-tr loopName="loop">
        <td>
          <div class="show_pubs">
            <a href="/${pub.zdbID}"
               id="${pub.zdbID}">${pub.authors}&nbsp;(${pub.year})&nbsp;${pub.title}.&nbsp;${pub.journal.abbreviation}&nbsp;<c:if
                      test="${pub.volume != null}">${pub.volume}:</c:if>${pub.pages}</a>
            <authz:authorize access="hasRole('root')"><c:if
              test="${pub.open}">OPEN</c:if><c:if
              test="${!pub.open}">CLOSED</c:if><c:if
              test="${pub.indexed}">, INDEXED</c:if>
            </authz:authorize>
          </div>
        </td>
      </zfin:alternating-tr>
    </c:forEach>
  </table>
</c:if>

<c:if test="${pubListBean.numOfUnpublishedPublications > 0}">
  <hr>
  <b>Other Citations (${pubListBean.numOfUnpublishedPublications}):</b>
  <table class="summary rowstripes">
  <c:forEach var="pub" items="${pubListBean.sortedUnpublishedPublications}"
             varStatus="loop">
    <zfin:alternating-tr loopName="loop">
      <td>
        <div class="show_pubs">
          <a href="/${pub.zdbID}">${pub.authors}&nbsp;(${pub.year})&nbsp;${pub.title}</a>
          <authz:authorize access="hasRole('root')"><c:if
            test="${pub.open}">OPEN</c:if><c:if
            test="${!pub.open}">CLOSED</c:if><c:if
            test="${pub.indexed}">, INDEXED</c:if>
          </authz:authorize>
        </div>
      </td>
    </zfin:alternating-tr>
  </c:forEach>
  </c:if>
</table>