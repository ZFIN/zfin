<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="genotypes" type="java.util.Collection" required="false" %>
<%@ attribute name="sequenceTargetReagen" type="java.lang.String" required="false" %>
<%@ attribute name="showNumberOfRecords" type="java.lang.Integer" required="true" %>

<table class="summary rowstripes">
     <caption>GENOTYPES <c:if test="${sequenceTargetReagen != null}">CREATED WITH ${sequenceTargetReagen}</c:if></caption>
     <tr>
        <th width="20%">
           Genotype (Background)
        </th>
        <th width="20%">
           Affected Genes
        </th>
        <th width="20%">
           Phenotype
        </th>
        <th width="20%">
           Gene Expression
        </th>
     </tr>

     <c:forEach var="featgenoStat" items="${genotypes}" varStatus="loop" end="${showNumberOfRecords-1}">
          <zfin:alternating-tr loopName="loop">
               <td>
                  <zfin:link entity="${featgenoStat.genotype}"/><a class="popup-link data-popup-link"
                                                                     href="/action/genotype/genotype-detail-popup?zdbID=${featgenoStat.genotype.zdbID}"></a>
               </td>
               <td>
                  <zfin:link entity="${featgenoStat.affectedMarkers}"/>
               </td>

               <td>
                  <c:if test="${featgenoStat.numberOfFigures > 0}">
                      <c:if test="${featgenoStat.numberOfFigures > 1}">
                          <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-pheno_summary.apg&OID=${featgenoStat.genotype.zdbID}&includingMO=yes&split=yes'>
                              <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                           integerEntity="${featgenoStat.numberOfFigures}"
                                           includeNumber="true"/></a>
                      </c:if>
                      <c:if test="${featgenoStat.numberOfFigures == 1 }">
                          <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.figure.zdbID}'>
                               <zfin2:figureOrTextOnlyLink figure="${featgenoStat.figure}"
                                           integerEntity="${featgenoStat.numberOfFigures}"/>
                          </a>
                      </c:if>
                  </c:if>

                  <c:if test="${featgenoStat.numberOfFigures == 0}">
                      &nbsp;
                  </c:if>

                  <c:if test="${featgenoStat.numberOfPublications ==1}">
                       from
                       <zfin:link entity="${featgenoStat.singlePublication}"/>
                  </c:if>

                  <c:if test="${featgenoStat.numberOfPublications > 1}">
                        from
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${featgenoStat.numberOfPublications}"
                                     includeNumber="true"/>
                  </c:if>

                  <zfin2:showCameraIcon hasImage="${featgenoStat.isImage}"/>

               </td>

               <td>
                  <c:if test="${featgenoStat.numberOfExpFigures > 0}">
                       <c:if test="${featgenoStat.numberOfExpFigures > 1}">
                           <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-xpatselect.apg&query_results=true&mutsearchtype=equals&mutant_id=${featgenoStat.genotype.zdbID}'>
                                 <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                              integerEntity="${featgenoStat.numberOfExpFigures}"
                                              includeNumber="true"/></a>
                       </c:if>

                       <c:if test="${featgenoStat.numberOfExpFigures == 1 }">
                           <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${featgenoStat.expFigure.zdbID}'>
                                 <zfin2:figureOrTextOnlyLink figure="${featgenoStat.expFigure}"
                                                             integerEntity="${featgenoStat.numberOfExpFigures}"/>
                           </a>
                       </c:if>
                  </c:if>

                  <c:if test="${featgenoStat.numberOfExpFigures == 0}">
                        &nbsp;
                  </c:if>

                  <c:if test="${featgenoStat.numberOfExpPublications ==1}">
                        from
                        <zfin:link entity="${featgenoStat.singleExpPublication}"/>
                  </c:if>

                  <c:if test="${featgenoStat.numberOfExpPublications > 1}">
                        from
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${featgenoStat.numberOfExpPublications}"
                                     includeNumber="true"/>
                  </c:if>

                  <zfin2:showCameraIcon hasImage="${featgenoStat.isImageExp}"/>
               </td>
          </zfin:alternating-tr>
     </c:forEach>
 </table>
