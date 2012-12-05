<%@ tag import="org.zfin.fish.presentation.SortBy" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.mutant.presentation.ConstructSearchFormBean" required="false" %>


<div style="margin-top: 2em ; margin-bottom: .3em">

<span style="text-align: center; margin-top: 8px; margin-left: 4px;">
    <c:if test="${formBean.totalRecords > 0}">
        <b>
            <fmt:formatNumber value="${formBean.totalRecords}" pattern="##,###"/> Constructs found
        </b>
    </c:if>
</span>

    <p/>

    <div style="float:right ; margin-top: 2px;">
        <%--
                <form:select path="maxDisplayRecords" items="${formBean.recordsPerPageList}"
                             onchange="submitFishSearchWithNumOfRecords(50);return true;"></form:select>
        --%>
        <select name="maxDisplayRecordsTop" id="max-display-records-top" >
            <c:forEach items="${formBean.recordsPerPageList}" var="option">
                <option>${option}</option>
            </c:forEach>
        </select>
        <label for="max-display-records-top">results per page</label>

    </div>


    <script>

        jQuery('#max-display-records-top').val(${formBean.maxDisplayRecords});
        jQuery('#max-display-records-bottom').val(${formBean.maxDisplayRecords});

        function setMaxDisplayRecords(value) {
            jQuery('#max-display-records-hidden').val(value);
            jQuery('#max-display-records-hidden').change();
        }

        jQuery('#max-display-records-top').change(function () {
            setMaxDisplayRecords(jQuery('#max-display-records-top option:selected').val());
        });



    </script>


    <script language="JavaScript">

        function showAll() {
            jQuery('.showAll').each(function () {
                jQuery(this).click();
            });
            jQuery('#showAllLink').hide();
            jQuery('#hideAllLink').show();
        }

        function hideAll() {
            jQuery('.hideAll').each(function () {
                jQuery(this).click();
            });
            jQuery('#showAllLink').show();
            jQuery('#hideAllLink').hide();
        }
    </script>

    <zfin2:pagination paginationBean="${formBean}"/>
</div>


<table class="searchresults rowstripes" style="clear: both;">

    <tr>

    <th>Construct</th>
    <%--<th>Inserted in gene</th>
    <th>Genomic Feature</th>--%>
    <th>Expression</th>


    <c:forEach var="construct" items="${formBean.constructList}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td class="bold">

                <a href="/${construct.ID}"> ${construct.name}</a>
            </td>
            <td>
                <c:if test="${construct.expressionFigureCount != 0}">
                    <%-- Case of a single figure --%>
                <c:if test="${construct.expressionFigureCount == 1}">
                    <zfin:link entity="${construct.singleFigure}"/>
                </c:if>
                    <%-- case of multiple figures --%>
                <c:if test="${construct.expressionFigureCount > 1}">
                <a href="construct-expression-summary?constructID=${construct.ID}&<%= request.getQueryString()%>">
                        <zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                                     integerEntity="${construct.expressionFigureCount}"/>
                        <%--</a>--%>
                    </c:if>
                        <zfin2:showCameraIcon hasImage="${construct.imageAvailable}"/>
                    </c:if>
            </td>


           <%-- <c:forEach var="featureGene" items="${construct.featureGenes}" varStatus="fgIndex" >
                <zfin:alternating-tr loopName="loop">
                    <td></td>
                    <td>

                            &lt;%&ndash;<zfin:link entity="${featureGene.alleleGene}"/>&ndash;%&gt;
                        <a href="/${featureGene.alleleGene.ID}"> ${featureGene.alleleGene.name}</a>



                    <td>
                        <c:if test="${featureGene.feature ne null}">

                            <zfin:link entity="${featureGene.feature}"/>

                        </c:if>
                        <c:if test="${featureGene.lab ne null}">
                            <a href="http://zebrafish.org/zirc/fish/lineAll.php?OID=${featureGene.feature.ID}"><font size="-1">(order this)</font></a>
                        </c:if>
                    </td>
                    <td></td>



                </zfin:alternating-tr>

            </c:forEach>
