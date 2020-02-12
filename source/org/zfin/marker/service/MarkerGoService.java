package org.zfin.marker.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerGoViewTableRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.MarkerGoTermAnnotationExtn;
import org.zfin.mutant.MarkerGoTermAnnotationExtnGroup;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by kschaper on 12/16/14.
 */
@Service
public class MarkerGoService {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private OntologyRepository ontologyRepository;

    public static Logger log = LogManager.getLogger(MarkerGoService.class);

    public List<MarkerGoViewTableRow> getMarkerGoViewTableRows(Marker marker) {
        return getMarkerGoViewTableRows(marker, marker.getGoTermEvidence());
    }

    public List<MarkerGoViewTableRow> getMarkerGoViewTableRows(Marker marker, Collection<MarkerGoTermEvidence> evidenceList) {
        List<MarkerGoViewTableRow> rows = new ArrayList<>();

        for (MarkerGoTermEvidence evidence : evidenceList) {
            MarkerGoViewTableRow row = new MarkerGoViewTableRow(evidence);
            row.setInferredFrom(getInferrenceLinks(evidence));
            row.setAnnotExtns(getAnnotationExtensionsAsString(evidence));
            if (!rows.contains(row)) {
                rows.add(row);
            } else {
                for (MarkerGoViewTableRow matchingRow : rows) {
                    if (row.equals(matchingRow)) {
                        matchingRow.addPublication(evidence.getSource());
                        matchingRow.setId(matchingRow.getId() + "+" + row.getId());
                    }
                }
            }
        }

        //once we're done with grouping, we can populate the presentation links
        for (MarkerGoViewTableRow row : rows) {
            row.setReferencesLink(getReferencesLink(row, marker));
        }

        Collections.sort(rows);

        return rows;
    }


    String getInferrenceLinks(MarkerGoTermEvidence evidence) {
        StringBuilder sb = new StringBuilder();

        if (CollectionUtils.isNotEmpty(evidence.getInferredFrom())) {
            for (String s : evidence.getInferencesAsString()) {
                if (org.apache.commons.lang.StringUtils.isNotEmpty(sb.toString())) {
                    sb.append(", ");
                }
                sb.append(MarkerGoEvidencePresentation.generateInferenceLink(s));
            }

        }

        return sb.toString();
    }

    String getAnnotationExtensionsAsString(MarkerGoTermEvidence evidence) {
        StringBuilder sb = new StringBuilder();

        if (CollectionUtils.isNotEmpty(evidence.getGoTermAnnotationExtnGroup())) {
            for (MarkerGoTermAnnotationExtnGroup mgtaeg : evidence.getGoTermAnnotationExtnGroup()) {

                for (MarkerGoTermAnnotationExtn mgtae : mgtaeg.getMgtAnnoExtns()) {
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(sb.toString())) {
                        sb.append(System.lineSeparator());
                    }

                    if (!sb.toString().contains(MarkerGoEvidencePresentation.generateAnnotationExtensionLink(mgtae))) {
                        sb.append(MarkerGoEvidencePresentation.generateAnnotationExtensionLink(mgtae));
                    }
                    //    sb.append("ExtID"+mgtae.getId().toString()+"GroupID="+ mgtae.getAnnotExtnGroupID().getId().toString());

                }
            }

        }

        return sb.toString();
    }

    String getReferencesLink(MarkerGoViewTableRow row, Marker marker) {
        if (CollectionUtils.isEmpty(row.getPublications())) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (row.getPublications().size() > 1) {
            sb.append(" <a href=\"/action/marker/go-citation-list/markerID/");
            sb.append(marker.getZdbID());
            sb.append("/mrkrGoEvdTermZdbID/");
            sb.append(row.getTerm().getZdbID());
            sb.append("/evidenceCode/");
            sb.append(row.getEvidenceCode().getCode().toUpperCase());
            sb.append("/inference/");
            sb.append(row.getFirstInference());
            sb.append("\">");
            sb.append(row.getPublications().size());
            sb.append(" Publications");
            sb.append("</a>");
        } else {
            Publication publication = row.getPublications().iterator().next();
            sb.append(PublicationPresentation.getLink(publication));
        }

        return sb.toString();
    }

    public Map<String, Integer> getRibbonAnnotationCountsForGene(String geneZdbId, List<String> includeTermIDs, List<String> excludeTermIDs)
            throws SolrServerException, IOException {
        Map<String, Integer> termCounts = new HashMap<>(includeTermIDs.size());

        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRequestHandler("/go-annotation");
        query.addFilterQuery("gene_zdb_id:" + geneZdbId);

        includeTermIDs.forEach(t -> query.addFacetQuery("term_id:" + SolrService.luceneEscape(t)));
        excludeTermIDs.forEach(t -> query.addFilterQuery("-term_id:" + SolrService.luceneEscape(t)));

        QueryResponse response = SolrService.getSolrClient("prototype").query(query);

        Pattern pattern = Pattern.compile("(GO:\\d+)");
        for (Map.Entry<String, Integer> entry : response.getFacetQuery().entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey().replace("\\", ""));
            if (matcher.find()) {
                String termID = matcher.group(1);
                termCounts.put(termID, entry.getValue());
            }
        }

        return termCounts;
    }

    public JsonResultResponse<MarkerGoViewTableRow> getGoEvidence(String geneId, String termId, boolean isOther, Pagination pagination) throws IOException, SolrServerException {
        long startTime = System.currentTimeMillis();
        Marker gene = markerRepository.getMarkerByID(geneId);

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/go-annotation");
        query.addFilterQuery("gene_zdb_id:" + geneId);
        if (termId != null) {
            query.addFilterQuery("term_id:" + SolrService.luceneEscape(termId));
            if (isOther) {
                List<GenericTerm> slimTerms = ontologyRepository.getTermsInSubset("goslim_agr");
                slimTerms.forEach(term -> query.addFilterQuery("-term_id:" + SolrService.luceneEscape(term.getOboID())));
            }
        }
        query.setStart(pagination.getStart());
        query.setRows(pagination.getLimit());
        query.setSort(FieldName.NAME_SORT.getName(), SolrQuery.ORDER.asc);

        QueryResponse queryResponse = SolrService.getSolrClient("prototype").query(query);
        MarkerGoTermEvidenceRepository repository = RepositoryFactory.getMarkerGoTermEvidenceRepository();
        List<MarkerGoTermEvidence> evidenceList = queryResponse.getResults().stream()
                .map(doc -> repository.getMarkerGoTermEvidenceByZdbID(doc.getFieldValue(FieldName.ID.getName()).toString()))
                .collect(Collectors.toList());
        List<MarkerGoViewTableRow> results = getMarkerGoViewTableRows(gene, evidenceList);

        JsonResultResponse<MarkerGoViewTableRow> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startTime);
        response.setTotal((int) queryResponse.getResults().getNumFound());
        response.setResults(results);

        return response;
    }

}
