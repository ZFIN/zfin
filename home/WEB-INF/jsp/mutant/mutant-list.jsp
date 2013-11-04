<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.mutant.presentation.MutantListBean" scope="request"/>

<c:if test="${formBean.publication != null}">

    <TABLE width=100%>
        <TR>
            <TD width=90%>
                <font size="+1">
                    <center>
    <c:if test="${formBean.callingPage == null}">
                        Mutant / Transgenic Lines (${fn:length(formBean.mutants)} records)
        </c:if>
    <c:if test="${formBean.callingPage == null}">
        Features/Constructs
    </c:if>
                    </center>

          </font>
      </TD>
             <td align="right" width="110" nowrap="nowrap">
                <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                    <tiles:putAttribute name="subjectName" value="Mutant / Transgenic Lines"/>
                    <tiles:putAttribute name="subjectID" value="${formBean.publication.zdbID}"/>
                </tiles:insertTemplate>
            </td>    
    </TR>
  </TABLE>

            <%--<TD width=10%>

                <table leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" border="0" cellspacing="0"
                       cellpadding="0">
                    <form method=post action="/cgi-bin/webdriver" target=comments>
                        <input type=hidden name=MIval value="aa-input_welcome_generic.apg">
                        <input type=hidden name=page_name value="ZFIN Marker Search ">
                        <tr>
                            <td><input type=submit value="Your Input Welcome"></td>
                        </tr>
                    </form>
                </table>
            </TD>
        </TR>
    </TABLE>--%>
    <c:if test="${formBean.callingPage == null}">
    <table class="summary rowstripes">
        <tbody>
        <tr>
            <th width="20%">
                Genotype (Background)
            </th>
            <th width="70%">
                <table border="0" cellspacing="0" width="100%">
                    <tr>
                        <th width="20%">Genomic Feature</th>
                        <th width="30%">Construct</th>
                        <th width="25%">Type</th>
                        <th width="34%">Affected Gene(s)</th>
                    </tr>
                </table>
            </th>
        </tr>
        <c:forEach var="geno" items="${formBean.mutants}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td width="20%">
                    <zfin:link entity="${geno}"/><a class="popup-link data-popup-link" href="/action/genotype/genotype-detail-popup?zdbID=${geno.zdbID}"></a>
                </td>

                <td width="70%">
                    <table border="0" cellspacing="0" width="100%">
                        <c:forEach var="genoFeat" items="${geno.genotypeFeatures}" varStatus="genoFeatloop">
                            <tr>
                                <td width="20%"><zfin:link entity="${genoFeat.feature}"/></td>
                                <td width="30%">
                                    <zfin2:listOfTgConstructs markerCollection="${genoFeat.feature.tgConstructs}"/></td>
                                <td width="25%">${genoFeat.feature.type.display}</td>
                                <td width="34%">
                                    <zfin2:listOfAffectedGenes markerCollection="${genoFeat.feature.affectedGenes}"/>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </td>
            </zfin:alternating-tr>
        </c:forEach>

        </tbody>
    </table>

</c:if>
</c:if>
<c:if test="${formBean.callingPage != null}">
    <table class="summary rowstripes">
        <tbody>




                    <tr>
                        <th width="33%">Genomic Feature</th>
                        <th width="33%">Relationship</th>
                        <th width="34%">Target(s)</th>
                    </tr>




                        <c:forEach var="fmRel" items="${formBean.fmRels}" varStatus="loop">
                                <zfin:alternating-tr loopName="loop">

                                <td width="33%"><zfin:link entity="${fmRel.feature}"/></td>
                                <td width="33%">${fmRel.featureMarkerRelationshipType.name}</td>
                                <td width="34%">
                                    <zfin:link entity="${fmRel.marker}"/>
                                </td>
                            </zfin:alternating-tr>
                        </c:forEach>



        </tbody>
    </table>

</c:if>


<c:if test="${formBean.gene != null}">

    <p/>

    <div class="summaryTitle">Mutant / Transgenic Lines for <zfin:link entity="${formBean.gene}"/> <br/></div>

    <table class="summary rowstripes">
        <tbody>
        <tr>
            <th width="25%">
                Genotype (Background)
            </th>
            <th width="60%">
                <table border="0" cellspacing="0" width="100%">
                    <tr>
                    <th border=0 width="33%">Genomic Feature
            </th>
            <th border=0 width="33%">Type</th>
            <th border=0 width="34%">Affected Gene(s)</th>
        </tr>
    </table>
    </th>
    <th width="15%">
        Phenotype
    </th>
    </tr>
    <c:forEach var="geno" items="${formBean.mutants}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td width="25%">
                <zfin:link entity="${geno}"/><a class="popup-link data-popup-link" href="/action/genotype/genotype-detail-popup?zdbID=${geno.zdbID}"></a>
            </td>

            <td width="60%">
                <table border="0" cellspacing="0" width="100%">
                    <c:forEach var="genoFeat" items="${geno.genotypeFeatures}" varStatus="genoFeatloop">
                        <tr>
                            <td border=0 width="33%"><zfin:link entity="${genoFeat.feature}"/></td>
                            <td border=0 width="33%">${genoFeat.feature.type.display}</td>
                            <td border=0 width="34%">
                            <c:if test="${fn:length(genoFeat.feature.affectedGenes) > 0 }">
                                <c:forEach var="affectedGene" items="${genoFeat.feature.affectedGenes}"
                                           varStatus="geneLoop">
                                    <zfin:link entity="${affectedGene}"/><c:if test="${!geneLoop.last}">,&nbsp</c:if>
                                </c:forEach>
                            </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
            <td width="15%">
                <c:if test="${geno.phenotypeFigures != null && fn:length(geno.phenotypeFigures) > 0}">
                    <c:choose>
                        <c:when test="${fn:length(geno.phenotypeFigures) == 1}">
                            <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() %>?MIval=aa-fxfigureview.apg&OID=${geno.phenotypeSingleFigure.zdbID}">1
                                figure</a>
                        </c:when>
                        <c:otherwise>
                            <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() %>?MIval=aa-pheno_summary.apg&OID=${geno.zdbID}">${fn:length(geno.phenotypeFigures)}
                                figures</a>
                        </c:otherwise>
                    </c:choose>
                    <zfin2:showCameraIcon hasImage="${geno.phenoFiguresHaveImg}"/>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>

    </tbody>
    </table>

</c:if>

