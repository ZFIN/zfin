package org.zfin.sequence.load;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EnsemblTranscript {

    private String id;
    @JsonProperty("Parent")
    private String parentID;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("object_type")
    private String objectType;
    private int start;
    private int end;
    private int length;
    private int version;
    private String source;
    @JsonProperty("assembly_name")
    private String assemblyName;
    private String biotype;


}
