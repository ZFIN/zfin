<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<div class="container-fluid">
    <h1>ZebraShare</h1>
    <p class="lead">
        ZebraShare is intended to help establish fruitful collaborations by sharing mutant fish lines, and information
        about
        these mutants, particularly when authors do not otherwise see a clear path to publication.
    </p>

    <div class="card-deck mb-3">
        <div class="card bg-primary">
            <a href="/zf_info/zebraShareSubmission.xlsx" class="card-body text-white">
                <h2 class="card-title">
                    <i class="fas fa-download icon"></i>
                    Download Submission Workbook
                </h2>
                <p class="card-text">
                    Get started by filling out the details for your alleles
                </p>
            </a>
        </div>

        <div class="card bg-primary">
            <a href="/action/zebrashare/new" class="card-body text-white">
                <h2 class="card-title">
                    <i class="fas fa-upload icon"></i>
                    Submit Completed Workbook
                </h2>
                <p class="card-text">
                    ZFIN curators will process your submission as part of their high priority literature curation
                    workflow
                </p>
            </a>
        </div>
    </div>

    <h2>About ZebraShare</h2>
    <p>
        ZebraShare is intended to help researchers form collaborations that put carefully constructed
        mutant lines to good use, augmenting sharing resources available through other services (i.e. ZIRC). ZebraShare
        is
        intended for researchers that wish to advertise and share high quality mutants that laboratories are not
        currently
        pursuing such as those having no observed phenotype. Contribution via ZebraShare does not constitute a
        publication,
        and labs of origination maintain full rights to the lines they generate. For instance, after listing fish on
        ZebraShare labs may decide to pursue these lines again. If fish lines are shared through ZebraShare there is an
        explicit expectation that collaborating labs will discuss appropriate authorship if research culminates in
        presentations or publications.
    </p>

    <p>
        To promote sharing of high quality lines, we ask that users only contribute lines with a confirmed lesion,
        carried
        to at minimum the F2 generation. This is not, for instance, a database for sharing tested CRISPRs. Other
        resources
        are already available for this (e.g. CRISPRz).
    </p>

    <p>
        Why do we invite data about wild-type looking fish? First, we invite data about a-phenotypic mutants to avoid
        unnecessary duplication of efforts. Second, we think this data is important because it can reveal information
        about
        a gene's requirements. Finally, understanding which single genes seem dispensable can help researchers hone in
        on
        the genes or gene-combinations which are most important to pattern tissues.
    </p>

</div>
