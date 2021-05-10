package org.zfin.gwt.root.util;

import com.google.gwt.i18n.client.Dictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * This convenience class reads JavaScript variables and puts them into dictionaries.
 */
public class JavaScriptPropertyReader {

    public final static String LOOKUP_STRING = "LookupProperties";

    public static List<Dictionary> getDictionaries() {
        List<Dictionary> dictionaries = new ArrayList<Dictionary>();

        for (int index = -1; index < 20; index++) {
            try {
                String javascriptVariableName = JavaScriptPropertyReader.LOOKUP_STRING;
                if (index > -1)
                    javascriptVariableName += index;
                Dictionary dictionary = Dictionary.getDictionary(javascriptVariableName);
                if (dictionary != null)
                    dictionaries.add(dictionary);
            } catch (Exception e) {
                // ignore as there is no other way to inspect if there are more variables defined.
            }
        }
        return dictionaries;
    }
}
