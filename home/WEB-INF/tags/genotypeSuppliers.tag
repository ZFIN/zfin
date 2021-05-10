<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="genotype" required="true" type="org.zfin.mutant.Genotype" %>

<tr>
    <th>
        <c:choose>
            <c:when test="${fn:length(genotype.suppliers) ne null && fn:length(genotype.suppliers) > 1}">
                Current&nbsp;Sources:
            </c:when>
            <c:otherwise>
                Current&nbsp;Source:
            </c:otherwise>
        </c:choose>
    </th>
    <td>
        <c:choose>
            <c:when test="${genotype.suppliers ne null && fn:length(genotype.suppliers) > 0}">
                <c:forEach var="supplier" items="${genotype.suppliers}" varStatus="status">
                    <c:choose>
                        <c:when test="${genotype.extinct}">
                            ${supplier.organization.name}&nbsp;&nbsp;<font size="3" color="red">Extinct</font> <i class="warning-icon" title="Extinct"></i>>&nbsp;
                        </c:when>
                        <c:otherwise>
                            <a href="/action/profile/view/${supplier.organization.zdbID}"
                               id="${supplier.organization.zdbID}">
                                    ${supplier.organization.name}</a>
                            <c:if test="${supplier.availState ne null}">(${supplier.availState})</c:if>
                            <c:choose>
                                <c:when test="${supplier.moensLab}">&nbsp;
                                    <c:forEach var="affectedGene"
                                               items="${genotype.affectedGenes}"
                                               varStatus="loop">
                                        (<a href="http://labs.fhcrc.org/moens/Tilling_Mutants/${affectedGene.abbreviation}"><font size="-1">request this mutant</font></a>)
                                        <c:if test="${!loop.last}">,&nbsp;
                                        </c:if>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${supplier.solnicaLab}">&nbsp;
                                        <c:forEach var="affectedGene"
                                                   items="${genotype.affectedGenes}"
                                                   varStatus="loop">
                                            (<a href="http://devbio.wustl.edu/solnicakrezellab/${affectedGene.abbreviation}.htm"><font size="-1">request this mutant</font></a>)
                                            <c:if test="${!loop.last}">,&nbsp;</c:if>
                                        </c:forEach>
                                    </c:if>
                                    <zfin2:orderThis accessionNumber="${genotype.zdbID}"
                                                     organization="${supplier.organization}"/>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${!status.last}"><br/></c:if>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${genotype.extinct}">
                        <font size="3" color="red">extinct</font> <i class="warning-icon" title="Extinct"></i>
                    </c:when>
                    <c:otherwise>
                        No data available
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </td>
</tr>