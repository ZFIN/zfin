<%@ tag import="org.zfin.anatomy.AnatomyItem" %>
<%@ tag import="org.zfin.anatomy.DevelopmentStage" %>
<%@ tag import="org.zfin.anatomy.presentation.AnatomySearchBean" %>
<%@ tag import="org.zfin.anatomy.presentation.StagePresentation" %>
<%@ tag import="org.zfin.framework.presentation.SectionVisibility" %>
<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ tag import="static org.zfin.framework.presentation.SectionVisibility.Action.SHOW_ALL" %>
<%
    /*
    This tag is used to display the [Show All] or [Hide All] link

    Parameters:
    1) propertyName: the attribute name of the variable on the form bean used to show or hide
    a particular section.
    2) showLink: a boolean flag that indicates if a showAll/hideAll link should be displayed at all.
    If no data is available we do not want to expand just to ofind out no data is available.
    3) enumeration: the String array of the section names. Used to remove any visiblity-related query strings on the
    request object.
    4) sectionVisiblity: The section visiblity object used to
    */

%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="sectionVisibility" type="org.zfin.framework.presentation.SectionVisibility" required="true" %>

<%@attribute name="propertyName" type="java.lang.String" required="false" %>
<%@attribute name="enumeration" type="java.lang.String[]" required="false" %>

<span id="show-hide-all-new">
    <c:if test="${sectionVisibility.hasData}">
        <c:choose>
            <c:when test="${sectionVisibility.allSectionsVisible}">
                <a href="javascript:showAllSections(sections, false)"><img src="/images/minus.png" alt="collapse" border="0" style="vertical-align:middle;"></a> <b>ALL</b>
            </c:when>
            <c:otherwise>
                <a href="javascript:showAllSectionsInitial()"><img src="/images/plus.png" alt="expand" border="0" style="vertical-align:middle;"></a> <b>ALL</b>
            </c:otherwise>
        </c:choose>
    </c:if>
    </span>

<span id="show-all" style="display:none" class="summary">
    <a href="javascript:showAllSections(sections, true)"><img src="/images/plus.png" alt="expand" border="0" style="vertical-align:middle;"></a> <b>ALL</b>
</span>

<span id="hide-all" style="display:none" class="summary">
    <a href="javascript:showAllSections(sections, false)"><img src="/images/minus.png" alt="collapse" border="0" style="vertical-align:middle;"></a> <b>ALL</b>
</span>

<script type="text/javascript">
    var sections = new Array();
    <c:forEach var="section" items="${enumeration}" varStatus="loop" >
    sections[${loop.index}] = '${section}';
    </c:forEach>
</script>

<script type="text/javascript">
    <%
    /*
    This array holds all the expanded sections and marks for each if it was expanded at some time.
     We need to keep track of it so we do not go back to the server for data that were already retrieved at some
     point iin time.
     If all individual sections have been expanded the Show All
     link should turn into Hide All.
     */
    %>
    var expandedSectionMap = new Array();
    var numberOfSections = ${fn:length(enumeration)};

    <c:forEach var="section" items="${enumeration}" varStatus="loop">
    expandedSectionMap[${loop.index}] = new ExpandedSection('${section}', false);
    </c:forEach>

    function ExpandedSection(sectionName, expandedAtLeastOnce) {
        this.sectionName = sectionName;
        this.expanded = expandedAtLeastOnce;
    }

    function addExpandedSection(sectionName) {
        // record if a section was expanded so it does not need to
        // create another server call.
        for (var i = 0; i < expandedSectionMap.length; i++) {
            if (expandedSectionMap[i].sectionName == sectionName) {
                expandedSectionMap[i].expanded = true;
            }
        }
        var numberOfElements = getNumberOfExpandedSections();
        //window.alert('Number of expanded sections / elements: ' + numberOfSections+' / ' +numberOfElements);
        if (numberOfElements == numberOfSections) {
            switchShowHideAllLink(true);
        }
    }

    function getNumberOfExpandedSections() {
        var number = 0;
        for (var i = 0; i < expandedSectionMap.length; i++) {
            if (expandedSectionMap[i].expanded) {
                number++;
            }
        }
        return number;
    }

    function showAllSections(sectionIDs, show) {
        for (j = 0; j < sectionIDs.length; j++) {
            showSection(sectionIDs[j], show);
        }
        // showAll/hideAll elements
        switchShowHideAllLink(show);
    }

    function switchShowHideAllLink(show) {
        // showAll/hideAll elements
        var showElement = document.getElementById("show-all");
        var hideElement = document.getElementById("hide-all");
        var showHideElement = document.getElementById("show-hide-all-new");
        if (show) {
            hideElement.style.display = "inline";
            showElement.style.display = "none";
            showHideElement.style.display = "none";
        } else {
            showElement.style.display = "inline";
            hideElement.style.display = "none";
            showHideElement.style.display = "none";
        }
    }

    // This function is called if not all sections have been expanded.
    function showAllSectionsInitial() {
    <c:forEach var="section" items="${enumeration}">
        for (i = 0; i < expandedSectionMap.length; i++) {
            if (expandedSectionMap[i].sectionName == '${section}') {
                if (!expandedSectionMap[i].expanded)
                    show_${section}();
            }
        }
    </c:forEach>
        var initialShowHideElement = document.getElementById("show-hide-all-new");
        var hideAllElement = document.getElementById("hide-all");
        initialShowHideElement.style.display = "none";
        hideAllElement.style.display = "inline";
    }
</script>


