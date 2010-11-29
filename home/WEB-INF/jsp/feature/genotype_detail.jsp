<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<script type="text/javascript">

    function start_note(ref_page) {
       top.zfinhelp=open("/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-"+ref_page+".apg","notewindow","scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=325");
    }

    function popup_url(url) {
        open(url,"Description","toolbar=yes,scrollbars=yes,resizable=yes");
    }

</script>

<zfin2:dataManager zdbID="${formBean.genotype.zdbID}"
                  latestUpdate="${formBean.latestUpdate}"
        rtype="genotype"/>
<br/>        
        
        

<table width="100%" border="0">
  <tr>
    <td width="150">
        <c:if test="${!formBean.genotype.wildtype}">
          <FONT SIZE=+1><STRONG>Genotype:</STRONG></FONT>
        </c:if>
        <c:if test="${formBean.genotype.wildtype}">
          <FONT SIZE=+1><STRONG>Wild-Type Line:</STRONG></FONT>
        </c:if>
    </td>
    <td>
            <div style="display:inline;vertical-align:middle;font-size:large;">
                <strong><em>
                    ${formBean.genotype.name}
                </em></strong>
            </div>
    </td>
    <td align="right">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
             <tiles:putAttribute name="subjectName" value="${formBean.genotype.handle}"/>
             <tiles:putAttribute name="subjectID" value="${formBean.genotype.zdbID}"/>
        </tiles:insertTemplate>
    </td>
  </tr>
  
  <c:if test="${formBean.genotype.wildtype}">
    <tr>
      <td>
            <FONT SIZE=+1><STRONG>Abbreviation:</STRONG></FONT>
      </td>
      <td>
            <div style="display:inline;vertical-align:middle;font-size:large;">
                <strong>
                    ${formBean.genotype.handle}
                </strong>
            </div>      
      </td>     
    </tr>
  </c:if>
  
  <c:if test="${fn:length(formBean.genotype.aliases) ne null && fn:length(formBean.genotype.aliases) > 0}">
    <tr>
      <td>
         <c:choose>
           <c:when test="${fn:length(formBean.genotype.aliases) > 1}">
              <b>Previous Names:</b>
           </c:when>
           <c:otherwise>
              <b>Previous Name:</b>
           </c:otherwise>
         </c:choose>
      </td>
      <td>
            <c:forEach var="genoAlias" items="${formBean.genotype.aliases}" varStatus="loop">
                ${genoAlias.alias}
                <c:if test="${!loop.last}">,&nbsp;</c:if>
                <c:if test="${genoAlias.publicationCount > 0}">
                    <c:choose>
                        <c:when test="${genoAlias.publicationCount == 1}">
                            (<a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${genoAlias.singlePublication.zdbID}">${genoAlias.publicationCount}</a>)
                        </c:when>
                        <c:otherwise>
                            (<a href="alias-publication-list?genoAlias.zdbID=${genoAlias.zdbID}&orderBy=author">${genoAlias.publicationCount}</a>)
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </c:forEach>   
      </td>     
    </tr>
  </c:if>  
  
  <c:if test="${!formBean.genotype.wildtype}">
    <tr>
      <td>
          <b>Background: </b>
      </td>
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
      <td>
        <c:choose>
          <c:when test="${fn:length(formBean.genotypeStatistics.affectedMarkers) ne null && fn:length(formBean.genotypeStatistics.affectedMarkers) > 1}">
            <b>Affected Genes: </b>
          </c:when>
          <c:otherwise>
            <b>Affected Gene: </b>
          </c:otherwise>
        </c:choose>
      </td>
      <td>
        <c:forEach var="affectedGene" items="${formBean.genotypeStatistics.affectedMarkers}" varStatus="loop">
           <zfin:link entity="${affectedGene}"/>
           <c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:forEach>
      </td>
    </tr>
  </c:if>
</table>

<c:if test="${formBean.genotype.wildtype}"><p/></c:if>

<table>
  <tr>
    <td width="150" valign="top">
      <c:choose>
        <c:when test="${fn:length(formBean.genotype.suppliers) ne null && fn:length(formBean.genotype.suppliers) > 1}">
          <b>Current Sources:</b>
        </c:when>
        <c:otherwise>  
          <b>Current Source:</b>
        </c:otherwise>
      </c:choose>
    </td><td valign="top"> 
  <c:choose>
    <c:when test="${formBean.genotype.suppliers ne null && fn:length(formBean.genotype.suppliers) > 0}">
      <c:forEach var="supplier" items="${formBean.genotype.suppliers}" varStatus="status">         
        <c:choose>
          <c:when test="${formBean.genotype.extinct}">
	    ${supplier.organization.name} &nbsp; &nbsp;<font size="3" color="red">extinct</font><img src="/images/warning-noborder.gif" text="extinct" alt="extinct" width="20" align="top" height="20>
          </c:when>
          <c:otherwise>
            <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}" id="${supplier.organization.zdbID}">
	      ${supplier.organization.name}</a>
	    <c:if test="${supplier.availState ne null}">(${supplier.availState})</c:if>&nbsp;
            <c:if test="${supplier.zirc}">(<a href="http://zebrafish.org/zirc/fish/lineAll.php?OID=${formBean.genotype.zdbID}"><font size="-1">order this</font></a>)
            </c:if>
            <c:if test="${supplier.riken}">(<a href="http://www.shigen.nig.ac.jp/zebrafish/strainDetailAction.do?zfinId=${formBean.genotype.zdbID}"><font size="-1">order this</font></a>)
            </c:if>
          </c:otherwise>
        </c:choose>
        <c:if test="${!status.last}"><br/></c:if>
      </c:forEach>
    </c:when>
    <c:otherwise>
      No data available
    </c:otherwise>
  </c:choose>
  </td></tr>
