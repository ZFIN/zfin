package org.zfin.construct.name;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Promoter {
    List<String> promoter = new ArrayList<>();

    public Promoter(String[] parts) {
        for (String part : parts) {
            addIfNotEmpty(part);
        }
    }

    public static Promoter create(String... parts) {
        return new Promoter(parts);
    }

    public void addIfNotEmpty(String part) {
        String trimmedPart = StringUtils.trim(part);
        if (StringUtils.isEmpty(trimmedPart)) {
            return;
        }
        promoter.add(trimmedPart);
    }

    public String toString() {
        return String.join("", promoter);
    }

    public int size() {
        return promoter.size();
    }

    public void addPromoterPart(String componentValue) {
        promoter.add(componentValue);
    }
}
