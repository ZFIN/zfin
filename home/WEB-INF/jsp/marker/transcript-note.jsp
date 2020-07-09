<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<style type="text/css">
    div.summary li { padding-top: .1em; padding-bottom: .1em; }
</style>

<div class="popup-header">
    Transcripts
</div>
<br class="popup-body">
    The Transcript section on the gene page provides a GBrowse image and a table of transcripts produced by the gene. The transcript table lists the type of transcript, the name which links to the more detailed transcript page, an external link to the corresponding transcript page in Ensembl, the length and a sequence analysis button that populates BLAST searches at ZFIN, NCBI, Ensembl and UCSC.
    A GBrowse image showing all the transcripts of the gene in a genomic context is presented when available. Links to Genome Browsers at UCSC, NCBI, Ensembl and ZFIN are available at the top of the GBrowse image in the transcript section.
    The transcript records are added to ZFIN and linked to ZFIN gene records during a data exchange with Ensembl, where the transcript records are generated. Transcript annotations are derived from manual annotations by the HAVANA team at the Sanger Institute or through automated annotation by the Ensemble annotation pipeline. Some non-coding transcripts are added to ZFIN through manual curation of literature by ZFIN Staff.
    A detailed description of the transcript types can be accessed by clicking on the 'i' icon next to the Type column heading in the table.
   <p>If you have questions or suggestions, please <zfin2:mailTo>contact us</zfin2:mailTo>.</p>
</div>
