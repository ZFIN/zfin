package org.zfin.framework

import geb.spock.GebSpec

/**
 * Trying out a Geb/Spock functional (smoke) test.
 */
class GroovyWebSpec extends GebSpec {

    def "marker search for fgf8a"() {
        when: "go to the marker search page"
            go "http://cell.zfin.org/action/marker/search"

        then: "should find the input box"
           $('input',name:'input_name')

    }
}