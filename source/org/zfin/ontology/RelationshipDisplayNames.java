package org.zfin.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class reads the display names for the ontology relationship names,
 * forward and inverse, from a property file, relationship-display-names.properites.
 */
public class RelationshipDisplayNames {

    private static final String FILE_NAME = "relationship-display-names.properties";

    private static Properties properties;
    public static final String DOT = ".";
    public static final String FORWARD = "forward";
    public static final String INVERSE = "inverse";
    public static final String ONTOLOGY_WILDCARD = "*";

    public RelationshipDisplayNames() {
        readPropertiesFile();
    }

    /**
     * Retrieve the display name of a given relationship type in forward or
     * inverse direction.
     *
     * @param relationshipType relationship type as used in the Ontology
     * @param forward          true (as given in the ontology) or false
     * @return display string
     */
    public static String getRelationshipName(String relationshipType, boolean forward) {
        return getRelationshipName(relationshipType, null, forward);
    }

    /**
     * Retrieve the display name of a given relationship type for a given ontology in forward or
     * inverse direction.
     *
     * @param ontology         Ontology
     * @param relationshipType relationship type as used in the Ontology
     * @param forward          true (as given in the ontology) or false
     * @return display string
     */
    public static String getRelationshipName(String relationshipType, Ontology ontology, boolean forward) {
        String key = generatePropertyKey(ontology, relationshipType, forward);
        if (properties == null) {
            return createDefaultDisplayName(relationshipType, forward);
        }
        String specificValue = (String) properties.get(key);
        if (specificValue != null)
            return specificValue;

        String generalKey = generatePropertyKey(null, relationshipType, forward);
        String generalValue = (String) properties.get(generalKey);
        if (generalValue != null)
            return generalValue;
        return createDefaultDisplayName(relationshipType, forward);
    }

    private static String createDefaultDisplayName(String relationshipType, boolean forward) {
        if (forward)
            return relationshipType;
        else
            return "inverse " + relationshipType;
    }

    private void readPropertiesFile() {
        try {
            InputStream inStream = this.getClass().getResourceAsStream(FILE_NAME);
            properties = new Properties();
            properties.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generatePropertyKey(Ontology ontology, String relationshipType, boolean forward) {
        StringBuffer key = new StringBuffer();
        if (ontology != null)
            key.append(ontology.getOntologyName());
        else
            key.append(ONTOLOGY_WILDCARD);
        key.append(DOT);
        key.append(relationshipType);
        key.append(DOT);
        if (forward)
            key.append(FORWARD);
        else
            key.append(INVERSE);
        return key.toString();
    }
}
