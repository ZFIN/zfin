package org.zfin.mutant.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.ontology.GenericTerm;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Controller
public class MutantListController {
    private static final Logger LOG = Logger.getLogger(MutantListController.class);
    private PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private MarkerRepository mrkrRepository = RepositoryFactory.getMarkerRepository();
    private PhenotypeRepository phenoRepository = RepositoryFactory.getPhenotypeRepository();

    @RequestMapping(value={"/mutant-list"})
    protected String getMutantList(@RequestParam String zdbID, Model model) {
        LOG.debug("Start MutantListController");

        if (zdbID.startsWith("ZDB-PUB-")) {
          Publication pub = pubRepository.getPublication(zdbID);

          if (pub == null) {
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if(replacedZdbID!=null){
                LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                pub = pubRepository.getPublication(replacedZdbID);
            }

          }

          if (pub == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
            return "record-not-found.page";
          }

          MutantListBean form = new MutantListBean();
          form.setPublication(pub);

          retrieveMutantListByPub(form, zdbID);

          model.addAttribute(LookupStrings.FORM_BEAN, form);
          model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Mutant and Transgenic Line List");

          return "mutant/mutant-list.page";
        }  else if (zdbID.startsWith("ZDB-GENE-")) {
            Marker gene = mrkrRepository.getMarkerByID(zdbID);

            if (gene == null) {
              String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
              if(replacedZdbID!=null){
                  LOG.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                  gene = mrkrRepository.getMarkerByID(replacedZdbID);
              }

            }

            if (gene == null) {
              model.addAttribute(LookupStrings.ZDB_ID, zdbID) ;
              return "record-not-found.page";
            }

            MutantListBean form = new MutantListBean();
            form.setGene(gene);

            retrieveMutantListByGene(form, zdbID);

            model.addAttribute(LookupStrings.FORM_BEAN, form);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Mutant and Transgenic Line List for" + gene.getAbbreviation());

            return "mutant/mutant-list.page";

        }  else {
              return "record-not-found.page";
        }

    }

    private void retrieveMutantListByPub(MutantListBean form, String pubId) {
        List<Genotype> mutantsAndTgs = pubRepository.getMutantsAndTgsByPublication(pubId);
        form.setMutants(mutantsAndTgs);
    }

    private void retrieveMutantListByGene(MutantListBean form, String geneId) {
        List<Genotype> mutantsAndTgs = mrkrRepository.getMutantsAndTgsByGene(geneId);
        for (Genotype geno : mutantsAndTgs) {
            List<Figure> figs = phenoRepository.getPhenoFiguresByGenotype(geno.getZdbID()) ;
            Set<Figure> phenoFigs = new HashSet<Figure> () ;
            phenoFigs.addAll(figs) ;
            geno.setPhenotypeFigures(phenoFigs);
            if(phenoFigs.size() == 1) {
              for (Figure figure : phenoFigs) {
                  geno.setPhenotypeSingleFigure(figure);
              }
            }
        }
        Collections.sort(mutantsAndTgs);
        form.setMutants(mutantsAndTgs);
    }
}