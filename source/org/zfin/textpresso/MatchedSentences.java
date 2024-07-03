package org.zfin.textpresso;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchedSentences {

    @JsonProperty("matched_sentences")
    private List<String> matchedSentences;

}
