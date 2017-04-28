<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<script type="text/javascript">

    function start_note(ref_page) {
        top.zfinhelp = open("/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-" + ref_page + ".apg", "notewindow", "scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=325");
    }

    function popup_url(url) {
        open(url, "Description", "toolbar=yes,scrollbars=yes,resizable=yes");
    }

</script>





<div class="popup-header">

        Genotype Name: <zfin:name entity="${formBean.genotype}"/>

    </div>
<div class="popup-body">
<table class="primary-entity-attributes">
<c:if test="${fn:length(formBean.sequenceTargetingReagents) == 0 }">
    <tr>
        <th class="genotype-name-label">
            <c:if test="${!formBean.genotype.wildtype}">
                <span class="name-label">Genotype:</span>
            </c:if>
            <c:if test="${formBean.genotype.wildtype}">
                <span class="name-value">Wild-Type Line:</span>
            </c:if>
        </th>
        <td class="genotype-name-value">
            <span class="name-value"><zfin:name entity="${formBean.genotype}"/></span>
        </td>
    </tr>
</c:if>
<c:if test="${fn:length(formBean.sequenceTargetingReagents) > 0 }">
        <tr>
            <th class="fish-name-label" style="vertical-align: bottom;">
                <span class="name-label">Genotype + <abbr title="Sequence Targeting Reagent">STR</abbr>:</span>
            </th>
            <td class="fish-name-value" style="vertical-align: bottom;">
                <span class="name-value">${formBean.fishName}</span>
            </td>
        </tr>
</c:if>


    <c:if test="${formBean.genotype.wildtype}">
        <tr>
            <th>
                <span class="name-label">Abbreviation:</span>
            </th>
            <td>
                <span class="name-value">${formBean.genotype.handle}</span>
            </td>
        </tr>
    </c:if>



    <c:if test="${!formBean.genotype.wildtype}">
        <tr>
            <th>
                Background:
            </th>
            <td>
                <c:choose>
                    <c:when test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
                        <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
                            <zfin:link entity="${background}"/>
                            <c:if test="${background.handle != background.name}">(${background.handle})</c:if>
                            <c:if test="${!loop.last}">,&nbsp;</c:if>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        Unspecified
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <th>
                <zfin2:pluralization list="${affectedMarkerList}" singular="Affected Genomic Region:"
                                     nonSingular="Affected Genomic Regions:"/>
            </th>
            <td>
                <c:forEach var="affectedGene" items="${affectedMarkerList}" varStatus="loop">
                    <zfin:link entity="${affectedGene}"/><c:if test="${!loop.last}">,&nbsp;</c:if>
                </c:forEach>
            </td>
        </tr>




    </c:if>



        </td>
    </tr>
</table>




<c:if test="${!formBean.genotype.wildtype}">
<div class="summary">
    <b>GENOTYPE COMPOSITION</b>
    <c:choose>
        <c:when test="${formBean.genotypeFeatures ne null && fn:length(formBean.genotypeFeatures) > 0}">
            <table class="summary rowstripes">
                <tbody>
                <tr>
                    <th width="20%">
                        Genomic Feature
                    </th>
                    <th width="20%">
                        Construct
                    </th>
                    <th width="20%">
                        Zygosity
                    </th><th width="20%">
                    Parental Zygosity
                </th>


                </tr>
                <c:forEach var="genoFeat" items="${formBean.genotypeFeatures}" varStatus="loop">
                    <zfin:alternating-tr loopName="loop">
                        <td>
                            <zfin:link entity="${genoFeat.feature}"/>
                        </td>
                        <td>
                            <c:forEach var="construct" items="${genoFeat.feature.constructs}"
                                       varStatus="constructsloop">
                                <a href="/${construct.marker.zdbID}"><i>${construct.marker.name}</i></a>
                                <c:if test="${!constructsloop.last}">
                                    ,&nbsp;
                                </c:if>
                            </c:forEach>
                        </td>
                        <td>
                                ${genoFeat.zygosity.name}
                        </td>
                        <td>
                                ${genoFeat.parentalZygosityDisplay}
                        </td>
                    </td>

                    </zfin:alternating-tr>
                </c:forEach>

                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <br>No data available</br>
        </c:otherwise>
    </c:choose>
</div>
</c:if>
 <p></p>
    <c:if test="${fn:length(formBean.sequenceTargetingReagents) > 0 }">
        <table class="summary rowstripes">
            <tr>
                <th width="20%">Knockdown(s)</th>
                <th width=20%>Targets</th>
            </tr>

                <c:forEach var="str" items="${formBean.sequenceTargetingReagents}" varStatus="loop">
                    <jsp:useBean id="str" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>
                    <tr>
                        <td><zfin:link entity="${str}"/></td>
                        <td>
                            <zfin2:listOfAffectedGenes markerCollection="${str.targetGenes}"/>
                        </td>
                    </tr>
                </c:forEach>

        </table>

    </c:if>
</div>

