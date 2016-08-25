package org.zfin.marker.presentation

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.marker.Marker
import spock.lang.Unroll

class GeneAddFormBeanValidatorSpec extends AbstractZfinIntegrationSpec {

    GeneAddFormBean form = new GeneAddFormBean(
            type: "Gene",
            name: "A really great gene 2b",
            abbreviation: "argg2b",
            publicationId: "ZDB-PUB-111111-1"
    )
    GeneAddFormBeanValidator validator = new GeneAddFormBeanValidator();

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

    def "validator should reject form with no type"() {
        given:
        form.type = "";
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        errors.hasFieldErrors("type")
        errors.getFieldError("type").code == "gene.type.empty"
    }

    @Unroll
    def "validator should reject #type form with #description"(String type, String description, String field, String value, String code) {
        given:
        form."$field" = value
        Errors errors = new BeanPropertyBindingResult(form, "form")

        when:
        validator.validate(form, errors)

        then:
        errors.hasFieldErrors(field)
        errors.getFieldError(field).code == code

        where:
        type                     | description                 | field           | value                   || code
        Marker.Type.GENE.name()  | "name not provided"         | "name"          | ""                      || "gene.name.empty"
        Marker.Type.GENE.name()  | "name already used"         | "name"          | "paired box 2a"         || "gene.name.inuse"
        Marker.Type.EFG.name()   | "name already used"         | "name"          | "DsRed"                 || "gene.name.inuse"
        Marker.Type.GENE.name()  | "abbreviation not provided" | "abbreviation"  | ""                      || "gene.abbreviation.empty"
        Marker.Type.GENEP.name() | "abbreviation not provided" | "abbreviation"  | ""                      || "gene.abbreviation.empty"
        Marker.Type.GENEP.name() | "abbreviation already used" | "abbreviation"  | "hoxa10ap"              || "gene.abbreviation.inuse"
        Marker.Type.GENE.name()  | "pub not provided"          | "publicationId" | ""                      || "pub.empty"
        Marker.Type.EFG.name()   | "pub not valid"             | "publicationId" | "ZDB-PUB-111111-111111" || "pub.notfound"
        Marker.Type.EFG.name()   | "name with gene abbrev"     | "name"          | "pax2a"                 || "gene.name.inuse"
        Marker.Type.GENE.name()  | "abbrev with upper case"    | "abbreviation"  | "LoLz1"                 || "gene.abbreviation.invalidcharacters"
    }

}
