package org.zfin.marker.Pages

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum

/**
 * A geb Page object for ZFIN Marker Search pages
 */
class MarkerSearchPage extends Page {

    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/marker/search"
    static at = { title == "ZFIN Search Markers" }

    static content = {

        nameField { $("input[name=input_name]") }
        searchButton{ $("input[value='Search']") }

    }



}
