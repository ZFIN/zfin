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
                    <a href="/action/marker/search" class="section primary-link">
                        <div class="title">Genes</div>
                        <div class="description">Search for genes, transcripts, clones, and other markers</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/action/expression/search" class="section primary-link">
                        <div class="title">Expression</div>
                        <div class="description">Search for gene expression data, and annotated images</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/action/fish/search" class="section primary-link">
                        <div class="title">Mutants/Tg</div>
                        <div class="description">Search for mutants, knockdowns, transgenics, and affected phenotypes</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/action/antibody/search" class="section primary-link">
                        <div class="title">Antibodies</div>
                        <div class="description">Search for antibodies by gene, labeled anatomy, and other attributes</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/action/ontology/search" class="section primary-link">
                        <div class="title">Ontologies</div>
                        <div class="description">Search Zebrafish Anatomy, Gene, and Human Disease Ontologies</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/action/blast/blast" class="section primary-link">
                        <div class="title">BLAST</div>
                        <div class="description">Align nucleotide and protein sequences with zebrafish datasets</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/action/publication/search" class="section primary-link">
                        <div class="title">Publications</div>
                        <div class="description">Search for zebrafish research publications and scientific literature</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="#" class="section primary-link">
                        <div class="title">Submit Data</div>
                        <div class="description">Guidelines and forms to submit data to ZFIN</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="https://zebrafish.org" class="section primary-link" target="_blank" rel="noopener noreferrer">
                        <div class="title external">ZIRC</div>
                        <div class="description">Browse or request products and research services (Fish lines, ESTs…)</div>
                    </a>
                </div>
                <div class="col-md-6">
                    <a href="/zf_info/zfbook/zfbk.html" class="section primary-link">
                        <div class="title">The Zebrafish Book</div>
                        <div class="description">A guide for the laboratory use of zebrafish</div>
                    </a>
                </div>
            </div>
        </div>
        <div class="col-lg-6 col-lg-pull-6">
            <div class="row">
                <div class="col-sm-12 col-md-6 col-lg-12">
                    <div class="section">
                        <div class="heading">Recently Curated Figures</div>
                        <zfin2:imageCarousel id="home-carousel" images="${carouselImages}" captions="${sanitizedCaptions}" />
                    </div>

                </div>
                <div class="col-md-3 col-lg-6">
                    <div class="section">
                        <div class="heading">News & Meeting Announcements</div>
                        <div class="__react-root" id="NewsAndMeetingsFeed"></div>
                        <a href="https://@WIKI_HOST@/display/news">More...</a>
                    </div>
                </div>
                <div class="col-md-3 col-lg-6">
                    <div class="section">
                        <div class="heading">Zebrafish-Related Job Announcements</div>
                        <div class="__react-root" id="JobsFeed"></div>
                        <a href="https://@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements">More...</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
