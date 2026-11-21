package org.zfin.marker.agr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Splits a free-text ZFIN publication authors string, e.g.
 * "Krylov, V.V., Osipova, E.A., and Batrakova, A.A.", into individual author names.
 * Used for publications that have no per-author (pubmed_publication_author) rows.
 * <p>
 * When the string cannot be split safely (placeholders, organizations, full names without
 * initials, strings with ';' or parentheses) the original string is returned as the only element.
 */
public final class AuthorStringParser {

    private static final Pattern PLACEHOLDER = Pattern.compile("(?i)^\\W*no authors listed\\W*$");
    private static final Pattern TRAILING_JUNK = Pattern.compile("[,\\s]+$");
    private static final Pattern TRAILING_EDS = Pattern.compile("(?i)[,\\s]\\s*(?:and\\s+)?eds?\\.?$");
    private static final Pattern TRAILING_ET_AL = Pattern.compile("(?i)[,\\s]\\s*et\\.? al\\.?$");
    // "Whitlock., K.E." -> "Whitlock, K.E." (stray period after a surname, before initials)
    private static final Pattern STRAY_PERIOD = Pattern.compile("(\\p{L}{3,})\\.,(?=\\s+(?:\\p{Lu}\\.){1,3}(?:\\s|,|$))");
    // Boundary between two authors: a comma (optionally followed by "and") or a bare " and",
    // directly after a final initial ("M.") or a name suffix ("III", "Jr").
    // Tolerates sloppy spacing around the comma ("Imagawa, T.,Kondo", "Hammond, C. L. , Huitema").
    private static final Pattern BOUNDARY = Pattern.compile(
        "(?<=\\.)\\s*,\\s*(?:and\\s+)?|(?<=\\.|\\bJr|\\bSr|\\bII|\\bIII|\\bIV)(?:,\\s+(?:and\\s+)?|\\s+and\\s+)");
    // "Gomez Jr., G.A.", "Carvan III, M.J." and "Easter, Jr., S.S." are one author each, not suffix + initials.
    private static final Pattern SUFFIXED_SURNAME = Pattern.compile("^[^,]+(?:,\\s*|\\s+)(?:Jr|Sr|II|III|IV)\\.?$");
    private static final Pattern SUFFIX_ONLY = Pattern.compile("^(?:Jr|Sr|II|III|IV)\\.?$");
    private static final Pattern INITIALS_ONLY = Pattern.compile("^(?:\\p{Lu}\\.[\\s-]?)+$");

    private AuthorStringParser() {
    }

    public static List<String> parse(String authors) {
        if (authors == null || authors.isBlank() || PLACEHOLDER.matcher(authors).matches()) {
            return Collections.singletonList(authors);
        }
        if (authors.indexOf(';') >= 0 || authors.indexOf('(') >= 0 || authors.indexOf(')') >= 0) {
            return Collections.singletonList(authors);
        }
        String text = authors.replaceAll("\\s+", " ").trim();
        text = TRAILING_JUNK.matcher(text).replaceFirst("");
        text = TRAILING_EDS.matcher(text).replaceFirst("");
        text = TRAILING_ET_AL.matcher(text).replaceFirst("");
        text = TRAILING_JUNK.matcher(text).replaceFirst("");
        // No initials/periods: organizations ("ZFIN and ZIRC Staff") or full names ("Wang, Wanhe").
        if (text.indexOf('.') < 0) {
            return Collections.singletonList(authors);
        }
        text = STRAY_PERIOD.matcher(text).replaceAll("$1,");

        List<String> result = new ArrayList<>();
        for (String part : BOUNDARY.split(text)) {
            String name = TRAILING_JUNK.matcher(part.trim()).replaceFirst("");
            if (name.isEmpty()) {
                continue;
            }
            int last = result.size() - 1;
            if (last >= 0 && ((SUFFIXED_SURNAME.matcher(result.get(last)).matches()
                && INITIALS_ONLY.matcher(name).matches()) || SUFFIX_ONLY.matcher(name).matches())) {
                result.set(last, result.get(last) + ", " + name);
            } else {
                result.add(name);
            }
        }
        return result.isEmpty() ? Collections.singletonList(authors) : result;
    }
}
