package org.zfin.construct.name;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

@Data
@NoArgsConstructor
public class Cassette {
    public static final String COMPONENT_SEPARATOR = ":";
    private static final String STORED_COMPONENT_SEPARATOR = "#";

    private Promoter promoter;
    private Coding coding;
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
        return promoter.toString() +
                COMPONENT_SEPARATOR +
                coding.toString();
    }
}
