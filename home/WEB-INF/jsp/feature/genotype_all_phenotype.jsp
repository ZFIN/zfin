<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<table border="0" width="100%">
    <tbody>
        <tr align="left">
            <td><strong>All ${formBean.numberOfPhenoDisplays} phenotypes for:</strong>
                <em><zfin:link entity="${formBean.genotype}"/></em>
            </td>
        </tr>
    </tbody>
</table>

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
                <c:forEach var="pheno" items="${formBean.phenoDisplays}" varStatus="loop">
                <tr class="search-result-table-entries">
                    <td>
                       <c:choose>
                         <c:when test="${pheno.MO ne null}">
                             <zfin:link entity="${pheno.MO}"/>
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
                             <c:when test="${pheno.MO ne null}">
                               <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${formBean.genotype.zdbID}&entID=${pheno.entityTermSuper.ID}&qualityID=${pheno.qualityTerm.ID}&tag=${pheno.tag}&includingMO=yes&moID=${pheno.MO.zdbID}&entIDsub=${pheno.entityTermSub.ID}'>
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