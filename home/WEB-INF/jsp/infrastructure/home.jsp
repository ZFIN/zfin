<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<div class="search-container">
    <div class="search-inner">
        <form class="fs-autocomplete" action="/search" method="get">
            <label>Search expert curated zebrafish data</label>
            <div class="input-group input-group-lg">
                <div class="input-group-btn category-dropdown">
                    <button type="button" class="btn btn-default" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <span class="category-label">Any</span> <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><a href="#">Any</a></li>
                        <li role="separator" class="divider"></li>
                        <c:forEach items="${searchCategories}" var="category">
                            <li><a href="#">${category}</a></li>
                        </c:forEach>
                    </ul>
                </div>
                <input type="hidden" name="category">
                <input type="text" class="form-control" name="q" autocomplete="off" data-placeholders="bmp2a|hindbrain development disrupted|pax morpholino">
                <span class="input-group-btn">
                    <button class="btn btn-zfin" type="submit">Search</button>
                </span>
            </div>
        </form>
    </div>
</div>

<div class="container">
    <div class="row">
        <div class="col-lg-6 col-lg-push-6">
            <div class="row">
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">G</div>
                        <div class="main">
                            <div class="title">Genes/Markers</div>
                            <div class="description">Lorem ipsum dolor sit</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">E</div>
                        <div class="main">
                            <div class="title">Expression</div>
                            <div class="description">Nam ac odio</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">M</div>
                        <div class="main">
                            <div class="title">Mutants/Tg</div>
                            <div class="description">Pellentesque at felis non</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">A</div>
                        <div class="main">
                            <div class="title">Antibodies</div>
                            <div class="description">Curabitur ullamcorper</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">O</div>
                        <div class="main">
                            <div class="title">Anatomy/GO/Disease</div>
                            <div class="description">Pellentesque at risus id</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">B</div>
                        <div class="main">
                            <div class="title">BLAST</div>
                            <div class="description">Praesent facilisis nibh interdum</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">P</div>
                        <div class="main">
                            <div class="title">Publications</div>
                            <div class="description">Cras vitae lorem ut eros</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">S</div>
                        <div class="main">
                            <div class="title">Submit Data</div>
                            <div class="description">Sed tempus diam</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">Z</div>
                        <div class="main">
                            <div class="title">ZIRC</div>
                            <div class="description">Praesent cursus dui vitae mi vestibulum</div>
                        </div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="home-card">
                        <div class="icon">T</div>
                        <div class="main">
                            <div class="title">The Zebrafish Book</div>
                            <div class="description">Dui vitae mi diam</div>
                        </div>
                    </a>
                </div>
            </div>
        </div>
        <div class="col-lg-6 col-lg-pull-6">
            <div class="row">
                <div class="col-sm-12 col-md-6 col-lg-12">
                    <div class="home-section">
                        <div class="heading">Recently Curated Figures</div>
                        <zfin2:imageCarousel id="home-carousel" images="${carouselImages}" captions="${sanitizedCaptions}" />
                    </div>

                </div>
                <div class="col-md-3 col-lg-6">
                    <div class="home-section">
                        <div class="heading">News & Meeting Announcements</div>
                        <div class="__react-root" id="NewsAndMeetingsFeed"></div>
                    </div>
                </div>
                <div class="col-md-3 col-lg-6">
                    <div class="home-section">
                        <div class="heading">Zebrafish-Related Job Announcements</div>
                        <div class="__react-root" id="JobsFeed"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
