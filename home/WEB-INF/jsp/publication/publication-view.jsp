<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="editURL">/cgi-bin/webdriver?MIval=aa-edit_pub.apg&OID=${publication.zdbID}&anon1=zdb_id&anon1text=${publication.zdbID}</c:set>

<c:if test="${allowDelete}">
    <c:set var="deleteURL">/action/infrastructure/deleteRecord/${publication.zdbID}</c:set>
</c:if>

<c:set var="trackURL">/cgi-bin/webdriver?MIval=aa-pubcuration.apg&OID=${publication.zdbID}</c:set>

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
            <c:out value="${publication.journal.name}"/>
            <c:out value="${publication.volume}"/>:
            <c:out value="${publication.pages}"/> (<c:out value="${publication.type.display}"/>)

            <span style="padding-left: 1em;">
                <form style="display: inline-block" method=post  action="/cgi-bin/webdriver">
                    <input type=hidden name=MIval value=aa-pubprintable.apg>
                    <input type=hidden name=constraint value="where zdb_id='${publication.zdbID}'">
                    <input type=submit name=printable value="Generate reference">
                </form>
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

    <authz:authorize ifAnyGranted="root">

        <tr>
            <th>File:</th>
            <td>
                <c:choose>
                    <c:when test="${not empty publication.fileName}">
                        <a href="/<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}">PDF</a>
                    </c:when>
                    <c:when test="${empty publication.fileName && allowCuration}">
                        Upload a PDF from the
                        <a href="/cgi-bin/webdriver?MIval=aa-pubcuration.apg&OID=${publication.zdbID}">Tracking</a>
                        page.
                    </c:when>
                </c:choose>
            </td>
        </tr>

        <tr>
            <th>Curation Status:</th>
            <td>${curationStatusDisplay}</td>
        </tr>
    </authz:authorize>

</table>

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
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_mrkr">Genes / Markers</a> (${markerCount})</li>
        </c:if>
        <c:if test="${morpholinoCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_mo">Morphilino</a> (${morpholinoCount})</li>
        </c:if>
        <c:if test="${talenCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_talen">TALEN</a> (${talenCount})</li>
        </c:if>
        <c:if test="${cirsprCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_crispr">CRISPR</a> (${crisprCount})</li>
        </c:if>
        <c:if test="${antibodyCount > 0}">
            <li><a href="/action/antibody/antibodies-per-publication/${publication.zdbID}" id="list-of-antibodies">Antibodies</a> (${antibodyCount})</li>
        </c:if>
        <c:if test="${efgCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-markerselect.apg&pubId=${publication.zdbID}&type=pub_efg">Engineered Foreign Genes</a> (${efgCount})</li>
        </c:if>
        <c:if test="${cloneProbeCount > 0}">
            <li><a href="/cgi-bin/webdriver?MIval=aa-msegselect.apg&pubId=${publication.zdbID}">Clones and Probes</a> (${cloneProbeCount})</li>
        </c:if>
        <c:if test="${expressionCount > 0 || phenotypeCount > 0}">
            <li><a href="/action/figure/all-figures/${publication.zdbID}">${expressionAndPhenotypeLabel}</a></li>
        </c:if>
        <c:if test="${phenotypeAlleleCount > 0}">
            <li><a href="/action/mutant/mutant-list?zdbID=${publication.zdbID}">Mutants / Transgenic Lines</a> (${phenotypeAlleleCount})
        </c:if>
        <c:if test="${orthologyCount > 0}">
            <li><a href="/action/publication/${publication.zdbID}/orthology-list">Orthology</a> (${orthologyCount})
        </c:if>


    </ul>

</zfin2:subsection>

<zfin2:subsection title="ERRATA and NOTES" test="${not empty publication.errataAndNotes}" showNoData="true">
    ${publication.errataAndNotes}
</zfin2:subsection>



<script>
    function start_note() {
        top.zfinhelp=open("/<%=ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect_note.apg","notewindow","scrollbars=no,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=300");
    }
</script>


