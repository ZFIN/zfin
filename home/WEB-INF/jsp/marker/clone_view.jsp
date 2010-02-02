<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker" />


<zfin2:inputWelcome marker="${formBean.clone}"/>

<zfin2:cloneHead clone="${formBean.clone}" />

<zfin2:cloneData clone="${formBean.clone}" isThisseProbe="${formBean.thisseProbe}"/>
 
<zfin2:markerExpression markerExpression="${formBean.markerExpression}" />

<zfin2:markerRelationships marker="${formBean.clone}" relationships="${formBean.markerRelationships}"/>


<div style="display:none;">
<zfin2:transcriptSequenceInformation sequenceInfo="${formBean.sequenceInfo}" title="Supporting Sequences:" showAllSequences="false"/>
</div>
<zfin2:transcriptSequenceInformation sequenceInfo="${formBean.sequenceInfo}" title="Supporting Sequences:" showAllSequences="true"/>

<zfin2:markerSummaryPages  marker="${formBean.marker}" links="${formBean.summaryDBLinkDisplay}"/>

<zfin2:mappingInformation mappedMarker="${formBean.mappedMarkerBean}"/>

<!--http://zfin.org/cgi-bin/webdriver?MIval=aa-showpubs.apg&OID=ZDB-EST-000426-1181&rtype=marker&title=EST+Name&name=fb73a06&abbrev=fb73a06&total_count=2-->

<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.clone}"/>

