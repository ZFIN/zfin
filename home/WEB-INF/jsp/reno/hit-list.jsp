<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>

        <a name="top"/>
      <h3>Query Hits</h3>
          <c:forEach var="query" items="${formBean.queries}" varStatus="queryLoop">
          <a href="#top">top</a>
          <br/>
            <c:forEach var="hit" items="${query.blastHits}" varStatus="hitLoop">
            <a name="<c:out value='${hit.zdbID}' />"/>
              <b>Alignment: <c:out value="${hitLoop.index+1}" /></b>
              <br/>
              <c:out value="${hit.species}"/> 
              <b>Score:</b> <c:out value="${hit.score}"/>
              <b>Positives:</b> <c:out value="${hit.positivesNumerator}"/> / <c:out value="${hit.positivesDenominator}"/>
              <b>Expect:</b> <c:out value="${hit.expectValue}"/> 
              <br/>
              <%--<pre>--%>
               ${hit.formattedAlignment}
              <%--<c:forEach var="alignment" items="${hit.formattedAlignment}">--%>
                  <%--<c:out value="${alignment}"/> --%>
              <%--</c:forEach>--%>
              <%--</pre>--%>
              <br/>
              <br/>
            </c:forEach>
            <br/>
            <br/>
        </c:forEach>


