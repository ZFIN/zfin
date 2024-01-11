package org.zfin.sequence.blast.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.Database;

@Controller
public class BlastDatabaseSelectController {

    @Autowired
    private ProfileService profileService;

    @RequestMapping("/blast/blast-select")
    protected String downloadSequence(@ModelAttribute("formBean") BlastInfoBean blastInfoBean) throws Exception {

        boolean isRoot = profileService.isCurrentSecurityUserRoot();
        blastInfoBean.setNucleotideDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.NUCLEOTIDE, !isRoot, true));
        blastInfoBean.setProteinDatabasesFromRoot(RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN, !isRoot, true));
        return "blast/blast_database_select";
    }
}