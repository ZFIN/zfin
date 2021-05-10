package org.zfin.publication.presentation

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.zfin.AbstractZfinIntegrationSpec
import spock.lang.Unroll

class PublicationValidatorSpec extends AbstractZfinIntegrationSpec {

    ObjectUnderTest tester

    def setup() {
        tester = new ObjectUnderTest()
    }

    @Unroll
    def "validator should accept '#id' a valid pub id"(String id) {
        given:
        Errors errors = setupErrors(id)

        when:
        PublicationValidator.validatePublicationID(id, "pubId", errors)

        then:
        !errors.hasErrors()

        where:
        id << ["ZDB-PUB-111111-1", "111111-1"]
    }

    @Unroll
    def "validator should reject '#id' as a blank value"(String id) {
        given:
        Errors errors = setupErrors(id)

        when:
        PublicationValidator.validatePublicationID(id, "pubId", errors)

        then:
        errors.hasFieldErrors("pubId")
        errors.getFieldError("pubId").getCode() == "pub.empty"

        where:
        id << ["", " ", "      "]
    }

    @Unroll
    def "validator should reject '#id' as an invalid id"(String id) {
        given:
        Errors errors = setupErrors(id)

        when:
        PublicationValidator.validatePublicationID(id, "pubId", errors)

        then:
        errors.hasFieldErrors("pubId")
        errors.getFieldError("pubId").getCode() == "pub.invalid"

        where:
        id << ["012345678", "ZDBPUB-111111-1", "ZDBPUB-1111111111-1", "PUB! PUB! PUB!"]
    }

    @Unroll
    def "validator should reject '#id' as non-existent pub"(String id) {
        given:
        Errors errors = setupErrors(id)

        when:
        PublicationValidator.validatePublicationID(id, "pubId", errors)

        then:
        errors.hasFieldErrors("pubId")
        errors.getFieldError("pubId").getCode() == "pub.notfound"

        where:
        id << ["ZDB-PUB-111111-11111111111"]
    }

    @Unroll
    def "isShortVersion should return true for '#id'"(String id) {
        when:
        def valid = PublicationValidator.isShortVersion(id)

        then:
        valid

        where:
        id << ["123456-111", " 132435-1 "]
    }

    @Unroll
    def "isShortVersion should return false for '#id'"(String id) {
        when:
        def valid = PublicationValidator.isShortVersion(id)

        then:
        !valid

        where:
        id << [null, "ZDB-PUB-132435-1", "so so false"]
    }

    def "completeZdbID should return null when given null"() {
        when:
        def completed = PublicationValidator.completeZdbID(null)

        then:
        completed == null
    }

    def "completeZdbID should return a complete ZDB-ID for a valid short id"() {
        when:
        def shortId = "123456-111"
        def completed = PublicationValidator.completeZdbID(shortId)

        then:
        completed == "ZDB-PUB-" + shortId
    }

    def "completeZdbID should return the input unmodified when not a valid short id"() {
        when:
        def invalidShort = "what the heck?!"
        def completed = PublicationValidator.completeZdbID(invalidShort)

        then:
        completed == invalidShort
    }

    /*
     * PublicationValidator doesn't act like a normal Validator class (i.e. one
     * that actually inherits from Validator), so we need to go through a bit of
     * rigmarole to set up an Errors object appropriately for testing purposes.
     */

    def setupErrors(String pubId) {
        tester.pubId = pubId
        new BeanPropertyBindingResult(tester, "pubId")
    }

    private class ObjectUnderTest {
        String pubId
    }
}
