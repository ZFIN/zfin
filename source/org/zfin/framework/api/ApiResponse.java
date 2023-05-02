package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ApiResponse {


    @JsonView({View.Default.class, View.UI.class})
    private String errorMessage;

    @JsonView({View.Default.class, View.UI.class})
    private Map<String, String> errorMessages;

    @JsonView({View.Default.class, View.UI.class})
    private String requestDuration;

    public void addErrorMessage(String fieldName, String errorMessage) {
        if (errorMessages == null)
            errorMessages = new HashMap<>(3);
        errorMessages.put(fieldName, errorMessage);
    }

    public void addErrorMessages(Map<String, String> newErrorMessages) {
        if (newErrorMessages != null) {
            if (errorMessages == null)
                errorMessages = new HashMap<>();
            errorMessages.putAll(newErrorMessages);
        }
    }

    public boolean hasErrors() {
        return StringUtils.isNotEmpty(errorMessage) || MapUtils.isNotEmpty(errorMessages);
    }

    public String errorMessagesString() {
        if (errorMessages == null)
            return null;

        return errorMessages.entrySet().stream().map(m -> m.getKey() + " - " + m.getValue()).sorted().collect(Collectors.joining(" | "));
    }

}
