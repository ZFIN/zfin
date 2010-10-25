package org.zfin.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SqlQueryUtilTest {

    @Test
    public void getHumanReadableQueryString() {
        String rawQuery = "load from ontology_header.unl" + SqlQueryUtil.true_newline +
                "  insert into tmp_header;";

        assertEquals("load from ontology_header.unl @insert into tmp_header; ", SqlQueryUtil.getHumanReadableQueryString(rawQuery, "@"));
    }
}
