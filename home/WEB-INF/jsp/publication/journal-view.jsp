<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<zfin2:dataManager zdbID="${journal.zdbID}"
                   rtype="publication"/>

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
</table>
