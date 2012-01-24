<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="form" type="org.zfin.feature.presentation.GenotypeBean" required="true" %>

<div class="summary">
    <div class="summaryTitle">
        All ${form.numberOfPhenoDisplays} phenotypes for:
        <zfin:link entity="${form.genotype}"/>
    </div>

    <table class="summary rowstripes">
        <tbody>
        <tr>
            <th width="48%">
                Phenotype
            </th>
            <th width="17%">
                Conditions
            </th>
            <th width="35%">
                Figures
            </th>
        </tr>
        <c:forEach var="pheno" items="${form.phenoDisplays}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${pheno.phenoStatement}"/>
                </td>
                <td>
                    <zfin:link entity="${pheno.experiment}"/>
                </td>
                <td>
                    <c:forEach var="figsPub" items="${pheno.figuresPerPub}">
                        <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${fig.zdbID}'><zfin2:figureOrTextOnlyLink
                                    figure="${fig}" integerEntity="1"/></a>
                            <zfin2:showCameraIcon hasImage="${!fig.imgless}"/>
                            <c:if test="${!figloop.last}">,&nbsp;</c:if>
                        </c:forEach>
                        from <zfin:link entity="${figsPub.key}"/><br/>
                    </c:forEach>
                </td>
            </zfin:alternating-tr>
        </c:forEach>

        </tbody>
    </table>
</div>
