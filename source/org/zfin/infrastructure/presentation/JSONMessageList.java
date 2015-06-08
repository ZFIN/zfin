package org.zfin.infrastructure.presentation;

import java.util.Collection;
import java.util.List;

public class JSONMessageList {

    private Collection<String> errors;
    private Collection<String> warnings;

    public Collection<String> getErrors() {
        return errors;
    }

    public void setErrors(Collection<String> errors) {
        this.errors = errors;
    }

    public Collection<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(Collection<String> warnings) {
        this.warnings = warnings;
    }

}
