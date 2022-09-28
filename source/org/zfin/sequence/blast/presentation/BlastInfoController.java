package org.zfin.sequence.blast.presentation;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.api.RestErrorException;
import org.zfin.framework.api.RestErrorMessage;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.DatabaseStatistics;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.sequence.blast.repository.BlastRepository;

@Controller
@RequestMapping("/api/blast")
@Log4j2
public class BlastInfoController {

    private final BlastRepository blastRepository = RepositoryFactory.getBlastRepository();

    @RequestMapping(value = "/info/{database}", method = RequestMethod.GET)
    public String getBlastDatabaseInfo(Model model, @PathVariable String database) {
        DatabaseStatistics statistics;
        try {
            Database.AvailableAbbrev type = Database.AvailableAbbrev.getType(database);
            Database blastDatabase = blastRepository.getDatabase(type);
            if (CollectionUtils.isEmpty(blastDatabase.getParentRelationships())) {
                statistics = MountedWublastBlastService.getInstance().getDatabaseStatistics(blastDatabase);
                statistics.setDatabase(blastDatabase);
            } else {
                statistics = null;
            }
        } catch (RuntimeException e) {
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        } catch (Exception e) {
            log.warn("Error while retrieving blast database info", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage("Could not retrieve Blast Database info for: " + database);
            throw new RestErrorException(error);
        }
        model.addAttribute("statistics", statistics);
        return "blast/blast-database-info";
    }

}