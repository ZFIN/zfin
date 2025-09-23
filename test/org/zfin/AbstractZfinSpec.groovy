package org.zfin

import spock.lang.Specification


/**
 * To make sure everyoen gets the same basic setup
 */
abstract class AbstractZfinSpec extends Specification {
    public void setupSpec() {
        TestConfiguration.configure();
    }
}
