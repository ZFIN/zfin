package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.server.MarkerRPCServiceImpl;
import org.zfin.gwt.root.ui.BlastDatabaseAccessException;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Iterator;

@RestController
@RequestMapping("/api")
@Log4j2
public class MarkerSequenceAPIController {


    /**
     * Add new nucleotide sequence to markerId by posting sequence data of the form:
     *
     * {
     *   "data": "TTACAATTAAAGGATATTTCTTGCGGCTGAATACGAGAACAGAAATGTCCCTTAATTGTTTGGTT",
     *   "referenceDatabaseZdbID": "ZDB-FDBCONT-090929-4",
     *   "references": [
     *     {
     *       "zdbID": "ZDB-PUB-140520-12"
     *     }
     *   ]
     * }
     *
     * eg. curl -X POST --header 'Content-Type: application/json' --data '{"data": "TTACAATTAAAGGATATTTCTTGCGGCTGAATACGAGAACAGAAATGTCCCTTAATTGTTTGGTT", "referenceDatabaseZdbID": "ZDB-FDBCONT-090929-4", "references": [{"zdbID": "ZDB-PUB-140520-12"}]}' https://{SITE}.zfin.org/action/api/marker/ZDB-GENE-980526-399/nucleotide-sequence
     *
     * @param markerId The marker ID to add the sequence to.
     * @param sequenceFormBean The sequence data to add (deserialized from json as described above).
     * @param errors The binding result to check for errors.
     * @return The DBLink for the created sequence. Serialized to json like: {"name":"ZFINNUCL0000006195"}
     */
    @JsonView(View.API.class)
    @RequestMapping(value = "/marker/{markerId}/nucleotide-sequence", method = RequestMethod.POST)
    public DBLinkDTO addInternalNucleotideSequence(@PathVariable String markerId,
                                                   @Valid @RequestBody SequenceFormBean sequenceFormBean,
                                                   BindingResult errors) throws InvalidWebRequestException, BlastDatabaseAccessException {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid Sequence Data", errors);
        }

        Collection<Publication> references = sequenceFormBean.getReferences();
        if (references.size() == 0) {
            throw new InvalidWebRequestException("Error: must include a publication reference");
        }

        Iterator<Publication> referencesIterator = references.iterator();
        Publication reference = referencesIterator.next();

        try {
            MarkerRPCServiceImpl markerRPCService = new MarkerRPCServiceImpl();
            DBLinkDTO result = markerRPCService.addInternalNucleotideSequence(markerId,
                    sequenceFormBean.getData(),
                    reference.getZdbID(),
                    sequenceFormBean.getReferenceDatabaseZdbID()
            );

            //add more references if there are more than one
            if (referencesIterator.hasNext()) {
                log.debug("Adding multiple references for internal nucleotide sequence");
                HibernateUtil.createTransaction();
                while (referencesIterator.hasNext()) {
                    RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(result.getZdbID(),
                            referencesIterator.next().getZdbID());
                }
                HibernateUtil.flushAndCommitCurrentSession();
            }

            return result;
        } catch (BlastDatabaseAccessException bdae) {
            log.error("Caught exception adding blast sequence for " + markerId, bdae);
            throw bdae;
        }
    }

}
