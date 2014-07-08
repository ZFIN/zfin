package org.zfin.framework

import geb.spock.GebSpec
import org.zfin.AbstractZfinSmokeSpec
import org.zfin.properties.ZfinPropertiesEnum

/**
 * Trying out a Geb/Spock functional (smoke) test.
 */
class GroovyWebSpec extends AbstractZfinSmokeSpec {

    def "marker search for fgf8a"() {
        when: "go to the marker search page"
            def url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/cgi-bin/webdriver?MIval=aa-newmrkrselect.apg"
            println url
            go url

        then: "should find the input box"
           $('input',name:'input_name')

    }
}