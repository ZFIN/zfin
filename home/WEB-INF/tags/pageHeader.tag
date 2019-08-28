<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<header>
    <div class="mobile-only">
        <a href="#" class="mobile-menu">
            <i class="fas fa-bars"></i>
        </a>
    </div>

    <div class="logo">
        <a href="/">
            <img src="/images/zfinlogo_lg.gif">
        </a>
    </div>

    <ul class="menu menu-collapse list-unstyled">
        <li class="reference">
            <span class="title">Research</span>
            <div class="dropdown">
                <div class="nav-column">
                    <span class="nav-column-header">Search</span>
                    <ul>
                        <li><a href="/action/marker/search">Genes / Clones</a></li>
                        <li><a href="/action/expression/search">Expression</a></li>
                        <li><a href="/action/fish/search">Mutants / Tg</a></li>
                        <li><a href="/action/antibody/search">Antibodies</a></li>
                        <li><a href="/action/ontology/search">Anatomy / GO / Human Disease</a></li>
                        <li><a href="/action/publication/search">Publications</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Data Mining</span>
                    <ul>
                        <li><a href="/downloads">Downloads</a></li>
                        <li><a href="/schemaSpy/index.html">Data Model</a></li>
                        <li><a href="http://www.zebrafishmine.org" class="external">ZebrafishMine</a></li>
                        <li><a href="http://www.ensembl.org/biomart" class="external">BioMart</a></li>
                    </ul>
                </div>
            </div>
        </li>

        <li class="reference">
            <span class="title">Resources</span>
            <div class="dropdown">
                <div class="nav-column">
                    <span class="nav-column-header">General</span>
                    <ul>
                        <li><a href="/zf_info/zfbook/zfbk.html">The Zebrafish Book</a></li>
                        <li><a href="https://@WIKI_HOST@/display/prot">Protocol Wiki</a></li>
                        <li><a href="https://@WIKI_HOST@/display/AB">Antibody Wiki</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/Anatomy+Atlases+and+Resources">Anatomy Atlases</a></li>
                        <li><a href="/zf_info/news/education.html">Resources for Students and Educators</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Zebrafish Programs</span>
                    <ul>
                        <li><a href="http://www.zf-health.org/" class="external">ZF-Health</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/Zebrafish+Programs#husbandry">Husbandry Resources</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/Zebrafish+Programs">More...</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Resource Centers</span>
                    <ul>
                        <li><a href="http://zebrafish.org" class="external">Zebrafish International Resource Center (ZIRC)</a></li>
                        <li><a href="http://www.zfish.cn/" class="external">China Zebrafish Resource Center (CZRC)</a></li>
                        <li><a href="https://www.ezrc.kit.edu/" class="external">European Zebrafish Resource Center (EZRC)</a></li>
                    </ul>
                </div>
            </div>
        </li>

        <li class="reference">
            <span class="title">Genomics</span>
            <div class="dropdown">
                <div class="nav-column">
                    <span class="nav-column-header">BLAST</span>
                    <ul>
                        <li><a href="/action/blast/blast">ZFIN</a></li>
                        <li><a href="http://www.ensembl.org/Danio_rerio/blastview" class="external">Ensembl</a></li>
                        <li><a href="http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastSearch&BLAST_SPEC=OGP__7955__9557" class="external">NCBI</a></li>
                        <li><a href="https://genome.ucsc.edu/cgi-bin/hgBlat?command=start" class="external">UCSC</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Genome Browsers</span>
                    <ul>
                        <li><a href="@GBROWSE_PATH_FROM_ROOT@">ZFIN</a></li>
                        <li><a href="http://www.ensembl.org/Danio_rerio/" class="external">Ensembl</a></li>
                        <li><a href="http://vega.sanger.ac.uk/Danio_rerio/" class="external">Vega</a></li>
                        <li><a href="http://www.ncbi.nlm.nih.gov/projects/genome/assembly/grc/zebrafish/" class="external">GRC</a></li>
                        <li><a href="http://genome.ucsc.edu/cgi-bin/hgGateway?hgsid=85282730&clade=vertebrate&org=Zebrafish&db=0" class="external">UCSC</a></li>
                        <li><a href="https://www.ncbi.nlm.nih.gov/genome/gdv/?org=danio-rerio" class="external">NCBI</a></li>
                        <li><a href="http://genome.igib.res.in/" class="external">FishMap</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Resources</span>
                    <ul>
                        <li><a href="https://@WIKI_HOST@/display/general/Genomic+Resources+for+Zebrafish">Zebrafish Genomics</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/Other+Databases">Other Genome Databases</a></li>
                    </ul>
                </div>
            </div>
        </li>

        <li class="reference">
            <span class="title">Community</span>
            <div class="dropdown">
                <div class="nav-column">
                    <span class="nav-column-header">Announcements</span>
                    <ul>
                        <li><a href="https://@WIKI_HOST@/display/news">News</a></li>
                        <li><a href="https://@WIKI_HOST@/display/meetings">Meetings</a></li>
                        <li><a href="https://@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements">Jobs</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Search</span>
                    <ul>
                        <li><a href="/action/profile/person/search">People</a></li>
                        <li><a href="/action/profile/lab/search">Labs</a></li>
                        <li><a href="/action/profile/company/search">Companies</a></li>
                    </ul>
                </div>
            </div>
        </li>

        <li class="reference">
            <span class="title">Support</span>
            <div class="dropdown">
                <div class="nav-column">
                    <span class="nav-column-header">Nomenclature</span>
                    <ul class="list-unstyled">
                        <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Zebrafish+Nomenclature+Conventions">Nomenclature Conventions</a></li>
                        <li><a href="/action/feature/line-designations">Line Designations</a></li>
                        <li><a href="/action/feature/wildtype-list">Wild-Type Lines</a></li>
                        <li><a href="/action/nomenclature/gene-name">Submit a Proposed Gene Name</a></li>
                        <li><a href="/action/nomenclature/line-name">Submit a Proposed Mutant/Tg Line Name</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Publications</span>
                    <ul>
                        <li><a href="/zf_info/author_guidelines.html">Guidelines for Authors</a></li>
                        <li><a href="/action/zebrashare">Zebrashare</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/ZFIN+db+information">Citing ZFIN</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">Using ZFIN</span>
                    <ul>
                        <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Tips">Help & Tips</a></li>
                        <li><a href="/zf_info/glossary.html">Glossary</a></li>
                        <li><a href="http://@WIKI_HOST@/display/general/ZFIN+Single+Box+Search+Help">Single Box Search Help</a></li>
                        <li><a href="/action/submit-data">Submit Data</a></li>
                    </ul>
                </div>
                <div class="nav-column">
                    <span class="nav-column-header">About Us</span>
                    <ul>
                        <li><a href="https://@WIKI_HOST@/display/general/ZFIN+db+information">About ZFIN</a></li>
                        <li><a href="/action/infrastructure/annual-stats-view">Statistics</a></li>
                        <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Contact+Information">Contact Information</a></li>
                        <li><a href="/zf_info/news/committees.html">Committees</a></li>
                        <li><a href="https://@WIKI_HOST@/display/jobs/ZFIN+Jobs">Jobs at ZFIN</a></li>
                    </ul>
                </div>
            </div>
        </li>

        <authz:authorize access="hasRole('root')">
            <li class="reference root-only">
                <span class="title">Curation</span>
                <div class="dropdown">
                    <div class="nav-column">
                        <span class="nav-column-header">Publications</span>
                        <ul>
                            <li><a href="/action/publication/dashboard">My Dashboard</a></li>
                            <li><a href="/action/publication/curating-bin">Curation Bins</a></li>
                            <li><a href="/action/publication/indexing-bin">Indexing Bin</a></li>
                            <li><a href="/action/publication/processing-bin">Processing Bin</a></li>
                            <li><a href="/action/publication/metrics">Metrics</a></li>
                        </ul>

                        <span class="nav-column-header">Curate</span>
                        <ul class="list-unstyled">
                            <li>
                                <form class="jump-to-pub">
                                    <input type="submit" style="display: none;">
                                    <span class="nowrap">ZDB-PUB-<input type="text"></span>
                                </form>
                            </li>
                            <li><a href="/action/reno/run-list">ReNo Pipeline</a></li>
                        </ul>
                    </div>

                    <div class="nav-column">
                        <span class="nav-column-header">Add</span>
                        <ul>
                            <li><a href="/action/marker/gene-add?type=GENE">Gene</a></li>
                            <li><a href="/action/marker/nonTranscribedRegion-add">NTR</a></li>
                            <li><a href="/action/marker/gene-add?type=GENEP">Pseudogene</a></li>
                            <li><a href="/action/marker/gene-add?type=EFG">Foreign Gene</a></li>
                            <li><a href="/action/marker/clone-add">Clone</a></li>
                            <li><a href="/action/antibody/add">Antibody</a></li>
                            <li><a href="/action/marker/transcript-add">Transcript</a></li>
                            <li><a href="/action/marker/engineeredRegion-add">Engineered Region</a></li>
                            <li><a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=MRPHLNO">Morpholino</a></li>
                            <li><a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=TALEN">TALEN</a></li>
                            <li><a href="/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentType=CRISPR">CRISPR</a></li>
                        </ul>
                    </div>

                    <div class="nav-column">
                        <span class="nav-column-header">Add</span>
                        <ul>
                            <li><a href="/action/profile/person/create">Person</a></li>
                            <li><a href="/action/profile/lab/create">Lab</a></li>
                            <li><a href="/action/profile/company/create">Company</a></li>
                            <li><a href="/action/publication/new#">Pub</a></li>
                            <li><a href="/action/publication/journal-add">Journal</a></li>
                            <li><a href="/action/feature/alleleDesig-add-form">Line Designation</a></li>
                        </ul>

                        <span class="nav-column-header">Distribution List</span>
                        <ul>
                            <li><a href="/action/profile/distribution-list">Full</a></li>
                            <li><a href="/action/profile/distribution-list?subset=usa">USA only</a></li>
                            <li><a href="/action/profile/distribution-list?subset=pi">PI only</a></li>
                        </ul>
                    </div>

                    <div class="nav-column">
                        <span class="nav-column-header">Development</span>
                        <ul>
                            <li><a href="/action/devtool/home">Developer Tools</a></li>
                            <li><a href="/action/devtool/deployed-version">Deployed Version</a></li>
                            <li><a href="/jobs">Jenkins Jobs</a></li>
                            <li><a href="/solr">Solr Admin</a></li>
                        </ul>
                    </div>
                </div>
            </li>
        </authz:authorize>
    </ul>

    <div class="right">
        <div class="search">
            <form class="fs-autocomplete" method="GET" action="/search">
                <input placeholder="Search" name="q" autocomplete="off" type="text">
                <button type="submit">
                    <i class="fas fa-search"></i>
                </button>
            </form>
        </div>
        <ul class="menu list-unstyled">
            <c:choose>
                <c:when test="${!empty currentUser}">
                    <li class="reference no-border">
                        <span class="title"><i class="fas fa-fw fa-user"></i></span>
                        <div class="dropdown left">
                            <div class="nav-column">
                                <span class="nav-column-header">${currentUser.display}</span>
                                <ul class="list-unstyled">
                                    <li><a href="/${currentUser.zdbID}">Your Profile</a></li>
                                    <c:choose>
                                        <c:when test="${fn:length(currentUser.labs) == 1}">
                                            <c:forEach items="${currentUser.labs}" var="lab">
                                                <!-- ${lab} -->
                                                <!-- ${lab.toString()} -->
                                                <li><a href="/${lab.zdbID}">Your Lab</a></li>
                                            </c:forEach>
                                        </c:when>
                                        <c:when test="${fn:length(currentUser.labs) > 1}">
                                            <c:forEach items="${currentUser.labs}" var="lab">
                                                <li><a href="/${lab.zdbID}">${lab.name}</a></li>
                                            </c:forEach>
                                        </c:when>
                                    </c:choose>
                                    <c:if test="${currentUserHasZebraShareSubmissions}">
                                        <li><a href="/action/zebrashare/dashboard">Your ZebraShare Submissions</a></li>
                                    </c:if>
                                    <li><a href="/action/logout">Logout</a></li>
                                </ul>
                            </div>
                        </div>
                    </li>
                </c:when>
                <c:otherwise>
                    <li class="no-border">
                        <a href="/action/login">Sign In</a>
                    </li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</header>