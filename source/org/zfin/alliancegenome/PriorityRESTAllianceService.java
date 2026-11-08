package org.zfin.alliancegenome;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.presentation.PriorityRESTInterface;
import org.zfin.alliancegenome.presentation.PriorityTag;
import org.zfin.expression.Experiment;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.TokenStorage;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.service.DiseaseAnnotationService;
import org.zfin.ontology.GenericTerm;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Log4j2
@Service
public class PriorityRESTAllianceService extends RestAllianceService {

    private final PriorityRESTInterface api = AllianceRestManager.getPriorityEndpoint();

    // Pass -Dpriority.useAuth=true to attach an Authorization: Bearer <token> header on outgoing
    // calls. Default off — useful when the upstream service is open or the token is unavailable.
    private final String literatureToken;

    // Pass -Dpriority.baseUrl=https://stage-literature-rest.alliancegenome.org to target ABC stage.
    private static final String DEFAULT_BASE_URL = "https://literature-rest.alliancegenome.org";

    private final String baseUrl = System.getProperty("priority.baseUrl", DEFAULT_BASE_URL).replaceAll("/+$", "");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public PriorityRESTAllianceService() {
        this(Boolean.getBoolean("priority.useAuth"));
    }

    public PriorityRESTAllianceService(boolean useAuth) {
        if (useAuth) {
            TokenStorage tokenStorage = new TokenStorage();
            literatureToken = tokenStorage.getValue(TokenStorage.ServiceKey.ALLIANCE_LITERATURE_API_TOKEN)
                    .orElseGet(() -> {
                        log.error("Could not find Alliance Literature API token. Use: gradle tokenStorage --args='write ALLIANCE_LITERATURE_API_TOKEN <token>'");
                        return null;
                    });
        } else {
            literatureToken = null;
        }
    }

    public PriorityTag findPriority(String publicationID) {
        PriorityTag priorityTag = null;
        String pubParam = "ZFIN:" + publicationID + "/ZFIN";
        String urlString = baseUrl + "/indexing_priority/get_priority_tag/" + pubParam;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (literatureToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + literatureToken);
            }
            log.info("Request: GET " + urlString);
            log.info("Response code: " + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.registerModule(new JavaTimeModule());
                priorityTag = mapper.readValue(response.toString(), PriorityTag.class);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                reader.close();
                log.error("Error response: " + errorResponse);
            }
            conn.disconnect();
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Error while connecting to Priority Pipeline at Alliance: " + message);
        }
        return priorityTag;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets (or, with atpId == null, clears) the curator indexing priority for a paper at the ABC.
     * The ABC leaves its predicted priority and confidence score untouched, and creates the record
     * if the paper has no prediction yet.
     *
     * @param paperId ZFIN curie (ZFIN:ZDB-PUB-...), PMID:... or AGRKB:...
     * @param atpId   ATP ID of an indexing priority term (e.g. ATP:0000211), not the term name
     * @return HTTP status and response body; status -1 if the request could not be sent
     */
    public CuratorPriorityResponse setCuratorPriority(String paperId, String atpId) {
        if (literatureToken == null) {
            throw new IllegalStateException("No Alliance Literature API token; construct with useAuth=true and store ALLIANCE_LITERATURE_API_TOKEN");
        }
        String value = atpId == null ? "null" : "\"" + atpId + "\"";
        String body = "{\"curator_indexing_priority\": " + value + "}";
        URI uri = URI.create(baseUrl + "/indexing_priority/curator_priority/" + paperId + "/ZFIN");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + literatureToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new CuratorPriorityResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("PATCH " + uri + " failed: " + e.getMessage(), e);
            return new CuratorPriorityResponse(-1, String.valueOf(e.getMessage()));
        }
    }

    public record CuratorPriorityResponse(int status, String body) {
        public boolean isSuccess() {
            return status >= 200 && status < 300;
        }
    }

    public static void main(String[] args) {
        //DashboardPublicationList list = RepositoryFactory.getPublicationRepository().getPublicationsByStatus(2L, 0L, null, 1000, 0, "date");

        PriorityRESTAllianceService serviceP = new PriorityRESTAllianceService();
        serviceP.findPriority("ZFIN:ZDB-PUB-240502-19");
        ZfinPropertiesEnum.TARGETROOT.setValue(".");
        ZfinProperties.init();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }

        //ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("https://alpha-curation.alliancegenome.org");
        DiseaseAnnotationService service = new DiseaseAnnotationService();
        //service.agmRESTAllianceService = new AgmRESTAllianceService();
        DiseaseAnnotationModel dam = new DiseaseAnnotationModel();
        FishExperiment experiment = new FishExperiment();
        Fish fish = new Fish();
        //fish.setZdbID("ZDB-FISH-220707-340");
        fish.setZdbID("ZDB-FISH-151211-15");
        experiment.setFish(fish);
        Experiment experimentCond = new Experiment();
        experimentCond.setZdbID("ZDB-EXP-041102-1");
        experimentCond.setName("_Standard");
        experiment.setExperiment(experimentCond);
        dam.setFishExperiment(experiment);
        DiseaseAnnotation diseaseAnnotation = new DiseaseAnnotation();
        diseaseAnnotation.setZdbID("ZDB-DAT-220710-12");
        GenericTerm evidence = new GenericTerm();
        evidence.setOboID("ECO:0000304");
        diseaseAnnotation.setEvidenceCode(evidence);
        GenericTerm disease = new GenericTerm();
        disease.setOboID("DOID:4");
        diseaseAnnotation.setDisease(disease);
        Publication publication = new Publication();
        publication.setAccessionNumber(26186000);
        diseaseAnnotation.setPublication(publication);
        dam.setDiseaseAnnotation(diseaseAnnotation);
        service.submitAnnotationToAlliance(dam);
    }

}
