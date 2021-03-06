<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
        <tr align="center">
            <td><font size="-1"> <b>ZFIN ID:</b> ${formBean.anatomyItem.zdbID} </font>
            </td>
            <td>
                &nbsp;
            </td>
        </tr>
    </tbody>
</table>

<table width="100%">
    <tbody>
        <tr bgcolor="#cccccc">
            <td>
                <font size="+1"><b>Anatomy: All Antibodies</b></font>
            </td>
        </tr>
    </tbody>
</table>

<table border="0" width="100%">
    <tbody>
        <tr>
            <td>
                <font>
                    <b>Anatomy Term: <zfin:link entity="${formBean.anatomyItem}"/> </b>
                </font>
            </td>
        </tr>
    </tbody>
</table>

<TABLE width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Antibody
            </TD>
            <TD width="20%">
                Gene Symbol
            </TD>
            <TD width="60%">
                Figures
            </TD>
        </TR>
        <c:if test="${!formBean.antibodiesExist}">
            <tr>
                <td colspan="4">No data available</td>
            </tr>
        </c:if>
        <c:if test="${formBean.antibodiesExist}">
            <c:forEach var="antibodyStats" items="${formBean.antibodyStatistics}">
                <tr class="search-result-table-entries">
                    <td>
                        <zfin:link entity="${antibodyStats.antibody}"/>
                    </td>
                    <td>
                        <zfin:link entity="${antibodyStats.antibody.allRelatedMarker}"/>
                    </td>
                    <td>
                        <c:if test="${antibodyStats.numberOfFigures > 0}">
                            <!-- link to figure search page if more than one figure available-->
                            <c:if test="${antibodyStats.numberOfFigures > 1}">
                                <a href='/action/antibody/figure-summary?anatomyItem.zdbID=${formBean.anatomyItem.zdbID}&antibody.zdbID=${antibodyStats.antibody.zdbID}'>
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${antibodyStats.numberOfFigures}"
                                                 includeNumber="true"/>
                                </a>
                            </c:if>
                            <!-- If only one figure available go directly to the figure page -->
                            <c:if test="${antibodyStats.numberOfFigures == 1}">
                                <a href='/${antibodyStats.figure.zdbID}'>
                                    <zfin2:figureOrTextOnlyLink figure="${antibodyStats.figure}" />
                                </a>
                            </c:if>
                            from
                            <c:if test="${antibodyStats.numberOfPublications ==1}">
                                <zfin:link entity="${antibodyStats.singlePublication}"/>
                            </c:if>
                            <c:if test="${antibodyStats.numberOfPublications > 1}">
                                <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                             collectionEntity="${antibodyStats.publications}"
                                             includeNumber="true"/>
                            </c:if>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </c:if>
    </tbody>
</TABLE>
