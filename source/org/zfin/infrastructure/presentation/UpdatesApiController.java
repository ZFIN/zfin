package org.zfin.infrastructure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.UpdatesDTO;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.wiki.presentation.Version;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/updates")
public class UpdatesApiController {

    @Autowired
    InfrastructureRepository infrastructureRepository;

    @Autowired
    PublicationRepository publicationRepository;


    @RequestMapping("/{zdbID}")
    @JsonView(View.API.class)
    public JsonResultResponse<UpdatesDTO> viewUpdates(@PathVariable String zdbID,
                                                       @RequestParam(value = "filter.fieldName", required = false) String filterFieldName,
                                                       @Version Pagination pagination,
                                                       HttpServletRequest request) {
        List<UpdatesDTO> updatesDTO = UpdatesDTO.fromUpdates(infrastructureRepository.getUpdates(zdbID));

        //is this a publication? If so, add pub tracking events
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication != null) {
            List<PublicationTrackingHistory> events = publicationRepository.fullTrackingHistory(publication);
            List<UpdatesDTO> publicationUpdates = UpdatesDTO.fromPublicationEvents(events);
            updatesDTO.addAll(publicationUpdates);
        }
        if (StringUtils.isNotEmpty(filterFieldName)) {
            updatesDTO.removeIf(updatesDTO1 -> !updatesDTO1.fieldName().toLowerCase().contains(filterFieldName.toLowerCase()));
        }

        updatesDTO.sort(Comparator.comparing(UpdatesDTO::whenUpdated, Comparator.nullsLast(Comparator.reverseOrder())));
        PaginationResult<UpdatesDTO> pagedResult = PaginationResultFactory.createPaginationResultFromList(updatesDTO, pagination);

        JsonResultResponse<UpdatesDTO> response = new JsonResultResponse<>();
        response.setResults(pagedResult.getPopulatedResults());
        response.setHttpServletRequest(request);
        response.setTotal(updatesDTO.size());

        return response;
    }
}
