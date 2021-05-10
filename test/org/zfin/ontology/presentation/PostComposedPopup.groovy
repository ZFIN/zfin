package org.zfin.ontology.presentation

import org.zfin.properties.ZfinPropertiesEnum

class PostComposedPopup extends PostComposedPage {
    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/ontology/post-composed-term-detail-popup"
    static at = { $(".popup-header") }
}
