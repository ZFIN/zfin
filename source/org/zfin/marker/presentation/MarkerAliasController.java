package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.repository.MarkerRepository;

import java.util.ArrayList;
import java.util.Collection;

@Controller
@RequestMapping("/marker")
public class MarkerAliasController {

    @Autowired
    MarkerRepository markerRepository;

    @ResponseBody
    @RequestMapping("/{markerID}/aliases")
    public Collection<MarkerAliasBean> getMarkerAliases(@PathVariable String markerID) {
        Marker marker = markerRepository.getMarkerByID(markerID);
        Collection<MarkerAliasBean> beans = new ArrayList<>();
        for (MarkerAlias markerAlias : marker.getAliases()) {
            MarkerAliasBean bean = new MarkerAliasBean();
            bean.setAlias(markerAlias.getAlias());
            Collection<String> references = new ArrayList<>();
            for (PublicationAttribution reference : markerAlias.getPublications()) {
                references.add(reference.getSourceZdbID());
            }
            bean.setReferences(references);
            beans.add(bean);
        }
        return beans;
    }

}
