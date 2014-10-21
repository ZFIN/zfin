<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="open" type="java.lang.Boolean" required="true" %>
<%@attribute name="facetQuery" type="org.zfin.search.presentation.FacetQuery"%>
<%@attribute name="gaCategory" type="java.lang.String" required="true" %>







<ol class="facet-value-list" id="${name}-facet-value-list">


    <div class="facet-value-outer-box"
         id="${name}-facet-value-outer-box"
    <c:if test="${open == false}">style="display: none"</c:if>
    >


        <div class="single-facet-value-container" id="${name}-facet-value-container">

            <c:choose>
                <%-- not selected --%>
                <c:when test="${!facetQuery.selected}">
                    <li style="min-height:10px" class="facet-value row-fluid">
                        <span style="min-height:10px" class="span9 selectable-facet-value">
                            <a style="padding-left: 31px; padding-right: 2px; min-height:10px"
                               onclick="ga('send', 'event', '${gaCategory}', 'include', '${facetQuery.label}')"
                               href="${facetQuery.url}">
                                ${facetQuery.label}
                           </a>
                        </span>
                        <ul style="min-height:10px"  <%--title="${specialTitle}"--%>
                            class="facet-count-container span3 unstyled">
                            <li class="unstyled"><span class="facet-count">
                                (<fmt:formatNumber value="${facetQuery.count}" pattern="##,###"/>)
                            </span></li>
                        </ul>
                    </li>
                </c:when>
                <%-- selected --%>
                <c:otherwise>
                    <li style="min-height:10px; padding-left: 16px; " class="facet-value row-fluid">
                        <a class="breadbox-link" href="${facetQuery.url}">
                                <%--            <img class="checkbox-icon" src="/images/icon-checked.png">--%>
                            <i class="fa fa-check-square"></i> ${facetQuery.label}
                        </a>
                    </li>
                </c:otherwise>

            </c:choose>




        </div>


    </div>

</ol>
