package org.zfin.infrastructure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
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
import org.zfin.infrastructure.service.UpdatesService;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/updates")
public class UpdatesApiController {

    @Autowired
    UpdatesService updatesService;


    @RequestMapping("/{zdbID}")
    @JsonView(View.API.class)
    public JsonResultResponse<UpdatesDTO> viewUpdates(@PathVariable String zdbID,
                                                       @RequestParam(value = "filter.fieldName", required = false) String filterFieldName,
                                                       @Version Pagination pagination,
                                                       HttpServletRequest request) {
        List<UpdatesDTO> updatesDTO = updatesService.getUpdatesDTOS(zdbID, filterFieldName);
        PaginationResult<UpdatesDTO> pagedResult = PaginationResultFactory.createPaginationResultFromList(updatesDTO, pagination);

        JsonResultResponse<UpdatesDTO> response = new JsonResultResponse<>();
        response.setResults(pagedResult.getPopulatedResults());
        response.setHttpServletRequest(request);
        response.setTotal(updatesDTO.size());

        return response;
    }
}
