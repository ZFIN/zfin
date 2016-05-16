package org.zfin.construct.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.construct.presentation.ConstructComponentPresentation;
import org.zfin.database.BtsContainsService;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.fish.WarehouseSummary;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.presentation.Construct;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Basic repository to handle fish search requests.
 */
public interface ConstructRepository {

    public ConstructSearchResult getConstructs(ConstructSearchCriteria criteria);

    public Construct getConstruct(String constructID);

    public List<ExpressionResult> getExpressionForConstructs(String constructID, List<String> termIDs);

    Set<ZfinFigureEntity> getFiguresByConstructAndTerms(String constructID, List<String> termIDs);

    Set<ZfinFigureEntity> getAllFigures(String constructID);

    WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart);

    /**
     * Retrieve the status of the fish mart:
     * true: fish mart ready for usage
     * false: fish mart is being rebuilt.
     *
     * @return status
     */
    ZdbFlag getConstructMartStatus();

    public List<ConstructRelationship> getConstructRelationshipsByPublication(String publicationZdbID);
    public ConstructRelationship getConstructRelationshipByID(String zdbID);
    public ConstructRelationship getConstructRelationship(ConstructCuration marker1, Marker marker2, ConstructRelationship.Type type);
    public void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, ConstructCuration construct, String pubID);
    public ConstructCuration getConstructByID(String zdbID);
    public ConstructCuration getConstructByName(String conName);
    void createConstruct(ConstructCuration construct, Publication publication);

    List<Fish> getFishByFigureConstruct(Figure figure, String constructID);

    List<ConstructComponent> getConstructComponentsByComponentID(String componentZdbID);
}