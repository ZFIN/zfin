package org.zfin.uniprot.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.diff.RichSequenceDiff;
import org.zfin.uniprot.diff.UniProtDiffSet;
import org.zfin.uniprot.serialize.RichSequenceDiffSerializer;
import org.zfin.uniprot.serialize.RichSequenceSerializer;

import java.io.IOException;
import java.util.Map;

public class UniProtDiffSetSerializer extends JsonSerializer<UniProtDiffSet> {
    @Override
    public void serialize(UniProtDiffSet value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        RichSequenceSerializer sequenceSerializer = new RichSequenceSerializer();
        RichSequenceDiffSerializer diffSerializer = new RichSequenceDiffSerializer();
        ObjectMapper objectMapper = new ObjectMapper();

        gen.writeStartObject();

        gen.writeFieldName("summary");
        String summaryJson = objectMapper.writeValueAsString(value.getSummary());
        gen.writeRawValue(summaryJson);

        gen.writeArrayFieldStart("addedSequences");
        for (RichSequence sequence : value.getAddedSequences()) {
            sequenceSerializer.serialize(sequence, gen, serializers);
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("removedSequences");
        for (RichSequence sequence : value.getRemovedSequences()) {
            sequenceSerializer.serialize(sequence, gen, serializers);
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("changedSequences");
        for (RichSequenceDiff diff : value.getChangedSequences()) {
            diffSerializer.serialize(diff, gen, serializers);
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }

    public static String serializeToString(UniProtDiffSet diffSet) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(diffSet);
        return json;
    }

}
