package org.zfin.infrastructure.ant;

import org.junit.Test;
import org.zfin.marker.agr.AuthorFormatChecker.Category;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PublicationAuthorFormatReportTaskTest {

    private final Map<Category, List<List<String>>> cleanup = new EnumMap<>(Category.class);
    private final List<List<String>> missing = new ArrayList<>();

    private static List<String> pub() {
        return new ArrayList<>(List.of("ZDB-PUB-990510-2", "1993-01-01", "Abstract", "A title"));
    }

    private void classify(String authors) {
        for (Category category : Category.values()) {
            cleanup.computeIfAbsent(category, c -> new ArrayList<>());
        }
        PublicationAuthorFormatReportTask.classify(pub(), authors, cleanup, missing);
    }

    @Test
    public void oneRowPerCategoryWithAllOffendingAuthors() {
        classify("Trimble A.C., Frost, D., and Cooper M.S.");
        List<List<String>> rows = cleanup.get(Category.NO_COMMA);
        assertEquals(1, rows.size());
        List<String> row = rows.get(0);
        assertEquals(PublicationAuthorFormatReportTask.CLEANUP_HEADER.size(), row.size());
        assertEquals("No comma after surname", row.get(0));
        assertEquals("ZDB-PUB-990510-2", row.get(1));
        assertEquals("Trimble A.C.; Cooper M.S.", row.get(6));
        assertEquals("Trimble, A.C.; Cooper, M.S.", row.get(7));
        assertTrue(missing.isEmpty());
    }

    @Test
    public void blankAndPlaceholderGoToMissingSheetOnly() {
        classify("");
        classify("Editors");
        assertEquals(2, missing.size());
        assertEquals("Blank", missing.get(0).get(0));
        assertEquals("Placeholder", missing.get(1).get(0));
        assertEquals(PublicationAuthorFormatReportTask.MISSING_HEADER.size(), missing.get(1).size());
        cleanup.values().forEach(rows -> assertTrue(rows.isEmpty()));
    }

    @Test
    public void cleanAuthorsProduceNoRows() {
        classify("Westerfield, M. and Eisen, J.S.");
        assertTrue(missing.isEmpty());
        cleanup.values().forEach(rows -> assertTrue(rows.isEmpty()));
    }
}
