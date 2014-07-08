package org.zfin

import geb.spock.GebSpec

/**
 * Basic setup for web tests
 */
abstract class AbstractZfinSmokeSpec extends GebSpec {

    def setupSpec() {
        TestConfiguration.configure();
    }


}
