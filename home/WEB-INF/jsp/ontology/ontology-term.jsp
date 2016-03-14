<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<script src="/javascript/table-collapse.js"></script>
<div class="data-page">

    <zfin2:dataManager oboID="${formBean.term.oboID}" termID="${formBean.term.zdbID}" />

    <div style="float: right;">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="${formBean.term.termName}"/>
        </tiles:insertTemplate>
    </div>

    <table class="primary-entity-attributes">

        <tr>
            <th width="5%"><span class="name-label">Term&nbsp;Name:</span></th>
            <td><span class="name-value">${formBean.term.termName}</span></td>
            <td valign="top" align="right" width="5%">
            </td>
            <td rowspan="3" valign="top" align="right" width="5%">
                <span style="font-size: 12px">
                Search Ontology: <zfin2:lookup ontologyName="<%=Ontology.AOGODO.toString()%>"
                                               action="<%= LookupComposite.ACTION_TERM_SEARCH %>"
                                               wildcard="true" useIdAsTerm="true" termsWithDataOnly="false"/>
                    </span>
            </td>
        </tr>
        <tr>

            <th>Synonyms:</th>
            <td id="term-synonyms">
                <zfin2:toggledHyperlinkStrings collection="${formBean.term.sortedAliases}" maxNumber="3"
                                               id="${formBean.term.zdbID}_alias"/>
            </td>
        </tr>

        <tr>
            <th>Definition:</th>
            <c:set var="term" value="${formBean.term}"/>
            <td id="term-definition">${term.definition}
                <zfin2:termDefinitionReferences term="${term}"/>
            </td>
        </tr>
        <c:if test="${formBean.term.ontology.ontologyName == 'zebrafish_anatomy'}">
            <tr>
                <th>Appears&nbsp;at:</th>
                <td>
                    <zfin:link entity="${formBean.term.start}" longVersion="true"/>
                </td>
            </tr>
            <tr>
                <th>Evident&nbsp;until:</th>
                <td>
                    <zfin:link entity="${formBean.term.end}" longVersion="true"/>
                </td>
            </tr>
        </c:if>
        <tr>
            <th>References:</th>
            <td id="term-xrefs">
                <zfin2:toggledHyperlinkList collection="${formBean.term.sortedXrefs}" maxNumber="3"
                                            id="${formBean.term.zdbID}" commaDelimited="true"/>


            </td>

        </tr>
        <tr>
            <th>Ontology:</th>
            <td id="ontology-name">${formBean.term.ontology.commonName}
                <zfin2:ontologyTermLinks term="${formBean.term}"/>
            </td>
        </tr>

        <tr>
            <td></td>
        </tr>

        <c:if test="${formBean.term.obsolete}">
            <tr>
                <th class="red">Obsolete</th>
                <td class="red">true</td>
            </tr>
        </c:if>
    </table>

    <p>

        <c:if test="${!empty formBean.term.images}">

    <div class="summary">
        <c:forEach var="image" items="${formBean.term.images}">
            <zfin:link entity="${image}"/>
        </c:forEach>
    </div>
    </c:if>

    <div class="summary">
        <span class="summaryTitle">Relationships<a class='popup-link info-popup-link'
                                                   href='/action/ontology/note/ontology-relationship'></a></span>
        <table class="summary horizontal-solidblock">
            <c:forEach var="relationshipPresentation" items="${formBean.termRelationships}" varStatus="index">
                <tr id="${fn:replace(relationshipPresentation.type," ","-")}">
                    <th>
                            <%-- keep the relationship types from wrapping --%>
                            ${fn:replace(relationshipPresentation.type," ","&nbsp;")}:
                    </th>
                    <td>
                        <zfin2:createExpandCollapseList items="${relationshipPresentation.items}" id="${index.count}"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>

    <script type="text/javascript">
        function toggle(shortVal, longVal) {
            document.getElementById(shortVal).style.display = 'none';
            document.getElementById(longVal).style.display = 'inline';
        }
    </script>


    <c:if test="${isDiseaseTerm}">
        <div id="genes-involved">
            <zfin2:genesAssociatedWithDisease formBean="${formBean}"/>
        </div>
        <div id="fish-models">
            <zfin2:fishModels term="${formBean.term}" fishModels="${fishModels}"/>
        </div>
    </c:if>

    <c:if test="${formBean.term.ontology.expressionData}">
        <tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_term_detail_expression.jsp" flush="false"/>
    </c:if>

    <tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_term_detail_phenotype.jsp" flush="false"/>

    <zfin2:ExpandRequestSections sectionVisibility="${formBean.sectionVisibility}"/>


    <%--<div class="summary">
        <%// Number of Publications with an abstract that contains the anatomical structure %>
        <A HREF='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubselect2.apg&anon1=pub_abstract&anon1text=<zfin2:urlEncode string="${formBean.term.termName}"/>&query_results=exists'>Search
            for publications with '${formBean.term.termName}' in abstract</A>
    </div>--%>



    <c:if test="${isDiseaseTerm}">
        <zfin-ontology:phenogrid doid="${formBean.term.oboID}"/>
    </c:if>

    <div class="summary">
        <c:choose>
            <c:when test="${numberOfCitations == 0}"><span class="name-label"> CITATIONS:</span> None</c:when>
            <c:otherwise>
                <a href="/action/ontology/disease-publication-list/${term.oboID}">CITATIONS</a> (${numberOfCitations})
            </c:otherwise>
        </c:choose>
    </div>

</div>
<script>
    jQuery(function () {
        jQuery("#genes-involved").tableCollapse({label: ""});
    });
</script>