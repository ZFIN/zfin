package org.zfin.antibody.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.isNotEmpty;
import org.zfin.Species;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.infrastructure.AllMarkerNamesFastSearch;
import org.zfin.infrastructure.AllNamesFastSearch;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.TextOnlyFigure;
import org.zfin.expression.FigureFigure;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.util.FilterType;

import java.util.List;


/**
 * Hibernate implementation of the Antibody Repository.
 */
public class HibernateAntibodyRepository implements AntibodyRepository {

    private AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();

    public Antibody getAntibodyByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Antibody) session.get(Antibody.class, zdbID);
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAntibodies(AntibodySearchCriteria searchCriteria) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("select distinct antibody ");
        hql.append(getAntibodiesByNameAndLabelingQueryBlock(searchCriteria));
        hql.append("order by antibody.abbreviationOrder");
        Query query = session.createQuery(hql.toString());
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.CONTAINS)
                query.setParameter("name", "%" + searchCriteria.getName().toLowerCase() + "%");
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.BEGINS)
                query.setParameter("name", searchCriteria.getName().toLowerCase() + "%");
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (searchCriteria.getAntigenNameFilterType() == FilterType.CONTAINS)
                query.setParameter("markerName", "%" + searchCriteria.getAntigenGeneName().toLowerCase().trim() + "%");
            if (searchCriteria.getAntigenNameFilterType() == FilterType.BEGINS)
                query.setParameter("markerName", searchCriteria.getAntigenGeneName().toLowerCase().trim() + "%");
            //query.setParameter("genePrecedence", getGenePrecedenceInClause());
        }
        if (searchCriteria.isHostSpeciesDefined())
            query.setParameter("hostSpecies", searchCriteria.getHostSpecies());
        if (searchCriteria.isAssaySearch())
            query.setParameter("assay", searchCriteria.getAssay());
        if (searchCriteria.isZircOnly())
            query.setParameter("zircLabID", "ZDB-LAB-991005-53");
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType()))
            query.setParameter("antibodyType", searchCriteria.getClonalType());
        if (searchCriteria.isStageDefined())
            bindStageFilterValues(searchCriteria, query);
        if (searchCriteria.isAnatomyDefined()){
            applyAnatomyTermsFilter(searchCriteria, query);
            query.setParameter("standard", Experiment.STANDARD);
            query.setParameter("generic", Experiment.GENERIC_CONTROL);
        }
        PaginationBean paginationBean = searchCriteria.getPaginationBean();
        setPaginationParameters(query, paginationBean);
        return (List<Antibody>) query.list();
    }

    private String getGenePrecedenceInClause() {
        AllNamesFastSearch.Precedence[] genePrecedence = AllNamesFastSearch.getGenePrecedences();
        if (genePrecedence == null)
            return null;
        StringBuilder inClause = new StringBuilder();
        for (AllMarkerNamesFastSearch.Precedence precedence : genePrecedence) {
            inClause.append("'");
            inClause.append(precedence.toString());
            inClause.append("',");
        }
        inClause.deleteCharAt(inClause.length() - 1);
        return inClause.toString();
    }

    public int getNumberOfAntibodies(AntibodySearchCriteria searchCriteria) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("select count(distinct antibody)");
        hql.append(getAntibodiesByNameAndLabelingQueryBlock(searchCriteria));
        Query query = session.createQuery(hql.toString());
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            // antibody name and abbreviation is the same
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.CONTAINS)
                query.setParameter("name", "%" + searchCriteria.getName().toLowerCase() + "%");
            else if (searchCriteria.getAntibodyNameFilterType() == FilterType.BEGINS)
                query.setParameter("name", searchCriteria.getName().toLowerCase() + "%");
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (searchCriteria.getAntigenNameFilterType() == FilterType.CONTAINS)
                query.setParameter("markerName", "%" + searchCriteria.getAntigenGeneName().toLowerCase() + "%");
            else if (searchCriteria.getAntigenNameFilterType() == FilterType.BEGINS)
                query.setParameter("markerName", searchCriteria.getAntigenGeneName().toLowerCase() + "%");
            //query.setParameter("genePrecedence", getGenePrecedenceInClause());
        }
        if (searchCriteria.isHostSpeciesDefined())
            query.setParameter("hostSpecies", searchCriteria.getHostSpecies());
        if (searchCriteria.isAssaySearch())
            query.setParameter("assay", searchCriteria.getAssay());
        if (searchCriteria.isZircOnly())
            query.setParameter("zircLabID", "ZDB-LAB-991005-53");
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType()))
            query.setParameter("antibodyType", searchCriteria.getClonalType());
        if (searchCriteria.isStageDefined())
            bindStageFilterValues(searchCriteria, query);
        if (searchCriteria.isAnatomyDefined()){
            applyAnatomyTermsFilter(searchCriteria, query);
            query.setParameter("standard", Experiment.STANDARD);
            query.setParameter("generic", Experiment.GENERIC_CONTROL);
        }
        return (Integer) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Species> getHostSpeciesList() {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Species.class);
        criteria.add(eq("antibodyHost", true));
        criteria.addOrder(Order.asc("displayOrder"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<Species> getUsedHostSpeciesList() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Species.class, "species");
        criteria.add(eq("antibodyHost", true));
        criteria.addOrder(Order.asc("displayOrder"));
        DetachedCriteria antibodies = DetachedCriteria.forClass(Antibody.class, "ab");
        antibodies.add(Restrictions.eqProperty("ab.hostSpecies", "species.commonName"));
        antibodies.setProjection(Property.forName("ab.zdbID").count());
        criteria.add(Subqueries.lt(0, antibodies));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<Species> getImmunogenSpeciesList() {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Species.class);
        criteria.add(eq("antibodyImmunogen", true));
        criteria.addOrder(Order.asc("displayOrder"));
        return criteria.list();
    }


    public int getAntibodiesByAOTermCount(AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.setProjection(Projections.countDistinct("zdbID"));
        Criteria labeling = criteria.createCriteria("antibodyLabelings");
        Criteria genotypeExperiment = labeling.createCriteria("genotypeExperiment");
        Criteria genotype = genotypeExperiment.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria results = labeling.createCriteria("expressionResults");
        results.add(eq("anatomyTerm", aoTerm));
        results.add(eq("expressionFound", true));
        Criteria experiment = genotypeExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));

        return (Integer) results.list().get(0);
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<Antibody> getAntibodiesByAOTerm(AnatomyItem aoTerm, PaginationBean paginationBean, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();
        StringBuffer hql = new StringBuffer();
        hql.append("select distinct antibody from Antibody antibody, ExpressionExperiment expExp,  ");
        hql.append("                ExpressionResult res, GenotypeExperiment genox, Genotype geno, ");
        hql.append("                Experiment exp ");
        hql.append("where ");
        hql.append("       expExp.antibody = antibody ");
        hql.append("       and res.expressionExperiment = expExp ");
        hql.append("       and genox = expExp.genotypeExperiment ");
        hql.append("       and geno = genox.genotype ");
        hql.append("       and geno.wildtype = :wildType ");
        if (includeSubstructures) {
            hql.append("   and ( res.anatomyTerm = :aoTerm  OR exists ( select 1 from AnatomyChildren child " +
                    "                  where res.anatomyTerm = child.child AND child.root = :aoTerm ) ) ");
        } else
            hql.append("       and res.anatomyTerm = :aoTerm ");
        hql.append("       and res.expressionFound = :expressionFound ");
        hql.append("       and exp = genox.experiment ");
        hql.append("       and exp.name in (:standard , :generic ) ");
        hql.append("order by antibody.abbreviationOrder");
        Query query = session.createQuery(hql.toString());
        query.setParameter("wildType", true);
        query.setParameter("aoTerm", aoTerm);
        query.setParameter("expressionFound", true);
        query.setParameter("standard", Experiment.STANDARD);
        query.setParameter("generic", Experiment.GENERIC_CONTROL);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean, query.scroll());
    }

    public int getNumberOfFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm, Figure.Type figureType) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria;
        if (figureType != null && figureType == Figure.Type.TOD)
            criteria = session.createCriteria(TextOnlyFigure.class);
        else
            criteria = session.createCriteria(FigureFigure.class);
        criteria.setProjection(Projections.countDistinct("zdbID"));
        Criteria results = criteria.createCriteria("expressionResults");
        results.add(eq("anatomyTerm", aoTerm));
        results.add(eq("expressionFound", true));
        Criteria labeling = results.createCriteria("expressionExperiment");
        labeling.add(eq("antibody", antibody));
        Criteria genotypeExperiment = labeling.createCriteria("genotypeExperiment");
        Criteria genotype = genotypeExperiment.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = genotypeExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD}));
        return (Integer) criteria.list().get(0);
    }

    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Figure.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        Criteria results = criteria.createCriteria("expressionResults");
        results.add(eq("anatomyTerm", aoTerm));
        results.add(eq("expressionFound", true));
        Criteria labeling = results.createCriteria("expressionExperiment");
        labeling.add(eq("antibody", antibody));
        Criteria genotypeExperiment = labeling.createCriteria("genotypeExperiment");
        Criteria genotype = genotypeExperiment.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = genotypeExperiment.createCriteria("experiment");
        experiment.add(Restrictions.eq("name", Experiment.STANDARD));

        return (List<Figure>) labeling.list();
    }

    private Criteria QueryBlockPublicationsWithFiguresPerAoTerm(Antibody antibody, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria pubs = session.createCriteria(Publication.class);
        Criteria labeling = pubs.createCriteria("expressionExperiments");
        labeling.add(eq("antibody", antibody));
        Criteria results = labeling.createCriteria("expressionResults");
        results.add(eq("anatomyTerm", aoTerm));
        results.add(isNotEmpty("figures"));
        results.add(eq("expressionFound", true));
        Criteria genotypeExperiment = labeling.createCriteria("genotypeExperiment");
        Criteria genotype = genotypeExperiment.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = genotypeExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
        return pubs;
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<Publication> getPublicationsWithFigures(Antibody antibody, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria pubs = session.createCriteria(Publication.class);
        Criteria labeling = pubs.createCriteria("expressionExperiments");
        labeling.add(eq("antibody", antibody));
        Criteria results = labeling.createCriteria("expressionResults");
        results.add(eq("anatomyTerm", aoTerm));
        results.add(isNotEmpty("figures"));
        results.add(eq("expressionFound", true));
        Criteria genotypeExperiment = labeling.createCriteria("genotypeExperiment");
        Criteria genotype = genotypeExperiment.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = genotypeExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<Publication>((List<Publication>) pubs.list());
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAntibodiesByPublication(Publication publication) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct antibody from Antibody as antibody, PublicationAttribution pubAttribute " +
                "            where pubAttribute.publication = :pub AND " +
                "            pubAttribute.dataZdbID = antibody.zdbID order by antibody.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setParameter("pub", publication);

        return (List<Antibody>) query.list();

    }

    public Antibody getAntibodyByName(String antibodyName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.add(Restrictions.eq("name", antibodyName));
        return (Antibody) criteria.uniqueResult();
    }

    private void applyAnatomyTermsFilter(AntibodySearchCriteria searchCriteria, Query query) {
        AnatomyItem[] terms = getAnatomyItemsByName(searchCriteria.getAnatomyTerms());
        for (int i = 0; i < terms.length; i++) {
            query.setParameter("aoTermID_" + i, terms[i].getZdbID());
        }
    }

    public AnatomyItem[] getAnatomyItemsByName(String[] anatomyTerms) {
        if (anatomyTerms == null)
            return null;
        AnatomyItem[] terms = new AnatomyItem[anatomyTerms.length];

        int index = 0;
        for (String name : anatomyTerms) {
            terms[index++] = anatomyRepository.getAnatomyItem(name);
        }
        return terms;
    }

    private void bindStageFilterValues(AntibodySearchCriteria searchCriteria, Query query) {
        DevelopmentStage startStageFilter = searchCriteria.getStartStage();
        DevelopmentStage ss = new DevelopmentStage();
        ss.setZdbID(startStageFilter.getZdbID());
        DevelopmentStage startStage = anatomyRepository.getStage(ss);
        query.setParameter("hoursStart", startStage.getHoursStart());
        DevelopmentStage endStageFilter = searchCriteria.getEndStage();
        DevelopmentStage end = new DevelopmentStage();
        end.setZdbID(endStageFilter.getZdbID());
        DevelopmentStage endStage = anatomyRepository.getStage(end);
        query.setParameter("hoursEnd", endStage.getHoursEnd());
    }

    /**
     * Uses the following parameters:
     * 1) :abbrev
     * 2) :hostSpeciesif not 'ANY" is set
     * 3) :immunogenSpecies if not 'ANY" is set
     *
     * @param searchCriteria antibody criteria
     * @return string
     */
    private String getAntibodiesByNameAndLabelingQueryBlock(AntibodySearchCriteria searchCriteria) {
        // Todo: need to do a better job!!!
        boolean hasOneWhereClause = false;
        StringBuilder hql = new StringBuilder(" from Antibody antibody ");

        if (searchCriteria.isZircOnly())
            hql.append(", MarkerSupplier markerSupplier ");
        if (searchCriteria.isStageDefined() || searchCriteria.isAnatomyDefined() || searchCriteria.isAssaySearch())
            hql.append(", ExpressionExperiment experiment ");
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName()))
            hql.append(",  MarkerRelationship rel   ");
        if (!StringUtils.isEmpty(searchCriteria.getName()))
            hql.append(",  AllMarkerNamesFastSearch mapAntibody   ");
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName()))
            hql.append(",  AllMarkerNamesFastSearch mapGene   ");
        if (searchCriteria.isAny())
            hql.append("where ");
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            hql.append(" mapAntibody.marker =  antibody AND mapAntibody.nameLowerCase like :name ");
            hasOneWhereClause = true;
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append("    rel.secondMarker = antibody AND ");
            hql.append("   (mapGene.marker =  rel.firstMarker AND mapGene.nameLowerCase like :markerName ) ");
