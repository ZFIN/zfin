<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.ontology.OntologyManager" %>
<%@ page import="org.zfin.ontology.presentation.OntologyBean" %>
<%@ page import="org.zfin.ontology.Ontology" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<h1>Ontology Manager</h1>
<span style="width:300px">
</span>

Ontologies loaded and available for use: (${fn:length(formBean.ontologyManager.ontologyMap)} of <%=OntologyManager.NUMBER_OF_SERIALIZABLE_ONTOLOGIES%>)
<script type="text/javascript" src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js"></script>

<table width="100%">
    <tr>
        <td colspan="3">
            Ontologies are loaded from the TERM table and cached within the OntologyManager class in sorted maps for
            fast lookup purposes. There is a <a href="quartz-jobs">quartz job</a> that is configured to reload the
            ontologies nightly to
            ensure that the cache is synchronized with the TERM table.

            Has ontology manager: ${empty formBean.ontologyManager}
        </td>
    </tr>
    <c:choose>
        <c:when test="${formBean.ontologiesLoaded}">
            <tr class="search-result-table-header left-top-aligned">
                <td width="200" class="sectionTitle">Ontology Name</td>
                <td width="200" class="sectionTitle">Internal Name</td>
                <td width="100" class="sectionTitle">Date of Last Load</td>
                <td class="sectionTitle">Time of Last Load</td>
                <td class="sectionTitle">Loading Duration [s]</td>
                <td class="sectionTitle">Total Number of Terms</td>
                <td class="sectionTitle">Number of Active Terms</td>
                <td class="sectionTitle">Number of Obsoleted Terms</td>
                <td class="sectionTitle">Number of Distinct Aliases</td>
                <td class="sectionTitle">Number of Lookup Keys</td>
                <td class="sectionTitle">Number of Unique Values</td>
                <td class="sectionTitle">Number of Distinct Relations</td>
                <td class="sectionTitle">Auto-complete</td>
            </tr>

            <c:forEach var="value" items="${formBean.ontologyManager.loadingData}" varStatus="loop">
                <c:if test="${zfn:isOntologyLoaded(formBean.ontologyManager,value.value.ontology)}">
                    <tr class="search-result-table-entries left-top-aligned">
                        <td class="listContentBold">
                                ${value.value.ontology.commonName}
                        </td>
                        <td class="listContentBo
                    ld">
                            <c:forEach var="individualOntologyName"
                                       items="${value.value.ontology.individualOntologies}">
                                ${individualOntologyName.ontologyName}<br/>
                            </c:forEach>
                        </td>
                        <td class="listContentBold">
                            <fmt:formatDate value="${value.value.dateLastLoaded}" pattern="MM/dd/yyyy"/>
                        </td>
                        <td class="listContentBold">
                            <fmt:formatDate value="${value.value.dateLastLoaded}" pattern="hh:mm:ss"/>
                        </td>
                        <td class="listContentBold">
                                ${value.value.timeLastLoaded}
                        </td>
                        <td class="listContentBold">
                            <fmt:formatNumber value="${value.value.lastLoad.totalNumberOfTerms}" />
                        </td>
                        <td class="listContentBold">
                            <a href="/action/ontology/terms?action=<%= OntologyBean.ActionType.SHOW_ALL_TERMS%>&ontologyName=${value.value.ontology.ontologyName}"
                               target="term-window">
                               <fmt:formatNumber value="${value.value.lastLoad.numberOfTerms}" /></a>
                        </td>
                        <td class="listContentBold">
                            <a href="/action/ontology/terms?action=<%= OntologyBean.ActionType.SHOW_OBSOLETE_TERMS%>&ontologyName=${value.value.ontology.ontologyName}"
                               target="term-window">
                               <fmt:formatNumber value="${value.value.lastLoad.numberOfObsoletedTerms}" /></a>
                        </td>
                        <td class="listContentBold">
                            <a href="/action/ontology/terms?action=<%= OntologyBean.ActionType.SHOW_ALIASES%>&ontologyName=${value.value.ontology.ontologyName}"
                               target="term-window">
                               <fmt:formatNumber value="${value.value.lastLoad.numberOfAliases}" /></a>
                        </td>
                        <td class="listContentBold">
                            <a href="/action/ontology/terms?action=<%= OntologyBean.ActionType.SHOW_KEYS%>&ontologyName=${value.value.ontology.ontologyName}"
                               target="term-window">
                               <fmt:formatNumber value="${value.value.lastLoad.numberOfKeys}" />
                            </a>
                        </td>
                        <td class="listContentBold">
                            <a href="/action/ontology/terms?action=<%= OntologyBean.ActionType.SHOW_VALUES%>&ontologyName=${value.value.ontology.ontologyName}"
                               target="term-window">
                               <fmt:formatNumber value="${value.value.lastLoad.numberOfValues}" />
                               </a>
                        </td>
                        <td class="listContentBold">
                            <a href="/action/ontology/terms?action=<%= OntologyBean.ActionType.SHOW_RELATIONSHIP_TYPES%>&ontologyName=${value.value.ontology.ontologyName}"
                               target="term-window">
                               <c:out value="${fn:length(zfn:getDistinctRelationshipTypes(formBean.ontologyManager ,value.value.ontology))}" />
                               </a>
                        </td>
                        <td>
                            <zfin2:lookup ontology="${value.value.ontology}" id="${loop.count}" showTermDetail="true"
                                          wildcard="false"/>
                        </td>
                    </tr>
                </c:if>
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
                    <p></p>
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
<p></p>
<a href="?action=<%= OntologyBean.ActionType.SERIALIZE_ONTOLOGIES%>">Serialize Ontologies</a>
