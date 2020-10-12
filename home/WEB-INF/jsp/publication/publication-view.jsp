<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

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

<c:set var="linkURL">/action/publication/${publication.zdbID}/link</c:set>

<c:if test="${allowCuration}">
    <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
</c:if>
<c:if test="${hasCorrespondence}">
    <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
</c:if>

<zfin2:dataManager zdbID="${publication.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   trackURL="${trackURL}"
                   correspondenceURL="${correspondenceURL}"
                   linkURL="${linkURL}"
                   curateURL="${curateURL}"/>

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
            ${publication.journal.name}
            <c:if test="${!empty publication.volume}">&nbsp;</c:if>
        ${publication.volume}:
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
            <c:choose>
                <c:when test="${!empty meshTermDisplayList}">
                    <ul class="comma-separated semicolon" data-toggle="collapse">
                        <c:forEach items="${meshTermDisplayList}" var="term">
                            <li>${term}</li>
                        </c:forEach>
                    </ul>
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
    <authz:authorize access="hasRole('root')">
        <tr>
            <th>Files:</th>
            <td>
                <c:forEach items="${publication.files}" var="file" varStatus="loop">
                    <a href="${ZfinPropertiesEnum.PDF_LOAD.value()}/${file.fileName}">
                            ${file.type.name.toString() eq 'Original Article' ? 'Original Article' : file.originalFileName}
                    </a>${loop.last ? " &mdash; " : ", "}
                </c:forEach>
                <a href="/action/publication/${publication.zdbID}/edit#files">Add/Update Files</a>
            </td>
        </tr>
        <tr>
            <th>Curation Status:</th>
            <td>
                    ${curationStatusDisplay}
                <c:if test="${publication.indexed}"><br>Indexed,
                    <fmt:formatDate value="${publication.indexedDate.time}" pattern="MM/dd/yy"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <th>Author Correspondence:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty correspondenceDisplay}">
                        <a href="/action/publication/${publication.zdbID}/track#correspondence">${correspondenceDisplay}</a>
                    </c:when>
                    <c:otherwise>
                        <span class="no-data-tag"><i>None</i></span>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </authz:authorize>
</table>

<div class="jq-modal" id="generate-reference-overlay" style="width: auto; height: auto; padding: 15px 15px;">
    <div class="popup-content">
        <div class="popup-header">
            Citation
        </div>
        <div class="popup-body" id="generate-reference-body">
            ${publication.printable}
        </div>
    </div>
</div>

<authz:authorize access="hasRole('root')">
    <c:if test="${!empty zebraShareMetadata}">
        <zfin2:subsection title="ZEBRASHARE SUBMISSION DETAILS">
            <table class="primary-entity-attributes">
                <tr>
                    <th>Submitter:</th>
                    <td>${zebraShareMetadata.submitterName} (${zebraShareMetadata.submitterEmail})</td>
                </tr>
                <tr>
                    <th>Editors:</th>
                    <td>
                        <c:choose>
                            <c:when test="${!empty zebraShareEditors}">
                                <c:forEach items="${zebraShareEditors}" var="editor">
                                    <div><zfin:link entity="${editor.person}"/></div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="no-data-tag"><i>None</i></span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <th>Lab of Origin:</th>
                    <td><zfin:link entity="${zebraShareMetadata.labOfOrigin}"/></td>
                </tr>
                <tr>
                    <th>Figure Filenames:</th>
                    <td>
                        <c:choose>
                            <c:when test="${!empty zebraShareFigures}">
                                <c:forEach items="${zebraShareFigures}" var="figure">
                                    <div>${figure.img.externalName} &rarr; ${figure.label}</div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="no-data-tag"><i>None</i></span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>
        </zfin2:subsection>
    </c:if>
</authz:authorize>


<%--todo: this should probably change both visually and in the code, doesn't match UI guidelines, so there's no classes for it and it seems wrong to make them --%>

<c:if test="${showFiguresLink}">
    <div style="margin-top: 1em;">
        <a href="/action/figure/all-figure-view/${publication.zdbID}" style="font-weight: bold">FIGURES</a>
        <a class="popup-link info-popup-link" href="/ZFIN/help_files/expression_help.html"></a>
    </div>
</c:if>

<zfin2:subsection title="ABSTRACT" test="${not empty abstractText}" showNoData="true">
    ${abstractText}
</zfin2:subsection>

<zfin2:subsection title="ADDITIONAL INFORMATION" showNoData="true" test="${not empty dataLinks}">
    <ul>
        <c:forEach items="${dataLinks}" var="link">
            <li>
                <a href="${link.path}">${link.label}</a>
                <c:if test="${!empty link.count}">(${link.count})</c:if>
            </li>
        </c:forEach>
        <authz:authorize access="hasRole('root')">
            <li><a href="/action/publication/${publication.zdbID}/directly-attributed">Directly Attributed Data
                (${numDirectlyAttributed})</a>
            </li>
        </authz:authorize>
    </ul>

</zfin2:subsection>

<c:if test="${not empty publication.errataAndNotes}">
    <zfin2:subsection title="ERRATA and NOTES">
        ${publication.errataAndNotes}
    </zfin2:subsection>
</c:if>
