package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.stats.GeneDbLinkStatisticService;
import org.zfin.stats.GeneTranscriptStatisticService;
import org.zfin.stats.StatisticRow;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/marker/stats")
public class GeneStatController {

    public static final String FILTER = "filter.";
    @Autowired
    private HttpServletRequest request;

    private static final String GENE_ID = "geneId";
    private static final String GENE_SYMBOL = "geneSymbol";
    private static final String GENE_TYPE = "geneType";
    private static final String PLASMID = "plasmidAccession";
    private static final String TRANSCRIPT_TYPE = "transcriptType";
    private static final String TRANSCRIPT_ID = "transcriptId";
    private static final String TRANSCRIPT_STATUS = "transcriptStatus";

    @JsonView(View.API.class)
    @RequestMapping(value = "/transcript/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow<Marker,Transcript>> getPublicationDatasetsStats(@RequestParam(value = FILTER + GENE_ID, required = false) String geneID,
                                                                        @RequestParam(value = FILTER + GENE_SYMBOL, required = false) String geneSymbol,
                                                                        @RequestParam(value = FILTER + GENE_TYPE, required = false) String geneType,
                                                                        @RequestParam(value = FILTER + TRANSCRIPT_TYPE, required = false) String type,
                                                                        @RequestParam(value = FILTER + TRANSCRIPT_ID, required = false) String transcriptId,
                                                                        @RequestParam(value = FILTER + TRANSCRIPT_STATUS, required = false) String status,
                                                                        @Version Pagination pagination) {

        if (type != null) {
            pagination.addFieldFilter(FieldFilter.TYPE, type);
        }
        // move <empty> into Pagination object
        if (status != null && status.equals("<empty>")) {
            pagination.addFieldFilter(FieldFilter.STATUS_EMPTY, status);
        } else if (status != null) {
            pagination.addFieldFilter(FieldFilter.STATUS, status);
        }
        if(StringUtils.isNotEmpty(geneID)){
            pagination.addFieldFilter(FieldFilter.ENTITY_ID, geneID);
        }
        if(StringUtils.isNotEmpty(transcriptId)){
            pagination.addFieldFilter(FieldFilter.TRANSCRIPT_ID, transcriptId);
        }
        if(StringUtils.isNotEmpty(geneSymbol)){
            pagination.addFieldFilter(FieldFilter.GENE_ABBREVIATION, geneSymbol);
        }
        if(StringUtils.isNotEmpty(geneType)){
            pagination.addFieldFilter(FieldFilter.ZDB_ENTITY_TYPE, geneType);
        }
        GeneTranscriptStatisticService service = new GeneTranscriptStatisticService();
        JsonResultResponse<StatisticRow<Marker, Transcript>> response = service.getTranscriptStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/plasmids/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow<Marker, MarkerDBLink>> getPlasmidsStats(@RequestParam(value = FILTER + GENE_ID, required = false) String geneID,
                                                                                   @RequestParam(value = FILTER + GENE_SYMBOL, required = false) String geneSymbol,
                                                                                   @RequestParam(value = FILTER + GENE_TYPE, required = false) String geneType,
                                                                                   @RequestParam(value = FILTER + PLASMID, required = false) String plasmid,
                                                                                   @Version Pagination pagination) {

        if(StringUtils.isNotEmpty(geneID)){
            pagination.addFieldFilter(FieldFilter.ENTITY_ID, geneID);
        }
        if(StringUtils.isNotEmpty(geneSymbol)){
            pagination.addFieldFilter(FieldFilter.GENE_ABBREVIATION, geneSymbol);
        }
        if(StringUtils.isNotEmpty(geneType)){
            pagination.addFieldFilter(FieldFilter.ZDB_ENTITY_TYPE, geneType);
        }
        if(StringUtils.isNotEmpty(plasmid)){
            pagination.addFieldFilter(FieldFilter.PLASMID, plasmid);
        }
        GeneDbLinkStatisticService service = new GeneDbLinkStatisticService();
        JsonResultResponse<StatisticRow<Marker, MarkerDBLink>> response = service.getPlasmidStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

}

