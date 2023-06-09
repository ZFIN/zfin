package org.zfin.nomenclature.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistoryDTO;
import org.zfin.marker.MarkerHistoryReasonsDTO;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

@RestController
@RequestMapping("/api/nomenclature")
@Log4j2
public class NomenclatureAPIController {

    @RequestMapping(value = "/history/{zdbID}")
    @JsonView(View.API.class)
    public List<MarkerHistoryDTO> getHistoryPrototypeViewJson(@PathVariable("zdbID") String zdbID) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        return marker.getMarkerHistory().stream().map(MarkerHistoryDTO::fromMarkerHistory).toList();
    }

    @RequestMapping(value = "/reasons")
    @JsonView(View.API.class)
    public MarkerHistoryReasonsDTO getHistoryPrototypeReasons() {
        return MarkerHistoryReasonsDTO.allReasons();
    }
}
