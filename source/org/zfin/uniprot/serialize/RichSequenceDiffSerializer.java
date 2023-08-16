package org.zfin.uniprot.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.biojavax.CrossRef;
import org.biojavax.Note;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.diff.RichSequenceDiff;

import java.io.IOException;

public class RichSequenceDiffSerializer extends JsonSerializer<RichSequenceDiff> {
    @Override
    public void serialize(RichSequenceDiff value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("accession", value.getAccession());

        gen.writeArrayFieldStart("addedCrossRefs");
        for (CrossRef crossRef : value.getAddedCrossRefs()) {
            gen.writeStartObject();
            gen.writeStringField("dbName", crossRef.getDbname());
            gen.writeStringField("accession", crossRef.getAccession());
            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("removedCrossRefs");
        for (CrossRef crossRef : value.getRemovedCrossRefs()) {
            gen.writeStartObject();
            gen.writeStringField("dbName", crossRef.getDbname());
            gen.writeStringField("accession", crossRef.getAccession());
            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("addedKeywords");
        for (Note kw : value.getAddedKeywords()) {
            gen.writeString(kw.getValue());
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("removedKeywords");
        for (Note kw : value.getRemovedKeywords()) {
            gen.writeString(kw.getValue());
        }
        gen.writeEndArray();

        // Get the existing SequenceSerializer from the SerializerProvider
        JsonSerializer<RichSequence> sequenceSerializer = new RichSequenceSerializer();

        gen.writeFieldName("oldSequence");
        sequenceSerializer.serialize(value.getOldSequence(), gen, serializers);

        gen.writeFieldName("newSequence");
        sequenceSerializer.serialize(value.getNewSequence(), gen, serializers);

        gen.writeEndObject();
    }

}
