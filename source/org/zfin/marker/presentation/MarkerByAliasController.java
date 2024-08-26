package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@RestController
@RequestMapping("/api")
@Log4j2
public class MarkerByAliasController {

    @Autowired
    private MarkerService markerService;

    /**
     * Given an alias for a gene (which may be the official abbreviation), this returns the gene abbreviation matches as a json object
     * For example, https://zfin.org/action/api/marker/from-alias/yap will return the string {"ZDB-GENE-030131-9710":"yap1"}
     * No match will return an empty object
     * Multiple matches will return multiple key-value pairs (like this example of cyr61):
     * {
     *   "ZDB-GENE-060404-5": "ccn1l2",
     *   "ZDB-GENE-040426-3": "ccn1"
     * }
     * @param alias
     * @return the gene abbreviation as a string
     */
    @JsonView(View.API.class)
    @RequestMapping("/marker/from-alias/{alias}")
    public Map<String, String> getMarkerAbbreviationByAlias(@PathVariable String alias) {
        List<DataAlias> aliases = getInfrastructureRepository().getDataAliases(alias);
        List<String> ids = aliases.stream().map(da -> da.getDataZdbID()).toList();
        Map<String, String> markerIdAndAbbreviation = new HashMap<>();
        for(String id : ids) {
            Marker marker = getMarkerRepository().getMarkerByID(id);
            markerIdAndAbbreviation.put(
                    marker != null ? marker.getZdbID() : id,
                    marker != null ? marker.getAbbreviation() : "");
        }

        return markerIdAndAbbreviation;
    }

    /**
     * Given a list of aliases for genes (which may be the official abbreviations), this returns a map of the aliases to the gene abbreviations as strings.
     * No match will return an empty string.
     * <p>
     * For example, https://zfin.org/action/api/marker/from-aliases with a POST request with the body ["yap", "yap1"] will return the map {"yap": "yap1", "yap1": "yap1"}
     * Example 2: curl -s -k -X POST -H "Content-Type: application/json" -d '["noi", "yap", "yap1", "pig", "cyr61"]' 'https://zfin.org/action/api/marker/from-aliases' |jq .
     * {
     *   "yap1": {},
     *   "noi": {
     *     "ZDB-GENE-990415-8": "pax2a"
     *   },
     *   "yap": {
     *     "ZDB-GENE-030131-9710": "yap1"
     *   },
     *   "cyr61": {
     *     "ZDB-GENE-060404-5": "ccn1l2",
     *     "ZDB-GENE-040426-3": "ccn1"
     *   },
     *   "pig": {
     *     "ZDB-TERM-180403-2869": ""
     *   }
     * }
     *
     * @param aliases
     * @return a map of the aliases to the gene abbreviations as strings
     */
    @JsonView(View.API.class)
    @RequestMapping(value = "/marker/from-aliases", method = RequestMethod.POST)
    public Map<String, Map<String, String>> getMarkerByAliases(@RequestBody List<String> aliases) {
        Map<String, Map<String, String>> resultsMap = new HashMap<>();
        for (String alias : aliases) {
            resultsMap.put(alias, getMarkerAbbreviationByAlias(alias));
        }
        return resultsMap;
    }


}
