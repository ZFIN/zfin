package org.zfin.marker.presentation

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.zfin.AbstractZfinIntegrationSpec
import spock.lang.Unroll

class SequenceTargetingReagentAddBeanValidatorSpec extends AbstractZfinIntegrationSpec {

    SequenceTargetingReagentAddBean form = new SequenceTargetingReagentAddBean(
            strType: "CRISPR",
            name: "MO1-Test2b",
            targetGeneSymbol: "robo1",
            sequence: "AAAAACCCCCGGGGGTTTTT",
            publicationID: "ZDB-PUB-111111-1"
    )
    SequenceTargetingReagentAddBeanValidator validator = new SequenceTargetingReagentAddBeanValidator()

    def "validator should support form-backing bean"() {
        when:
        def supported = validator.supports(form.class)

        then:
        supported
    }

    def "validator should accept minimally filled out form"() {
        given:
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        !errors.hasErrors()
    }

    @Unroll
    def "validator should reject form with #description"(String description, String field, String value, String code) {
        given:
        form."$field" = value
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        errors.hasFieldErrors(field)
        errors.getFieldError(field).getCode() == code

        where:
        description             | field              | value                   || code
        "name not provided"     | "name"             | ""                      || "str.name.empty"
        "name already used"     | "name"             | "MO1-pax2a"             || "str.name.inuse"
        "pub not provided"      | "publicationID"    | ""                      || "pub.empty"
        "pub not valid"         | "publicationID"    | "ZDB-PUB-111111-111111" || "pub.notfound"
        "target not provided"   | "targetGeneSymbol" | ""                      || "str.target.empty"
        "target not valid"      | "targetGeneSymbol" | "SomeLousyGene-1"       || "str.target.notfound"
        "supplier not valid"    | "supplier"         | "Not a real supplier"   || "str.supplier.notfound"
        "sequence not provided" | "sequence"         | ""                      || "str.sequence.empty"
        "sequence not ATGC"     | "sequence"         | "ACTGQUERTY"            || "str.sequence.characters"
    }

    @Unroll
    def "validator should reject #type form with sequence already used"(String type, String sequence) {
        given:
        form.strType = type
        form.sequence = sequence
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        errors.hasFieldErrors("sequence")
        errors.getFieldError("sequence").getCode() == "str.sequence.inuse"

        where:
        type         | sequence
        "Morpholino" | "GGTCTGCTTTGCAGTGAATATCCAT"
        "CRISPR"     | "GCACCGGACATGGACTCGAG"
    }

    @Unroll
    def "validator should reject TALEN form with sequence #description"(String description, String sequence1, String sequence2) {
        given:
        form.strType = "TALEN"
        form.sequence = sequence1
        form.sequence2 = sequence2
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        errors.hasFieldErrors("sequence")
        errors.getFieldError("sequence").getCode() == "str.sequence.inuse"
        errors.hasFieldErrors("sequence2")
        errors.getFieldError("sequence2").getCode() == "str.sequence.inuse"

        where:
        description                | sequence1            | sequence2
        "already used"             | "TACAATACTCCCACTGAA" | "TACCAACTGTCCATGAGT"
        "already used but swapped" | "TACCAACTGTCCATGAGT" | "TACAATACTCCCACTGAA"
    }

    @Unroll
    def "validator should accept #formType form with existing #sequenceType sequence"(String formType, String sequenceType, String sequence) {
        given:
        form.strType = formType
        form.sequence = sequence
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        !errors.hasErrors()

        where:
        formType     | sequenceType | sequence
        "Morpholino" | "CRISPR"     | "GCACCGGACATGGACTCGAG"
        "CRISPR"     | "Morpholino" | "GGTCTGCTTTGCAGTGAATATCCAT"
        "Morpholino" | "TALEN"      | "TACAATACTCCCACTGAA"
        "CRISPR"     | "TALEN"      | "TACAATACTCCCACTGAA"
    }

}
