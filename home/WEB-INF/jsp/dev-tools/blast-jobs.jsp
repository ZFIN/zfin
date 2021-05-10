<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Blast Jobs">
    <zfin2:blastJobs blastJobs="${formBean}" blastStatistics="${formBean.blastStatistics}"/>
</z:devtoolsPage>
