package org.zfin.textpresso;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Matches {

    @JsonProperty("matches")
    private List<String> matchedSentences;

    private String identifier;

}
