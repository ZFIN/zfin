<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<zfin-ontology:anatomy-search-form formBean="${formBean}"/>

<script type="text/javascript">
    var ontologyDisplay = "All";
</script>
<TABLE width="98%">
    <TR>
        <TD>
            <c:choose>
                <c:when test="${query == ''}">
                    All Anatomical Terms in alphabetical order:
                </c:when>
                <c:otherwise>
                    <table width="100%">
                        <tr>
                            <td>
                                <c:if test="${labelMap != null && labelMap.size() > 1}">
                                    Show Terms only from <select name="ontologyName" onchange="selectOntology(this);">
                                    <option value="" selected="selected">All</option>
                                    <c:forEach var="ontologyMap" items="${labelMap}">
                                        <option value="${ontologyMap.key}">${ontologyMap.value}</option>
                                    </c:forEach>
                                </select>
                                </c:if>
                            </td>
                            <td>
                                <zfin:collectionSize collectionEntity="${terms}"/>
                                <zfin:choice collectionEntity="${terms}" choicePattern="0# Terms| 1# Term| 2# Terms"
                                             scope="Request"/>
                                for: <span style="font-weight: bold;"> ${query} </span>
                            </td>
                        </tr>
                    </table>
                </c:otherwise>
            </c:choose>

            <HR width=500 size=1 noshade align=left/>

            <c:forEach var="ontologyMap" items="${termGroups}">
            <c:choose>
            <c:when test="${ontologyMap.key eq 'All'}">
            <div id="termResults-${ontologyMap.key}" class="hideAll" style="display: inline">
                </c:when>
                <c:otherwise>
                <div id="termResults-${ontologyMap.key}" class="hideAll" style="display: none">
                    </c:otherwise>
                    </c:choose>
                    <zfin2:displayTermResults query="${query}" termList="${ontologyMap.value}"/>
                </div>
                </c:forEach>
                <c:if test="${termGroups eq null}">
                    <zfin2:displayTermResults query="${query}" termList="${terms}"/>
                </c:if>
        </TD>
    </TR>
</TABLE>


<script type="text/javascript">
    function selectOntology(selection) {
        var ontology = selection.options[selection.selectedIndex].text;
        //alert('Selection: '+sel);
        jQuery('.hideAll').each(function () {
            jQuery(this).hide();
        });
        jQuery('#termResults-' + ontology).show();
    }
</script>