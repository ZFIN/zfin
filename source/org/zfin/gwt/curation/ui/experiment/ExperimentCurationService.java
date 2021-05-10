package org.zfin.gwt.curation.ui.experiment;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.event.ZfinDispatcher;
import org.zfin.gwt.root.ui.ValidationException;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExperimentCurationService extends DirectRestService {


    @GET
    @Path("/action/curation/{publicationID}/experiment-list")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> getExperiments(@PathParam("publicationID") String publicationID) throws ValidationException;

    @GET
    @Path("/action/curation/zeco-child-map")
    @Options(dispatcher = ZfinDispatcher.class)
    Map<String, Set<String>> getZecoChildMap();

    @POST
    @Path("/action/curation/{publicationID}/create-condition")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> createCondition(@PathParam("publicationID") String publicationID, ConditionDTO dto)
            throws ValidationException, TermNotFoundException;


    @POST
    @Path("/action/curation/{experimentID}/copy-condition")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> copyConditions(@PathParam("experimentID") String experimentID, List<String> copyConditionIdList)
            throws ValidationException, TermNotFoundException;

    @POST
    @Path("/action/curation/{publicationID}/create-experiment")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> createExperiment(@PathParam("publicationID") String publicationID,
                                         ExperimentDTO environmentDTO)
            throws ValidationException, TermNotFoundException;

    @POST
    @Path("/action/curation/{publicationID}/update-experiment")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> updateExperiment(@PathParam("publicationID") String publicationID,
                                         ExperimentDTO experimentDTO)
            throws ValidationException, TermNotFoundException;

    @DELETE
    @Path("/action/curation/{publicationID}/delete-experiment")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> deleteExperiment(@PathParam("publicationID") String publicationID,
                                 ExperimentDTO experimentDTO)
            throws ValidationException, TermNotFoundException;

    @DELETE
    @Path("/action/curation/{publicationID}/delete-condition")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> deleteCondition(@PathParam("publicationID") String publicationID,
                                 ConditionDTO conditionDTO)
            throws ValidationException, TermNotFoundException;


}
