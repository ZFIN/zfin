<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="hit" type="org.zfin.sequence.blast.results.view.HitViewBean" rtexprvalue="true" required="true" %>
<%@ attribute name="hitIndex" type="java.lang.Integer" rtexprvalue="true" required="true" %>
<%@ attribute name="queryLength" type="java.lang.Integer" rtexprvalue="true" required="true" %>
<%@ attribute name="width" type="java.lang.Integer" rtexprvalue="true" required="true" %>
<%@ attribute name="accessionWidth" type="java.lang.Integer" rtexprvalue="true" required="true" %>

<%--This file is supposed to contain the sequence alignment image--%>

<c:set var="styletoset" value="${(hitIndex>50?'none':'')}"/>

<table id="hitTableRow${hitIndex}" cellpadding="0" cellspacing="0" border="0" style="display:${styletoset}">
    <c:set var="rowColor" value="${hitIndex%2==0 ? '#EEEEEE' : '#FFFFFF'}"/>
    <tr onmouseover="{
            this.style.background='#CCFFFF';
            document.getElementById('defline').innerHTML='${fn:replace(hit.definition,"'","\\'")}';
            }"
        onmouseout="{
                this.style.background='${rowColor}';
            } "
        bgcolor="${rowColor}"
            >
        <td style="font-family: monospace; width: ${accessionWidth}px;">
            <a href="#${hit.hitNumber}">${hit.accessionNumber}</a>
        </td>
        <td>
            <c:set var="hspCutoff" value="10"/>
            <c:forEach var="hsp" items="${hit.highScoringPairs}" varStatus="loopStatus">
                <c:choose>
                    <c:when test="${
                            (loopStatus.index < hspCutoff)
                            ||
                            (loopStatus.index == hspCutoff && fn:length(hit.highScoringPairs)==hspCutoff)
                            }">

                        <table cellspacing="0" cellpadding="0" border="0">
                            <tbody>
                            <tr>
                                <td valign="CENTER" align="left"><img width="50" height="4" src="/images/transp.gif"/>
                                </td>
                                <c:set var="queryFromNormalizedSpacerLength"/>
                                <c:choose>
                                    <c:when test="${hsp.queryFrom < hsp.queryTo}">
                                        <c:set var="queryFromNormalizedSpacerLength"
                                               value="${ (hsp.queryFrom )  * width / queryLength }"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="queryFromNormalizedSpacerLength"
                                               value="${ (hsp.queryTo)  * width / queryLength }"/>
                                    </c:otherwise>
                                </c:choose>
                                <c:set var="hspNormalizedLength" value="${hsp.queryLength * width / queryLength }"/>
                                <c:set var="fillerNormalizedSpacerLength"
                                       value="${width - hspNormalizedLength - queryFromNormalizedSpacerLength}"/>

                                <td valign="CENTER" align="left"><img width="${queryFromNormalizedSpacerLength}"
                                                                      height="4" src="/images/transp.gif"/></td>
                                <c:set var="color" value=""/>
                                <c:choose>
                                    <c:when test="${hsp.score >= 200}">
                                        <c:set var="color" value="red.gif"/>
                                    </c:when>
                                    <c:when test="${hsp.score >= 80}">
                                        <c:set var="color" value="purple.gif"/>
                                    </c:when>
                                    <c:when test="${hsp.score >= 50}">
                                        <c:set var="color" value="green.gif"/>
                                    </c:when>
                                    <c:when test="${hsp.score >= 40}">
                                        <c:set var="color" value="blue.gif"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="color" value="black.gif"/>
                                    </c:otherwise>
                                </c:choose>
                                <td valign="CENTER" align="left"><a href="#${hit.hitNumber}.${hsp.hspNumber}"><img
                                        width="${hspNormalizedLength}" height="3" border="0" src="/images/${color}"></a>
                                <td valign="CENTER" align="left"><img width="${fillerNormalizedSpacerLength}" height="4"
                                                                      src="/images/transp.gif"/></td>

                            </tr>
                            </tbody>
                        </table>
                    </c:when>
                    <c:when test="${loopStatus.index == hspCutoff}">
                        <table cellspacing="0" cellpadding="0" border="0">
                            <tbody>
                            <tr>
                                <td valign="CENTER" align="left"><img width="50" height="4" src="/images/transp.gif"/>
                                </td>
                                <td valign="CENTER" align="left"><img width="50" height="4" src="/images/transp.gif"/>
                                <div  style="font-family: monospace;">
                                    (${fn:length(hit.highScoringPairs)-hspCutoff} high scoring pairs not shown)
                                    </div>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </c:when>
                </c:choose>
            </c:forEach>


        </td>
    </tr>

</table>

