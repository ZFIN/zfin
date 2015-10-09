package org.zfin.orthology.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.zfin.criteria.ZfinCriteria;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.*;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.presentation.DBLinkPresentation;
import org.zfin.util.FilterType;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * This class creates the calls to Hibernate to retrieve the Orthology information.
 */
// ToDo: SQL query for OR relation is using basic query and thus to real column names.
public class HibernateOrthologyRepository implements OrthologyRepository {

    public static final String AND = " AND ";
    public static final String LIKE = "like";
    public static final String EQUAL_SIGN = "= ";
    public static final String CLOSE_BRACKET = ")";
    public static final String IN = "in ";
    public static final String OPEN_BRACKET = "(";

    public Object[] getOrthologies(List<SpeciesCriteria> speciesCriteria, ZfinCriteria criteria) {
        if (criteria.isOrRelationship())
            return getOrthologiesOr(speciesCriteria, criteria);

        Object[] repositoryResults = new Object[2];
        List<Orthologs> orthologies = new ArrayList<Orthologs>();
        repositoryResults[0] = orthologies;
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("from ");

        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            if (currentSpecies.getName().equals(Species.ZEBRAFISH.toString()))
                hql.append(" ZebrafishOrthologyHelper ");
            else
                hql.append(" OrthologyHelper ");
            hql.append(currentSpecies.getName());
            hql.append(",");
        }
        hql.deleteCharAt(hql.length() - 1);

        hql.append(" WHERE ");
        getSpeciesJoinWhereClause(speciesCriteria, hql, criteria.isOrRelationship());
        hql.append(" AND " + Species.ZEBRAFISH.toString());
        hql.append(".chromosome != '0'");

        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            String species = currentSpecies.getName();
            hql.append(AND);
            hql.append(species);
            if (criteria.isOrRelationship())
                hql.append(".vorthy_species = '");
            else
                hql.append(".species = '");

            hql.append(species);
            hql.append("'");

