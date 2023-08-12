package org.zfin.uniprot.diff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UniProtDiffSetJsonSerializer {
    public static String serializeToString(UniProtDiffSet diffSet) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(diffSet);
        return json;
    }
}
