<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true"
             hasData="${formBean.allPhenotypeDisplays != null && fn:length(formBean.allPhenotypeDisplays) > 0 }">

    <thead>
    <tr>
        <th>
            Phenotype
        </th>
            <th>
                Fish
            </th>
            <th>
                Conditions
            </th>
        <th>
            Figures
        </th>
    </tr>
    </thead>
    <c:forEach var="phenotypeDisplay" items="${formBean.allPhenotypeDisplays}" varStatus="loop">
        <tr>
            <td>
                <zfin:link entity="${phenotypeDisplay.phenoStatement}"/>
            </td>
                <td>
                    <zfin:link entity="${phenotypeDisplay.phenoStatement.phenotypeExperiment.fishExperiment.fish}"/>
                </td>
                <td>
                    <zfin:link entity="${phenotypeDisplay.experiment}"/>
                </td>
            <td>
                <c:forEach var="figsPub" items="${phenotypeDisplay.figuresPerPub}">
                    <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                        <a href='/${fig.zdbID}'>${fig.label}</a><c:if test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if test="${!figloop.last}">,&nbsp;</c:if>
                    </c:forEach>
                    from <zfin:link entity="${figsPub.key}"/><br/>
                </c:forEach>
            </td>
        </tr>
    </c:forEach>

</z:dataTable>