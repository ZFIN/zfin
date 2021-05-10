package org.zfin.marker.pages

import geb.Module

class SearchFormModule extends Module {
    static content = {
        nameField { $("input#name") }
        searchButton{ $(".search-form-bottom-bar button[type='submit']") }
    }
}
