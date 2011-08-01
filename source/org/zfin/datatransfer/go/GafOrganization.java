package org.zfin.datatransfer.go;

/**
 *
 */
public class GafOrganization {

    public enum OrganizationEnum {
        ZFIN("ZFIN"),
        FP_INFERENCES("FP Inferences"),
        GOA("GOA"),
        PAINT("PAINT"),;

        private String value;

        private OrganizationEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static OrganizationEnum getType(String type) {
            for (OrganizationEnum t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No OrganizationEnum named " + type + " found.");
        }
    }

    private long id;
    private String organization;
    private String definition;
    private String url;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
