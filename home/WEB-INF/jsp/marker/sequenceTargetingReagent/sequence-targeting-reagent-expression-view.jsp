<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true"
             hasData="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
    <thead>
    <tr>

        <th>
            Expressed Gene
        </th>
        <th>
            Anatomy
        </th>
        <th>
            Figures
        </th>
    </tr>
    </thead>
    <c:forEach var="xp" items="${formBean.expressionDisplays}" varStatus="loop">
        <zfin:alternating-tr loopName="loop"
                             groupBeanCollection="${formBean.expressionDisplays}"
                             groupByBean="expressedGene">
            <td valign="top">
                <zfin:groupByDisplay loopName="loop"
                                     groupBeanCollection="${formBean.expressionDisplays}"
                                     groupByBean="expressedGene">
                    <zfin:link entity="${xp.expressedGene}"/>
                </zfin:groupByDisplay>
            </td>
            <td valign="top">
                <zfin2:toggledLinkList collection="${xp.expressionResults}" maxNumber="3" commaDelimited="true"/>
            </td>
            <td valign="top">
            <c:choose>
                <c:when test="${xp.numberOfFigures < 10}">
                    <c:forEach var="figsPub" items="${xp.figuresPerPub}">
                        <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                            <a href='/${fig.zdbID}'>${fig.label}</a><c:if
                                test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if
                                test="${!figloop.last}">,&nbsp;</c:if>
                        </c:forEach>
                        from <zfin:link entity="${figsPub.key}"/><br/>
                    </c:forEach>
                </c:when>
            </c:choose>
            <zfin2:showCameraIcon hasImage="${xp.imgInFigure}"/> from
            <c:choose>
                <c:when test="${xp.numberOfPublications > 1 }">
                    ${xp.numberOfPublications} publications
                </c:when>
                <c:otherwise>
                    <zfin:link entity="${xp.singlePublication}"/>
                </c:otherwise>
            </c:choose>
        </td>
    </zfin:alternating-tr>
</c:forEach>

</z:dataTable>


