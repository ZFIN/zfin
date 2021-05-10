package org.zfin.marker.pages

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum

/**
 * A geb Page object for ZFIN Marker Search pages
 */
class MarkerSearchPage extends Page {
    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/marker/search"

    static at = { title == "ZFIN Marker Search" }

    static content = {
        searchForm { module(SearchFormModule) }
    }
}
