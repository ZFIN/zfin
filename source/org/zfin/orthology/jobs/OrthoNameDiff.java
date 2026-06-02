package org.zfin.orthology.jobs;

import com.github.difflib.text.DiffRowGenerator;

import java.util.List;

/**
 * Inline character-level diff between two strings, with insertions/deletions
 * wrapped in {@code <u>...</u>} for rendering inside an "html" Report cell.
 *
 * <p>Backed by java-diff-utils' {@link DiffRowGenerator}; we keep a single
 * configured instance because reconfiguration is non-trivial and the generator
 * is documented as thread-safe-for-reuse when used in inline mode.
 *
 * <p>Input is HTML-escaped first so any raw {@code <} in the source can't break
 * out of the cell. The {@code <u>...</u>} markup we add afterwards is the only
 * markup that reaches the output.
 *
 * <p>Why {@code <u>}: Excel's HTML importer honours per-character underline,
 * but not per-character background colour (Excel cell background is whole-cell
 * only). The report template adds {@code .data-table u { background-color:
 * yellow }} so the browser also shows the diffs as yellow-highlighted; the
 * underline is the fallback for Excel export.
 */
final class OrthoNameDiff {

    /**
     * Treat each character as a token so single-character changes (a hyphen, a
     * comma) show up. {@code oldTag}/{@code newTag} receive {@code true} for
     * the opening side of a marked span and {@code false} for the close —
     * forgetting that produces orphaned opening tags that nest forever.
     */
    private static final DiffRowGenerator GENERATOR = DiffRowGenerator.create()
        .showInlineDiffs(true)
        .inlineDiffByWord(false)
        .mergeOriginalRevised(false)
        .oldTag(open -> open ? "<u>" : "</u>")
        .newTag(open -> open ? "<u>" : "</u>")
        .build();

    private OrthoNameDiff() {}

    /** Returns the "old" side of the diff with deletions marked. */
    static String highlightOld(String oldText, String newText) {
        return GENERATOR.generateDiffRows(List.of(escape(oldText)), List.of(escape(newText)))
            .get(0).getOldLine();
    }

    /** Returns the "new" side of the diff with insertions marked. */
    static String highlightNew(String oldText, String newText) {
        return GENERATOR.generateDiffRows(List.of(escape(oldText)), List.of(escape(newText)))
            .get(0).getNewLine();
    }

    /** Minimal HTML escape — the diff library doesn't escape for us. */
    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default  -> out.append(c);
            }
        }
        return out.toString();
    }
}
