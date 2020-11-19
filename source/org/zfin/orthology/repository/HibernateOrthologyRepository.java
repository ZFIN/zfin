package org.zfin.orthology.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.BasicTransformerAdapter;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.orthology.*;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * This class creates the calls to Hibernate to retrieve the Orthology information.
 */
public class HibernateOrthologyRepository implements OrthologyRepository {

    public static final String AND = " AND ";
    public static final String LIKE = "like";
    public static final String IN = "in ";

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
        // need to be root user for this.
        if (!isRootUser()) {
            throw new RuntimeException("No Authenticated User. Please log in first.");
        }

        currentSession().save(ortholog);

        Updates up = new Updates();
        up.setRecID(ortholog.getZebrafishGene().getZdbID());
        up.setFieldName("Ortholog");
        up.setNewValue(ortholog.getNcbiOtherSpeciesGene().getOrganism().getCommonName());
        up.setComments("Create new ortholog record.");
        up.setSubmitter(getCurrentSecurityUser());
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

    private boolean isRootUser() {
        Person currentSecurityUser = ProfileService.getCurrentSecurityUser();
        return currentSecurityUser != null;
    }

    @Override
    public List<String> getEvidenceCodes(Marker gene) {
        return getEvidenceCodes(gene, null);
    }

    public List<OrthologySlimPresentation> getOrthologySlimForGeneId(String geneId) {
        Session session = HibernateUtil.currentSession();

        String sql = "select organism_common_name, ortho_other_species_symbol, oev_evidence_code, oev_pub_zdb_id from ortholog, organism, ortholog_evidence " +
                "      where ortho_zebrafish_gene_zdb_id = :geneID  " +
                "        and organism_taxid = ortho_other_species_taxid  " +
                "        and  ortho_zdb_id = oev_ortho_zdb_id  " +
                "   order by organism_common_name, ortho_other_species_symbol, oev_evidence_code, oev_pub_zdb_id ";

        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("geneID", geneId)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public OrthologySlimPresentation transformTuple(Object[] tuple, String[] aliases) {
                        OrthologySlimPresentation orthoSlim = new OrthologySlimPresentation();
                        orthoSlim.setOrganism(tuple[0].toString());
                        orthoSlim.setOrthologySymbol(tuple[1].toString());
                        orthoSlim.setEvidenceCode(tuple[2].toString());
                        orthoSlim.setPublication(tuple[3].toString());
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

        // remove attributions for the zebrafish gene
        for (OrthologEvidence evidence : ortholog.getEvidenceSet()) {
            getInfrastructureRepository().deleteRecordAttribution(ortholog.getZebrafishGene().getZdbID(), evidence.getPublication().getZdbID());
        }

        Session session = HibernateUtil.currentSession();
        session.delete(ortholog);
        int numOfRecords = getInfrastructureRepository().deleteRecordAttributionsForData(ortholog.getZdbID());
        Updates up = new Updates();
        up.setRecID(ortholog.getZebrafishGene().getZdbID());
        up.setFieldName("Ortholog");
        up.setOldValue(ortholog.getZdbID());
        up.setComments("Delete Ortholog");
        up.setWhenUpdated(new Date());
        up.setSubmitter(currentSecurityUser);
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
