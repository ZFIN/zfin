<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin2:dataManager zdbID="${formBean.anatomyItem.zdbID}"
                   oboID="${formBean.anatomyItem.oboID}"
                   termID="${formBean.aoTerm.zdbID}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="anatomy_item"/>

<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.anatomyItem.termName}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.anatomyItem.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:anatomy-view-header-info formBean="${formBean}" showSearchBox="true" linkStageTerms="true"/>

<c:if test="${!empty formBean.anatomyItem.images}">
    <div class="summary">
        <c:forEach var="image" items="${formBean.anatomyItem.images}">
            <zfin:link entity="${image}"/>
        </c:forEach>
    </div>
</c:if>

<div class="summary">
    <!-- Relationships -->
    <span class="summaryTitle">Relationships</span> (<a href="/zf_info/ontology_relationship_info.html">about</a>)
    <table class="summary horizontal-solidblock">

        <c:forEach var="rt" items="${formBean.relations}">
            <tr>
                <th>
                        <%-- keep the relationship types from wrapping --%>
                        ${fn:replace(rt.type," ","&nbsp;")}:

                </th>
                <TD>
                    <c:forEach var="session" items="${rt.items}">
                        <zfin:link entity="${session}" name="anatomy-visibility"/> &nbsp;
                        &nbsp;
                    </c:forEach>
                </TD>
            </TR>
        </c:forEach>
    </TABLE>
</div>

<%--   If you would like to display the Show All link 
<zfin2:sectionVisibilityShowAll sectionVisibility="${formBean.sectionVisibility}"
                                enumeration="${formBean.sectionVisibility.sectionsWithData}"/>
<p/>
--%>
<tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_term_detail_expression.jsp" flush="false"/>

<tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_term_detail_phenotype.jsp" flush="false"/>

<zfin2:ExpandRequestSections sectionVisibility="${formBean.sectionVisibility}"/>

<div class="summary">
    <%// Number of Publications with an abstract that contains the anatomical structure %>
    <A HREF='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubselect2.apg&anon1=pub_abstract&anon1text=<zfin2:urlEncode string="${formBean.anatomyItem.termName}"/>&anon1textAllOneWord=1&query_results=exists'>Search
        for publications with '${formBean.anatomyItem.termName}' in abstract</A>
</div>

