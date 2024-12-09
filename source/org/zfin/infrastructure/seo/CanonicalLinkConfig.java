package org.zfin.infrastructure.seo;

import org.springframework.ui.Model;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.zfin.infrastructure.service.RequestService.getRequestedUrlPath;

public class CanonicalLinkConfig {

    /**
     * URL patterns and their replacement formats
     *
     *    based on contents of view-page-url-map.txt:
     *     ALT	/action/feature/view/ZDB-ALT
     *     ANAT	/action/ontology/term/ZDB-ANAT
     *     ATB	/action/antibody/view/ZDB-ATB
     *     BAC	/action/marker/clone/view/ZDB-BAC
     *     ...
     *
     * @return Map of URL patterns and their replacement formats
     */
    public static Map<Pattern, String> urlPatterns() {
        Map<Pattern, String> patterns = new HashMap<>();
        String zdbIDPattern = "ZDB-[A-Z_]+-\\d{6}-\\d+";
        List<String> zdbURLs = List.of(
    "/action/antibody/view/",
//                "/action/data/alias/", //not needed since it is a redirect
                "/action/feature/view/",
                "/action/figure/view/",
                "/action/fish/",
                "/action/genotype/view/",
                "/action/image/view/",
                "/action/mapping/linkage/",
                "/action/mapping/panel-detail/",
                "/action/marker/clone/view/",
                "/action/marker/construct/view/",
                "/action/marker/efg/view/",
                "/action/marker/eregion/view/",
                "/action/marker/gene/view/",
                "/action/marker/generic/view/",
                "/action/marker/pseudogene/view/",
                "/action/marker/region/view/",
                "/action/marker/snp/view/",
                "/action/marker/str/view/",
                "/action/marker/transcript/view/",
                "/action/nomenclature/view/", //eg. ZDB-NOMEN-230912-3
                "/action/ontology/term/",
                "/action/profile/view/",
                "/action/publication/",
                "/action/publication/journal/"
        );
        zdbURLs.forEach(url -> patterns.put(Pattern.compile(url + "(" + zdbIDPattern + ")$"), "/$1"));
        patterns.put(Pattern.compile("/action/publication/journal/(" + zdbIDPattern + ")$"), "/$1");

        String termIDPattern = "[^/]+"; //every character other than a forward slash
        List<String> termPrefixes = List.of("BSPO", "CHEBI", "DOID", "ECO", "GO", "MMO", "MPATH", "NBO", "NBO", "NCBITaxon", "OBI", "PATO", "RO", "SO", "ZECO", "ZFA", "ZFS");
        termPrefixes.forEach(prefix ->
                patterns.put(Pattern.compile("/action/ontology/term/(" + prefix + termIDPattern + ")$"), "/$1"));

        return patterns;
    }

    public static String match(String url) {
        Map<Pattern, String> urlPatterns = urlPatterns();
        for (Map.Entry<Pattern, String> entry : urlPatterns.entrySet()) {
            if (entry.getKey().matcher(url).matches()) {
                String path = entry.getKey().matcher(url).replaceAll(entry.getValue());
                return "https://" + ZfinPropertiesEnum.DOMAIN_NAME + path;
            }
        }
        return null;
    }


    public static void addCanonicalIfFound(Model model) {
        String canonicalUrl = match(getRequestedUrlPath());
        if (canonicalUrl != null) {
            model.addAttribute("canonicalUrl", canonicalUrl);
        }
    }

    public static String getCanonicalIfFound(String url) {
        String canonicalUrl = match(url);
        if (canonicalUrl != null) {
            return canonicalUrl;
        }
        return "";
    }
}
