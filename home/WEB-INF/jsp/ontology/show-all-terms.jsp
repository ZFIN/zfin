<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>
<jsp:useBean id="query" class="java.lang.String" scope="request"/>
<jsp:useBean id="labelMap" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="termGroups" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="terms" class="java.util.ArrayList" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<script type="text/javascript">
    var ontologyDisplay = "All";
</script>

<c:choose>
    <c:when test="${query == ''}">
        All Anatomical Terms in alphabetical order:
    </c:when>
    <c:otherwise>
        <table>
            <tr>
                <td>
                    <c:if test="${labelMap != null && labelMap.size() > 1}">
                        <c:forEach var="ontologyMap" items="${termGroups}">
                            <span id="showTermsFrom-${ontologyMap.key}" style="display: none" class="hideAll">
                                Show ${ontologyMap.value.size()}
                                <zfin:choice collectionEntity="${ontologyMap.value}" choicePattern="0# Terms| 1# Term| 2# Terms"
                                             scope="Request"/> from
                            </span>
                        </c:forEach>
                        <label>
                            <select name="ontologyName" onchange="selectOntology(this);">
                                <option value="" selected="selected">All</option>
                                <c:forEach var="ontologyMap" items="${labelMap}">
                                    <option value="${ontologyMap.key}">${ontologyMap.value}</option>
                                </c:forEach>
                            </select>
                        </label>
                    </c:if>
                    <c:if test="${labelMap != null && labelMap.size() == 1}">
                        <c:forEach var="ontologyMap" items="${termGroups}" end="0">
                        All ${ontologyMap.value.size()} Terms
                        </c:forEach>
                    </c:if>
                </td>
                <td>
                    &nbsp;for: <span style="font-weight: bold;"> <c:out value="${query}" /> </span>
                </td>
            </tr>
        </table>
    </c:otherwise>
</c:choose>

<c:forEach var="ontologyMap" items="${termGroups}">
    <c:choose>
        <c:when test="${ontologyMap.key eq 'All'}">
            <div id="termResults-${ontologyMap.key}" class="hideAll" style="display: inline">
        </c:when>
        <c:otherwise>
            <div id="termResults-${ontologyMap.key}" class="hideAll" style="display: none">
        </c:otherwise>
    </c:choose>
    <zfin2:displayTermResults query="${query}" termList="${ontologyMap.value}" showOntologyColumn="true"/>
    </div>
</c:forEach>
<c:if test="${termGroups eq null || termGroups.size() == 0}">
    <zfin2:displayTermResults query="${query}" termList="${terms}"/>
</c:if>

<script type="text/javascript">
    jQuery('#showTermsFrom-All').show();

    function selectOntology(selection) {
        var ontology = selection.options[selection.selectedIndex].text;
        //alert('Selection: '+sel);
        jQuery('.hideAll').each(function () {
            jQuery(this).hide();
        });
        jQuery('#termResults-' + ontology).show();
        jQuery('#showTermsFrom-' + ontology).show();
    }
</script>