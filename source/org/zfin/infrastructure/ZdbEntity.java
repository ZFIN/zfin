package org.zfin.infrastructure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ZdbEntity implements ZdbID {

    private String zdbID;

    public String getZdbType() {
        String pattern = "(ZDB-)(.*)(-.*-.*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(zdbID);
        if (m.find()) {
            return m.group(1);
        }
        return zdbID;
    }
}
