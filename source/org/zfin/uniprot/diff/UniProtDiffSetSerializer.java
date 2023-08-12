package org.zfin.uniprot.diff;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.UniProtTools;

import java.io.IOException;
import java.util.Map;

public class UniProtDiffSetSerializer extends JsonSerializer<UniProtDiffSet> {
    @Override
    public void serialize(UniProtDiffSet value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        RichSequenceSerializer sequenceSerializer = new RichSequenceSerializer();
        RichSequenceDiffSerializer diffSerializer = new RichSequenceDiffSerializer();

        gen.writeStartObject();

        gen.writeFieldName("summary");
        JsonSerializer<Object> defaultMapSerializer = serializers.findValueSerializer(Map.class, null);
        defaultMapSerializer.serialize(value.getSummary(), gen, serializers);

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

}
