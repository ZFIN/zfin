<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/copyToClipboard.js" type="text/javascript"></script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        var $overlay = jQuery("#generate-reference-overlay"), $triggerButton = jQuery("#generate-reference-button");
        $overlay.appendTo(jQuery("body"));
        $overlay.on(jQuery.modal.CLOSE, function () {
        });

        $triggerButton.click(function (evt) {
            evt.preventDefault();
            $overlay.modal({
                fadeDuration: 100
            });
        });

    });
</script>

<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>

<c:if test="${allowDelete}">
    <c:set var="deleteURL">/action/infrastructure/deleteRecord/${publication.zdbID}</c:set>
</c:if>

<c:set var="trackURL">/action/publication/${publication.zdbID}/track</c:set>

<c:set var="linkURL">/cgi-bin/webdriver?MIval=aa-link_authors.apg&OID=${publication.zdbID}&anon1=zdb_id&anon1text=${publication.zdbID}</c:set>

<c:if test="${allowCuration}">
    <c:set var="curateURL">/cgi-bin/webdriver?MIval=aa-curation.apg&OID=${publication.zdbID}</c:set>
</c:if>

<zfin2:dataManager zdbID="${publication.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   trackURL="${trackURL}"
                   linkURL="${linkURL}"
                   curateURL="${curateURL}"
                   rtype="publication"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${publication.zdbID}"/>
    </tiles:insertTemplate>
</div>

<div style="text-align: center; font-size: x-large; margin-top: 1em; ">
    ${publication.title}
</div>

<div style="text-align: center; font-weight: bold">
    ${publication.authors}
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Date:</th>
        <td><fmt:formatDate value="${publication.publicationDate.time}" type="Date" pattern="yyyy"/></td>
    </tr>
    <tr>
        <th>Source:</th>
        <td>
            ${publication.journal.name}<c:if test="${!empty publication.volume}">&nbsp;</c:if>${publication.volume}:
            ${publication.pages} (${publication.type.display})

            <span style="padding-left: 1em;">
                    <a href="#" id="generate-reference-button" rel="#generate-reference-overlay"><button>Generate reference</button></a>
            </span>
        </td>
    </tr>
    <tr>
        <th>Registered Authors:</th>
        <td>
            <zfin:link entity="${publication.people}"/>
        </td>
    </tr>
    <tr>
        <th>Keywords:</th>
        <td>
            <c:choose>
                <c:when test="${!empty publication.keywords}">
                    ${publication.keywords}
                </c:when>
                <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
            </c:choose>
        </td>
    </tr>
    <c:if test="${!empty publication.dbXrefs}">
        <tr>
            <th>Microarrays:</th>
            <td>
                <c:forEach var="xref" items="${publication.dbXrefs}" varStatus="loop">
                    <zfin:link entity="${xref}"/><c:if test="${!loop.last}">, </c:if>
                </c:forEach>
            </td>
        </tr>
    </c:if>
    <tr>
        <th>MeSH Terms:</th>
        <td>
            <c:forEach var="meshHeading" items="${publication.meshHeadings}" varStatus="idx1">
                <c:forEach var="displayString" items="${meshHeading.displayList}" varStatus="idx2">
                    ${displayString}<c:if test="${!idx1.last || !idx2.last}">; </c:if>
                </c:forEach>
            </c:forEach>
        </td>
    </tr>

    <tr>
        <th>PubMed:</th>
        <td>
            <c:choose>
                <c:when test="${!empty publication.accessionNumber}">
                    <a href="http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=search&db=PubMed&dopt=Abstract&term=${publication.accessionNumber}">
                            ${publication.accessionNumber}
                    </a>
                </c:when>
                <c:otherwise>
                    <span class="no-data-tag">none</span>
                </c:otherwise>
            </c:choose>
            <span style="padding-left: 2em;">
                <zfin-figure:journalAbbrev publication="${publication}"/>
            </span>
        </td>
    </tr>

    <authz:authorize access="hasRole('root')">

        <tr>
            <th>File:</th>
            <td>
                <c:if test="${not empty publication.fileName}">
                    <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}">PDF</a>
                </c:if>
            </td>
        </tr>

        <tr>
            <th>Curation Status:</th>
            <td>${curationStatusDisplay}</td>
        </tr>
    </authz:authorize>

