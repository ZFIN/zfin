<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<spring:bind path="formBean">
       <c:choose>
       <c:when test="${status.error}">
            <c:forEach items="${status.errorMessages}" var="error">
              <span class="error"><c:out value="${error}"/><br/></span>
            </c:forEach>
       </c:when>
       <c:otherwise>
    <h3>Candidate Alignments</h3>

        View Candidate: <a href="candidate-view?runCandidate.zdbID=${formBean.runCandidate.zdbID}">
         ${formBean.runCandidate.candidate.suggestedName}
        </a>
        <br/>

        <a name="top"/>
          <c:forEach var="query" items="${formBean.runCandidate.candidateQueryList}" varStatus="queryLoop">
           
<%--              <h3>${formBean.runCandidate.zdbID} (${query.accession.number})  Alignments</h3>--%>
            <c:forEach var="hit" items="${query.blastHits}" varStatus="hitLoop">
            <c:if test="${!hitLoop.first}">
              <a href="#top">top</a> 
              </c:if>
               
                <a name="hit${hit.hitNumber}-${queryLoop.index}"/>

              <br/>

              <b>Alignment <c:out value="${hit.hitNumber}" /> </b>

              Query: 
              <zfin:link entity="${query.accession}"  /> 
              &nbsp;
              &nbsp;
              Subject:
              <zfin:link entity="${hit.targetAccession}"/>

              <zfin:markerRelationLink accession="${hit.targetAccession}"  showParent="true"/> 

              <br/>
              <c:out value="${hit.species}"/> 
              <b>Score:</b> <c:out value="${hit.score}"/>
              <b>Positives:</b> <c:out value="${hit.positivesNumerator}"/> / <c:out value="${hit.positivesDenominator}"/>(<c:out value="${hit.percentAlignment}"/>%)
              <b>Expect:</b> <c:out value="${hit.expectValue}"/> 
              <br/>
              <pre><c:out value="${hit.formattedAlignment}"/></pre>
              <br/>
              <br/>
            </c:forEach>
            <br/>
            <br/>
        </c:forEach>
       </c:otherwise>
       </c:choose>
         
</spring:bind>


