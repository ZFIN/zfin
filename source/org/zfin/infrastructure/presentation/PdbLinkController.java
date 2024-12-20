package org.zfin.infrastructure.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.ProteinToPDB;

import java.util.List;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Controller
@RequestMapping("/infrastructure")
@Log4j2
public class PdbLinkController {

    public static final String PDB_URL = "https://rcsb.org/structure/";

    @RequestMapping(value = "pdb-link-list/{uniprotID}")
    public String getCitationList(Model model, @PathVariable String uniprotID) {
        List<ProteinToPDB> pdbs = getMarkerRepository().getPDB(uniprotID);
        if (pdbs == null || pdbs.isEmpty()) {
            //404
            model.addAttribute(LookupStrings.ZDB_ID, uniprotID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        if (pdbs.size() == 1) {
            return "redirect:" + PDB_URL + pdbs.get(0).getPdbID();
        }
        model.addAttribute("pdbCount", pdbs.size());
        model.addAttribute("pdbList", pdbs.stream().map(ProteinToPDB::getPdbID)
                        .collect(Collectors.toMap(pdbID -> pdbID, pdbID -> PDB_URL + pdbID)));
        model.addAttribute("uniprotID", uniprotID);
        return "infrastructure/pdb-link-list";
    }
}


