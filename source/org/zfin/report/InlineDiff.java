package org.zfin.report;

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
 * <p>Input is fed in raw — callers are responsible for confirming their data
 * has no {@code <}, {@code >}, or {@code &} that would be misinterpreted as
 * markup once rendered, since the only escaping this class does is adding the
 * {@code <u>...</u>} pair itself. (Originally written for ortholog gene names,
 * which are verified clean; DNA sequence data is similarly clean by
 * construction.) If a caller's data can contain HTML metacharacters, escape it
 * before calling in.
 *
 * <p>Why {@code <u>}: Excel's HTML importer honours per-character underline,
 * but not per-character background colour (Excel cell background is whole-cell
 * only). The report template adds {@code .data-table u { background-color:
 * yellow }} so the browser also shows the diffs as yellow-highlighted; the
 * underline is the fallback for Excel export.
 */
public final class InlineDiff {

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

    private InlineDiff() {}

    /**
     * Index of the first position where the two strings differ, or -1 if they're equal.
     * Useful for windowing a long value down to the interesting part before diffing it for
     * display -- a character-level diff of two long, wholly-different strings marks up almost
     * every character, which is both unreadable and very large.
     */
    public static int firstDifference(String a, String b) {
        String left = nullToEmpty(a);
        String right = nullToEmpty(b);
        int shared = Math.min(left.length(), right.length());
        for (int i = 0; i < shared; i++) {
            if (left.charAt(i) != right.charAt(i)) {
                return i;
            }
        }
        return left.length() == right.length() ? -1 : shared;
    }

    /** Returns the "old" side of the diff with deletions marked. */
    public static String highlightOld(String oldText, String newText) {
        return GENERATOR.generateDiffRows(List.of(nullToEmpty(oldText)), List.of(nullToEmpty(newText)))
            .get(0).getOldLine();
    }

    /** Returns the "new" side of the diff with insertions marked. */
    public static String highlightNew(String oldText, String newText) {
        return GENERATOR.generateDiffRows(List.of(nullToEmpty(oldText)), List.of(nullToEmpty(newText)))
            .get(0).getNewLine();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
