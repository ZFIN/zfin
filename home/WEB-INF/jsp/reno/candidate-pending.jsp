<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:choose>
   <c:when test='${formBean.run == null}'>
      <tr>
           <td><font size=+1 color="red" face="Arial">${formBean.zdbID} is not a valid run zdbID</font></td>
           <td><a href="/action/reno/run-list">View All Runs</a></td>
      </tr>
   </c:when>

   <c:otherwise>

<h3>Pending Curation</h3>
<table width=100%>
    <tr>
        <td>Run Name: ${formBean.run.name}</td>
        <td><a href="/action/reno/run-list">View All Runs</a></td>
    </tr>
</table>
<table>
    <tr>
        <td>Number of Finished: ${formBean.run.finishedCandidateCount}</td>
    </tr>
    <tr>
        <td>Number Pending: ${formBean.run.pendingCandidateCount}</td>
    </tr>
    <tr>
        <td>Number in Queue:
            <a href="/action/reno/candidate/inqueue/${formBean.run.zdbID}">
                ${formBean.run.queueCandidateCount}
            </a></td>
    </tr>
</table>
<br>
<table class="searchresults">
    <tr style="background: #ccc">

        <th></th>
        <th>Candidate Gene</th>
        <th>Curator</th>
    </tr>
    <c:forEach var="rc" items="${formBean.runCandidates}" varStatus="loop">

        <zfin:alternating-tr loopName="loop">
            <td>
              <a href="/action/reno/candidate-view/${rc.zdbID}">
		        <c:if test="${rc.owner}">
		           <img src=/images/continue.gif height=47 border=0>
		        </c:if>
		        <c:if test="${!rc.owner}">
		           <img src=/images/glasses.jpg height=25 border=0>
		        </c:if>
              </a>
            </td>
            <td>
                <c:if test="${formBean.run.nomenclature}">
                    <zfin:link entity="${rc}" />
                </c:if>
                <c:if test="${formBean.run.redundancy}">
                    <zfin:name entity="${rc}" /> 
                </c:if>
            </td>

            <td>${rc.lockPerson.name}</td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

   </c:otherwise>
 </c:choose>
