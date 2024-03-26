package org.zfin.construct.name;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zfin.marker.Marker;

import java.util.Optional;

import static org.zfin.construct.presentation.ConstructComponentService.getConstructTypeEnumByConstructName;
import static org.zfin.construct.presentation.ConstructComponentService.getTypeAbbreviationFromType;

/**
 * ConstructName is a structured way of representing a construct name.
 * It is composed of a construct type, a prefix, and a list of cassettes.
 *
 * Example of the structure in json format:
 *
 * Tg5(tdg.1-Hsa.TEST1:EGFP,tdg.2-Hsa.TEST2:EGFP) structured version is analogous to this json object:
 * {
 *   "typeAbbreviation": "Tg",
 *   "prefix": "5",
 *   "cassettes": [
 *     {
 *       "cassetteNumber": 1,
 *       "promoterParts": [
 *         "tdg.1",
 *         "-",
 *         "Hsa.TEST1"
 *       ],
 *       "codingParts": [
 *         "EGFP"
 *       ]
 *     },
 *     {
 *       "cassetteNumber": 2,
 *       "promoterParts": [
 *         ",",
 *         "tdg.2",
 *         "-",
 *         "Hsa.TEST2"
 *       ],
 *       "codingParts": [
 *         "EGFP"
 *       ]
 *     }
 *   ]
 * }
 *
 */
@Data
@NoArgsConstructor
public class ConstructName {
    private Marker.Type type;
    private String prefix = "";

    @JsonUnwrapped
    private Cassettes cassettes = new Cassettes();

    public ConstructName(String typeAbbreviation, String prefix) {
        getConstructTypeEnumByConstructName(typeAbbreviation).ifPresent(this::setType);
        this.prefix = prefix;
        this.cassettes = new Cassettes();
    }

    /**
     * Construct name from a stored name.
     * The stored name is a string submitted by the user that contains the construct cassettes with "#" separators.
     *
     * @param typeAbbreviation example: "Tg"
     * @param prefix example: "5"
     * @param storedName example: "tdg.1#-#Hsa.TEST1#:EGFP#Cassette#,#tdg.2#-#Hsa.TEST2#:EGFP#"
     * @return
     */
    public static ConstructName fromStoredName(String typeAbbreviation, String prefix, String storedName) {
        ConstructName name = new ConstructName();
        Optional<Marker.Type> maybeType = getConstructTypeEnumByConstructName(typeAbbreviation);
        maybeType.ifPresent(name::setType);

        name.setPrefix(prefix);
        name.setCassettes(Cassettes.fromStoredName(storedName));
        return name;
    }

    public String getTypeAbbreviation() {
        Optional<String> typeAbbr = getTypeAbbreviationFromType(type);
        if (typeAbbr.isEmpty()) {
            throw new RuntimeException("Could not determine construct type abbreviation from construct type: " + type);
        }
        return typeAbbr.get();
    }

    public String getTypeAbbreviationOrEmpty() {
        Optional<String> typeAbbr = getTypeAbbreviationFromType(type);
        if (typeAbbr.isEmpty()) {
            return "";
        }
        return typeAbbr.get();
    }

    public String toString() {
        return getTypeAbbreviationOrEmpty() +
                prefix +
                "(" +
                cassettes.toString() +
                ")";
    }

    public void addCassette(Cassette cassette) {
        cassettes.add(cassette);
    }

    public void addCassette(Promoter promoter, Coding coding) {
        cassettes.add(new Cassette(promoter, coding));
    }

    public void setCassettesFromStoredName(String storedName) {
        cassettes = Cassettes.fromStoredName(storedName);
    }

    public void setTypeByAbbreviation(String componentValue) {
        getConstructTypeEnumByConstructName(componentValue).ifPresent(this::setType);
    }

    public void reinitialize() {
        cassettes.reinitialize();
    }
}