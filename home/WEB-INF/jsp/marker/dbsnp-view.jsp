<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<span class="name-label">Clone Name: <zfin:link entity="${formBean.marker}"/></span>
<p>
<strong>Note:</strong>
The list of reference SNPs mapped on this genomic clone has been retrieved through data exchange between NCBI and ZFIN. These reference SNP identifiers are created by NCBI during periodic 'builds' of the dbSNP database. SNP details can be obtained by submitting a query to dbSNP. Click the Batch Query button at the bottom of the page and follow instructions at dbSNP to get details
</p>
<p>${dbsnps}</p>

<form ACTION="http://www.ncbi.nlm.nih.gov/projects/SNP/dbSNP.cgi" method=post target ="_blank">
    <input type="hidden" name="organism" value="zebrafish_7955">
    <input type="hidden" name="result_option" value="RSR">
    <input type="hidden" name=list value="rslist">
    <INPUT TYPE="hidden" name="subnum" value="${dbsnps}">
    <input name="query" type="submit" value="Batch Query">
</form>

