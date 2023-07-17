<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.framework.presentation.NavigationMenuOptions" %>

<jsp:useBean id="image" class="org.zfin.expression.Image" scope="request"/>

<c:set var="SUMMARY" value="${NavigationMenuOptions.SUMMARY.value}"/>
<c:set var="IMAGE" value="${NavigationMenuOptions.IMAGE.value}"/>
<c:set var="COMMENTS" value="${NavigationMenuOptions.COMMENTS.value}"/>
<c:set var="FIGURE_CAPTION" value="${NavigationMenuOptions.FIGURE_CAPTION.value}"/>
<c:set var="DEVELOPMENTAL_STAGE" value="${NavigationMenuOptions.DEVELOPMENTAL_STAGE.value}"/>
<c:set var="ORIENTATION" value="${NavigationMenuOptions.ORIENTATION.value}"/>
<c:set var="FIGURE_DATA" value="${NavigationMenuOptions.FIGURE_DATA.value}"/>
<c:set var="ACKNOWLEDGEMENT" value="${NavigationMenuOptions.ACKNOWLEDGEMENT.value}"/>


<c:set var="title" value="${image.zdbID}"/>
<c:if test="${!empty image.figure && !empty image.figure.label}">
    <c:set var="title" value="${image.figure.label}"/>
</c:if>

<c:set var="titleUrl" value="/${image.zdbID}"/>
<c:if test="${!empty image.figure}">
    <c:set var="titleUrl" value="/${image.figure.zdbID}"/>
</c:if>

<z:dataPage sections="${[]}" navigationMenu="${navigationMenu}" additionalBodyClass="image">

    <jsp:attribute name="entityName">
        <div data-toggle="tooltip" data-placement="bottom" title="">
                ${title}
        </div>
    </jsp:attribute>

    <jsp:body>
        <z:dataManagerDropdown>
            <a class="dropdown-item" href="/action/publication/image-edit?zdbID=${image.zdbID}"><i class="fas fa-pen"></i> Edit</a>
            <a class="dropdown-item" href="/action/image/view/${image.zdbID}"><i class="fas fa-eye"></i> Old View</a>
        </z:dataManagerDropdown>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">IMAGE</div>
            <h1><a href="${titleUrl}">${title}</a></h1>
            <z:attributeList>
                <z:attributeListItem label="ID">
                    ${image.zdbID}
                </z:attributeListItem>
                <c:if test="${!empty expressionGeneList}">
                    <z:attributeListItem label="Genes">
                        <zfin2:toggledLinkList collection="${expressionGeneList}" maxNumber="5" commaDelimited="true"/>
                    </z:attributeListItem>
                </c:if>
                <c:if test="${!empty antibodyList}">
                    <z:attributeListItem label="Antibodies">
                        <zfin2:toggledLinkList collection="${antibodyList}" maxNumber="5" commaDelimited="true"/>
                    </z:attributeListItem>
                </c:if>
                <z:attributeListItem label="Source">
                    <c:if test="${!empty image.figure && fn:length(image.figure.publication.figures) > 1}">
                        <c:set var="probeUrlPart" value=""/>
                        <c:set var="probeDisplay" value=""/>
                        <c:if test="${!empty probe}">
                            <c:set var="probeUrlPart" value="?probeZdbID=${probe.zdbID}"/>
                            <c:set var="probeDisplay" value="[${probe.abbreviation}]"/>
                        </c:if>

                        <c:if test="${image.figure.publication.type == CURATION}">
                            <c:if test="${!empty probe}">
                                <a class="additional-figures-link" href="/action/figure/all-figure-view/${image.figure.publication.zdbID}${probeUrlPart}">All Figures for ${image.figure.publication.shortAuthorList}</a>
                            </c:if>
                        </c:if>
                        <c:if test="${image.figure.publication.type != CURATION}">
                            <a class="additional-figures-link" href="/action/figure/all-figure-view/${image.figure.publication.zdbID}${probeUrlPart}">Figures for ${image.figure.publication.shortAuthorList}${probeDisplay}</a>
                        </c:if>
                    </c:if>
                </z:attributeListItem>
            </z:attributeList>
        </div>

        <z:section title="${IMAGE}">
            <div style="text-align:center; max-width:100%">
                <table border=0 cellpadding=20>
                    <tr>
                        <td align="center" bgcolor="#000000">
                            <c:if test="${!empty image.figure}">
                                <zfin-figure:placeholderImages figure="${image.figure}"/>
                            </c:if>

                            <c:if test="${empty image.figure || image.figure.publication.canShowImages}">
                                <zfin-figure:showSingleImage image="${image}" autoplayVideo="${autoplayVideo}"/>
                            </c:if>
                        </td>
                    </tr>
                </table>
            </div>
        </z:section>

        <z:section title="${COMMENTS}">
            <p class="fig">
                    ${image.comments}
            </p>
        </z:section>

        <z:section title="${FIGURE_CAPTION}">
            <zfin-figure:figureLabelAndCaption figure="${image.figure}"/>
        </z:section>

        <z:section title="${DEVELOPMENTAL_STAGE}">
            <c:if test="${!empty image.imageStage.start}">
                <c:if test="${!empty image.imageStage.start && image.imageStage.start ne image.imageStage.end}">
                    <zfin:link entity="${image.imageStage.start}"/> <b> to </b><zfin:link entity="${image.imageStage.end}"/>
                </c:if>
                <c:if test="${image.imageStage.start eq image.imageStage.end}">
                    <zfin:link entity="${image.imageStage.start}"/>
                </c:if>
            </c:if>
        </z:section>

        <z:section title="${ORIENTATION}">
            <c:if test="${image.preparation ne 'not specified' || image.form ne 'not specified' || image.direction ne 'not specified' || image.view ne 'not specified'}">
                <table class="summary bg-white border-white">
                    <tr>

                        <td><b>Preparation</b></td>
                        <td><b>Image Form</b></td>
                        <td><b>View</b></td>
                        <td><b>Direction</b></td>
                    </tr>
                    <tr>
                        <td>${image.preparation}</td>
                        <td>${image.form}</td>
                        <td>${image.view}</td>
                        <td>${image.direction}</td>
                    </tr>

                </table>
            </c:if>
        </z:section>

        <z:section title="${FIGURE_DATA}">
            <zfin-figure:imageDetailsPrototype image="${image}"/>
        </z:section>

        <z:section title="${ACKNOWLEDGEMENT}">
            <c:if test="${!empty image.figure}">
                <c:choose>
                    <c:when test="${image.figure.publication.canShowImages && image.figure.publication.type != UNPUBLISHED}">
                        <zfin2:acknowledgment-text hasAcknowledgment="${hasAcknowledgment}" showElsevierMessage="${showElsevierMessage}" publication="${publication}"/>
                    </c:when>
                    <c:otherwise>
                        <zfin2:subsection>
                            <zfin-figure:journalAbbrev publication="${image.figure.publication}"/>
                        </zfin2:subsection>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </z:section>


    </jsp:body>

</z:dataPage>
