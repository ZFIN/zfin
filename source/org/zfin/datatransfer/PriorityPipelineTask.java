package org.zfin.datatransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.alliancegenome.util.ProcessDisplayHelper;
import org.apache.commons.io.FileUtils;
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
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.PublicationTrackingService;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.presentation.DashboardPublicationList;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationTrackingController;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.gff.ReportBuilder;
import org.zfin.sequence.load.EnsemblLoadSummaryItemDTO;
import org.zfin.sequence.load.LoadAction;
import org.zfin.sequence.load.LoadActionsContainer;
import org.zfin.sequence.load.LoadLink;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;
import static org.zfin.sequence.load.LoadAction.SubType.*;

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
        dto = getEnsemblLoadSummaryItemDTO();
        ReportBuilder builder = prepareReports();

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
        Map<String, Integer> priorityHistogram = new HashMap<>();
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
                LoadLink newTranscriptLink = new LoadLink(pubID, "/" + pubID);
                LoadAction.SubType subType = null;
                priorityHistogram.merge(priority, 1, Integer::sum);
                switch (priority) {
                    case "1" -> subType = PRIORITY_1;
                    case "2" -> subType = PRIORITY_2;
                    case "3" -> subType = PRIORITY_3;
                }
                LoadAction loadAction = new LoadAction(LoadAction.Type.LOAD, subType, pubID, "", "This Publication was assigned the priority " + priority, 0, new HashMap<>());
                loadAction.addLink(newTranscriptLink);
                actions.add(loadAction);
            } else {
                LoadAction ignoreAction = new LoadAction(LoadAction.Type.INFO, NO_PRIORITY_FOUND, pubID, "", "No Priority found for this Publication  ", 0, new HashMap<>());
                ignoreAction.addLink(new LoadLink(pubID, "/" + pubID));
                actions.add(ignoreAction);
            }

            ph.progressProcess();
        });
        priorityHistogram.forEach((priority, count) -> {
            switch (priority) {
                case "1" -> dto.getCounts().put("p1Count", count.longValue());
                case "2" -> dto.getCounts().put("p2Count", count.longValue());
                case "3" -> dto.getCounts().put("p3Count", count.longValue());
            }
        });
        ph.finishProcess();
        writeOutputReportFile();
        System.out.println("Done");
    }

    protected EnsemblLoadSummaryItemDTO getEnsemblLoadSummaryItemDTO() {
        EnsemblLoadSummaryItemDTO dto = new EnsemblLoadSummaryItemDTO();
        return dto;
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

    private ReportBuilder.SummaryTable summaryTableLoad;
    private static final String JSON_PLACEHOLDER_IN_TEMPLATE = "JSON_GOES_HERE";
    public static final String REPORT_HOME_DIRECTORY = "/home/ensembl/";
    Set<LoadAction> actions = new HashSet<>();
    EnsemblLoadSummaryItemDTO dto;


    protected void writeOutputReportFile() {
        String reportFile = "priority-pipeline-report.html";

        log.info("Creating report file: " + reportFile);
        try {
            LoadActionsContainer actionsContainer = LoadActionsContainer.builder()
                .actions(actions)
                .summary(dto)
                .build();
            String jsonContents = actionsToJson(actionsContainer);
            String template = ZfinPropertiesEnum.SOURCEROOT.value() + REPORT_HOME_DIRECTORY + "/priority-pipeline-report-template.html";
            String templateContents = FileUtils.readFileToString(new File(template), "UTF-8");
            String filledTemplate = templateContents.replace(JSON_PLACEHOLDER_IN_TEMPLATE, jsonContents);
            FileUtils.writeStringToFile(new File(reportFile), filledTemplate, "UTF-8");
        } catch (IOException e) {
            log.error("Error creating report (" + reportFile + ") from template\n" + e.getMessage(), e);
        }
    }

    private String actionsToJson(LoadActionsContainer actions) throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(actions);
    }


    private ReportBuilder prepareReports() {
        ReportBuilder builder = new ReportBuilder();
        builder.setTitle("ABC Priority Pipeline Report");
        summaryTableLoad = builder.addSummaryTable("Publication Records");
        //summaryTableLoad.setHeaders(List.of("Record Type", "Count"));
        return builder;
    }

}