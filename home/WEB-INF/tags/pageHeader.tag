<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<header>
    <div class="logo">
        <a href="/">
            <img src="/images/zfinlogo_lg.gif">
        </a>
    </div>

    <ul class="menu list-unstyled">
        <li class="reference">
            <span class="title">Research</span>
            <div class="dropdown">
                <ul class="list-unstyled">
                    <li><a href="/action/marker/search">Genes/Clones</a></li>
                    <li><a href="/action/expression/search">Expression</a></li>
                    <li><a href="/action/fish/search">Mutants/Tg</a></li>
                    <li><a href="/action/antibody/search">Antibodies</a></li>
                    <li><a href="/action/blast/blast">BLAST</a></li>
                    <li><a href="/action/gbrowse">GBrowse</a></li>
                    <li><a href="/action/ontology/search">Anatomy/GO/Disease</a></li>
                    <li><a href="/action/publication/search">Publications</a></li>
                </ul>
            </div>
        </li>
        <li class="reference">
            <span class="title">Community</span>
            <div class="dropdown">
                <div class="column">
                    <span class="column-header">Announcements</span>
                    <ul class="list-unstyled">
                        <li><a href="https://@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements">Jobs</a></li>
                        <li><a href="https://@WIKI_HOST@/display/meetings">Meetings</a></li>
                        <li><a href="https://@WIKI_HOST@/display/news">News</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/Zebrafish+Newsgroup+Information">Newsgroups</a></li>
                    </ul>
                </div>
                <div class="column">
                    <span class="column-header">Search</span>
                    <ul class="list-unstyled">
                        <li><a href="/action/profile/person/search">People</a></li>
                        <li><a href="/action/profile/lab/search">Labs</a></li>
                        <li><a href="/action/profile/company/search">Companies</a></li>
                    </ul>
                </div>
                <div class="column">
                    <span class="column-header">Resource Centers</span>
                    <ul class="list-unstyled">
                        <li><a href="http://zebrafish.org">ZIRC</a></li>
                        <li><a href="http://zfish.cn/">CZRC</a></li>
                        <li><a href="http://www.ezrc.kit.edu/">EZRC</a></li>
                    </ul>
                </div>
            </div>
        </li>
        <li>
            <span class="title">Education</span>
        </li>
        <li class="reference">
            <span class="title">Genomics</span>
            <div class="dropdown">
                <div class="column">
                    <span class="column-header">Data Mining</span>
                    <ul class="list-unstyled">
                        <li><a href="http://www.zebrafishmine.org">ZebrafishMine</a></li>
                        <li><a href="http://www.ensembl.org/biomart">BioMart</a></li>
                    </ul>
                    <span class="column-header">BLAST</span>
                    <ul class="list-unstyled">
                        <li><a href="/action/blast/blast">ZFIN</a></li>
                        <li><a href="http://www.ensembl.org/Danio_rerio/blastview">Ensembl</a></li>
                        <li><a href="http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastSearch&BLAST_SPEC=OGP__7955__9557">NCBI</a></li>
                    </ul>
                </div>
                <div class="column">
                    <span class="column-header">Genome Browsers</span>
                    <ul class="list-unstyled">
                        <li><a href="@GBROWSE_PATH_FROM_ROOT@">ZFIN</a></li>
                        <li><a href="http://www.ensembl.org/Danio_rerio/">Ensembl</a></li>
                        <li><a href="http://vega.sanger.ac.uk/Danio_rerio/">Vega</a></li>
                        <li><a href="http://www.ncbi.nlm.nih.gov/projects/genome/assembly/grc/zebrafish/">GRC</a></li>
                        <li><a href="http://genome.ucsc.edu/cgi-bin/hgGateway?hgsid=85282730&clade=vertebrate&org=Zebrafish&db=0">UCSC</a></li>
                        <li><a href="https://www.ncbi.nlm.nih.gov/genome/gdv/?org=danio-rerio">NCBI</a></li>
                        <li><a href="http://genome.igib.res.in/">FishMap</a></li>
                    </ul>
                </div>
            </div>
        </li>
        <li>
            <span class="title">Help</span>
        </li>
        <authz:authorize access="hasRole('root')">
            <li class="reference root-only">
                <span class="title">Curation</span>
                <div class="dropdown">
                    <div class="column">
                        <span class="column-header">Publications</span>
                        <ul class="list-unstyled">
                            <li><a href="/action/publication/dashboard">My Dashboard</a></li>
                            <li><a href="/action/publication/curating-bin">Curation Bins</a></li>
                            <li><a href="/action/publication/indexing-bin">Indexing Bin</a></li>
                            <li><a href="/action/publication/processing-bin">Processing Bin</a></li>
                        </ul>

                        <span class="column-header">Curate</span>
                        <ul class="list-unstyled">
                            <li>
                                <form class="jump-to-pub">
                                    <input type="submit">
                                    ZDB-PUB-<input type="text">
                                </form>
                            </li>
                            <li><a href="/action/reno/run-list">ReNo Pipeline</a></li>
                        </ul>

                        <span class="column-header">Distribution List</span>
                        <ul class="list-unstyled">
                            <li><a href="/action/profile/distribution-list">Full</a></li>
                            <li><a href="/action/profile/distribution-list?subset=usa">USA only</a></li>
                            <li><a href="/action/profile/distribution-list?subset=pi">PI only</a></li>
                        </ul>

                        <span class="column-header">Development</span>
                        <ul class="list-unstyled">
                            <li><a href="/action/devtool/home">Developer Tools</a></li>
                            <li><a href="/action/devtool/deployed-version">Deployed Version</a></li>
                            <li><a href="/jobs">Jenkins Jobs</a></li>
                            <li><a href="/solr">Solr Admin</a></li>
                        </ul>
                    </div>

                    <div class="column">
                        <span class="column-header">Add</span>
                        <ul class="list-unstyled">
                            <li><a href="/action/marker/gene-add?type=GENE">gene</a></li>
                            <li><a href="/action/marker/nonTranscribedRegion-add">NTR</a></li>
                            <li><a href="/action/marker/gene-add?type=GENEP">pseudogene</a></li>
                            <li><a href="/action/marker/gene-add?type=EFG">foreign gene</a></li>
                            <li><a href="/action/marker/clone-add">clone</a></li>
                            <li><a href="/action/antibody/add">antibody</a></li>
                            <li><a href="/action/marker/transcript-add">transcript</a></li>
                            <li><a href="/action/marker/engineeredRegion-add">engineered region</a></li>
                            <li><a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=MRPHLNO">morpholino</a></li>
                            <li><a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=TALEN">TALEN</a></li>
                            <li><a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=CRISPR">CRISPR</a></li>
                        </ul>
                        <ul class="list-unstyled">
                            <li><a href="/action/profile/person/create">person</a></li>
                            <li><a href="/action/profile/lab/create">lab</a></li>
                            <li><a href="/action/profile/company/create">company</a></li>
                            <li><a href="/action/publication/new#">pub</a></li>
                            <li><a href="/action/publication/journal-add">journal</a></li>
                            <li><a href="/action/feature/alleleDesig-add-form">line designation</a></li>
                        </ul>
                    </div>
                </div>
            </li>
        </authz:authorize>
    </ul>

    <div class="right">
        <div class="search">
            <form method="GET" action="/search">
                <input type="submit">
                <input placeholder="Search" name="q" autocomplete="off" type="text">
            </form>
        </div>
        <c:choose>
            <c:when test="${!empty currentUser}">
                <div class="reference">
                    <span class="title">${currentUser.display}</span>
                    <div class="dropdown">
                        <div class="column">
                            <ul class="list-unstyled">
                                <li><a href="/${currentUser.zdbID}">View Profile</a></li>
                                <li><a href="/action/logout">Logout</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <a href="/action/login">Login</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>