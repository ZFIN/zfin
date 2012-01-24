<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<c:if test="${formBean.wildCard}">
    <CENTER>
        <TABLE width="98%" class="">
            <TR>
                <TD>
                    <caption class="searchresults" id="Results for emb search">
                        <zfin:collectionSize collectionEntity="${formBean.statisticItems}"/>
                        <zfin:choice collectionEntity="${formBean.statisticItems}" choicePattern="0# Results| 1# Result| 2# Results"
                                     scope="Request"/>

                        <!-- use only for term search. If all structures are listed leave it out -->
                        <c:if test="${formBean.searchTerm != null}">
                            for:<a>
                            <c:out value="${formBean.searchTerm}"/>
                            </a>
                        </c:if>
                    </caption>
                </TD>
            </TR>
            <TR>
                <TD>
                    <tiles:insertTemplate template="/WEB-INF/jsp/anatomy/anatomy_list.jsp" flush="false"/>
                </TD>
            </TR>
        </TABLE>
    </CENTER>
</c:if>

<authz:authorize ifAnyGranted="root">
    <a href="anatomy-expression-search">Expression Report</a>
    ||
    <a href="anatomy-phenotype-search">Phenotype Report</a>
    ||
    <a href="anatomy-go-evidence-search">Go Evidence Report</a>
</authz:authorize>

