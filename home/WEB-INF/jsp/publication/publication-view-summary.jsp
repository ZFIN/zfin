<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="publication" class="org.zfin.publication.Publication" scope="request"/>

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

<z:attributeList>
    <z:attributeListItem label="Authors">
        ${publication.authors}
    </z:attributeListItem>

    <z:attributeListItem label="ID" copyable="true">
        ${publication.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Date">
        <fmt:formatDate value="${publication.publicationDate.time}" type="Date" pattern="yyyy"/>
    </z:attributeListItem>

    <z:attributeListItem label="Source">
        ${publication.journal.name}
        <c:if test="${!empty publication.volume}">&nbsp;</c:if>
        ${publication.volume}:
        ${publication.pages} (${publication.type.display})

        <span style="padding-left: 1em;">
                    <a href="#" id="generate-reference-button" rel="#generate-reference-overlay"><button>Generate reference</button></a>
                </span>
    </z:attributeListItem>

    <z:attributeListItem label="Registered Authors">
        <zfin:link entity="${publication.people}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Keywords">
        <c:choose>
            <c:when test="${!empty publication.keywords}">
                ${publication.keywords}
            </c:when>
            <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
        </c:choose>
    </z:attributeListItem>

    <c:if test="${!empty publication.dbXrefs}">
        <z:attributeListItem label="Datasets">
            <c:forEach var="xref" items="${publication.dbXrefs}" varStatus="loop">
                <zfin:link entity="${xref}"/><c:if test="${!loop.last}">, </c:if>
            </c:forEach>
        </z:attributeListItem>
    </c:if>

    <z:attributeListItem label="MeSH Terms">
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
    </z:attributeListItem>

    <z:attributeListItem label="PubMed">
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
    </z:attributeListItem>

    <c:if test="${!empty ctdPublicationID}">
        <z:attributeListItem label="CTD">
            <a href="https://ctdbase.org/detail.go?type=reference&acc=${ctdPublicationID}">${ctdPublicationID}</a>
        </z:attributeListItem>
    </c:if>

    <authz:authorize access="hasRole('root')">
        <z:attributeListItem label="Files">
            <c:forEach items="${publication.files}" var="file" varStatus="loop">
                <a href="${ZfinPropertiesEnum.PDF_LOAD.value()}/${file.fileName}">
                        ${file.type.name.toString() eq 'Original Article' ? 'Original Article' : file.originalFileName}
                </a>${loop.last ? " &mdash; " : ", "}
            </c:forEach>
            <a href="/action/publication/${publication.zdbID}/edit#files">Add/Update Files</a>
        </z:attributeListItem>

        <z:attributeListItem label="Curation Status">
            ${curationStatusDisplay}
            <c:if test="${publication.indexed}"><br>Indexed,
                <fmt:formatDate value="${publication.indexedDate.time}" pattern="MM/dd/yy"/>
            </c:if>
        </z:attributeListItem>

        <z:attributeListItem label="Author Correspondence">
            <c:choose>
                <c:when test="${!empty correspondenceDisplay}">
                    <a href="/action/publication/${publication.zdbID}/track#correspondence">${correspondenceDisplay}</a>
                </c:when>
                <c:otherwise>
                    <span class="no-data-tag"><i>None</i></span>
                </c:otherwise>
            </c:choose>
            <a href="/action/publication/${publication.zdbID}/edit#files">Add/Update Files</a>
        </z:attributeListItem>

        <z:attributeListItem label="Related Data">
            ${relatedData}
        </z:attributeListItem>

        <z:attributeListItem label="Statistics Data (Testing)">
            <a href="/action/publication/stats">Publication Statistics</a>
        </z:attributeListItem>

    </authz:authorize>


</z:attributeList>

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
