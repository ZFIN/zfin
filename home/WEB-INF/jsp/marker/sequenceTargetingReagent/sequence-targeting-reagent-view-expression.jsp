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
        <tr>
            <td><zfin:link entity="${xp.expressedGene}"/></td>
            </td>
            <td valign="top">
                <zfin2:toggledLinkList collection="${xp.expressionResults}" maxNumber="3" commaDelimited="true"/>
            </td>
            <td valign="top">
                <c:forEach var="figsPub" items="${xp.figuresPerPub}">
                    <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                        <a href='/${fig.zdbID}'>${fig.label}</a><c:if
                            test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if
                            test="${!figloop.last}">,&nbsp;</c:if>
                    </c:forEach>
                    from <zfin:link entity="${figsPub.key}"/><br/>
                </c:forEach>

            </td>
        </tr>
    </c:forEach>

</z:dataTable>


