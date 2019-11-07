<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<footer>
    <div class="footer-row">
        <div class="footer-contact">
            <img src="/images/ZfinWordmarkWhite.gif">

            <div>The Zebrafish Information Network</div>

            <div class="contact-info">
                <div class="contact-icon"><i class="fas fa-fw fa-map-marker-alt"></i></div>
                <div class="contact-details">
                    <address>
                        5291 University of Oregon<br>
                        Eugene, OR 97403-5291
                    </address>
                </div>
            </div>

            <zfin2:mailTo>
                <div class="contact-info">
                    <div class="contact-icon"><i class="fas fa-fw fa-envelope"></i></div>
                    <div class="contact-details">zfinadmn@zfin.org</div>
                </div>
            </zfin2:mailTo>

            <a href="https://twitter.com/ZFINmod">
                <div class="contact-info">
                        <div class="contact-icon"><i class="fab fa-fw fa-twitter"></i></div>
                        <div class="contact-details">@ZFINmod</div>
                </div>
            </a>
        </div>

        <div class="footer-nav">
            <div class="nav-column">
                <div class="nav-column-header">Search</div>
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
                <div class="nav-column-header">Data</div>
                <ul>
                    <li><a href="/downloads">Downloads</a></li>
                    <li><a href="/action/submit-data">Submit Data</a></li>
                </ul>

                <div class="nav-column-header">Resources</div>
                <ul>
                    <li><a href="/zf_info/zfbook/zfbk.html">The Zebrafish Book</a></li>
                    <li><a href="http://zebrafish.org">ZIRC</a></li>
                </ul>

                <div class="nav-column-header">Genomics</div>
                <ul>
                    <li><a href="/action/blast/blast">BLAST</a></li>
                    <li><a href="@GBROWSE_PATH_FROM_ROOT@">Genome Browser</a></li>
                </ul>
            </div>

            <div class="nav-column">
                <div class="nav-column-header">Community</div>
                <ul>
                    <li><a href="https://@WIKI_HOST@/display/news">News</a></li>
                    <li><a href="https://@WIKI_HOST@/display/meetings">Meetings</a></li>
                    <li><a href="https://@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements">Jobs</a></li>
                    <li><a href="/action/profile/person/search">People</a></li>
                    <li><a href="/action/profile/lab/search">Labs</a></li>
                    <li><a href="/action/profile/company/search">Companies</a></li>
                </ul>
            </div>

            <div class="nav-column">
                <div class="nav-column-header">Support</div>
                <ul>
                    <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Tips">Help & Tips</a></li>
                    <li><a href="/zf_info/glossary.html">Glossary</a></li>
                    <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Single+Box+Search+Help">Single Box Search</a></li>
                    <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Database+Information">About ZFIN</a></li>
                    <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Database+Information">Citing ZFIN</a></li>
                    <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Contact+Information">Contact Information</a></li>
                </ul>
            </div>
        </div>
    </div>

    <div class="footer-row copyright-row">
        <div>
            <a href="http://www.uoregon.edu/"><img src="/images/UOSignature-WHT.png"></a>
            <a href="http://www.alliancegenome.org/"><img src="/images/AllianceLogoWhite.png"></a>
        </div>
        <div class="nav-column right-align">
            <div class="nowrap">&copy; 1994&ndash;${copyrightYear} University of Oregon</div>
            <div><a href="/zf_info/warranty.html">Terms of Use</a></div>
            <div><small>ZFIN logo designed by Kari Pape</small></div>
            <div style="max-width: 450px"><small>Home page banner reprinted from Hearing Research, 341, Monroe, J.D. et al., Hearing sensitivity differs between zebrafish lines used in auditory research, 220-231, Copyright (2016) with permission from Elsevier</small></div>
        </div>
    </div>
</footer>
