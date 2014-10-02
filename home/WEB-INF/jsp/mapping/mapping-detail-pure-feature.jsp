<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="marker" class="org.zfin.marker.Marker" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td class="titlebar" style="">
                <span style="font-size: x-large; margin-left: 0.5em; font-weight: bold;">
                        Mapping Details
            </span>
        </td>
        <td align="right" class="titlebarRight">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Mapping Details"/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>


<table class="primary-entity-attributes">
    <tr>
        <th>Genomic Feature Name:</th>
        <td><span class="name-value"><zfin:link entity="${feature}"/></span></td>
    </tr>
</table>

<div class="summary">
    <c:choose>
        <c:when test="${!otherMappingDetail}">
            <b>OTHER MAPPING INFORMATION:</b> <span class="no-data-tag">No data available</span>
        </c:when>
        <c:otherwise>
            <zfin2:otherMappingDetailFeature/>
        </c:otherwise>
    </c:choose>
</div>

<zfin2:showSingletonInfo singleLinkageList="${singletonFeatureList}"/>
