package org.zfin.ontology.presentation

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum

class PostComposedPage extends Page {

    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/ontology/post-composed-term-detail"
    static at = { title.contains("Post-Composed Term") }
    static content = {
        termName { $(".post-composed-term-name") }
    }

}
