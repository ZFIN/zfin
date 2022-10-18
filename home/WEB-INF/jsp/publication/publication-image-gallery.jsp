<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<style>
    #xpresimg_control_box {
        margin-top: 20px;
    }
</style>

<c:if test="${!empty imageResults}">
    <div id="xpresimg_all">
        <input type="hidden" name="xpatsel_thumbnail_page" id="xpatsel_thumbnail_page_hidden_field" value="1"/>
        <div id="xpresimg_control_box">
            <span id="xpresimg_thumbs_title" class="summary"/>
            <span id="xpresimg_controls"></span>
        </div>
        <div id="xpresimg_box"></div>
        <div id="imagebox_maxnote" style="display: none;"></div>
        <div id="xpresimg_imagePreload"></div>
    </div>

    <script type="text/javascript">
        var imageBox = new ImageBox();
        imageBox.setImageDivById("xpresimg_box");
        imageBox.setControlDivById("xpresimg_controls");
        imageBox.setHiddenCountFieldById("xpatsel_thumbnail_page_hidden_field");
        imageBox.setMaxImages(5000);
        imageBox.images = [
            <c:forEach items="${imageResults}" var="image">
            {
                imgThumb: "${image.imageThumbnail}",
                imgZdbId: "${image.imageZdbId}"
            },
            </c:forEach>
        ];
        document.getElementById('xpresimg_thumbs_title').innerHTML = "Figure Gallery (${fn:length(imageResults)} images)";

        function loadImages() {
            storedpage = imageBox.getHiddenCountInput().value;
            if ((storedpage != null) && (storedpage != "") && Number.isInteger(storedpage)) {
                imageBox.jumpToPage(storedpage);
            } else {
                imageBox.displayFirstSet();
            }
        }

        document.hasImages = true;
        loadImages();
    </script>
</c:if>
<div><a href="/action/figure/all-figure-view/${publication.zdbID}">Show all Figures</a></div>
