<%@ taglib prefix="zfin"    uri="/WEB-INF/tld/zfin-tags.tld"%>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%@ attribute name="markerBean" type="org.zfin.marker.presentation.MorpholinoBean"
              rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.marker.Marker"
              rtexprvalue="true" required="true" %>
<%@ attribute name="typeName" type="java.lang.String" required="false" rtexprvalue="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>

<c:if test="${empty typeName}">
    <c:set var="typeName">${marker.markerType.name}</c:set>
</c:if>

<script type="text/javascript" language="JavaScript">
    function jump(form_select){
        if (0 != form_select.selectedIndex){
            window.open(form_select.options[form_select.selectedIndex].value,parseInt(Math.random()*2000000000));
        }
        return true;
    }
</script>


<table class="primary-entity-attributes">

    <tr>
      <th class="data-label"><span class="name-label">${typeName}&nbsp;Name:</span></th>
      <td><span class="name-value"><zfin:name entity="${marker}"/></span></td>
    </tr>
    <tr>

        <%--targeted genes--%>
        <th><span class="name-label">Targeted Gene${fn:length(formBean.markerRelationshipPresentationList)>1 ? "s" : ""}:</span></th>
        <td>
            <span class="">
                    <c:forEach var="entry" items="${formBean.markerRelationshipPresentationList}" varStatus="loop">
                        ${entry.link} ${entry.attributionLink}
                        ${!loop.last ? ", " : ""}
                    </c:forEach>
            </span>
        </td>
    </tr>

    <c:if test="${!empty previousNames}">
        <zfin2:previousNamesFast label="Previous Names:" previousNames="${previousNames}"/>
    </c:if>

    <tr>
        <td>
            <b>Sequence:</b>
        </td>
        <td align="left" nowrap="true">
            5' - ${markerBean.sequence.sequence} - 3'
            <c:if test="${!empty markerBean.sequenceAttribution}">
                (${markerBean.sequenceAttribution})
            </c:if>
            &nbsp;
            &nbsp;
            <zfin2:markerSequenceBlastDropDown
                    sequence="${markerBean.sequence.sequence}"
                    databases="${markerBean.databases}"
                    instructions="Select Sequence Analysis Tool"
                    />
            <br>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <small>
                (Although ZFIN verifies reagent sequence data, we recommend that you conduct independent sequence analysis before ordering any reagent.)
            </small>
        </td>
    </tr>

    <zfin2:notesInDiv hasNotes="${formBean.marker}"/>

</table>


