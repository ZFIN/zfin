package org.zfin.datatransfer;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.alliancegenome.util.ProcessDisplayHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.test.util.ReflectionTestUtils;
import org.zfin.alliancegenome.PriorityRESTAllianceService;
import org.zfin.alliancegenome.presentation.PriorityTag;
import org.zfin.curation.presentation.CurationStatusDTO;
import org.zfin.curation.presentation.PersonDTO;
import org.zfin.curation.repository.HibernateCurationRepository;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.publication.PublicationTrackingService;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.presentation.DashboardPublicationList;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationTrackingController;
import org.zfin.repository.RepositoryFactory;

import java.io.IOException;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;

@Log4j2
public class PriorityPipelineTask extends AbstractScriptWrapper {

    static {
        Configurator.setLevel(PriorityPipelineTask.class.getName(), Level.INFO);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        PriorityPipelineTask task = new PriorityPipelineTask();
        task.initAll();
        task.run();
    }

    @SneakyThrows
    private void run() {

        DashboardPublicationList list = RepositoryFactory.getPublicationRepository().getPublicationsByStatus(2L, 0L, null, 1000, 0, "date");
        PublicationTrackingService trackingService = new PublicationTrackingService();
        PublicationTrackingStatus trackingStatus = trackingService.getPublicationStatus(PublicationTrackingStatus.Type.READY_FOR_INDEXING);
        PersonDTO personDTO = new PersonDTO();
        personDTO.setZdbID("ZDB-PERS-060413-1");
        // Use repositories directly instead of Spring context
        PublicationTrackingController controller = createControllerWithRepositories();
        PriorityRESTAllianceService serviceP = new PriorityRESTAllianceService();
        ProcessDisplayHelper ph = new ProcessDisplayHelper(log, 10000);
        ph.startProcess("Retrieving Priority for total of publications of: ", list.getTotalCount());
        PublicationService service = new PublicationService();
        list.getPublications().forEach(dashboardPublications -> {
            String pubID = dashboardPublications.getZdbId();
            PriorityTag tag = serviceP.findPriority(pubID);
            if (tag != null) {
                CurationStatusDTO dto = new CurationStatusDTO();
                dto.setStatus(trackingStatus);
                dto.setCurrent(true);
                dto.setPubZdbID(pubID);
                String indexingPriority = tag.getCurrentPriorityTag().getIndexingPriority();
                String priority = indexingPriority.substring(indexingPriority.length() - 1);
                dto.setLocation(trackingService.getLocation(priority));
                controller.updateCurationStatus(pubID, false, false, "ABC-Indexing Priority Classifier", dto);
            }
            ph.progressProcess();
        });
        ph.finishProcess();
        System.out.println("Done");
    }

    private PublicationTrackingController createControllerWithRepositories() {
        PublicationTrackingController controller = new PublicationTrackingController();

        // Manually inject repositories using RepositoryFactory
        ReflectionTestUtils.setField(controller, "publicationRepository",
            RepositoryFactory.getPublicationRepository());
        ReflectionTestUtils.setField(controller, "markerRepository",
            RepositoryFactory.getMarkerRepository());
        ReflectionTestUtils.setField(controller, "profileRepository",
            getProfileRepository());
        ReflectionTestUtils.setField(controller, "phenotypeRepository",
            RepositoryFactory.getPhenotypeRepository());
        ReflectionTestUtils.setField(controller, "expressionRepository",
            RepositoryFactory.getExpressionRepository());
        ReflectionTestUtils.setField(controller, "mutantRepository",
            RepositoryFactory.getMutantRepository());
        ReflectionTestUtils.setField(controller, "curationRepository",
            new HibernateCurationRepository());
        ReflectionTestUtils.setField(controller, "zebrashareRepository",
            RepositoryFactory.getZebrashareRepository());
        ReflectionTestUtils.setField(controller, "converter",
            new CurationDTOConversionService());
        ReflectionTestUtils.setField(controller, "publicationService",
            new PublicationService());

        return controller;
    }

}