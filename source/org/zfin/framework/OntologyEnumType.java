package org.zfin.framework;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.zfin.ontology.Ontology;

@Converter
public class OntologyEnumType implements AttributeConverter<Ontology, String> {

    @Override
    public String convertToDatabaseColumn(Ontology ontology) {
        if (ontology == null)
            return null;
        return ontology.getOntologyName();
    }

    @Override
    public Ontology convertToEntityAttribute(String ontologyName) {
        if (ontologyName == null)
            return null;

        return Ontology.getOntology(ontologyName);
    }

}