</table>

<div class="jq-modal" id="generate-reference-overlay" style="width: auto; height: auto; padding: 20px 20px; margin-right: 150px;">
    <div class="popup-content">
        <div class="popup-header">
            Citation
        </div>
        <div class="popup-body" id="generate-reference-body">
            ${publication.printable}
        </div>
    </div>
</div>

<%--todo: this should probably change both visually and in the code, doesn't match UI guidelines, so there's no classes for it and it seems wrong to make them --%>

<c:if test="${showFiguresLink}">
    <div style="margin-top: 1em;">
        <a href="/action/figure/all-figure-view/${publication.zdbID}" style="font-weight: bold">FIGURES</a>
        &nbsp;
        <span style="font-size: small">(<a href="javascript:start_note();">current status</a>)</span>
    </div>
</c:if>

<zfin2:subsection title="ABSTRACT" test="${not empty abstractText}" showNoData="true">
    ${abstractText}
</zfin2:subsection>

<zfin2:subsection title="ADDITIONAL INFORMATION" showNoData="true" test="${showAdditionalData}">

    <ul>
        <c:if test="${markerCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_mrkr">Genes /
                Markers</a> (${markerCount})
            </li>
        </c:if>
        <c:if test="${morpholinoCount > 0}">
            <li>
                <a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_mo">Morpholino</a>
                (${morpholinoCount})
            </li>
        </c:if>
        <c:if test="${talenCount > 0}">
            <li>
                <a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_talen">TALEN</a>
                (${talenCount})
            </li>
        </c:if>
        <c:if test="${crisprCount > 0}">
            <li>
                <a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_crispr">CRISPR</a>
                (${crisprCount})
            </li>
        </c:if>
        <c:if test="${antibodyCount > 0}">
            <li><a href="/action/antibody/antibodies-per-publication/${publication.zdbID}" id="list-of-antibodies">Antibodies</a>
                (${antibodyCount})
            </li>
        </c:if>
        <c:if test="${efgCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_efg">Engineered
                Foreign Genes</a> (${efgCount})
            </li>
        </c:if>
        <c:if test="${cloneProbeCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-msegselect.apg&pubId=${publication.zdbID}">Clones and Probes</a>
                (${cloneProbeCount})
            </li>
        </c:if>
        <c:if test="${expressionCount > 0 || phenotypeCount > 0}">
            <li><a href="/action/figure/all-figure-view/${publication.zdbID}">${expressionAndPhenotypeLabel}</a></li>
        </c:if>
        <c:if test="${mappingDetailsCount > 0}">
            <li><a href="/action/mapping/publication/${publication.zdbID}">Mapping Details</a> (${mappingDetailsCount})
            </li>
        </c:if>
        <c:if test="${featureCount > 0}">

            <li><a href="/action/publication/${publication.zdbID}/feature-list">Mutations and Transgenics</a> (${featureCount})</li>

            </li>
        </c:if>
        <c:if test="${fishCount > 0}">
            <li><a href="/action/publication/${publication.zdbID}/fish-list">Fish</a>
                (${fishCount})
            </li>
        </c:if>
        <c:if test="${orthologyCount > 0}">
            <li><a href="/action/publication/${publication.zdbID}/orthology-list">Orthology</a> (${orthologyCount})</li>
        </c:if>
        <c:if test="${diseaseCount > 0}">
            <li><a href="/action/publication/${publication.zdbID}/disease">Human Disease / Zebrafish Model Data</a>
            </li>
        </c:if>
    </ul>

</zfin2:subsection>

<c:if test="${not empty publication.errataAndNotes}">
    <zfin2:subsection title="ERRATA and NOTES">
        ${publication.errataAndNotes}
    </zfin2:subsection>
</c:if>


<script>
    function start_note() {
        top.zfinhelp = open("/<%=ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect_note.apg", "notewindow", "scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=300");
    }
</script>


