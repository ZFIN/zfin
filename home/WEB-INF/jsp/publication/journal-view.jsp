<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="authorized" value="no"/>

<authz:authorize access="hasRole('root')">

<c:set var="authorized" value="yes"/>

<zfin2:dataManager zdbID="${journal.zdbID}"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${journal.zdbID}"/>
    </tiles:insertTemplate>
</div>

<div style="text-align: center; font-size: x-large; margin-top: 1em; ">
    ${journal.name}
</div>


<table class="primary-entity-attributes">
    <tr>
        <th>Abbreviation:</th>
        <td>${journal.abbreviation}</td>
    </tr>
    <tr>
        <th>Synonyms:</th>
        <td>
            <c:forEach var="alias" items="${journal.aliases}" varStatus="loop">
            ${alias}<c:if test="${!loop.last}">,</c:if>
            </c:forEach>
        </td>
    </tr>
    <tr>
        <th>Publisher:</th>
        <td>
            <c:if test="${!empty journal.publisher}">${journal.publisher}</c:if>
        </td>
    </tr>
    <tr>
        <th>Print Issn:</th>
        <td>
            <c:if test="${!empty journal.printIssn}">${journal.printIssn}</c:if>
        </td>
    </tr>
    <tr>
        <th>Online Issn:</th>    
        <td>    
            <c:if test="${!empty journal.onlineIssn}">${journal.onlineIssn}</c:if>    
        </td>    
    </tr>  
    <tr>
        <th>NLM ID:</th>
        <td>
            <c:if test="${!empty journal.nlmID}">${journal.nlmID}</c:if>
        </td>
    </tr> 
    <tr>
        <th>Can Reproduce Images?</th>
        <td>
           ${journal.nice ? "yes" : "no"} 
        </td>
    <tr>
       <th>Publications:</th>
       <td>
          <c:forEach var="publication" items="${journal.publications}">
              <zfin:link entity="${publication}" /><br/>
          </c:forEach>
       </td>
    </tr>
</table>

</authz:authorize>

<c:if test="${authorized eq 'no'}"><h3 class="red">You need to log in to see the journal</h3></c:if>
