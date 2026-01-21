<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h1>Submitting Data to ZFIN</h1>

        <p>
            Data in ZFIN are primarily manually curated from peer-reviewed publications or through data exchanges and
            submissions from other databases. With the increased production of research data, ZFIN aims to provide the
            zebrafish research community with support for direct submission of data sets that cannot be easily represented
            in publications. A detailed description of the process for direct submission of data is presented in
            <a href="/ZDB-PUB-160725-13">Howe et al. Methods in Cell Biology Vol. 135, 2016, pg 483-508</a>. In addition,
            the templates including the necessary set of information for each data type are made available
            <a href="https://docs.google.com/spreadsheets/d/1p7e6LyxU1wSObD4q8Fon0f6Kf5w-O_xPF-QTmDZSwJc/edit#gid=2099777656">here</a>.
        </p>

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
            <h4>Submit a proposed gene name</h4>
            <p>
                <a href="/action/nomenclature/gene-name">Propose and reserve a name and symbol for a gene</a> in accordance
                with current guidelines. Search existing records in ZFIN for potential naming conflicts prior to submission.
            </p>
        </div>

        <hr />

        <div>
            <h4>Submit a proposed mutant/Tg line name</h4>
            <p>
                <a href="/action/nomenclature/line-name">Propose and reserve a name for a mutant or transgenic fish line</a>
                in accordance with current guidelines. Search existing records in ZFIN for potential naming conflicts prior
                to submission.
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
            <h4>Request a zebrafish anatomical ontology term</h4>
            <p>
                The anatomical ontology is a list of structures, organized hierarchically into an ontology, with
                descriptions of each structure.
                <a href="https://github.com/cerivs/zebrafish-anatomical-ontology/issues">Request anatomical term at GitHub</a>
                or email us: <zfin2:mailTo to="curators@zfin.org"/>.
            </p>
        </div>

        <hr />
        <div>
            <h4>Create ZFIN Account</h4>
            <p>
                <a href="/action/profile/person/submit">Request a ZFIN account</a> to be able to edit your records.
            </p>
        </div>

        <div>
            <h4>Create Lab Record</h4>
            <p>
                <a href="/action/profile/organization/submit">Request a lab record</a>.
            </p>
        </div>

        <hr />

        <p>
            Have a question about other data submissions? ZFIN welcomes
            <a href="https://wiki.zfin.org/display/general/ZFIN+Contact+Information">questions, comments and suggestions</a>
            from
            the community.
        </p>
    </div>
</z:page>