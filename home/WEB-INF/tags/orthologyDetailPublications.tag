<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="title" type="java.lang.String" required="false" %>


<zfin2:subsection title="${title}" showNoData="true" test="${!empty publicationOrthologyList}">
    <table class="summary rowstripes">
        <tr>
            <th>Publication</th>
            <th> Evidence Code<a class="popup-link info-popup-link" href="/zf_info/oev.html"></a></th>
            <th> Zebrafish</th>
            <th> Human</th>
            <th> Mouse</th>
            <th> Fruit fly</th>
            <th width="30%"></th>
        </tr>

        <c:forEach var="orthology" items="${publicationOrthologyList}" varStatus="loop">
            <zfin:alternating-tr loopName="loop"
                                 groupBeanCollection="${publicationOrthologyList}"
                                 groupByBean="publication.shortAuthorList"
                                 showRowStyleClass="true">
            <td>
                <zfin:groupByDisplay loopName="loop"
                                     groupBeanCollection="${publicationOrthologyList}"
                                     groupByBean="publication.shortAuthorList">
                    <zfin:link entity="${orthology.publication}"/>
                </zfin:groupByDisplay>

            </td>
            <td>${orthology.evidenceCode.code}</td>
            <td><img src="/images/fill_green_ball.gif" border="0" height="10"></td>
            <td>
                <c:if test="${orthology.containsSpeciesString('Human')}">
                    <img src="/images/fill_green_ball.gif" border="0" height="10">
                </c:if>
            </td>
            <td>
                <c:if test="${orthology.containsSpeciesString('Mouse')}">
                    <img src="/images/fill_green_ball.gif" border="0" height="10">
                </c:if>
            </td>
            <td>
                <c:if test="${orthology.containsSpeciesString('Fruit fly')}">
                    <img src="/images/fill_green_ball.gif" border="0" height="10">
                </c:if>
            </td>
            <td/>
            </zfin:alternating-tr>
        </c:forEach>
            <%--
                    <c:set var="lastType" value=""/>
                    <c:set var="groupIndex" value="0"/>
                    <c:forEach var="dblink" items="${sequenceInfo.dbLinks}" varStatus="loop">
                        <tr class=${loop.index%2==0 ? "even" : "odd"}>
                            <td>
                                    ${dblink.referenceDatabase.foreignDBDataType.dataType.toString()}
                            </td>
                            <td>
                                <zfin:link entity="${dblink}"/>
                                <zfin:attribution entity="${dblink}"/>
                            </td>
                            <td style="text-align: right">
                                <c:if test="${!empty dblink.length}"> ${dblink.length} ${dblink.units} </c:if>
                            </td>
                            <td style="text-align: center; margin-left: 100px;">
                                <zfin2:externalAccessionBlastDropDown dbLink="${dblink}"/>
                            </td>
                                &lt;%&ndash;</zfin:alternating-tr>&ndash;%&gt;
                        </tr>
                        &lt;%&ndash;</c:if>&ndash;%&gt;
                        &lt;%&ndash;<c:set var="lastType" value="${dblink.referenceDatabase.foreignDBDataType.dataType}"/>&ndash;%&gt;
                    </c:forEach>
            --%>
    </table>
</zfin2:subsection>

