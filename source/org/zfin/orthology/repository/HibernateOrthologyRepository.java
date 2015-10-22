package org.zfin.orthology.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.BasicTransformerAdapter;
import org.zfin.criteria.ZfinCriteria;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.orthology.*;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
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
        if (publication != null) {
            getInfrastructureRepository().insertRecordAttribution(orthologyZdbID, publication.getZdbID());
        }
        currentSession().flush();
    }

    private Person getCurrentSecurityUser() {
        Person currentSecurityUser = ProfileService.getCurrentSecurityUser();
        if (currentSecurityUser == null) {
            throw new RuntimeException("No Authenticated User. Please log in first.");
        }

        return currentSecurityUser;
    }

    @Override
    public List<String> getEvidenceCodes(Marker gene) {
        return getEvidenceCodes(gene, null);
    }

    public List<OrthologySlimPresentation> getOrthologySlimForGeneId(String geneId) {
        Session session = HibernateUtil.currentSession();

        String hql = "select ortho.organism.commonName, ortho.symbol from Ortholog ortho " +
                "      where ortho.zebrafishGene.zdbID = :geneID  " +
                "   order by ortho.organism.commonName ";

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
        for (EvidenceCode code : evidenceCodes) {
            evidenceCodeNames.add(code.getCode());
        }
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
                "where zebrafishGene = :gene " +
                "order by ncbiOtherSpeciesGene.organism.displayOrder, symbol";
        Query query = HibernateUtil.currentSession().createQuery(sql);
        query.setParameter("gene", gene);
        return (List<Ortholog>) query.list();
    }

    @Override
    public NcbiOtherSpeciesGene getNcbiGene(String ncbiID) {
        return (NcbiOtherSpeciesGene) HibernateUtil.currentSession().get(NcbiOtherSpeciesGene.class, ncbiID);
    }

    @Override
    public List<EvidenceCode> getEvidenceCodes() {
        return HibernateUtil.currentSession().createCriteria(EvidenceCode.class).list();
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

    @Override
    public List<NcbiOrthoExternalReference> getNcbiExternalReferenceList(String ncbiID) {
        String hql = "from NcbiOrthoExternalReference where " +
                "ncbiOtherSpeciesGene.ID = :accessionNumber  ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("accessionNumber", ncbiID);
        return (List<NcbiOrthoExternalReference>) query.list();
    }

}
