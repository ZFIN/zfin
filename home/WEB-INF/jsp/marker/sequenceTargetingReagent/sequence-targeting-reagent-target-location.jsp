<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="false" hasData="${!empty formBean.externalNotes and fn:length(formBean.externalNotes) > 0}">
    <div id="gbrowse-images" class="summary">
        <div class="summaryTitle">
            TARGET LOCATION${fn:length(formBean.gbrowseImages) == 1 ? "" : "S"}
            <a class="popup-link info-popup-link" href="/action/marker/note/sequence-targeting-reagent-gbrowse"></a>
        </div>

        <c:forEach items="${formBean.gbrowseImages}" var="image" end="1">
            <div class="gbrowse-image"
                 data-gbrowse-image='{"imageUrl": "${image.imageUrl}", "linkUrl": "${image.linkUrl}", "build": "${image.build}"}'>
            </div>
        </c:forEach>

        <c:if test="${fn:length(formBean.gbrowseImages) > 2}">
            <div>
                <a href="/action/marker/view/${formBean.marker.zdbID}/str-targeted-genes">View
                    all ${fn:length(formBean.gbrowseImages)} target locations</a>
            </div>
        </c:if>

        <span id="gbrowse-no-data" class="no-data-tag">No data available</span>
    </div>

    <script>
        jQuery(".gbrowse-image").gbrowseImage({
            success: function () {
                jQuery("#gbrowse-no-data").hide();
            }
        });
    </script>
</z:dataTable>
