<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="blastResults" type="org.zfin.sequence.blast.results.view.BlastResultBean" rtexprvalue="true" required="true"%>
<%@ attribute name="width" type="java.lang.Integer" rtexprvalue="true" required="true"%>
<%@ attribute name="numTicksPlusOne" type="java.lang.Integer" rtexprvalue="true" required="true"%>
<%@ attribute name="accessionWidth" type="java.lang.Integer" rtexprvalue="true" required="true"%>

<c:set var="queryLength" value="${blastResults.queryLength}"/>

<%--This file is supposed to contain the sequence alignment image--%>

<div id="defline" style="font-family: monospace; font-size: medium; height: 30px; " id="defline" >Mouse over to see the defline, click to show alignments</div>

<%--start of ticks--%>
<c:set var="rightSpace" value="&nbsp;&nbsp;&nbsp;" />


<table cellspacing="0" cellpadding="10" border="1" bordercolorlight="#0000FF" bordercolordark="#0000FF"> <tbody><tr><td valign="CENTER" align="left">
    <table cellspacing="1" cellpadding="0" border="0"><tbody><tr>
        <td style="font-family: monospace; width: ${accessionWidth}px;">
            <c:if test="${fn:length(blastResults.hits)>50}">
                <script type="text/javascript">
                    var showAll = false ;
                    function toggleShow50(allOr50){

                        // is currently false, so we set to true (show all)
                        if(showAll==false && allOr50=='showall'){
                            showAll = true ;
                            s=document.getElementById('showAll').style ;
                            s.cursor = 'auto' ;
                            s.textDecoration='none';
                            s.color = 'black';
                            f=document.getElementById('show50');
                            t=f.style ;
                            t.cursor = 'pointer' ;
                            t.textDecoration='underline';
                            t.color = 'blue';
                            for(var i = 50 ; i < ${fn:length(blastResults.hits)} ; i+=1 ) {
                                document.getElementById('hitTableRow'+i).style.display = 'inline';
                            }
                        }
                        // is currently true, so we set to false (show only 50)
                        else
                        if(showAll==true&& allOr50=='show50'){
                            showAll = false ;
                            s=document.getElementById('show50').style ;
                            s.cursor = 'auto' ;
                            s.textDecoration='none';
                            s.color = 'black';
                            f=document.getElementById('showAll');
                            t=f.style ;
                            t.cursor = 'pointer' ;
                            t.textDecoration='underline';
                            t.color = 'blue';

                            for(var i = 50 ; i < ${fn:length(blastResults.hits)} ; i+=1 ) {
                                document.getElementById('hitTableRow'+i).style.display = 'none';
                            }
                        }

                    }
                </script>
            <span id="show50" style="cursor:auto;text-decoration:none;color:black;"  onclick="toggleShow50('show50');">Show 50</span>
                |
            <span id="showAll" style="cursor:pointer;text-decoration:underline;color:blue;" onclick="toggleShow50('showall');">Show All</span>
            </c:if>
        </td>
        <td valign="CENTER" align="left"><img width="50" height="4" src="/images/transp.gif"/></td>
        <td style="font-size: small;" valign="top" halign="center">
            <img width="10" height="10" src="/images/black.gif"/>&nbsp;&le;40${rightSpace}
            <img width="10" height="10" src="/images/blue.gif"/>&nbsp;40-50${rightSpace}
            <img width="10" height="10" src="/images/green.gif"/>&nbsp;50-80${rightSpace}
            <img width="10" height="10" src="/images/purple.gif"/>&nbsp;80-200${rightSpace}
            <img width="10" height="10" src="/images/red.gif"/>&nbsp;&ge;200${rightSpace}
        </td>
    </tr></tbody></table>
    <%--<table cellspacing="1" cellpadding="0" border="0"><tbody><tr><td style="font-family: monospace; width: ${accessionWidth}px;"></td><td valign="CENTER" align="left"><img width="550" height="10" src="/images/query_no_scale.gif"/></td></tr></tbody></table>--%>
    <table cellspacing="1" cellpadding="0" border="0"><tbody><tr><td style="font-family: monospace; width: ${accessionWidth}px;"></td>
        <td valign="CENTER" width="50" align="center" style="font-size: small;">Query</td>
        <td valign="CENTER" align="left"><img width="${width}px;" height="10" src="/images/red.gif"/></td>
    </tr></tbody></table>

    <%--these are the tick marks--%>
    <table cellspacing="0" cellpadding="0" border="0">
        <tbody>
        <tr>
            <td style="font-family: monospace; width: ${accessionWidth}px;"></td>
            <td valign="CENTER" align="left"><img width="50" height="4" src="/images/transp.gif"/></td>
            <c:forEach var="x" begin="0"  end="${numTicksPlusOne}" step="1" varStatus="loopStatus">
                <td valign="CENTER" align="${( loopStatus.first ? "left" : "CENTER" )}"><img align="CENTER" width="2" height="10" src="/images/scale.gif"/></td>
                <c:if test="${!loopStatus.last}">
                    <td valign="CENTER" align="left"><img width="${ (width - (numTicksPlusOne*2) ) / numTicksPlusOne}" height="4" src="/images/transp.gif"/></td>
                </c:if>
            </c:forEach>
        </tr>
        </tbody>
    </table>


    <%--set letter spacing here--%>
    <c:set var="maxLetterSpaces" value="6"/>
    <c:set var="letterSpacing" value="5"/>
    <table cellspacing="0" cellpadding="0" border="0">
        <tbody>
        <tr>
            <td style="font-family: monospace; width: ${accessionWidth}px;"></td>

            <td valign="CENTER" align="left"><img width="${ 50-(maxLetterSpaces * letterSpacing) /2}" height="4" src="/images/transp.gif"/></td>

            <c:forEach var="tick" begin="0" end="${numTicksPlusOne}" varStatus="loopStatus">
                <fmt:formatNumber var="tickValue" value="${tick * queryLength / numTicksPlusOne}" pattern="#"/>
                <c:set var="tickValueLength" value="${fn:length(tickValue)}"/>

                <%--add the actual tick--%>
                <td valign="CENTER" align="CENTER" style="font-family: monospace; width: ${maxLetterSpaces*letterSpacing}px;">${tickValue}</td>

                <%--if not last, then add the standard buffer allowing for the maxLetter space --%>
                <c:if test="${!loopStatus.last}">
                    <td valign="CENTER" align="left"><img width="${ (width / (numTicksPlusOne)) - (maxLetterSpaces *letterSpacing) }" height="4" src="/images/transp.gif"/></td>
                </c:if>
            </c:forEach>
        </tr>
        </tbody>
    </table>

    <%--end of ticks--%>

    <br/>


    <%--put gaps into different rows--%>
    <c:forEach var="hit" items="${blastResults.hits}" varStatus="hitLoop">
        <zfin2:blastResultHit hit="${hit}" hitIndex="${hitLoop.index}" queryLength="${queryLength}" width="${width}" accessionWidth="${accessionWidth}"/>
    </c:forEach>

</td>
</tr>
</tbody>
</table>


