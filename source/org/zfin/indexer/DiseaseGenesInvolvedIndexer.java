package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.service.OntologyService;
import org.zfin.properties.ZfinProperties;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@Log4j2
public class DiseaseGenesInvolvedIndexer {

	public static void main(String[] args) {
		DiseaseGenesInvolvedIndexer indexer = new DiseaseGenesInvolvedIndexer();
		indexer.init();
		indexer.run();
		System.out.println("Finished Indexing");
	}

	private void run() {
		HibernateUtil.createTransaction();
		//GenericTerm term = getOntologyRepository().getTermByOboID("DOID:0112009");
		List<GenericTerm> diseaseList = getOntologyRepository().getAllTermsFromOntology(Ontology.DISEASE_ONTOLOGY);
		//GenericTerm term = getOntologyRepository().getTermByOboID("DOID:9952");
		diseaseList.forEach(term -> {
			List<OmimPhenotypeDisplay> displayListSingle = OntologyService.getOmimPhenotype(term, new Pagination(), false);
			OntologyService.fixupSearchColumns(displayListSingle);
			displayListSingle.forEach(omimisplay -> HibernateUtil.currentSession().save(omimisplay));
		});
		HibernateUtil.flushAndCommitCurrentSession();
		//log.info("Number of Records: "+displayListSingle.size());
	}

	public void init() {
		ZfinProperties.init();
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		if (sessionFactory == null) {
			new HibernateSessionCreator(false);
		}
	}

}