            createGeneSymbolWhereClause(currentSpecies, hql, criteria.isOrRelationship());
            createCromosomeWhereClause(currentSpecies, hql, criteria);
            createPositionWhereClause(currentSpecies, hql);
        }

        if (criteria.getFirstRow() == 1) {
            String hqlCountQuery = "select count(" + Species.ZEBRAFISH.toString() + ") " + hql;
            Query countQuery = session.createQuery(hqlCountQuery);
            List numbers = countQuery.list();
//            repositoryResults[1] = countQuery.uniqueResult();
            repositoryResults[1] = (Integer) numbers.get(0);
        }

        String orderBy = criteria.getOrderByClause();
        // ToDo: Get wrong records: with a number 0 and the true number.
        // need to fix the view
        hql.append(" AND ");
        hql.append(Species.ZEBRAFISH.toString());
        hql.append(".chromosome != '0' ");
        hql.append(orderBy);
        hql.insert(0, "select distinct " + Species.ZEBRAFISH.toString() + " ");

        Query query = session.createQuery(hql.toString());
        query.setFirstResult(criteria.getFirstRow() - 1);
        query.setMaxResults(criteria.getMaxDisplayRows());
        List<ZebrafishOrthologyHelper> results = query.list();

        // filter out duplicates via hashset.
        // awkward but Hibernate does not do it for you here.
        List<ZebrafishOrthologyHelper> distinctResult = new ArrayList<ZebrafishOrthologyHelper>();
        for (ZebrafishOrthologyHelper helper : results) {
            if (!distinctResult.contains(helper))
                distinctResult.add(helper);
        }

        for (ZebrafishOrthologyHelper zebrafishOrthologue : distinctResult) {
            Orthologs currentOrthologs = new Orthologs();
            currentOrthologs.setGeneSymbol(zebrafishOrthologue.getSymbol());
            List<OrthologySpecies> orthologySpecies = new ArrayList<OrthologySpecies>();
            currentOrthologs.setOrthologSpecies(orthologySpecies);

            // ToDo: Do the sorting in the database query
            Set<OrthologyHelper> orthologySet = zebrafishOrthologue.getOrthologies();
            List<OrthologyHelper> orthologyList = new ArrayList<OrthologyHelper>(orthologySet);
            Collections.sort(orthologyList, new OrthologyComparator());
            for (OrthologyHelper currentHelper : orthologyList) {
                OrthologySpecies currentOrthologySpecies = new OrthologySpecies();

                // if the species is not part of the criteria skip it.
                // Todo: Find a way to not even retrieve the species
                if (!requestedSpecies(currentHelper.getSpecies(), speciesCriteria))
                    continue;

                if (currentHelper.getSpecies().equals(Species.ZEBRAFISH.toString())) {
                    currentOrthologySpecies.setSpecies(Species.ZEBRAFISH);
                    MarkerRepository mr = RepositoryFactory.getMarkerRepository();
                    Marker marker = mr.getMarkerByID(currentHelper.getZdbID());
                    currentOrthologySpecies.setMarker(marker);
                } else if (currentHelper.getSpecies().equals(Species.HUMAN.toString())) {
                    currentOrthologySpecies.setSpecies(Species.HUMAN);
                } else if (currentHelper.getSpecies().equals(Species.MOUSE.toString())) {
                    currentOrthologySpecies.setSpecies(Species.MOUSE);
                } else if (currentHelper.getSpecies().equals(Species.FRUIT_FLY.toString())) {
                    currentOrthologySpecies.setSpecies(Species.FRUIT_FLY);
                } else if (currentHelper.getSpecies().equals(Species.YEAST.toString())) {
                    currentOrthologySpecies.setSpecies(Species.YEAST);
                }

                List<OrthologyItem> currentOrthItems = new ArrayList<OrthologyItem>();
                currentOrthologySpecies.setItems(currentOrthItems);
                OrthologyItem currentOrthItem = new OrthologyItem();
                currentOrthItems.add(currentOrthItem);
                currentOrthItem.setSymbol(currentHelper.getSymbol());
                List<Chromosome> chromList = new ArrayList<Chromosome>();
                Chromosome currentChrom = new Chromosome();
                currentChrom.setNumber(currentHelper.getChromosome());
                Position currentPos = new Position();
                currentPos.setPosition(currentHelper.getPosition());
                currentChrom.setPosition(currentPos);
                chromList.add(currentChrom);
                currentOrthItem.setChromosomes(chromList);

                if (!currentHelper.getSpecies().equals(Species.ZEBRAFISH.toString())) {
                    retrieveAccessionLinks(currentHelper, currentOrthItem);
                }

                currentOrthologs.getOrthologSpecies().add(currentOrthologySpecies);
                currentOrthologs.getDistinctCodes();
            }
            orthologies.add(currentOrthologs);
        }

        return repositoryResults;
    }

    private boolean requestedSpecies(String species, List<SpeciesCriteria> speciesCriteria) {
        if (speciesCriteria == null)
            return false;
        for (SpeciesCriteria criteria : speciesCriteria) {
            if (criteria.getName().equals(species))
                return true;
        }
        return false;
    }

    private Object[] getOrthologiesOr(List<SpeciesCriteria> speciesCriteria, ZfinCriteria criteria) {
//        StringBuilder fullSql = new StringBuilder("select zebra from ZebrafishOrthologyHelper zebra left join zebra.orthologies human where zebra.species = 'Zebrafish' and zebra.geneID = 'ZDB-GENE-000112-47' and human.species = 'Human'");
        StringBuilder hqlCount = new StringBuilder("select count( distinct " + Species.ZEBRAFISH.toString() + ") ");
        StringBuilder hqlRecords = new StringBuilder("select distinct " + Species.ZEBRAFISH.toString());
        StringBuilder hql = new StringBuilder(" from ZebrafishOrthologyHelper " + Species.ZEBRAFISH.toString());
        hql.append(" WHERE ");
        hql.append(Species.ZEBRAFISH.toString());
        hql.append(".species = :species");
        // ToDo: Get wrong records: with a number 0 and the true number.
        // need to fix the view
        hql.append(" AND " + Species.ZEBRAFISH.toString());
        hql.append(".chromosome != '0'");

        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            createGeneSymbolWhereClause(currentSpecies, hql, criteria.isOrRelationship());
            createCromosomeWhereClause(currentSpecies, hql, criteria);
        }
        Session session = HibernateUtil.currentSession();
        // count number of records
        hqlCount.append(hql);
        Query query = session.createQuery(hqlCount.toString());
        query.setString("species", Species.ZEBRAFISH.toString());
        Number number = (Number) query.uniqueResult();

        // retrieve records
        hqlRecords.append(hql);
        // order by gene symbol
        String orderByClause = criteria.getOrderByClause();
        hqlRecords.append(orderByClause);

        query = session.createQuery(hqlRecords.toString());
        query.setString("species", Species.ZEBRAFISH.toString());
        query.setFirstResult(criteria.getFirstRow() - 1);
        query.setMaxResults(criteria.getMaxDisplayRows());
        List<ZebrafishOrthologyHelper> results = query.list();
