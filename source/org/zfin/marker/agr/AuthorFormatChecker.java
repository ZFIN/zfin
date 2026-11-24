package org.zfin.marker.agr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks a free-text ZFIN publication authors string (publication.authors) for entries a curator should fix by hand.
 * The string is split with {@link AuthorStringParser}, so "and" and ", and" are never reported: they separate
 * authors correctly. What is reported are individual names that are not written "Last, I." and lists the parser
 * could not split.
 * <p>
 * Placeholder or blank authors are a separate, policy-pending issue and are reported by
 * {@link #isBlank(String)} / {@link #isPlaceholder(String)}, not by {@link #check(String)}.
 */
public final class AuthorFormatChecker {

    public enum Category {
        INITIALS_FIRST("Initials before surname"),
        NO_COMMA("No comma after surname"),
        PERIOD_FOR_COMMA("Period instead of comma after surname"),
        SEVERAL_IN_ONE("Several authors in one entry"),
        UNSPLITTABLE("Author list can't be split"),
        NOT_SPLIT("Not split: contains ( ) or ;");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * One problem found in one author entry.
     *
     * @param author     the offending entry as written (or the whole field for list-level problems)
     * @param suggestion the corrected form where it can be derived mechanically, otherwise empty
     * @param message    plain-language instruction for the curator
     */
    public record Finding(Category category, String author, String suggestion, String message) {
    }

    private static final String SURNAME_CHARS = "[\\p{L}'’\\-]";
    private static final String INITIALS = "(?:\\p{Lu}\\.[\\s-]?)+";
    // "Aleström. P.", "Yu.S.F.", "Waterman.R.E.." (stray period instead of a comma; extra trailing periods are junk)
    private static final Pattern PERIOD_FOR_COMMA = Pattern.compile(
        "^(\\p{L}{2}" + SURNAME_CHARS + "*(?:\\s+\\p{L}" + SURNAME_CHARS + "+)*)\\.\\s*(" + INITIALS + ")\\.*$");
    // "L.S. Ross", "F.-Y. Deng", "R. Bruggemann"
    private static final Pattern INITIALS_FIRST = Pattern.compile(
        "^(" + INITIALS + ")\\s*(\\p{L}[\\p{L}'’\\- ]*)$");
    // "Butler C.A.", "Zhang J.", "Mishoe Hernandez L.", "Yan Y.-L."
    private static final Pattern NO_COMMA = Pattern.compile(
        "^(\\p{L}" + SURNAME_CHARS + "*(?:\\s+\\p{L}" + SURNAME_CHARS + "+)*?)\\s+((?:\\p{Lu}\\.-?)+)$");
    // "Easter, Jr., S.S.", "Campbell, W.A., IV", "Gomez Jr., G.A." are single, valid authors.
    private static final Pattern SUFFIXED = Pattern.compile(
        "^[^,]+,\\s*(?:Jr|Sr|II|III|IV)\\.?,?\\s*(?:\\p{Lu}\\.?[\\s-]?)*$"
            + "|^[^,]+,\\s*(?:\\p{Lu}\\.[\\s-]?)+,\\s*(?:Jr|Sr|II|III|IV)\\.?$");
    private static final Pattern PLACEHOLDER = Pattern.compile(
        "(?i)^\\W*(none|no authors?( listed)?|editors?|n/?a|unknown|anonymous|\\p{L})\\W*$");

    private AuthorFormatChecker() {
    }

    public static boolean isBlank(String authors) {
        return authors == null || authors.isBlank();
    }

    public static boolean isPlaceholder(String authors) {
        return !isBlank(authors) && PLACEHOLDER.matcher(authors.trim()).matches();
    }

    public static List<Finding> check(String authors) {
        List<Finding> findings = new ArrayList<>();
        if (isBlank(authors) || isPlaceholder(authors)) {
            return findings;
        }
        List<String> parts = AuthorStringParser.parse(authors);
        boolean unsplit = parts.size() == 1;
        if (unsplit && (authors.indexOf(';') >= 0 || authors.indexOf('(') >= 0 || authors.indexOf(')') >= 0)) {
            findings.add(new Finding(Category.NOT_SPLIT, authors.trim(), "",
                "The authors field contains \"(\" or \";\" so it was not split into individual authors. "
                    + "Remove the annotation (for example \"(ed.)\") if it is not part of a name."));
            return findings;
        }
        if (unsplit && authors.indexOf('.') < 0) {
            String text = authors.trim();
            long commas = text.chars().filter(c -> c == ',').count();
            if (commas == 1 && text.endsWith(",")) {
                findings.add(new Finding(Category.UNSPLITTABLE, text, "",
                    "The authors field ends with a comma and looks incomplete."));
            } else if (commas >= 2) {
                findings.add(new Finding(Category.UNSPLITTABLE, text, "",
                    "The authors field has no initials, so the names could not be separated. "
                        + "Rewrite as \"Last, F., Last, F.\" (or enter one author per name)."));
            }
            return findings;
        }
        for (String part : parts) {
            checkEntry(part.trim(), findings);
        }
        return findings;
    }

    private static void checkEntry(String entry, List<Finding> findings) {
        if (SUFFIXED.matcher(entry).matches()) {
            return;
        }
        long commas = entry.chars().filter(c -> c == ',').count();
        if (commas >= 2 || (commas >= 1 && entry.matches(".*\\band\\b.*")) || entry.length() > 45) {
            findings.add(new Finding(Category.SEVERAL_IN_ONE, entry, "",
                "\"" + entry + "\" looks like more than one author. Check for a missing period after an initial, "
                    + "a missing comma between authors, or a doubled comma."));
            return;
        }
        if (commas == 1) {
            return; // "Last, I." form
        }
        Matcher m = PERIOD_FOR_COMMA.matcher(entry);
        if (m.matches()) {
            String suggestion = m.group(1) + ", " + m.group(2).trim();
            findings.add(new Finding(Category.PERIOD_FOR_COMMA, entry, suggestion,
                "\"" + entry + "\" has a period where a comma should follow the surname. Write it as \""
                    + suggestion + "\"."));
            return;
        }
        m = INITIALS_FIRST.matcher(entry);
        if (m.matches()) {
            String suggestion = m.group(2).trim() + ", " + m.group(1).trim();
            findings.add(new Finding(Category.INITIALS_FIRST, entry, suggestion,
                "\"" + entry + "\" has the initials before the surname. Write it surname first with a comma, as \""
                    + suggestion + "\"."));
            return;
        }
        m = NO_COMMA.matcher(entry);
        if (m.matches()) {
            String suggestion = m.group(1) + ", " + m.group(2);
            findings.add(new Finding(Category.NO_COMMA, entry, suggestion,
                "\"" + entry + "\" has no comma after the surname. Write it as \"" + suggestion + "\"."));
        }
    }
}
