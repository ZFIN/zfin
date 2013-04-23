package org.zfin.util;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

public class TermStageUpdateFileParserTest {

    private static final String FILENAME = "term_figure_stage_update.txt";

    @Test
    public void parseFile() {
        File folder1 = new File("test", "ontologies");
        File folder = new File(folder1, "data-transfer");
        File file = new File(folder, FILENAME);
        TermStageUpdateFileParser parser = new TermStageUpdateFileParser(file);
        List<TermStageSplitStatement> list = parser.parseFile();
        assertNotNull(list);
        assertEquals(3, list.size());
    }

}
