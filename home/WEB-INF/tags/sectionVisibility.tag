<%@ tag import="org.zfin.framework.presentation.SectionVisibility" %>
<%
    /*
    This tag is used to display show/hide links on a given section.
    The section name and the visibility map is passed in.
    The third parameter is a boolean triggering to display the show/hide feature at all.
    If there are no data within the section to be expanded you probably do not
    want to offer a 'show' link.
    */

%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="sectionName" type="java.lang.String" required="true" %>
<%@attribute name="sectionVisibility" type="org.zfin.framework.presentation.SectionVisibility" required="true" %>
<%@attribute name="showLink" type="java.lang.Boolean" required="true" %>
<%@attribute name="enumeration" type="java.lang.String[]" required="true" %>

<%@attribute name="propertyName" type="java.lang.String" required="false" %>
<%-- hyperlinke name attribute of links that need to be adjusted in order
     to keep the expansion/collapse state--%>
<%@attribute name="hyperlinkName" type="java.lang.String" required="false" %>
<% // Only in conjunction with the showAll tag some functions are defined %>
<%@attribute name="showAllUsed" type="java.lang.Boolean" required="false" %>
<% // Name of section for display %>
<%@attribute name="displaySectionName" type="java.lang.String" required="false" %>

<c:if test="${propertyName == null}">
    <c:set var="propertyName" scope="page">
        sectionVisibility.
    </c:set>
</c:if>

<span id="${sectionName}-show-hide-new">
    <c:choose>
        <c:when test="${showLink}">
            <c:choose>
                <c:when test="${zfn:isSectionVisible(sectionName,sectionVisibility)}">
                    <span onclick="javascript:showSection('${sectionName}', false)"
                          class="summaryTitle" onmouseover="this.style.cursor='pointer'">${displaySectionName}</span>
                    (<a href="javascript:showSection('${sectionName}', false)">
                    <img src="/images/minus-13.png" alt="collapse" border="0" style="vertical-align:absmiddle;"></a>
                </c:when>
                <c:otherwise>

                    <a href="javascript:show_${sectionName}()" title="Click to see all data.!">
                        <img src="/images/plus-13.png" alt="expand" border="0" style="vertical-align:absmiddle;"></a>
                    &nbsp;
                    <a onclick="javascript:show_${sectionName}()" class="summaryTitle"
                       onmouseover="this.style.cursor='pointer'"
                       title="Click to see all data.">${displaySectionName}</a>
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:otherwise>
            <span class="summaryTitle" style="vertical-align:top"> ${displaySectionName}</span>: (No data available)
        </c:otherwise>
    </c:choose>
</span>

<span id="${sectionName}-show" style="display:none">
    <a href="javascript:showSection('${sectionName}', true)" title="Click to see all data." >
        <img src="/images/plus-13.png" alt="expand" border="0" style="vertical-align:absmiddle;"></a>
    &nbsp;
    <a onclick="javascript:showSection('${sectionName}', true)" 
       onmouseover="this.style.cursor='pointer'" title="Click to see all data.">${displaySectionName}</a>
</span>

<span id="${sectionName}-hide" style="display:none">
    <a href="javascript:showSection('${sectionName}', false)" title="Click to hide section.">
        <img src="/images/minus-13.png" alt="collapse" border="0" style="vertical-align:absmiddle;" class="summaryTitle"></a>
    &nbsp;
    <a onclick="javascript:showSection('${sectionName}', false)"
       onmouseover="this.style.cursor='pointer'" title="Click to hide section.">${displaySectionName}</a>
</span>

<script type="text/javascript">
    function showSection(sectionID, show) {
        var element = document.getElementById(sectionID + "-id");
        var showElement = document.getElementById(sectionID + "-show");
        var hideElement = document.getElementById(sectionID + "-hide");
        var showHideElement = document.getElementById(sectionID + "-show-hide-new");
    <c:if test="${hyperlinkName != null}">
        var links = new Array();
        //window.alert("sectionID: " + sectionID);
        links = document.getElementsByName('${hyperlinkName}');
        for (i = 0; i < links.length; i++) {
            var link = links[i].href;
            links[i].href = replaceVisibilityParameter(link, sectionID, show);
        }
    </c:if>
        if (show) {
            element.style.display = "inline";
            hideElement.style.display = "inline";
            showElement.style.display = "none";
        <c:if test="${showAllUsed}">
            addExpandedSection(sectionID);
        </c:if>
        } else {
            element.style.display = "none";
            showElement.style.display = "inline";
            hideElement.style.display = "none";
        <c:if test="${showAllUsed}">
            switchShowHideAllLink(false)
        </c:if>
        }
        // always turn off the element displayed by default upon loading first time.
        showHideElement.style.display = "none";
    }

    function replaceVisibilityParameter(link, sectionID, show) {
        var addParameter = '${propertyName}';
        var removeParameter = '${propertyName}';
        if (show) {
            addParameter += '<%= SectionVisibility.Action.SHOW_SECTION.toString()%>';
            addParameter += '=' + sectionID;
            if (link.indexOf(addParameter) == -1) {
                link += "&" + addParameter;
            }
        } else {
            removeParameter += '<%= SectionVisibility.Action.SHOW_SECTION.toString()%>';
            removeParameter += '=' + sectionID;
            link = link.replace("&" + removeParameter, "");
        }
        return link;
    }

</script>
