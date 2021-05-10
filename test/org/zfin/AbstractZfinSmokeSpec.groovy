package org.zfin

import geb.spock.GebSpec

/**
 * Basic setup for web tests
 */
abstract class AbstractZfinSmokeSpec extends GebSpec {

    def setupSpec() {
        TestConfiguration.configure()

        //This is how we *could* enable javascript, but we'll need to upgrade HtmlUnit first, or use a different browser for geb
        //getDriver().setJavascriptEnabled(true)

    }


}
