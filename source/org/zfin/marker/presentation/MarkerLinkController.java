package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.sequence.DisplayGroup;

import java.util.List;

@Controller
@RequestMapping("/marker")
public class MarkerLinkController {

    @Autowired
    private MarkerRepository markerRepository;

    @ResponseBody
    @RequestMapping("/{markerId}/links")
    public List<LinkDisplay> getMarkerLinks(@PathVariable String markerId,
                                            @RequestParam(name = "group", required = true) String groupName) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        DisplayGroup.GroupName group = DisplayGroup.GroupName.getGroup(groupName);

        return markerRepository.getMarkerDBLinksFast(marker, group);
    }

}
