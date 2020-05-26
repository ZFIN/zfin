package org.zfin.marker.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GeneOntologyAnnotationTableRow;
import org.zfin.marker.presentation.MarkerGoViewTableRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermAnnotationExtn;
import org.zfin.mutant.MarkerGoTermAnnotationExtnGroup;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.presentation.MarkerGoEvidencePresentation;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.ontology.service.RibbonService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by kschaper on 12/16/14.
 */
@Service
public class MarkerGoService {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private RibbonService ribbonService;

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

    public JsonResultResponse<GeneOntologyAnnotationTableRow> getGoEvidence(String geneId, String termId, boolean isOther, Pagination pagination) throws IOException, SolrServerException {
        long startTime = System.currentTimeMillis();
        Marker gene = markerRepository.getMarkerByID(geneId);

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/go-annotation");
        query.addFilterQuery("gene_zdb_id:" + geneId);
        ribbonService.addRibbonTermQuery(query, termId, isOther);
        String termNameFilter = pagination.getFieldFilter(FieldFilter.FILTER_TERM_NAME);
        if (StringUtils.isNotEmpty(termNameFilter)) {
            query.addFilterQuery("name_ac:(" + SolrService.luceneEscape(termNameFilter) + ")");
        }

        query.setStart(pagination.getStart());
        query.setRows(pagination.getLimit());
        query.add("group", "true");
        query.add("group.field", FieldName.GROUP_KEY.getName());
        query.add("group.limit", "100");
        query.add("group.ngroups", "true");

        query.addSort(FieldName.TERM_ONTOLOGY.getName(), SolrQuery.ORDER.asc);
        query.addSort(FieldName.NAME_SORT.getName(), SolrQuery.ORDER.asc);
        query.addSort(FieldName.QUALIFIER.getName(), SolrQuery.ORDER.asc);
        query.addSort(FieldName.EVIDENCE_CODE.getName(), SolrQuery.ORDER.asc);
        query.addSort(FieldName.GROUP_KEY.getName(), SolrQuery.ORDER.asc);

        QueryResponse queryResponse = SolrService.getSolrClient("prototype").query(query);
        GroupCommand groupResults = queryResponse.getGroupResponse().getValues().get(0);
        MarkerGoTermEvidenceRepository repository = RepositoryFactory.getMarkerGoTermEvidenceRepository();

        List<GeneOntologyAnnotationTableRow> results = groupResults.getValues().stream()
                .map(group -> {
                    GeneOntologyAnnotationTableRow row = new GeneOntologyAnnotationTableRow();
                    SolrDocument firstInGroup = group.getResult().get(0);
                    String id = (String) firstInGroup.getFieldValue(FieldName.ID.getName());
                    MarkerGoTermEvidence groupEntry = repository.getMarkerGoTermEvidenceByZdbID(id);
                    row.setRowKey(group.getGroupValue());
                    row.setOntology(groupEntry.getGoTerm().getOntology().getCommonName().replace("GO: ", ""));
                    row.setQualifier(Objects.toString(groupEntry.getFlag(), ""));
                    row.setTerm(groupEntry.getGoTerm());
                    row.setEvidenceCode(groupEntry.getEvidenceCode());
                    row.setInferenceLinks(groupEntry.getInferredFrom().stream()
                            .map(InferenceGroupMember::getInferredFrom)
                            .map(MarkerGoEvidencePresentation::generateInferenceLink)
                            .collect(Collectors.toSet()));
                    row.setAnnotationExtensions(groupEntry.getAnnotationExtensions().stream()
                            .map(MarkerGoEvidencePresentation::generateAnnotationExtensionLink)
                            .collect(Collectors.toSet()));
                    Set<Publication> publications = group.getResult().stream()
                            .map(doc -> (String) doc.getFieldValue(FieldName.ID.getName()))
                            .map(repository::getMarkerGoTermEvidenceByZdbID)
                            .map(MarkerGoTermEvidence::getSource)
                            .collect(Collectors.toSet());
                    row.setPublications(publications);
                    return row;
                })
                .collect(Collectors.toList());

        JsonResultResponse<GeneOntologyAnnotationTableRow> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startTime);
        response.setTotal(groupResults.getNGroups());
        response.setResults(results);

        return response;
    }

}
