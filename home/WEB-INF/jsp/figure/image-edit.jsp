<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.publication.PublicationType" %>

<z:page>
    <meta name="image-view-page"/> <%-- this is used by the web testing framework to know which page this is--%>

    <%--
         Nothing is stored in the updates table for figures, so no lastUpdated date is passed in
    --%>

    <c:set var="UNPUBLISHED" value="${PublicationType.UNPUBLISHED}"/>
    <c:set var="CURATION" value="${PublicationType.CURATION}"/>

    <c:set var="viewURL">/${image.zdbID}</c:set>
    <c:set var="deleteURL">/action/infrastructure/deleteRecord/${image.zdbID}</c:set>



    <zfin2:dataManager zdbID="${image.zdbID}"
                       viewURL="${viewURL}"/>


    <c:if test="${image.figure ne null}">
    <c:if test="${fn:length(image.figure.publication.figures) > 1}">
        <div style="margin-top: 1em;">
            <c:set var="probeUrlPart" value=""/>
            <c:if test="${!empty probe}">
                <c:set var="probeUrlPart" value="?probeZdbID=${probe.zdbID}"/>
            </c:if>

        <c:if test="${image.figure.publication.type == CURATION}">
            <c:if test="${!empty probe}">
            <a class="additional-figures-link" href="/action/figure/all-figure-view/${image.figure.publication.zdbID}${probeUrlPart}">All Figures for ${image.figure.publication.shortAuthorList}</a>
            </c:if>
            </c:if>
        <c:if test="${image.figure.publication.type != CURATION}">
            <a class="additional-figures-link" href="/action/figure/all-figure-view/${image.figure.publication.zdbID}${probeUrlPart}">All Figures for ${image.figure.publication.shortAuthorList}</a>
            </c:if>
        </div>
    </c:if>
    </c:if>
    <p>

    <p>
        <tr>
            <td>



    <zfin-figure:imageView image="${image}"/>


                <div style="display: none;"><input type="text" name="magicvoodoo"></div>

                <script type="text/javascript">
                    var MarkerProperties = { zdbID : "${image.zdbID}",
                        imageEditDiv : "imageTermBox",
                        imageStageEditDiv: "imageStageBox",
                        imageConstructEditDiv: "imageConstructBox"} ;

                </script>
                <script language="javascript"
                        src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>
                <b>Anatomy</b><div id="imageTermBox"></div>
                <b>Construct</b><div id="imageConstructBox"></div>

    <b>Developmental Stage</b>
                <div id="imageStageBox"></div>
            </td>
        </tr>

    <script>
        jQuery(document).ready(function() {
            jQuery('.fish-label').tipsy({gravity:'sw', opacity:1, delayIn:750, delayOut:200});
        });
    </script>
</z:page>