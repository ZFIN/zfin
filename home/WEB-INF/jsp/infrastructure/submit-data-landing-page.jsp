<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<div class="container-fluid">
    <h1>Submitting Data to ZFIN</h1>

    <b><a href="/action/nomenclature/gene-name">Submit a proposed Gene Name/Symbol</a></b>
    <p>
        Propose and reserve a name and symbol for a gene in accordance with current guidelines. Search existing records
        in ZFIN for potential naming conflicts prior to submission.
    </p>

    <b><a href="/action/nomenclature/line-name">Submit Mutant/Transgenic line names</a></b>
    <p>
        Propose and reserve a name for a mutant or transgenic fish line in accordance with current guidelines. Search
        existing records in ZFIN for potential naming conflicts prior to submission.
    </p>

    <b><a href="/action/zebrashare">ZebraShare</a></b>
    <p>
        ZebraShare is intended to help establish fruitful collaborations by sharing mutant fish lines, and information
        about these mutants, particularly when authors do not otherwise see a clear path to publication.
    </p>

    <b><a href="https://@WIKI_HOST@/display/AB">Share Antibody Information in the ZFIN Community Antibody Wiki</a></b>
    <p>
        This is where zebrafish researchers can share experimental protocols and tips with the rest of the research
        community.
    </p>

    <b><a href="https://@WIKI_HOST@/display/prot">Share an experimental protocol in the ZFIN Protocol Wiki</a></b>
    <p>
        This is where zebrafish researchers can share experimental protocols and tips with the rest of the research
        community.
    </p>

    <b><a href="http://zebrafish.org/submissions/submitTerms.php">Submit a fish line to ZIRC</a></b>
    <p>
        Propose and reserve a name and symbol for a gene in accordance with current guidelines. Search existing records
        in ZFIN for potential naming conflicts prior to submission.
    </p>

    <b>
        <a href="https://github.com/cerivs/zebrafish-anatomical-ontology/issues">
            Request Zebrafish Anatomy Term at GitHub
        </a>
    </b>
    <p>
        The anatomical ontology is a list of structures, organized hierarchically into an ontology, with descriptions of
        each structure. Request anatomical term at GitHub or email us: <zfin2:mailTo to="curators@zfin.org" />
    </p>

    <b><a href="http://zfin.org/search?q=">Submit questions or feedback on search results in ZFIN</a></b>
    <p>
        The anatomical ontology is a list of structures, organized hierarchically into an ontology, with descriptions of
        each structure. Request anatomical term at GitHub or email us: <zfin2:mailTo to="curators@zfin.org" />
    </p>

    <b>Create a Person, Lab or Company record in ZFIN</b>
    <p>
        Contact <zfin2:mailTo to="zfinadmn@zfin.org" /> to create a Person, Laboratory or Company record and a ZFIN
        account to edit your records.
    </p>

    <p>
        Have a question about other data submissions? ZFIN welcomes
        <a href="https://@WIKI_HOST@/display/general/ZFIN+Contact+Information">questions, comments and suggestions</a> from
        the community.
    </p>
</div>
