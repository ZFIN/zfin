<%@ page import="org.zfin.ontology.presentation.OntologyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<h1>Ontology Manager</h1>
<span style="width:300px">
</span>

<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>

<table width="100%">
    <tr>
        <td colspan="3">
            Ontologies are loaded from the TERM table and cached within the OntologyManager class in sorted maps for
            fast lookup purposes. There is a <a href="quartz-jobs">quartz job</a> that is configured to reload the
            ontologies nightly to
            ensure that the cache is synchronized with the TERM table.
        </td>
    </tr>
    <c:choose>
        <c:when test="${formBean.ontologiesLoaded}">
            <tr class="search-result-table-header">
                <td width="200" class="sectionTitle">Ontology Name</td>
                <td width="200" class="sectionTitle">Internal Name</td>
                <td width="100" class="sectionTitle">Date of Last Load</td>
                <td class="sectionTitle">Time of Last Load</td>
                <td class="sectionTitle">Loading Duration [s]</td>
                <td class="sectionTitle">Number of Terms</td>
                <td class="sectionTitle">Auto-complete</td>
            </tr>
            <c:forEach var="dataMap" items="${formBean.ontologyManager.loadingData}" varStatus="loop">
                <tr class="search-result-table-entries">
                    <td class="listContentBold">
                        <c:out value='${dataMap.value.ontology.commonName}'/>
                    </td>
                    <td class="listContentBold">
                        <c:out value='${dataMap.value.ontology.ontologyName}'/>
                    </td>
                    <td class="listContentBold">
                        <fmt:formatDate value="${dataMap.value.dateLastLoaded}" pattern="MM/dd/yyyy"/>
                    </td>
                    <td class="listContentBold">
                        <fmt:formatDate value="${dataMap.value.dateLastLoaded}" pattern="hh:mm:ss"/>
                    </td>
                    <td class="listContentBold">
                        <c:out value='${dataMap.value.timeLastLoaded}'/>
                    </td>
                    <td class="listContentBold">
                        <c:out value='${dataMap.value.numberOfTerms}'/>
                    </td>
                    <td>
                        <zfin2:lookup ontologyName="${dataMap.value.ontology.ontologyName}" id="${loop.count}"
                                      wildcard="false"/>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td colspan="7" style="text-align:right;">
                    <iframe src="javascript:''" id='__gwt_historyFrame'
                            style='position:absolute;width:0;height:0;border:0'></iframe>
                    <table style="width:550px; overflow: auto; border: 0.1px; border-style:solid">
                        <tr>
                            <td>
                                <div id="term-info"></div>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <p/>
                    <a href="?action=<%= OntologyBean.ActionType.LOAD_FROM_DATABASE%>">
                        Re-load </a> from database again.<br/>
                </td>
            </tr>
        </c:when>
        <c:otherwise>
            <tr>
                <td>
                    Ontologies are not loaded yet.<br/>
                    <a href="?action=<%= OntologyBean.ActionType.LOAD_FROM_SERIALIZED_FILE%>">
                        Load </a> from a serialized file.<br/>
                    <a href="?action=<%= OntologyBean.ActionType.LOAD_FROM_DATABASE%>">
                        Load </a> from a the database.<br/>
                </td>
            </tr>
        </c:otherwise>
    </c:choose>
</table>
<p/>
<a href="?action=<%= OntologyBean.ActionType.SERIALIZE_ONTOLOGIES%>">Serialize Ontologies</a> (create file)
