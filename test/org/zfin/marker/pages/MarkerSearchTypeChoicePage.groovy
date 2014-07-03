package org.zfin.marker.Pages



class MarkerSearchTypeChoicePage extends MarkerSearchPage {

    //you know you're at this page if there's at least one type-choice-link
    static at = { $('.type-choice-link') }

    static content = {
        geneResultsLink { $('.GENE-type-choice-link') }
        transcriptResultsLink { $('.TSCRIPT-type-choice-link') }
        morpholinoResultsLink { $('.MRPHLNO-type-choice-link') }
    }
}
