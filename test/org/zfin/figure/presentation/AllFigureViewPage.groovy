package org.zfin.figure.presentation

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum


class AllFigureViewPage extends Page {
    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/figure/all-figure-view"
    static at = { $("meta", name:"all-figure-view-page") }

    static content = {
        submittedByLinks (required: false) { $(".submitted-by a.person-link") }
        geneLinks (required: false) { $(".genedom") }
    }

}