--%>
           <%-- <c:if test="${fn:length(construct.featureGenes) > 0 }">
               &lt;%&ndash; <c:choose>
                    <c:when test="${fn:length(construct.featureGenes) > 5 }">

                        <c:forEach var="featureGene" items="${construct.featureGenes}" varStatus="fgIndex" end="4">
        &lt;%&ndash;<tr div style="display:inline;" class="${construct.constructpkid}-short">&ndash;%&gt;
                            &lt;%&ndash;<zfin:alternating-tr loopName="loop" trStyleName="display:inline;" trNames="${construct.constructpkid}-short">&ndash;%&gt;
                            <zfin:alternating-tr loopName="loop" trStyleName="display:inline;" trNames="${construct.constructpkid}-short" >
                                <span style="display:inline;" name="${construct.constructpkid}-short">


                                &lt;%&ndash;<td  style="display:inline;" class="${construct.constructpkid}-short"></td>&ndash;%&gt;
                                <td  style="display:inline;" name="${construct.constructpkid}-short" colspan=3>                   </td>
                                    <td  style="display:inline;" name="${construct.constructpkid}-short" colspan=3>                   </td>
                                    <td  style="display:inline;" name="${construct.constructpkid}-short" colspan=3>                   </td>
                                    <td  style="display:inline;" name="${construct.constructpkid}-short" colspan=3>                   </td>

                                <td  style="display:inline;" name="${construct.constructpkid}-short">
                                    <a href="/${featureGene.alleleGene.ID}"> ${featureGene.alleleGene.name}</a>
                                <td  style="display:inline;" name="${construct.constructpkid}-short">
                                    <c:if test="${featureGene.feature ne null}">
                                        <zfin:link entity="${featureGene.feature}"/>
                                    </c:if>
                                    <c:if test="${featureGene.lab ne null}">
                                        <a href="http://zebrafish.org/zirc/fish/lineAll.php?OID=${featureGene.feature.ID}"><font
                                                size="-1">(order this)</font></a>
                                    </c:if>
                                    <c:if test="${(!fgIndex.last)}"><br/></c:if>
                                    <c:if test="${(fgIndex.last)}">
                                        <nobr>
                                            (<a href="javascript:onClick=showEntityList('${construct.constructpkid}', true)">all ${fn:length(construct.featureGenes)}</a>)
                                            <img onclick="showEntityList('${construct.constructpkid}', true)"

                                                 class="clickable"
                                                 src="/images/right_arrow.gif" alt="expand"
                                                 title="Show all ${fn:length(construct.featureGenes)} terms">
                                        </nobr>
                                    </c:if>
                                </td>
                                &lt;%&ndash;<td  style="display:inline;" class="${construct.constructpkid}-short"></td>&ndash;%&gt;
                                <td  style="display:inline;" name="${construct.constructpkid}-short"></td>
                               </span>
                            </zfin:alternating-tr>
                           &lt;%&ndash;</tr>&ndash;%&gt;
                        </c:forEach>



                        <c:forEach var="featureGene" items="${construct.featureGenes}" varStatus="fgIndex">
                            <zfin:alternating-tr loopName="loop" trStyleName="display:none;" trNames="${construct.constructpkid}-long">
                            &lt;%&ndash;<tr div style="display:none;" class="${construct.constructpkid}-long">&ndash;%&gt;
                                <span style="display:none;" name="${construct.constructpkid}-long">
                                &lt;%&ndash;<td div style="display:none;" class="${construct.constructpkid}-long"></td>
                                <td div style="display:none;" class="${construct.constructpkid}-long">&ndash;%&gt;
                                    <td></td>
                                    <td div style="display:none;" name="${construct.constructpkid}-long">
                                    <a href="/${featureGene.alleleGene.ID}"> ${featureGene.alleleGene.name}</a>
                                <td div style="display:none;" name="${construct.constructpkid}-long">
                                <c:if test="${featureGene.feature ne null}">
                                    <zfin:link entity="${featureGene.feature}"/>
                                </c:if>
                                <c:if test="${featureGene.lab ne null}">
                                    <a href="http://zebrafish.org/zirc/fish/lineAll.php?OID=${featureGene.feature.ID}"><font
                                            size="-1">(order this)</font></a>
                                </c:if>
                                <c:if test="${(!fgIndex.last)}"><br/></c:if>
                                <c:if test="${(fgIndex.last)}">
                                    <img onclick="showEntityList('${construct.constructpkid}', false)" class="clickable"
                                         src="/images/left_arrow.gif" alt="collapse" title="Show only first 5 terms">

                                </c:if>
                                </td>
                                &lt;%&ndash;<td div style="display:none;" class="${construct.constructpkid}-long"></td>&ndash;%&gt;
                                    <td></td>
                                    </span>

        </zfin:alternating-tr>
                                &lt;%&ndash;</tr>&ndash;%&gt;
                        </c:forEach>

                    </c:when>
                    <c:otherwise>&ndash;%&gt;
                        <c:forEach var="featureGene" items="${construct.featureGenes}" varStatus="fgIndex">
                           <zfin:alternating-tr loopName="loop">
                                <td></td>
                                <td>


                                    <a href="/${featureGene.alleleGene.ID}"> ${featureGene.alleleGene.name}</a>


                                <td>
                                    <c:if test="${featureGene.feature ne null}">

                                        <zfin:link entity="${featureGene.feature}"/>

                                    </c:if>
                                    <c:if test="${featureGene.lab ne null}">
                                        <a href="http://zebrafish.org/zirc/fish/lineAll.php?OID=${featureGene.feature.ID}"><font
                                                size="-1">(order this)</font></a>
                                    </c:if>
                                </td>
                                <td></td>


                            </zfin:alternating-tr>

                        </c:forEach>


                    &lt;%&ndash;</c:otherwise>
                </c:choose>&ndash;%&gt;
            </c:if>

--%>




        </zfin:alternating-tr>

    </c:forEach>
</table>
<input name="page" type="hidden" value="1" id="page"/>
<div style="float:right ; margin-top: 2px;">
    <select name="maxDisplayRecordsBottom" id="max-display-records-bottom">
        <c:forEach items="${formBean.recordsPerPageList}" var="option">
            <option>${option}</option>
        </c:forEach>
    </select>
    <label for="max-display-records-bottom">results per page</label>

</div>

<zfin2:pagination paginationBean="${formBean}"/>

<script>
    jQuery('#max-display-records-bottom').change(function () {

        setMaxDisplayRecords(jQuery('#max-display-records-bottom option:selected').val());
    });

</script>

