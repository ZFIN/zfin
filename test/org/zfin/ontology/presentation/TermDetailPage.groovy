package org.zfin.ontology.presentation

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum

class TermDetailPage extends Page {

    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/ontology/term-detail"
    static content = {
        contentContainer(required: true) { $(".data-page") }
        ontologyName { $("#ontology-name") }
        ontologyLinks { ontologyName.find("a") }
        termDefinition { $("#term-definition") }
        isTypeOfRow { $("#isatypeof") }
        termSynonyms { $("#term-synonyms") }
    }

}
