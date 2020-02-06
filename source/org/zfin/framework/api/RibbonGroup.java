package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RibbonGroup {

    private String id;
    private String label;
    private String description;
    private Type type;

    public enum Type {
        TERM("Term"),
        ALL("All"),
        OTHER("Other");

        private String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        @JsonValue
        public String toString() {
            return displayName;
        }
    }

}
