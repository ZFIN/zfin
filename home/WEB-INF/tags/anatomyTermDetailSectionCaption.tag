<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="anatomyStatistics" type="org.zfin.anatomy.AnatomyStatistics" required="true" %>
<%@ attribute name="substructureSearchLink" type="java.lang.String" required="false" %>
<%@ attribute name="structureSearchLink" type="java.lang.String" required="true" %>
<%@ attribute name="choicePattern" type="java.lang.String" required="true" %>
<%@ attribute name="anatomyItem" type="org.zfin.ontology.Term" required="true" %>

<%@ attribute name="recordsExist" type="java.lang.Boolean" required="true" %>
<%@ attribute name="allRecordsAreDisplayed" type="java.lang.Boolean" required="true" %>
<%@ attribute name="displayImages" type="java.lang.Boolean" required="false" %>
<%@ attribute name="useWebdriverURL" type="java.lang.Boolean" required="false" %>
<%@ attribute name="totalRecordCount" type="java.lang.Integer" required="true" %>
<%@ attribute name="imageCount" type="java.lang.Integer" required="false" %>

<%--
* This shows different captions in case there are more records than the section
* can display:
* 1) Also no substructures have an annotation: Display 'No data available'
* 2) substructures have an annotation: Display link to search page
--%>
<c:choose>
    <c:when test="${recordsExist}">
        <c:choose>
            <c:when test="${allRecordsAreDisplayed}">
                <c:if test="${anatomyStatistics.numberOfTotalDistinctObjects > 0 &&
                          anatomyStatistics.numberOfTotalDistinctObjects > totalRecordCount }">
                    <table width="100%">
                        <tbody>
                        <tr align="left">
                            <td>
                                Show all
                                <c:choose>
                                    <c:when test="${substructureSearchLink ne null}">
                                        <a href='${substructureSearchLink}'>
                                            <zfin:choice choicePattern="${choicePattern}"
                                                         integerEntity="${anatomyStatistics.numberOfTotalDistinctObjects}"
                                                         includeNumber="true"/>
                                        </a>
                                        in substructures
                                    </c:when>
                                    <c:otherwise>
                                        <zfin:choice choicePattern="${choicePattern}"
                                                     integerEntity="${anatomyStatistics.numberOfTotalDistinctObjects}"
                                                     includeNumber="true"/>
                                        in substructures
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </c:if>
            </c:when>
            <c:otherwise>

                <table width="100%">
                    <tbody>
                    <tr align="left">
                        <td>
                            Show all
                            <c:choose>
                                <c:when test="${useWebdriverURL}">
                                    <c:set var="webdriver" value='/${webdriverURL}${structureSearchLink}'/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="webdriver" value="${structureSearchLink}"/>
                                </c:otherwise>
                            </c:choose>
                            <a href="${webdriver}">
                                    ${totalRecordCount}
                                <zfin:choice choicePattern="${choicePattern}"
                                             integerEntity="${totalRecordCount}"/>
                                <c:if test="${displayImages}">,
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${imageCount}"
                                                 includeNumber="true"/></c:if></a> &nbsp;
                            <c:if test="${anatomyStatistics.numberOfTotalDistinctObjects > totalRecordCount }">
                                (including substructures
                                <c:choose>
                                    <c:when test="${substructureSearchLink ne null}">
                                        <a href='${substructureSearchLink}'>
                                            <zfin:choice choicePattern="${choicePattern}"
                                                         integerEntity="${anatomyStatistics.numberOfTotalDistinctObjects}"
                                                         includeNumber="true"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <zfin:choice choicePattern="${choicePattern}"
                                                     integerEntity="${anatomyStatistics.numberOfTotalDistinctObjects}"
                                                     includeNumber="true"/> 
                                    </c:otherwise>
                                </c:choose>)
                            </c:if>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise> <!-- no record exists -->
        <c:choose>
            <c:when test="${anatomyStatistics.numberOfTotalDistinctObjects > 0}">
                </br>No data for '${anatomyItem.termName}'.
                Show all <a
                    href='${substructureSearchLink}'>
                <zfin:choice choicePattern="${choicePattern}"
                             integerEntity="${anatomyStatistics.numberOfTotalDistinctObjects}"
                             includeNumber="true"/></a> in substructures.
            </c:when>
            <c:otherwise>
                </br>No data available.
            </c:otherwise>
        </c:choose>

    </c:otherwise>
</c:choose>

