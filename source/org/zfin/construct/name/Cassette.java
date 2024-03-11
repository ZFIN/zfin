package org.zfin.construct.name;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Cassette {
    public static final String COMPONENT_SEPARATOR = ":";
    private static final String STORED_COMPONENT_SEPARATOR = "#";

    @JsonUnwrapped
    private Promoter promoter = new Promoter();

    @JsonUnwrapped
    private Coding coding = new Coding();
    private int cassetteNumber;

    public Cassette (Promoter promoter, Coding coding) {
        this.promoter = promoter;
        this.coding = coding;
    }

    public static Cassette fromStoredName(String cassetteName) {
        String promoterString = StringUtils.substringBefore(cassetteName, COMPONENT_SEPARATOR);
        String codingString = StringUtils.substringAfter(cassetteName, COMPONENT_SEPARATOR);

        String[] promoterArray = promoterString.split(STORED_COMPONENT_SEPARATOR);
        String[] codingArray = codingString.split(STORED_COMPONENT_SEPARATOR);

        Cassette cassette = new Cassette();
        cassette.setPromoter(new Promoter(promoterArray));
        cassette.setCoding(new Coding(codingArray));

        return cassette;
    }

    public String toString() {
        List<String> nameParts = new ArrayList<>();
        if (StringUtils.isNotEmpty(promoter.toString())) {
            nameParts.add(promoter.toString());
        }
        if (StringUtils.isNotEmpty(coding.toString())) {
            nameParts.add(coding.toString());
        }
        return String.join(COMPONENT_SEPARATOR, nameParts);
    }
}