</table>

<c:if test="${formBean.genotype.wildtype}"><p/></c:if>

<authz:authorize ifAnyGranted="root">
  <table>
    <tr><td width="100%"><b>Curator Note:</b></td></tr>
    <c:forEach var="dataNote" items="${formBean.genotype.sortedDataNotes}" varStatus="loopCurNote">
       <tr><td>${dataNote.curator.name}&nbsp;&nbsp;${dataNote.date}<br/>${dataNote.note}
         <c:if test="${!loopCurNote.last}"><br/>&nbsp;<br/></c:if>
       </td></tr>
    </c:forEach>  
  </table>
</authz:authorize>

<c:if test="${formBean.genotype.wildtype}"><p/></c:if>

<table>
  <tr><td width="100%"><b>Note:</b></td></tr>
  <c:forEach var="extNote" items="${formBean.genotype.externalNotes}">
     <tr><td>${extNote.note}
       <c:if test="${extNote.singlePubAttribution ne null}">
         &nbsp;(<a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pubview2.apg&OID=${extNote.singlePubAttribution.publication.zdbID}'>1</a>)
       </c:if>
     </td></tr>
  </c:forEach>  
</table>

<c:if test="${!formBean.genotype.wildtype}">
<p/>
<b>GENOTYPE COMPOSITION</b>
<c:choose>
    <c:when test="${fn:length(formBean.genotypeFeatures) ne null && fn:length(formBean.genotypeFeatures) > 0 }">
            <table width="100%">
                <tbody>
                <TR class="search-result-table-header">
                    <TD width="20%">
                        Feature
                    </TD>
                    <TD width="20%">
                        Zygosity
                    </TD>
                    <TD width="20%">
                        Parental Genotype
                    </TD>
                    <TD width="20%">
                        Lab of Origin
                    </TD>
                    <TD width="20%">
                        Construct
                    </TD>

                </TR>
                <c:forEach var="genoFeat" items="${formBean.genotypeFeatures}" varStatus="loop">
                <tr class="search-result-table-entries">
                    <td>
                      <c:choose>
                        <c:when test="${genoFeat.feature.numberOfRelatedGenotypes > 1}">
                          <zfin:link entity="${genoFeat.feature}"/> &nbsp; <i><a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fishselect.apg&query_results=exists&compareAllele=starts&fsel_allele_id=${genoFeat.feature.zdbID}">(in ${genoFeat.feature.numberOfRelatedGenotypes} genotypes)</a></i>
                        </c:when>
                        <c:otherwise>
                          <zfin:link entity="${genoFeat.feature}"/> &nbsp; <i>(in 1 genotype)</i>                        
                        </c:otherwise> 
                      </c:choose>
                    </td>
                    <td>
                       ${genoFeat.zygosity.name}
                    </td>
                    <td>
                       ${genoFeat.parentalZygosityDisplay}
                    </td>
                    <td>
                        <c:forEach var="source" items="${genoFeat.feature.sources}" varStatus="status">
                            <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-sourceview.apg&OID=${source.organization.zdbID}">
                                    ${source.organization.name}
                            </a>
                            <c:if test="${!status.last}">,&nbsp;</c:if>
                         </c:forEach>
                    </td>
                    <td>
                         <c:forEach var="construct" items="${genoFeat.feature.constructs}" varStatus="constructsloop">
                            <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-markerview.apg&OID=${construct.marker.zdbID}">${construct.marker.name}</a>
                            <c:if test="${!constructsloop.last}">
                               ,&nbsp;
                            </c:if>
                         </c:forEach>
                    </td>
                </tr>
                </c:forEach>

                </tbody>
            </table>
    </c:when>
    <c:otherwise>
        <br>No data available</br>
    </c:otherwise>
</c:choose>

<p/>
<b>GENE EXPRESSION</b>&nbsp;<font size=-1>(<a href="javascript:start_note('xpatselect_note')">current status</a></font>)
<br/>
<b>Gene expression in <i>${formBean.genotype.name}</i><c:if test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
        <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
           (${background.handle})
           <c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:forEach>
      </c:if></i></b>
<c:choose>
    <c:when test="${formBean.numberOfExpressionDisplays > 0 }">
            <table width="100%">
                <tbody>
                <TR class="search-result-table-header">
                    <TD width="20%">
                        Expressed Gene
                    </TD>
                    <TD width="20%">
                        Structure
                    </TD>
                    <TD width="30%">
                        Figures
                    </TD>
                </TR>
                <c:forEach var="xp" items="${formBean.expressionDisplays}" varStatus="loop" end="4">
                <tr class="search-result-table-entries">
                    <td>
                        <zfin:link entity="${xp.expressedGene}"/>
                    </td>
                    <td>
                        <zfin2:toggledPostcomposedList expressionResults="${xp.expressionResults}" maxNumber="3" id="${xp.expressedGene.zdbID}"/>                    
                    </td>
                    <td>
                       <c:choose>
                         <c:when test="${xp.numberOfFigures > 1}">
                           <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-xpatselect.apg&query_results=true&xpatsel_geneZdbId=${xp.expressedGene.zdbID}&mutant_id=${formBean.genotype.zdbID}'>
                             ${xp.numberOfFigures} figures</a>
                         </c:when>
                         <c:otherwise>
                           <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${xp.singleFig.zdbID}'>
                             1 figure</a>                         
                           <c:if test="${xp.imgInFigure}"><img src="/images/camera_icon.gif" border="0" alt="with image"></c:if>
                           <c:if test="${xp.moInExperiment}"><img src="/images/MO_icon.gif" border="0" alt="MO image"></c:if>
                         </c:otherwise>
                       </c:choose>
                       &nbsp;from 
                       <c:choose>
                         <c:when test="${xp.numberOfPublications > 1 }">
                             ${xp.numberOfPublications} publications
                         </c:when>
                         <c:otherwise>
                             <zfin:link entity="${xp.singlePublication}"/>
                         </c:otherwise>
                       </c:choose>
                    </td>
                </tr>
                </c:forEach>

                </tbody>
            </table>

        <c:if test="${formBean.numberOfExpressionDisplays > 5}">
         <table width="100%">
            <tbody>
                <tr align="left">
                    <td>
                        Show all <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-xpatselect.apg&query_results=true&mutant_id=${formBean.genotype.zdbID}'>
                        ${formBean.totalNumberOfExpressedGenes}&nbsp;genes,
                        <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${formBean.totalNumberOfExpressionFigures}" includeNumber="true"/></a>
                    </td>
                </tr>
            </tbody>
        </table>
       </c:if>
    </c:when>

    <c:otherwise>
        <br>No data available</br>
    </c:otherwise>
</c:choose> 

<p/>
<b>PHENOTYPE</b>&nbsp;<font size=-1>(<a href="javascript:start_note('phenotype_note')">current status</a></font>)
<br/>
<b>Phenotype in <i>${formBean.genotype.name}</i><c:if test="${fn:length(formBean.genotype.associatedGenotypes) ne null && fn:length(formBean.genotype.associatedGenotypes) > 0}">
        <c:forEach var="background" items="${formBean.genotype.associatedGenotypes}" varStatus="loop">
           (${background.handle})
           <c:if test="${!loop.last}">,&nbsp;</c:if>
        </c:forEach>
      </c:if></b>
<c:choose>
    <c:when test="${formBean.numberOfPhenoDisplays > 0 }">
        <table width="100%">
            <tbody>
                <TR class="search-result-table-header">
                    <TD width="15%">
                        Conditions
                    </TD>
                    <TD width="20%">
                        Observed in
                    </TD>
                    <TD width="30%">
                        Phenotype
                    </TD>
                    <TD width="35%">
                        Figures
                    </TD>
                </TR>
                <c:forEach var="pheno" items="${formBean.phenoDisplays}" varStatus="loop" end="4">
                <tr class="search-result-table-entries">
                    <td>
                       <c:choose>
                         <c:when test="${pheno.morpholino ne null}">
                             <zfin:link entity="${pheno.morpholino}"/>
                         </c:when>
                         <c:otherwise>
                             Standard or control
                         </c:otherwise>
                       </c:choose>                      
                    </td>
                    <td>
                        <zfin2:displayPostcomposedTerm superTerm="${pheno.entityTermSuper}" supTerm="${pheno.entityTermSub}" />
                    </td>
                    <td>
                       ${pheno.qualityTerm.termName}, ${pheno.tag}
                    </td>
                    <td>
                       <c:choose>
                         <c:when test="${pheno.numberOfFigures >1}">
                           <c:choose>
                             <c:when test="${pheno.morpholino ne null}">
                               <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${formBean.genotype.zdbID}&entID=${pheno.entityTermSuper.ID}&qualityID=${pheno.qualityTerm.ID}&tag=${pheno.tag}&includingMO=yes&moID=${pheno.morpholino.zdbID}&entIDsub=${pheno.entityTermSub.ID}'>
                                 ${pheno.numberOfFigures} figures</a>
                               <c:if test="${pheno.imgInFigure}">
                                 <img src="/images/camera_icon.gif" border="0" alt="with image">
                               </c:if>
                               <img src="/images/MO_icon.gif" border="0" alt="MO image">                                                                                                               
                             </c:when>
                             <c:otherwise>
                               <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${formBean.genotype.zdbID}&entID=${pheno.entityTermSuper.ID}&qualityID=${pheno.qualityTerm.ID}&tag=${pheno.tag}&entIDsub=${pheno.entityTermSub.ID}'>
                                 ${pheno.numberOfFigures} figures</a>
                               <c:if test="${pheno.imgInFigure}">
                                 <img src="/images/camera_icon.gif" border="0" alt="with image">
                               </c:if>
                             </c:otherwise>
                           </c:choose>
                         </c:when>
                         <c:otherwise>
                           <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${pheno.singleFig.zdbID}'>
                             1 figure</a>
                           <c:if test="${pheno.imgInFigure}">
                             <img src="/images/camera_icon.gif" border="0" alt="with image">
                           </c:if>     
                           <c:if test="${pheno.moInExperiment}">
                             <img src="/images/MO_icon.gif" border="0" alt="MO image">
                           </c:if>                             
                         </c:otherwise>
                       </c:choose>
                         &nbsp;from                        
                       <c:choose>
                         <c:when test="${pheno.numberOfPubs > 1 }">
                             ${pheno.numberOfPubs} publications
                         </c:when>
                         <c:otherwise>
                             <zfin:link entity="${pheno.singlePub}"/>
                         </c:otherwise>
                       </c:choose>  
                    </td>
                </tr>
                </c:forEach>

            </tbody>
        </table>

        <c:if test="${formBean.numberOfPhenoDisplays > 5}">
         <table width="100%">
            <tbody>
                <tr align="left">
                    <td>
                        Show all <a href="show_all_phenotype?genotype.zdbID=${formBean.genotype.zdbID}">${formBean.numberOfPhenoDisplays}&nbsp;experiments</a>
                    </td>
                </tr>
            </tbody>
        </table>
       </c:if>
    </c:when>

    <c:otherwise>
        <br>No data available</br>
    </c:otherwise>
</c:choose>    

<p/>
<a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-showpubs.apg&OID=${formBean.genotype.zdbID}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.totalNumberOfPublications})

</c:if>