/*
        // filter out duplicates via hashset.
        // awkward but Hibernate does not do it for you here.
        HashSet<ZebrafishOrthologyHelper> resultSet = new HashSet<ZebrafishOrthologyHelper>(results);
        List<ZebrafishOrthologyHelper> distinctResult = new ArrayList<ZebrafishOrthologyHelper>(resultSet);
*/

        List<Orthologs> orthologies = createOrthologs(results);

        Object[] repositoryResults = new Object[2];
        repositoryResults[1] = number;
        repositoryResults[0] = orthologies;
        return repositoryResults;
    }

    private List<Orthologs> createOrthologs(List<ZebrafishOrthologyHelper> results) {
        List<Orthologs> orthologies = new ArrayList<Orthologs>();
        for (ZebrafishOrthologyHelper zebrafishOrthologue : results) {
            Orthologs currentOrthologs = new Orthologs();
            currentOrthologs.setGeneSymbol(zebrafishOrthologue.getSymbol());
            List<OrthologySpecies> orthologySpecies = new ArrayList<OrthologySpecies>();
            currentOrthologs.setOrthologSpecies(orthologySpecies);

            // ToDo: Do the sorting in the database query
            Set<OrthologyHelper> orthologySet = zebrafishOrthologue.getOrthologies();
            List<OrthologyHelper> orthologyList = new ArrayList<OrthologyHelper>(orthologySet);
            Collections.sort(orthologyList, new OrthologyComparator());
            for (OrthologyHelper currentHelper : orthologyList) {
                OrthologySpecies currentOrthologySpecies = new OrthologySpecies();

                if (currentHelper.getSpecies().equals(Species.ZEBRAFISH.toString())) {
                    currentOrthologySpecies.setSpecies(Species.ZEBRAFISH);
                    MarkerRepository mr = RepositoryFactory.getMarkerRepository();
                    Marker marker = mr.getMarkerByID(currentHelper.getZdbID());
                    currentOrthologySpecies.setMarker(marker);
                } else if (currentHelper.getSpecies().equals(Species.HUMAN.toString())) {
                    currentOrthologySpecies.setSpecies(Species.HUMAN);
                } else if (currentHelper.getSpecies().equals(Species.MOUSE.toString())) {
                    currentOrthologySpecies.setSpecies(Species.MOUSE);
                } else if (currentHelper.getSpecies().equals(Species.FRUIT_FLY.toString())) {
                    currentOrthologySpecies.setSpecies(Species.FRUIT_FLY);
                } else if (currentHelper.getSpecies().equals(Species.YEAST.toString())) {
                    currentOrthologySpecies.setSpecies(Species.YEAST);
                }

                List<OrthologyItem> currentOrthItems = new ArrayList<OrthologyItem>();
                currentOrthologySpecies.setItems(currentOrthItems);
                OrthologyItem currentOrthItem = new OrthologyItem();
                currentOrthItems.add(currentOrthItem);
                currentOrthItem.setSymbol(currentHelper.getSymbol());
                List<Chromosome> chromList = new ArrayList<Chromosome>();
                Chromosome currentChrom = new Chromosome();
                currentChrom.setNumber(currentHelper.getChromosome());
                Position currentPos = new Position();
                currentPos.setPosition(currentHelper.getPosition());
                currentChrom.setPosition(currentPos);
                chromList.add(currentChrom);
                currentOrthItem.setChromosomes(chromList);

                if (!currentHelper.getSpecies().equals(Species.ZEBRAFISH.toString())) {
                    retrieveAccessionLinks(currentHelper, currentOrthItem);
                }

                currentOrthologs.getOrthologSpecies().add(currentOrthologySpecies);
                currentOrthologs.getDistinctCodes();
            }
            orthologies.add(currentOrthologs);
        }
        return orthologies;
    }

    private Object[] getOrthologiesInOrClause(List<SpeciesCriteria> speciesCriteria, ZfinCriteria criteria) {
        Object[] repositoryResults = new Object[2];
        List<Orthologs> orthologies = new ArrayList<Orthologs>();
        repositoryResults[0] = orthologies;
        Session session = HibernateUtil.currentSession();
        StringBuilder fullSql = new StringBuilder();
        fullSql.append("select {Zebrafish.*}, {Human.*}, {Mouse.*}, {Fly.*} from ");
        StringBuilder sqlCount = new StringBuilder();
        sqlCount.append("select count(Zebrafish.vorthy_gene_zdb_id) as count from ");

        StringBuilder sql = new StringBuilder();
        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            if (!currentSpecies.getName().equals(Species.ZEBRAFISH.toString())) {
                sql.append(" OUTER ");
                sql.append(" Orthology_view ");
            } else
                sql.append(" Orthology_view ");
            sql.append(currentSpecies.getName());
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);

        sql.append(" WHERE ");
        getSpeciesJoinWhereClause(speciesCriteria, sql, criteria.isOrRelationship());

        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            String species = currentSpecies.getName();
            sql.append(AND);
            sql.append(species);
            sql.append(".vorthy_species = '");
            sql.append(species);
            sql.append("'");

            createGeneSymbolWhereClause(currentSpecies, sql, criteria.isOrRelationship());
            createCromosomeWhereClause(currentSpecies, sql, criteria);
            createPositionWhereClause(currentSpecies, sql);
        }

        fullSql.append(sql);
        sqlCount.append(sql);

        if (criteria.getFirstRow() == 1) {
            SQLQuery countQuery = session.createSQLQuery(sqlCount.toString());
            countQuery.addScalar("count", StandardBasicTypes.INTEGER);
            repositoryResults[1] = countQuery.uniqueResult();
        }


        String orderByClause = criteria.getOrderByClause();
        if (criteria.isOrRelationship())
            orderByClause = StringUtils.replace(orderByClause, "symbol", "vorthy_gene_abbrev");
        fullSql.append(orderByClause);

        SQLQuery query = session.createSQLQuery(fullSql.toString());
        query.addEntity("Zebrafish", OrthologyHelper.class);
        query.addEntity("Human", OrthologyHelper.class);
        query.addEntity("Mouse", OrthologyHelper.class);
        query.addEntity("Fly", OrthologyHelper.class);
        query.setFirstResult(criteria.getFirstRow() - 1);
        query.setMaxResults(criteria.getMaxDisplayRows());
        List<Object[]> results = query.list();
        for (Object[] currentOrthologyArray : results) {
            Orthologs currentOrthologs = new Orthologs();
            OrthologyHelper zebrafishHelper = (OrthologyHelper) currentOrthologyArray[0];
            currentOrthologs.setGeneSymbol(zebrafishHelper.getSymbol());
            List<OrthologySpecies> orthologySpecies = new ArrayList<OrthologySpecies>();
            currentOrthologs.setOrthologSpecies(orthologySpecies);

            for (Object aCurrentOrthologyArray : currentOrthologyArray) {
                OrthologyHelper currentHelper = (OrthologyHelper) aCurrentOrthologyArray;
                if (currentHelper == null)
                    continue;

                OrthologySpecies currentOrthologySpecies = new OrthologySpecies();

                if (currentHelper.getSpecies().equals(Species.ZEBRAFISH.toString())) {
                    currentOrthologySpecies.setSpecies(Species.ZEBRAFISH);
                    MarkerRepository mr = RepositoryFactory.getMarkerRepository();
                    Marker marker = mr.getMarkerByID(currentHelper.getZdbID());
                    currentOrthologySpecies.setMarker(marker);
                } else if (currentHelper.getSpecies().equals(Species.HUMAN.toString())) {
                    currentOrthologySpecies.setSpecies(Species.HUMAN);
                } else if (currentHelper.getSpecies().equals(Species.MOUSE.toString())) {
                    currentOrthologySpecies.setSpecies(Species.MOUSE);
                } else if (currentHelper.getSpecies().equals(Species.FRUIT_FLY.toString())) {
                    currentOrthologySpecies.setSpecies(Species.FRUIT_FLY);
                } else if (currentHelper.getSpecies().equals(Species.YEAST.toString())) {
                    currentOrthologySpecies.setSpecies(Species.YEAST);
                }

                List<OrthologyItem> currentOrthItems = new ArrayList<OrthologyItem>();
                currentOrthologySpecies.setItems(currentOrthItems);
                OrthologyItem currentOrthItem = new OrthologyItem();
                currentOrthItems.add(currentOrthItem);
                currentOrthItem.setSymbol(currentHelper.getSymbol());
                List<Chromosome> chromList = new ArrayList<Chromosome>();
                Chromosome currentChrom = new Chromosome();
                currentChrom.setNumber(currentHelper.getChromosome());
                Position currentPos = new Position();
                currentPos.setPosition(currentHelper.getPosition());
                currentChrom.setPosition(currentPos);
                chromList.add(currentChrom);
                currentOrthItem.setChromosomes(chromList);

                if (!currentHelper.getSpecies().equals(Species.ZEBRAFISH.toString())) {
                    retrieveAccessionLinks(currentHelper, currentOrthItem);
                }

                currentOrthologs.getOrthologSpecies().add(currentOrthologySpecies);
                currentOrthologs.getDistinctCodes();
            }
            orthologies.add(currentOrthologs);
        }

        return repositoryResults;
    }


    private void retrieveAccessionLinks(OrthologyHelper currentHelper, OrthologyItem currentOrthItem) {
        Session session = HibernateUtil.currentSession();
        Criteria accessionCriteriaDBLink = session.createCriteria(AccessionHelperDBLink.class);
        accessionCriteriaDBLink.add(Restrictions.eq("orthoID", currentHelper.getZdbID()));
        Criteria accessionCriteriaFDBCont = accessionCriteriaDBLink.createCriteria("fdbContHelper");
        accessionCriteriaFDBCont.add(Restrictions.eq("species", currentHelper.getSpecies()));
        Criteria accessionCriteriaFDB = accessionCriteriaFDBCont.createCriteria("FDBhelper");
        accessionCriteriaFDB.add(Restrictions.lt("significance", 10));
        List<AccessionHelperDBLink> accessionResults = accessionCriteriaDBLink.list();

        List<AccessionItem> accessionItems = populuateAccessionLinks(accessionResults);

        currentOrthItem.setAccessionItems(accessionItems);
    }

    private List<AccessionItem> populuateAccessionLinks(List<AccessionHelperDBLink> accessionResults) {
        List<AccessionItem> accessionItems = new ArrayList<AccessionItem>();

        for (AccessionHelperDBLink currentAccessionHelper : accessionResults) {
            AccessionItem currentItem = new AccessionItem();
            currentItem.setNumber(currentAccessionHelper.getAccessionNum());
            currentItem.setName(currentAccessionHelper.getFdbContHelper().getFDBhelper().getFDBname());
            String url = currentAccessionHelper.getFdbContHelper().getFDBhelper().getHyperLinkQuery() + currentAccessionHelper.getAccessionHyperLinkNum();
            currentItem.setUrl(url);
            accessionItems.add(currentItem);
        }
        return accessionItems;
    }

    /**
     * Create a where clause for the position information.
     * We do not search by ZF position since the position is not well defined in our database
     *
     * @param currentSpecies
     * @param hql
     */
    public void createPositionWhereClause(SpeciesCriteria currentSpecies, StringBuilder hql) {
        String species = currentSpecies.getName();
        PositionCriteria position = currentSpecies.getPosition();
        if (position == null)
            return;

        // Exclude ZF
        if (species.equals(Species.ZEBRAFISH.toString()))
            return;

        hql.append(AND);
        hql.append(species);
        hql.append(".position");
        if (!species.equals(Species.HUMAN.toString())) {
            if (position.getType().equals(FilterType.EQUALS)) {
                hql.append(" = '");
                hql.append(position.getPosition());
                hql.append("'");
            } else if (position.getType().equals(FilterType.BEGINS)) {
                hql.append(" like '");
                hql.append(position.getPosition());
                hql.append("%'");
            } else if (position.getType().equals(FilterType.RANGE)) {
                // since position is a double, but stored in the database as string,
                // this is harder than it would be
            }
        } else {
            if (position.getType().equals(FilterType.EQUALS)) {
                hql.append(" = '");
                hql.append(position.getHumanPosCharacter());
                hql.append(position.getPosition());
                hql.append("'");
            } else if (position.getType().equals(FilterType.BEGINS)) {
                hql.append(" = '");
                hql.append(position.getHumanPosCharacter());
                hql.append(position.getPosition());
                hql.append("%'");
            } else if (position.getType().equals(FilterType.RANGE)) {
                //
            }
        }
    }

    public void createCromosomeWhereClause(SpeciesCriteria currentSpecies, StringBuilder hql, ZfinCriteria criteria) {
        ChromosomeCriteria chromosome = currentSpecies.getChromosome();
        if (chromosome == null || !chromosome.hasChromosomeNames())
            return;

        hql.append(AND);
        hql.append(currentSpecies.getName());
        hql.append(".chromosome ");
        if (chromosome.getType().equals(FilterType.EQUALS) && chromosome.hasExactlyOneChromosomeName()) {
            hql.append(EQUAL_SIGN);
            hql.append("'");
            hql.append(chromosome.getChromosomesNames().get(0));
            hql.append("'");
        } else if (chromosome.getType().equals(FilterType.LIST)) {
            hql.append(IN);
            hql.append(OPEN_BRACKET);
            List<String> chromList = chromosome.getChromosomesNames();
            for (String currentChrom : chromList) {
                hql.append("'");
                hql.append(currentChrom);
                hql.append("',");
            }
            hql.deleteCharAt(hql.length() - 1);
            hql.append(CLOSE_BRACKET);
        } else if (chromosome.getType().equals(FilterType.RANGE)) {
            hql.append(IN);
            hql.append(OPEN_BRACKET);
            for (int i = chromosome.getMin(); i <= chromosome.getMax(); i++) {
                hql.append("'");
                hql.append(i);
                hql.append("',");
            }
            hql.deleteCharAt(hql.length() - 1);
            hql.append(CLOSE_BRACKET);
        }
    }

    /**
     * Create the where clauses related to a species gene symbol criteria.
     *
     * @param currentSpecies
     * @param hql
     */
    public void createGeneSymbolWhereClause(SpeciesCriteria currentSpecies, StringBuilder hql, boolean isOrRelation) {
        String species = currentSpecies.getName();
        GeneSymbolCriteria symbol = currentSpecies.getSymbol();
        if (symbol == null)
            return;

        hql.append(" AND upper");
        hql.append(OPEN_BRACKET);
        hql.append(species);
        hql.append(getSymbolColumn(isOrRelation));
        hql.append(CLOSE_BRACKET);
        hql.append(" ");
        if (symbol.getType().equals(FilterType.EQUALS)) {
            hql.append("= '");
            hql.append(symbol.getSymbol().toUpperCase());
            hql.append("'");
        } else if (symbol.getType().equals(FilterType.BEGINS)) {
            hql.append(LIKE);
            hql.append(" '");
            hql.append(symbol.getSymbol().toUpperCase());
            hql.append("%'");
        } else if (symbol.getType().equals(FilterType.ENDS)) {
            hql.append(LIKE);
            hql.append(" '%");
            hql.append(symbol.getSymbol().toUpperCase());
            hql.append("'");
        } else {
            hql.append(LIKE);
            hql.append(" '%");
            hql.append(symbol.getSymbol().toUpperCase());
            hql.append("%'");
        }
    }

    /**
     * This method creates the join clause for all species via their geneID.
     * You need at least two species to have a join.
     *
     * @param speciesCriteria
     * @param hql
     */
    public void getSpeciesJoinWhereClause(List<SpeciesCriteria> speciesCriteria, StringBuilder hql, boolean isOrRelationhip) {
        String lastSpecies = null;
        boolean firstPass = true;
        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            if (lastSpecies == null) {
                lastSpecies = currentSpecies.getName();
            } else {
                if (firstPass) {
                    firstPass = false;
                } else {
                    hql.append(AND);
                }
                hql.append(lastSpecies);
                if (isOrRelationhip)
                    hql.append(".vorthy_gene_zdb_id");
                else
                    hql.append(".geneID");
                hql.append("=");
                hql.append(currentSpecies.getName());
                if (isOrRelationhip)
                    hql.append(".vorthy_gene_zdb_id");
                else
                    hql.append(".geneID");
            }
        }

    }

    public void getSpeciesOuterJoinWhereClause(List<SpeciesCriteria> speciesCriteria, StringBuilder hql) {

        boolean firstPass = true;
        for (SpeciesCriteria currentSpecies : speciesCriteria) {
            if (firstPass) {
                firstPass = false;
            } else {
                hql.append(AND);
            }
            hql.append(currentSpecies.getName());
            hql.append(".species");
            hql.append("= '");
            hql.append(currentSpecies.getName());
            hql.append("'");
        }
    }

    private String getSymbolColumn(boolean isOrRelationship) {
        return ".symbol";
    }

    public void invalidateCachedObjects() {

    }

    /**
     * Save a new orthology including evidence codes.
     * In addition, a record attribution is created as well.
     *
     * @param ortholog    Ortholog object
     * @param publication Publication object
     */
    public void saveOrthology(Ortholog ortholog, Publication publication) {

        currentSession().save(ortholog);

        Updates up = new Updates();
        up.setRecID(ortholog.getZebrafishGene().getZdbID());
        up.setFieldName("Ortholog");
        up.setNewValue(ortholog.getNcbiOtherSpeciesGene().getOrganism().getCommonName());
        up.setComments("Create new ortholog record.");
        up.setSubmitterID(getCurrentSecurityUser().getZdbID());
        up.setSubmitterName(getCurrentSecurityUser().getUsername());
        up.setWhenUpdated(new Date());
        currentSession().save(up);
        String orthologyZdbID = ortholog.getZdbID();
        // create record attribution record if exists
        if (publication != null)
            getInfrastructureRepository().insertRecordAttribution(orthologyZdbID, publication.getZdbID());
    }

    private Person getCurrentSecurityUser() {
        Person currentSecurityUser = ProfileService.getCurrentSecurityUser();
        if (currentSecurityUser == null)
            throw new RuntimeException("No Authenticated User. Please log in first.");

        return currentSecurityUser;
    }

    public void updateFastSearchEvidenceCodes(Set<Ortholog> orthologs) {
        Set<OrthologyEvidenceFastSearch> fastSearches = OrthologyEvidenceService.getOrthoEvidenceFastSearches(orthologs);
        for (OrthologyEvidenceFastSearch fastSearch : fastSearches) {
            currentSession().save(fastSearch);
            String orthologyFastSearchZdbID = fastSearch.getZdbID();
            getInfrastructureRepository().
                    insertRecordAttribution(orthologyFastSearchZdbID, fastSearch.getPublication().getZdbID());
        }
    }

    @Override
    public List<OrthologyPresentationRow> getOrthologyForGene(Marker m) {
        return getOrthologyForGene(m, null);
    }

    public List<OrthologyPresentationRow> getOrthologyForGene(Marker m, Publication publication) {
        String sql = " select distinct o.organism ,o.ortho_abbrev,o.ortho_chromosome, " +
                "o.ortho_position,oe.oev_evidence_code , fdb.fdb_db_name,dbl.dblink_acc_num, " +
                "fdb.fdb_db_query,fdb.fdb_url_suffix " +
                "from orthologue o  " +
                "join orthologue_evidence oe on o.zdb_id=oe.oev_ortho_zdb_id " +
                "left outer join db_link dbl on o.zdb_id=dbl.dblink_linked_recid " +
                "left join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
                "left join foreign_db fdb on fdb.fdb_db_pk_id=fdbc.fdbcont_fdb_db_id " +
                "where o.c_gene_id=:markerZdbId ";
        if (publication != null)
            sql += " and oe.oev_pub_zdb_id = :pubID";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", m.getZdbID());
        if (publication != null)
            query.setString("pubID", publication.getZdbID());
        return query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                OrthologyPresentationRow row = new OrthologyPresentationRow();
                row.setSpecies(tuple[0].toString());
                row.setAbbreviation(tuple[1].toString());
                if (tuple[2] != null) {
                    row.setChromosome(tuple[2].toString());
                }
                if (tuple[3] != null) {
                    row.setPosition(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    row.addEvidenceCode(tuple[4].toString());
                }
                if (tuple[6] != null) {
                    row.addAccession(DBLinkPresentation.getGeneralHyperLink(
                            tuple[7].toString() + tuple[6]
                                    + (tuple[8] != null ? tuple[8].toString() : ""),   // url
                            tuple[5] + ":" + tuple[6])      // name
                    );
                }


                return row;
            }

            /**
             * Here we compact the list.
             * @param collection
             * @return
             */
            @Override
            public List transformList(List collection) {
                Map<String, OrthologyPresentationRow> condensed = new HashMap<String, OrthologyPresentationRow>();
                for (Iterator iter = collection.iterator(); iter.hasNext(); ) {
                    OrthologyPresentationRow row = (OrthologyPresentationRow) iter.next();
                    if (condensed.containsKey(row.getSpecies())) {
                        OrthologyPresentationRow rowInstance = condensed.get(row.getSpecies());
                        rowInstance.copyFrom(row);
                    } else {
                        condensed.put(row.getSpecies(), row);
                    }
                }

                return new ArrayList<OrthologyPresentationRow>(condensed.values());
            }
        })
                .list();
    }

    @Override
    public List<String> getEvidenceCodes(Marker gene) {
        return getEvidenceCodes(gene, null);
    }

    public List<OrthologySlimPresentation> getOrthologySlimForGeneId(String geneId) {
        Session session = HibernateUtil.currentSession();

        String hql = "select ortho.organism, ortho.abbreviation from Orthologue ortho " +
                "      where ortho.gene.zdbID = :geneID  " +
                "   order by ortho.organism ";

        return HibernateUtil.currentSession().createQuery(hql)
                .setString("geneID", geneId)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public OrthologySlimPresentation transformTuple(Object[] tuple, String[] aliases) {
                        OrthologySlimPresentation orthoSlim = new OrthologySlimPresentation();
                        orthoSlim.setOrganism(tuple[0].toString());
                        orthoSlim.setOrthologySymbol(tuple[1].toString());
                        return orthoSlim;
                    }

                })
                .list();
    }

    @Override
    public List<String> getEvidenceCodes(Marker gene, Publication publication) {

        String sql = " from Ortholog " +
                "where zebrafishGene = :gene ";

        Query query = HibernateUtil.currentSession().createQuery(sql)
                .setParameter("gene", gene);

        List<Ortholog> orthologs = (List<Ortholog>) query.list();

        Set<EvidenceCode> evidenceCodes = new HashSet<>();
        for (Ortholog ortholog : orthologs) {
            for (OrthologEvidence evidence : ortholog.getEvidenceSet()) {
                if (publication == null || evidence.getPublication().equals(publication)) {
                    evidenceCodes.add(evidence.getEvidenceCode());
                }
            }
        }
        List<String> evidenceCodeNames = new ArrayList<>();
        for (EvidenceCode code : evidenceCodes)
            evidenceCodeNames.add(code.getCode());
        return evidenceCodeNames;

    }

    @Override
    public List<Ortholog> getOrthologs(String zdbID) {
        String sql = "from Ortholog " +
                "where ";

        return null;
    }

    @Override
    public List<Ortholog> getOrthologs(Marker gene) {
        String sql = "from Ortholog " +
                "where zebrafishGene = :gene";
        Query query = HibernateUtil.currentSession().createQuery(sql);
        query.setParameter("gene", gene);
        return (List<Ortholog>) query.list();
    }

    @Override
    public NcbiOtherSpeciesGene getNcbiGene(String ncbiID) {
        return (NcbiOtherSpeciesGene) HibernateUtil.currentSession().get(NcbiOtherSpeciesGene.class, ncbiID);
    }

    @Override
    public EvidenceCode getEvidenceCode(String name) {
        return (EvidenceCode) HibernateUtil.currentSession().get(EvidenceCode.class, name);
    }

    @Override
    public Ortholog getOrtholog(String orthID) {
        return (Ortholog) HibernateUtil.currentSession().get(Ortholog.class, orthID);
    }

    @Override
    public void deleteOrtholog(Ortholog ortholog) {
        Person currentSecurityUser = getCurrentSecurityUser();
        if (currentSecurityUser == null)
            throw new RuntimeException("No Authenticated User. Please log in first.");

        Session session = HibernateUtil.currentSession();
        session.delete(ortholog);
        int numOfRecords = getInfrastructureRepository().deleteRecordAttributionsForData(ortholog.getZdbID());
        Updates up = new Updates();
        up.setRecID(ortholog.getZdbID());
        up.setFieldName("Ortholog");
        up.setOldValue(ortholog.getZdbID());
        up.setComments("Delete Ortholog");
        up.setWhenUpdated(new Date());
        up.setSubmitterID(currentSecurityUser.getZdbID());
        up.setSubmitterName(currentSecurityUser.getUsername());
        session.save(up);
    }

    @Override
    public Ortholog getOrthologByGeneAndNcbi(Marker gene, NcbiOtherSpeciesGene ncbiGene) {
        String hql = "from Ortholog where " +
                "zebrafishGene = :zebrafishGene and ncbiOtherSpeciesGene = :otherSpeciesGene ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("zebrafishGene", gene);
        query.setParameter("otherSpeciesGene", ncbiGene);
        return (Ortholog) query.uniqueResult();
    }

}
