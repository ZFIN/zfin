package org.zfin.construct.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.marker.repository.MarkerRepository;

import java.util.List;

@Controller
@RequestMapping("/construct")
public class ConstructAddController {

    @Autowired
    private MarkerRepository mr;

    //method to find markers for autocomplete in construct builder
    @RequestMapping(value = "/find-constructMarkers", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LookupEntry> lookupConstructMarkers(@RequestParam("term") String lookupString, @RequestParam("pub") String zdbId) {
        return mr.getConstructComponentsForString(lookupString, zdbId);
    }
}