//                    "       AND mapGene.precedence in (:genePrecedence) )");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isAssaySearch()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" experiment.assay.name = :assay AND experiment.antibody = antibody ");
            hasOneWhereClause = true;
        }
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType())) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" antibody.clonalType = :antibodyType ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isHostSpeciesDefined()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" antibody.hostSpecies = :hostSpecies ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isZircOnly()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" markerSupplier.organization.zdbID = :zircLabID AND markerSupplier.marker = antibody ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isStageDefined()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hasOneWhereClause = true;
            hql.append(" experiment.antibody = antibody " +
                    " AND ( exists ( select result from ExpressionResult result " +
                    "                  where result.startStage.hoursStart >= :hoursStart " +
                    "                    AND result.endStage.hoursEnd <= :hoursEnd " +
                    "                    AND result.expressionExperiment = experiment )) ");
        }
        if (searchCriteria.isAnatomyDefined()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" experiment.antibody = antibody AND ( ");
            int numberOfTerms = searchCriteria.getAnatomyTerms().length;
            for (int i = 0; i < numberOfTerms; i++) {
                hql.append("    ( exists ( select result from ExpressionResult result " +
                        "                  where result.anatomyTerm.zdbID = :aoTermID_" + i +
                        "                     AND result.expressionExperiment = experiment" +
                        "                     AND result.expressionExperiment.genotypeExperiment.genotype.wildtype = 't'" +
                        "                     AND result.expressionExperiment.genotypeExperiment.experiment.name in (:standard , :generic )" +
                        "                     AND result.expressionFound = 't' ) ");
                if (searchCriteria.isIncludeSubstructures())
                    hql.append("     OR exists ( select result from ExpressionResult result, AnatomyChildren child " +
                            "                  where result.anatomyTerm = child.child AND child.root = :aoTermID_" + i +
                            "                     AND result.expressionExperiment = experiment" +
                            "                     AND result.expressionExperiment.genotypeExperiment.genotype.wildtype = 't'" +
                            "                     AND result.expressionExperiment.genotypeExperiment.experiment.name in (:standard , :generic )" +
                            "                     AND result.expressionFound = 't' ) ");
                hql.append(" ) ");
                if (i < numberOfTerms - 1) {
                    if (searchCriteria.isAnatomyEveryTerm())
                        hql.append(" AND ");
                    else
                        hql.append(" OR ");
                }
            }
            hql.append(")");
        }

        return hql.toString();
    }

    private void setPaginationParameters(Query query, PaginationBean paginationBean) {
        if (paginationBean == null)
            return;
        query.setMaxResults(paginationBean.getMaxDisplayRecords());
        query.setFirstResult(paginationBean.getFirstRecord() - 1);
    }

    @SuppressWarnings("unchecked")
    public List<AllMarkerNamesFastSearch> getAllNameAntibodyMatches(String string) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AllMarkerNamesFastSearch.class);
        criteria.add(Restrictions.like("nameLowerCase", "%" + string + "%"));
        Criteria marker = criteria.createCriteria("marker");
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        MarkerType type = mr.getMarkerTypeByName(Marker.Type.ATB.toString());
        marker.add(Restrictions.eq("markerType", type));
        return (List<AllMarkerNamesFastSearch>) marker.list();

    }

}

