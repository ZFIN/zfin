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
    The list of reference SNPs mapped on this genomic clone has been retrieved through data exchange between NCBI and ZFIN. These reference SNP identifiers were created by NCBI during periodic 'builds' of the dbSNP database. NCBI has phased out support for non-human organisms in dbSNP and dbVar. Zebrafish SNP details can be obtained from the archive directory <a href="ftp://ftp.ncbi.nih.gov/snp/archive">ftp://ftp.ncbi.nih.gov/snp/archive</a>.
</p>
<p>${dbsnps}</p>


