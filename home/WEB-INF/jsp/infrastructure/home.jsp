<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<%@ include file="homeSearch.jsp" %>

<div class="container">
    <div class="row">
        <div class="col-lg-6 col-lg-push-6">
            <%@ include file="homePrimaryLinks.jsp" %>
        </div>

        <div class="col-lg-6 col-lg-pull-6">
            <div class="section">
                <div class="heading">Recently Curated Figures</div>
                <zfin2:imageCarousel id="home-carousel" images="${carouselImages}" captions="${sanitizedCaptions}" />
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6">
            <div class="section">
                <div class="heading">News & Meeting Announcements</div>
                <div class="__react-root" id="NewsAndMeetingsFeed"></div>
                <a href="https://@WIKI_HOST@/display/news">More...</a>
            </div>
        </div>

        <div class="col-md-6">
            <div class="section">
                <div class="heading">Zebrafish-Related Job Announcements</div>
                <div class="__react-root" id="JobsFeed"></div>
                <a href="https://@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements">More...</a>
            </div>
        </div>
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
