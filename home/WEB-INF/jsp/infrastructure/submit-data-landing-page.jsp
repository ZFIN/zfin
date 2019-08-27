<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<div class="container-fluid">
    <h1>Submitting Data to ZFIN</h1>

    <div>
        <h4>Submit a proposed Gene Name/Symbol</h4>
        <p>
            <a href="/action/nomenclature/gene-name">Propose and reserve a name and symbol for a gene</a> in accordance
            with
            current guidelines. Search existing records in ZFIN for potential naming conflicts prior to submission.
        </p>
    </div>

    <hr />

    <div>
        <h4>Submit Mutant/Transgenic line names</h4>
        <p>
            <a href="/action/nomenclature/line-name">Propose and reserve a name for a mutant or transgenic fish line</a>
            in accordance with current guidelines. Search existing records in ZFIN for potential naming conflicts prior
            to submission.
        </p>
    </div>

    <hr />

    <div>
        <h4>ZebraShare</h4>
        <p>
            <a href="/action/zebrashare">ZebraShare</a> is intended to help establish fruitful collaborations by sharing
            mutant fish lines, and information about these mutants, particularly when authors do not otherwise see a
            clear path to publication.
        </p>
    </div>

    <hr />

    <div>
        <h4>Share Antibody Information in the ZFIN Community Antibody Wiki</h4>
        <p>
            The <a href="https://@WIKI_HOST@/display/AB">Community Antibody Wiki</a> is where zebrafish researchers can
            help each other by sharing antibody information&mdash;new antibodies, protocols and tips.
        </p>
    </div>

    <hr />

    <div>
        <h4>Share an experimental protocol in the ZFIN Protocol Wiki</h4>
        <p>
            The <a href="https://@WIKI_HOST@/display/prot">Protocol Wiki</a> is where zebrafish researchers can share
            experimental protocols and tips with the rest of the research community.
        </p>
    </div>

    <hr />

    <div>
        <h4>Submit a fish line to ZIRC</h4>
        <p>
            <a href="http://zebrafish.org/submissions/submitTerms.php">Submit fish strains</a> to the Zebrafish
            International Resource Center.
        </p>
    </div>

    <hr />

    <div>
        <h4>Request Zebrafish Anatomy Term at GitHub</h4>
        <p>
            The anatomical ontology is a list of structures, organized hierarchically into an ontology, with
            descriptions of each structure.
            <a href="https://github.com/cerivs/zebrafish-anatomical-ontology/issues">Request anatomical term at GitHub</a>
            or email us: <zfin2:mailTo to="curators@zfin.org"/>.
        </p>
    </div>

    <hr />

    <div>
        <h4>Submit questions or feedback on search results in ZFIN</h4>
        <p>
            <a href="http://zfin.org/search?q=">As you explore</a>, if you see ways to make the tool better, please tell
            us! Your input is essential to help us improve this tool to meet your needs. Send feedback with the
            "Feedback" button near the search box.
        </p>
    </div>

    <hr />

    <div>
        <h4>Create a Person, Lab or Company record in ZFIN</h4>
        <p>
            Contact <zfin2:mailTo to="zfinadmn@zfin.org"/> to create a Person, Laboratory or Company record and a ZFIN
            account to edit your records.
        </p>
    </div>

    <hr />

    <p>
        Have a question about other data submissions? ZFIN welcomes
        <a href="https://@WIKI_HOST@/display/general/ZFIN+Contact+Information">questions, comments and suggestions</a>
        from
        the community.
    </p>
</div>
