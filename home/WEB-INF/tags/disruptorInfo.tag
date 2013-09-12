<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%@ attribute name="markerBean" type="org.zfin.marker.presentation.DisruptorBean"
              rtexprvalue="true" required="true" %>
<%@ attribute name="marker" type="org.zfin.mutant.SequenceTargetingReagent"
              rtexprvalue="true" required="true" %>
<%@ attribute name="typeName" type="java.lang.String" required="false" rtexprvalue="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="true" %>

<c:if test="${empty typeName}">
    <c:set var="typeName">${marker.markerType.name}</c:set>
    <c:if test="${typeName eq 'MRPHLNO'}">
        <c:set var="typeName">Morpholino</c:set>
    </c:if>
</c:if>


<table class="primary-entity-attributes">

    <tr>
        <th class="data-label"><span class="name-label">${typeName}&nbsp;Name:</span></th>
        <td><span class="name-value"><zfin:name entity="${marker}"/></span></td>
    </tr>
    <tr>

        <%--targeted genes--%>
        <th><span class="name-label">Targeted Gene${fn:length(formBean.markerRelationshipPresentationList)>1 ? "s" : ""}:</span>
        </th>
        <td>
            <span class="">
                    <c:forEach var="entry" items="${formBean.markerRelationshipPresentationList}" varStatus="loop">
                        <i>${entry.link}</i> ${entry.attributionLink}
                        ${!loop.last ? ", " : ""}
                    </c:forEach>
            </span>
        </td>
    </tr>

    <c:if test="${!empty previousNames}">
        <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}"/>
    </c:if>

    <c:if test="${typeName ne 'Morpholino'}">
        <tr>
            <td>
                <b>Source:</b>
            </td>
            <td align="left" nowrap="true">
                <zfin2:orderThis markerSuppliers="${markerBean.suppliers}" accessionNumber="${marker.zdbID}"/>
            </td>
        </tr>
    </c:if>

    <tr>
        <td>
            <b>Sequence<c:if test="${typeName eq 'TALEN'}">&nbsp;1</c:if>:</b>
        </td>
        <td align="left" nowrap="true">
            <c:choose>
                <c:when test="${!empty marker.sequence}">
                    <div class="sequence">
                        5' - ${marker.sequence.sequence} - 3'
                        <c:if test="${!empty markerBean.sequenceAttribution}">
                            (${markerBean.sequenceAttribution})
                        </c:if>
                    </div>
                    &nbsp;&nbsp;&nbsp;
                    <c:if test="${typeName eq 'TALEN'}">
                        <c:set var="firstSeqLen">${fn:length(marker.sequence.sequence)}</c:set>
                        <c:set var="secondSeqLen">${fn:length(marker.sequence.secondSequence)}</c:set>
                        <c:if test="${firstSeqLen < secondSeqLen}">
                            <c:set var="loopEnd">${secondSeqLen - firstSeqLen}</c:set>
                            <c:forEach var="i" begin="1" end="${loopEnd}">&nbsp;</c:forEach>
                        </c:if>
                    </c:if>
                    <zfin2:markerSequenceBlastDropDown
                            sequence="${marker.sequence.sequence}"
                            databases="${markerBean.databases}"
                            instructions="Select Sequence Analysis Tool"
                            />
                    <br>
                </c:when>
                <c:otherwise>
                    <zfin2:noDataAvailable/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>

    <c:if test="${typeName eq 'TALEN'}">
        <tr>
            <td>
                <b>Sequence&nbsp;2:</b>
            </td>
            <td align="left" nowrap="true">
                <c:choose>
                    <c:when test="${!empty marker.sequence}">
                        <div class="sequence">
                            5' - ${marker.sequence.secondSequence} - 3'
                            <c:if test="${!empty markerBean.sequenceAttribution}">
                                (${markerBean.sequenceAttribution})
                            </c:if>
                        </div>
                        &nbsp;&nbsp;&nbsp;
                        <c:if test="${firstSeqLen > secondSeqLen}">
                            <c:set var="loopEnd">${firstSeqLen - secondSeqLen}</c:set>
                            <c:forEach var="i" begin="1" end="${loopEnd}">&nbsp;</c:forEach>
                        </c:if>
                        <zfin2:markerSequenceBlastDropDown
                                sequence="${marker.sequence.secondSequence}"
                                databases="${markerBean.databases}"
                                instructions="Select Sequence Analysis Tool"
                                />
                        <br>
                    </c:when>
                    <c:otherwise>
                        <zfin2:noDataAvailable/>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:if>
    <tr>
        <td>&nbsp;</td>
        <td>
            <c:if test="${!empty markerBean.sequence}">
                <small>
                    (Although ZFIN verifies reagent sequence data, we recommend that you conduct independent sequence
                    analysis before ordering any reagent.)
                </small>
            </c:if>
        </td>
    </tr>

    <zfin2:notesInDiv hasNotes="${formBean.marker}"/>

</table>



