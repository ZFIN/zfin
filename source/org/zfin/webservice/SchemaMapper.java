package org.zfin.webservice;

import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.webservice.schema.*;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SchemaMapper {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    private static final Logger logger = Logger.getLogger(SchemaMapper.class);

    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    public static Gene convertMarkerToGeneWebObject(Gene gene,Marker marker,boolean showExpressionAnatomyWildtype){
        if(marker!=null){
            gene.setZdbId(marker.getZdbID());
            gene.setName(marker.getName());
            gene.setAbbreviation(marker.getAbbreviation());
            gene.setLink(MarkerPresentation.getJumpToLink(marker.getZdbID()));

            if(showExpressionAnatomyWildtype){
                gene.getExpressionAnatomyWildType().addAll(convertAnatomyListFromAnatomyItemList(expressionRepository.getWildTypeAnatomyExpressionForMarker(marker.getZdbID())));
            }
        }

        return gene ;
    }

    public static List<Anatomy> convertAnatomyListFromAnatomyItemList(List<AnatomyItem> anatomyItems) {
        List<Anatomy> anatomyList = new ArrayList<Anatomy>();
        if(anatomyItems==null) return anatomyList ;
        for(AnatomyItem anatomyItem : anatomyItems){
            anatomyList.add(convertAnatomyFromAnatomyItem(anatomyItem)) ;
        }
        return anatomyList;
    }

    private static Anatomy convertAnatomyFromAnatomyItem(AnatomyItem anatomyItem) {
        Anatomy anatomy = new Anatomy();
        if(anatomyItem==null) return anatomy;

        anatomy.setZdbId(anatomyItem.getZdbID());
        anatomy.setOboId(anatomyItem.getOboID());
        anatomy.setName(anatomyItem.getName());
        anatomy.setDefinition(anatomyItem.getDefinition());
        anatomy.setDescription(anatomyItem.getDescription());
        anatomy.setStageStart(anatomyItem.getStart().getAbbreviation());
        anatomy.setStageEnd(anatomyItem.getEnd().getAbbreviation());
        anatomy.setLink(AnatomyItemPresentation.getJumpToLink(anatomyItem.getZdbID()));
        return anatomy;
    }

    public static GeneSearchResponse convertMarkersToGeneWebObjects(GeneSearchResponse geneSearchResponse, List<Marker> markers, boolean showExpressionAnatomyWildtype) {
        if(markers!=null){
            for(Marker m : markers){
                geneSearchResponse.getGenes().add(convertMarkerToGeneWebObject( objectFactory.createGene(), m,showExpressionAnatomyWildtype));
            }
        }
        return geneSearchResponse;
    }

    public static GeneSearchResponse convertMarkersToGeneWebObjects(GeneSearchResponse geneSearchResponse, List<Marker> markers, GeneSearchRequest geneSearchRequest) {
        return convertMarkersToGeneWebObjects(geneSearchResponse,markers,geneSearchRequest.isExpressionAnatomyWildType());
    }

    public static GeneSearchResponse convertMarkersToGeneWebObjects(GeneSearchResponse geneSearchResponse, List<Marker> markers) {
        return convertMarkersToGeneWebObjects(geneSearchResponse,markers,false);
    }

    public static Gene createGeneFromMarker(Marker m) {
        return convertMarkerToGeneWebObject(new Gene(),m,false);
    }

    public static Gene createGeneFromMarker(Marker m, boolean showExpressionAnatomyWildtype) {
        return convertMarkerToGeneWebObject(new Gene(),m,showExpressionAnatomyWildtype);
    }
}
