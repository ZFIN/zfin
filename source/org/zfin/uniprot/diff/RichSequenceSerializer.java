package org.zfin.uniprot.diff;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.biojavax.CrossRef;
import org.biojavax.Note;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.UniProtTools;

import java.io.IOException;

public class RichSequenceSerializer extends JsonSerializer<RichSequence> {
    @Override
    public void serialize(RichSequence value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("accession", value.getAccession());
        gen.writeStringField("rawData", UniProtTools.sequenceToString(value));
        gen.writeEndObject();
    }

}
