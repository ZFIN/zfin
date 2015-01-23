<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relatissonships in a table --%>

<%@ attribute name="mutantsOnMarkerBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.MutantOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false" %>


<c:if test="${empty title}">
    <c:set var="title" value="MUTANTS AND TARGETED KNOCKDOWNS"/>
</c:if>

<zfin2:subsection title="${title}"
                  test="${!empty mutantsOnMarkerBean and (!empty mutantsOnMarkerBean.features or !empty mutantsOnMarkerBean.knockdownReagents)}"
                  showNoData="true">

    <table class="summary horizontal-solidblock">

        <c:if test="${!empty mutantsOnMarkerBean.genotypeList}">
            <tr>
                <td class="data-label"><b>Mutant lines:</b></td>
                <td>
                    <c:set var="numberOfGenotypes" value="${mutantsOnMarkerBean.genotypeList.size()}"/>
                    <c:choose>
                        <c:when test="${numberOfGenotypes == 1}">
                            <zfin:link entity="${mutantsOnMarkerBean.genotypeList.get(0)}"/>
                        </c:when>
                        <c:otherwise>
                            <a href="/action/mutant/mutant-list?zdbID=${marker.zdbID}">${numberOfGenotypes}
                                Genotypes</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            </c:if>
            <c:if test="${!empty mutantsOnMarkerBean.knockdownReagents}">
                <tr>
                    <td class="data-label"><b>Knockdown reagents:</b> </td>
                    <td>
                        <zfin2:toggledProvidesLinkList collection="${mutantsOnMarkerBean.knockdownReagents}" maxNumber="5"/>
                    </td>
                </tr>
            </c:if>
    </table>

    <c:if test="${!empty mutantsOnMarkerBean.features}">
        <br/>
        <div id="short-version">
                <table class="summary rowstripes">
                    <tr>
                        <th width="10%">Allele</th>
                        <th width="13%">Type</th>


                        <th width="10%">Mutagen</th>
                        <th width="50%">Suppliers</th>
                    </tr>

                    <c:forEach var="feature" items="${mutantsOnMarkerBean.features}" varStatus="loop" end="4">
                        <tr class=${loop.index%2==0 ? "even" : "odd"}>
                            <td>
                                <a href="/${feature.zdbID}">${feature.abbreviation}</a>
                            </td>
                            <td>
                                    ${feature.type.display}
                            </td>
                            <td>
                                <c:set var="mutagen" value="${feature.featureAssay.mutagen}"/>
                                <c:choose>
                                    <c:when test="${mutagen ne zfn:getMutagen('not specified')}">
                                        ${feature.featureAssay.mutagen}
                                    </c:when>
                                </c:choose>
                            </td>
                            <td>
                                <c:forEach var="supplier" items="${feature.suppliers}">
                                       <li style="list-style-type: none;">
                                            <a href="/${supplier.organization.zdbID}"> ${supplier.organization.name}</a>
                                                <c:if test="${!empty supplier.orderURL}"> <a href="${supplier.orderURL}"> (order
                                                    this)</a>
                                                </c:if>
                                       </li>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                </table>

            </tr>
            <div>

                <c:if test="${mutantsOnMarkerBean.features.size() > 5}">
                    &nbsp;&nbsp;
                    <a href="javascript:expand()">
                        <img src="/images/darrow.gif" alt="expand" border="0">
                        Show all</a>
                    ${mutantsOnMarkerBean.features.size()} alleles
                </c:if>
            </div>
        </div>

        <div style="display:none" id="long-version">
            <table class="summary rowstripes">
                <tr>
                    <th width="10%">Allele</th>
                    <th width="13%">Type</th>


                    <th width="10%">Mutagen</th>
                    <th width="50%">Suppliers</th>
                </tr>

                <c:forEach var="feature" items="${mutantsOnMarkerBean.features}" varStatus="loop">
                    <tr class=${loop.index%2==0 ? "even" : "odd"}>
                        <td>
                            <a href="/${feature.zdbID}">${feature.abbreviation}</a>
                        </td>
                        <td>
                                ${feature.type.display}
                        </td>
                        <td>
                            <c:set var="mutagen" value="${feature.featureAssay.mutagen}"/>
                            <c:choose>
                                <c:when test="${mutagen ne zfn:getMutagen('not specified')}">
                                    ${feature.featureAssay.mutagen}

                                </c:when>
                            </c:choose>
                        </td>
                        <td>
                            <c:forEach var="supplier" items="${feature.suppliers}">
                                <li style="list-style-type: none;">
                                    <a href="/${supplier.organization.zdbID}"> ${supplier.organization.name}</a>
                                    <c:if test="${!empty supplier.orderURL}"> <a href="${supplier.orderURL}"> (order
                                        this)</a>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <div>
                &nbsp;&nbsp;
                <a href="javascript:collapse()">
                    <img src="/images/up.gif" alt="expand" title="Show first 5 alleles" border="0">
                    Show first</a> 5 alleles
            </div>
        </div>
    </c:if>

    <script type="text/javascript">
        function expand() {
            document.getElementById('short-version').style.display = 'none';
            document.getElementById('long-version').style.display = 'inline';
        }

        function collapse() {
            document.getElementById('short-version').style.display = 'inline';
            document.getElementById('long-version').style.display = 'none';
            window.scrollTo(0,0);

        }
    </script>
</zfin2:subsection>

