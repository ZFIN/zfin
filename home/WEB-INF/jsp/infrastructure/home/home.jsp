<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<%@ include file="home-search.jsp" %>

<div class="container">
    <div class="row">
        <div class="col-xl-7">
            <jsp:include page="home-primary-links.jsp"/>

            <%-- we might be able to this reordering more elegantly with bootstrap 4 --%>
            <div class="d-block d-xl-none">
                <div class="row">
                    <div class="col-lg-6">
                        <jsp:include page="home-about-box.jsp"/>
                    </div>
                    <div class="col-lg-6">
                        <jsp:include page="home-additional-resources.jsp"/>
                    </div>
                </div>

                <z:section title="New Data in ZFIN" cssClass="carousel-section">
                    <zfin2:imageCarousel
                            id="home-carousel-1"
                            images="${carouselImages}"
                            captions="${sanitizedCaptions}"
                            interval="60000"
                    />
                </z:section>
            </div>

            <z:section title="News">
                <div class="__react-root" id="NewsFeed"></div>
            </z:section>

            <z:section title="Upcoming Meetings">
                <div class="__react-root" id="MeetingsFeed"></div>
            </z:section>

            <z:section title="Zebrafish-Related Job Announcements">
                <div class="__react-root" id="JobsFeed"></div>
            </z:section>
        </div>


        <div class="col-xl-5 d-none d-xl-block">
            <jsp:include page="home-about-box.jsp"/>

            <jsp:include page="home-additional-resources.jsp"/>

            <z:section title="New Data in ZFIN" cssClass="carousel-section">
                <zfin2:imageCarousel
                        id="home-carousel-2"
                        images="${carouselImages}"
                        captions="${sanitizedCaptions}"
                        interval="60000"
                />
            </z:section>
        </div>
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
