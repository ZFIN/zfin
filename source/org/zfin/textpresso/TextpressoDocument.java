package org.zfin.textpresso;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextpressoDocument {

    private String year;
    @JsonProperty("doc_type")
    private String type;
    private String identifier;
    private String score;
    private String title;
    private String author;
    private String accession;
    private String journal;

}
