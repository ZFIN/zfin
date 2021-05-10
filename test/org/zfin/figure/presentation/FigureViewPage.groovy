package org.zfin.figure.presentation

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum


class FigureViewPage extends Page {
    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/figure/view"
    static at = { $("meta", name:"figure-view-page") }

    static content = {
        additionalFiguresLink (required: false) { $("a.additional-figures-link")}
        submittedByLinks (required: false) { $(".submitted-by a.person-link") }
        expressionQualifierColumnValues (required: false) { $("td.qualifier")}
        footer { $('div', id: 'footer') }
        footerCredits { $('div', id: 'footercredits') }
    }

}
