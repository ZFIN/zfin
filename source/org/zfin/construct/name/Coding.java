package org.zfin.construct.name;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Coding {
    private List<String> codingParts = new ArrayList<>();

    public Coding(String[] parts) {
        for (String part : parts) {
            addIfNotEmpty(part);
        }
    }

    public static Coding create(String... parts) {
        return new Coding(parts);
    }

    public void addIfNotEmpty(String part) {
        String trimmedPart = StringUtils.trim(part);
        if (StringUtils.isEmpty(trimmedPart)) {
            return;
        }
        codingParts.add(trimmedPart);
    }

    public String toString() {
        return String.join("", codingParts);
    }

    public int size() {
        return codingParts.size();
    }
}
