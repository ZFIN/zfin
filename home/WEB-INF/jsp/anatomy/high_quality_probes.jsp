<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<table bgcolor="#eeeeee" border="0" width="100%">
    <tbody>
    <tr align="center">
        <td><font size="-1">&nbsp;</font>
        </td>
    </tr>
    </tbody>
</table>

<TABLE width="100%">
    <TR>
        <TD bgcolor="#CCCCCC" colspan="3">
            <STRONG>
                In-Situ Probes for <i>
                <zfin:link entity="${formBean.aoTerm}"/>
            </i></STRONG>: &nbsp; <a
                href="/zf_info/stars.html">Recommended</a>
            by
            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-labview.apg&OID=ZDB-LAB-980204-15'>
                Thisse lab</a></TD>
    </TR>
</table>
<table width="100%">
    <tr>
        <td bgcolor="#EEEEEE" valign=top align=left width=110>
            Gene Symbol
        </td>
        <td bgcolor="#EEEEEE">Probe Name <br/>
        </td>
        <td bgcolor="#EEEEEE">Gene Expression Data<br/>
        </td>
    </tr>
    <c:forEach var="probeStats" items="${formBean.highQualityProbeGenes}" varStatus="rowCounter">
        <c:choose>
            <c:when test="${rowCounter.count % 2 != 0}">
                <tr class="odd">
            </c:when>
            <c:otherwise>
                <tr>
            </c:otherwise>
        </c:choose>
        <td>
            <zfin:link entity="${probeStats.genes}"/>
        </td>
        <td>
            <zfin:link entity="${probeStats.probe}"/>
        </td>
        <td>
            <c:if test="${probeStats.numberOfFigures > 0}">
                <!-- link to figure search page if more than one figure available-->
                <c:if test="${probeStats.numberOfFigures > 1}">
                    <zfin:createFiguresLink marker="${probeStats.probe}" term="${formBean.aoTerm}"
                                            numberOfFigures="${probeStats.numberOfFigures}" author="Thisse"
                                            useGeneZdbID="false"/>
                    (<zfin:choice choicePattern="0#images| 1#image| 2#images"
                                  integerEntity="${probeStats.numberOfImages}"
                                  includeNumber="true"/>)
                </c:if>
                <!-- If only one figure available go directly to the figure page -->
                <c:if test="${probeStats.numberOfFigures == 1}">
                    <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${probeStats.figure.zdbID}'>
                        <zfin2:figureOrTextOnlyLink figure="${probeStats.figure}"
                                                    integerEntity="${probeStats.numberOfFigures}"/>
                    </a>
                    (<zfin:choice choicePattern="0#images| 1#image| 2#images"
                                  integerEntity="${probeStats.numberOfImages}"
                                  includeNumber="true"/>)
                </c:if>
                from
                <c:if test="${probeStats.numberOfPubs ==1}">
                    <zfin:link entity="${probeStats.singlePub}"/>
                </c:if>
                <c:if test="${probeStats.numberOfPubs > 1}">
                    <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                 integerEntity="${probeStats.numberOfPubs}"
                                 includeNumber="true"/>
                </c:if>
            </c:if>
        </td>
        </tr>
    </c:forEach>
</table>
<p/>
<zfin2:pagination paginationBean="${formBean}"/>
