<%@ page import="org.zfin.marker.Marker" %>
<%@ page import="org.zfin.sequence.reno.Candidate" %>
<%@ page import="org.zfin.marker.MarkerType" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>
<%@ page import="org.zfin.sequence.reno.RunCandidate" %>
<%@ page import="org.zfin.sequence.reno.Run" %>
<%@ taglib prefix="tagunit" uri="http://www.tagunit.org/tagunit/core" %>
<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%
    Marker marker = new Marker();
    marker.setZdbID("ZDB-GENE-081507-1");
    marker.setAbbreviation("fgf8");
    marker.setName("fibroblast growth factor 8 a");
    MarkerType type = new MarkerType();
    type.setType(Marker.Type.GENE);
    Set<Marker.TypeGroup> groups = new HashSet<Marker.TypeGroup>();
    groups.add(Marker.TypeGroup.GENEDOM);
    type.setTypeGroups(groups);
    marker.setMarkerType(type);
    Candidate candidate = new Candidate();
    candidate.setIdentifiedMarker(marker);

    RunCandidate runC = new RunCandidate();
    runC.setCandidate(candidate);
    Run run = new Run();
    run.setZdbID("ZDB_RUN");
    run.setName("Test  RUN");
    runC.setRun(run);
    CandidateBean formBean = new CandidateBean();
    formBean.setRunCandidate(runC);

    pageContext.setAttribute("formBean", formBean, PageContext.PAGE_SCOPE);

%>

<tagunit:assertEquals name="Create a Run Link">
    <tagunit:expectedResult>
        <a href="/action/reno/candidate-inqueue?zdbID=ZDB_RUN">TestRUN</a>
    </tagunit:expectedResult>
    <tagunit:actualResult>
        <zfin:link entity="${formBean.runCandidate.run}"/>
    </tagunit:actualResult>
</tagunit:assertEquals>

