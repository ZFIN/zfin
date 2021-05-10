<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.sequence.blast.presentation.XMLBlastBean" %>

Processing blast result: <a href="/action/blast/blast-view?resultFile=${formBean.ticketNumber}">${formBean.ticketNumber}</a>
<br>
<span style="font-size:small;">The browser will automatically check for results in ${formBean.refreshTime} seconds.</span> 
<meta http-equiv="refresh" content="${formBean.refreshTime};url=/action/blast/blast-view?resultFile=${formBean.resultFile.name}">
