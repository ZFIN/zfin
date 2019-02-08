<%@ taglib prefix="auths" uri="http://www.springframework.org/security/tags" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<ul>
    <li><a href="/action/marker/search"
               title="Search by name, accession number, chromosome, vector or sequence type">
                <strong>Genes / Markers / Clones</strong>
        </a>
    </li>
    <ul>
        <li><a href="https://@WIKI_HOST@/display/general/ZFIN+Zebrafish+Nomenclature+Conventions"
               title="Zebrafish nomenclature conventions">Nomenclature Conventions</a></li>
        <li><a href="/action/nomenclature/gene-name"
               title="Contact the Nomenclature Committee">Obtain approval for gene names</a>
    </ul>
</ul>


<ul>
    <li><a href="/action/blast/blast"
           title="Search for sequence alignment against ZFIN datasets and zebrafish datasets">
        <strong>BLAST</strong> at ZFIN</a> </li>
    <li><a href="/@GBROWSE_PATH_FROM_ROOT@"><strong>GBrowse</strong> genome browser</a>
    </li>
</ul>


<ul>
    <li>
        <a class="small" href="/action/expression/search"
           title="Search by gene, developmental stage, anatomy and other attributes">
            <strong>Gene Expression</strong>
        </a>
    </li>

    <li><a href="/action/antibody/search"
           title="Search for antibodies by gene, labeled anatomy and other attributes"><strong>Antibodies</strong></a></li>

</ul>

<ul>
    <li><a href="/action/fish/search"
           title="Search by allele, mutant/morphant gene, affected anatomy and other attributes">
        <strong>Mutants / Knockdowns / Transgenics</strong>
    </a></li>
    <ul>
        <li><a href="/action/feature/wildtype-list"
               title="Zebrafish wild-type lines">Wild-Type Lines</a></li>
        <li><a href="/action/feature/line-designations" title ="Line designation nomenclature and requests">Line Designations</a></li>
        <li><a href="/action/nomenclature/line-name" title="Guidelines for proposing and reserving line names">
            Submit mutant/transgenic line names</a>
    </ul>
</ul>
<ul>
    <li><a href="/search?q=&fq=category%3A%22Construct%22&category=Construct"
           title="Search for constructs"><strong>Constructs</strong></a></li>

</ul>

<ul>
    <li><a href="/action/ontology/search"
           title="Search anatomy, gene and disease ontology"><strong>Anatomy / GO / Human Disease</strong></a></li>
    <ul>
        <li><a href="https://@WIKI_HOST@/display/general/Anatomy+Atlases+and+Resources" title="Atlases, anatomical resources and ontology">Anatomy Atlases and Resources</a></li>
    </ul>
</ul>

<ul>
    <li><a href="/action/publication/search"
           title="Search for zebrafish research publications by author, title or citation">
        <strong>Publications</strong></a></li>
    <ul>
        <li><a href="/zf_info/author_guidelines.html" title="Tips on how to facilitate published data integration in online databases and increase the impact of your research">Author Guidelines</a></li>
    </ul>
</ul>


<ul>
    <li>Community
        <ul>
            <li><a href="@SECURE_HTTP@@WIKI_HOST@" title="ZFIN-hosted community wiki">Wiki</a>:<a href="@SECURE_HTTP@@WIKI_HOST@/display/prot" title="Browse, contribute experimental protocols">&nbsp; Protocols,</a><a href="@SECURE_HTTP@@WIKI_HOST@/display/AB" title="Browse, contribute antibody data">&nbsp;Antibodies</a>

            <li>
                <a href="@SECURE_HTTP@@WIKI_HOST@/display/jobs/Zebrafish-Related+Job+Announcements" title="Zebrafish-related job announcements">Jobs</a>,
                <!-- <a href="/zf_info/news/mtgs.html" title="Zebrafish-related meeting announcements">Meetings</a>, -->
                <a href="@SECURE_HTTP@@WIKI_HOST@/display/meetings" title="Zebrafish-related meeting announcements">Meetings</a>,
                <a href="@SECURE_HTTP@@WIKI_HOST@/display/general/Zebrafish+Newsgroup+Information" title="Moderated, online discussion group for anyone intested in zebrafish research">Newsgroup</a>
            <li><a href="/action/profile/person/search"
                   title="Search for zebrafish researchers by name or address">
                People,</a>

                <a href="/action/profile/lab/search"
                   title="Search for laboratories by name, address or research interests">
                    Labs,</a>

                <a href="/action/profile/company/search"
                   title="Search for companies supplying zebrafish reagents">
                    Companies</a> </li>

            <c:if test="${!empty user}">

                <li><a href="/action/profile/view/${user.zdbID}"><em>Update Your ZFIN Record</em></a>

                <c:if test="${!empty user.labs}">
                    <li> View Lab Record:
                    <c:forEach var="lab" items="${user.labs}">
                        <a href="/action/profile/view/${lab.zdbID}"><em>${lab.name}</em></a>
                    </c:forEach>
                </c:if>

            </c:if>

            <li><a href="/zf_info/news/education.html" title="Educational websites for students and educators">Educational Resources</a>
            <li><a href="/zf_info/zfbook/zfbk.html" title="Browse The Zebrafish Book"><i>The Zebrafish Book</i></a>
        </ul>
</ul>

<ul>
    <li>Data
        <ul>
            <li><a href="downloads" title="Download bulk data files generated from the ZFIN database" >Downloads</a></li>
            <li>
                <a href="/action/zebrashare" title="Submit unpublished alleles to ZebraShare">ZebraShare</a>
                <c:if test="${userHasZebraShareSubmissions}">
                    <a href="/action/zebrashare/dashboard"><em>View Your ZebraShare Submissions</em></a>
                </c:if>
            </li>
            <li><a href="/action/infrastructure/annual-stats-view" title="Annual summary of ZFIN data content">Statistics</a></li>
            <li><a href="/schemaSpy/index.html" title="Browse ZFIN database schema">Data Model</a></li>
        </ul>
</ul>

