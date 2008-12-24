<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%" bgcolor="#CCCCCC">
    <tbody>
        <tr>
            <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                Orthology Search Results
            </span>
            </td>
        </tr>
    </tbody>
</table>

<%--
<tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false"/>
--%>

<hr>

<a href="search"> New Search</a>

<tiles:insert page="/WEB-INF/jsp-include/pagination_header.jsp" flush="false"/>

<table width="100%">
    <center>
        <tr>
            <td colspan="3">
                <TABLE width="100%" border=0>
                    <TR bgcolor=#ccccc0>
                        <TD><b>Zebrafish Gene</b></TD>
                        <TD><b>Species</b></TD>
                        <TD><b>Symbol</b></TD>
                        <TD><b>Addtional Resources</b></TD>
                        <TD><b>Chromosome (Position)</b></TD>
                        <TD><b>Details</b></TD>
                    </TR>
                    <c:forEach var="orth" items="${formBean.orthologies}" varStatus="rowCounter">
                        <c:forEach var="speciesItem" items="${orth.orthologSpecies}">
                            <c:choose>
                                <c:when test="${rowCounter.count % 2 != 0}">
                                    <tr class="odd">
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                </c:otherwise>
                            </c:choose>
                            <TD>
                                    <%--                                <c:if test="${speciesItem.species.name == 'Zebrafish'}">--%>
                                <c:if test="${speciesItem.species == 'Zebrafish'}">
                                    <zfin:link entity="${speciesItem.marker}"/>
                                </c:if>
                            </TD>
                            <TD>
                                    ${speciesItem.species}
                            </TD>
                            <c:forEach var="orthItem" items="${speciesItem.items}">
                                <td>
                                    <c:if test="${orthItem.symbol != null}">
                                        ${orthItem.symbol}
                                    </c:if>
                                    <c:if test="${orthItem.symbol == null}">
                                        &nbsp;
                                    </c:if>
                                </td>
                                <td>
                                    <c:if test="${orthItem.accessionItems != null}">
                                        <c:forEach var="accessionItem" items="${orthItem.accessionItems}">
                                            <a href="${accessionItem.url}">
                                                    ${accessionItem.name}
                                            </a>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${orthItem.accessionItems == null}">
                                        &nbsp;
                                    </c:if>
                                </td>
                                <td>
                                    <c:if test="${orthItem.chromosomes != null}">
                                        <zfin:createDelimitedList collectionEntity="${orthItem.chromosomes}"
                                                                  delimiter=","/>

                                        <%
                                            /** This tag needs to be expanded to support a "type" attribute which allows it to be used for any
                                             bean/collection combination.  Right now, it only works for OrthologyItem.  See the logic:iterate
                                             source code for an example of how to use the type parameter in the tag class.  Also, the
                                             collectionName attribute is currently required for this tag, but it is not used in the current
                                             tag class.  This attribute will be necessary in the future in order to generalize the tag.
                                             */
                                        %>

                                    </c:if>
                                    <c:if test="${orthItem.chromosomes == null}">
                                        &nbsp;
                                    </c:if>
                                </td>
                                <td>
                                    <c:if test="${speciesItem.species == 'Zebrafish'}">
                                        <zfin:link entity="${speciesItem}"/>
                                    </c:if>
                                </td>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                    </c:forEach>
                </TABLE>
                <hr/>
            </td>
        </tr>
    </center>
</table>

<zfin2:pagination paginationBean="${formBean}" />

<%--
<tiles:insert page="/WEB-INF/jsp/orthology/orthology_search.jsp" flush="true"/>
--%>
