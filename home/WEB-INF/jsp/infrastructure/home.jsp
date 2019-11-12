<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<%@ include file="homeSearch.jsp" %>

<div class="container">
    <div class="row">
        <div class="col-lg-7">
            <jsp:include page="homePrimaryLinks.jsp"/>

            <%-- we might be able to this reordering more elegantly with bootstrap 4 --%>
            <div class="hide-large">
                <jsp:include page="homeAboutBox.jsp"/>

                <div class="section genomic-resources">
                    <div class="heading">Additional Resources</div>
                    <a href="http://uswest.ensembl.org/Danio_rerio/Info/Index">Ensembl</a> <a href="http://www.ensembl.org/biomart/index.html">BioMart</a> <a href="http://genome.ucsc.edu/cgi-bin/hgGateway?hgsid=85282730&clade=vertebrate&org=Zebrafish&db=0">UCSC</a>
                </div>

                <div class="section carousel-section">
                    <div class="heading">New Data in ZFIN</div>
                    <zfin2:imageCarousel id="home-carousel-1" images="${carouselImages}" captions="${sanitizedCaptions}"
                                         interval="60000"/>
                </div>
            </div>

            <div class="section">
                <div class="heading">News & Meeting Announcements</div>
                <div class="__react-root" id="NewsAndMeetingsFeed"></div>
            </div>

            <div class="section">
                <div class="heading">Zebrafish-Related Job Announcements</div>
                <div class="__react-root" id="JobsFeed"></div>
            </div>
        </div>

        <div class="col-lg-5 display-large">
            <jsp:include page="homeAboutBox.jsp"/>

            <div class="section genomic-resources">
                <div class="heading">Additional Resources</div>
                <a href="http://uswest.ensembl.org/Danio_rerio/Info/Index">Ensembl</a> <a href="http://www.ensembl.org/biomart/index.html">BioMart</a> <a href="http://genome.ucsc.edu/cgi-bin/hgGateway?hgsid=85282730&clade=vertebrate&org=Zebrafish&db=0">UCSC</a>
            </div>

            <div class="section carousel-section">
                <div class="heading">New Data in ZFIN</div>
                <zfin2:imageCarousel id="home-carousel-2" images="${carouselImages}" captions="${sanitizedCaptions}"
                                     interval="60000"/>
            </div>
        </div>
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
