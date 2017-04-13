<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>

<c:if test="${allowDelete}">
  <c:set var="deleteURL">/action/infrastructure/deleteRecord/${publication.zdbID}</c:set>
</c:if>

<c:set var="trackURL">/action/publication/${publication.zdbID}/track</c:set>

<c:set var="linkURL">/action/publication/${publication.zdbID}/link</c:set>

<c:if test="${allowCuration}">
  <c:set var="curateURL">/cgi-bin/webdriver?MIval=aa-curation.apg&OID=${publication.zdbID}</c:set>
</c:if>

<zfin2:dataManager zdbID="${publication.zdbID}"/>

<div style="float: right">
  <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
    <tiles:putAttribute name="subjectName" value="${publication.zdbID}"/>
  </tiles:insertTemplate>
</div>

<div style="text-align: center; font-size: x-large; margin-top: 1em; ">
  ${publication.title}
</div>

<div style="text-align: center; font-weight: bold">
  ${publication.authors}
</div>
