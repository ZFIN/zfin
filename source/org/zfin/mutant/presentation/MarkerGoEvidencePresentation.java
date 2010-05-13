package org.zfin.mutant.presentation;

import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GenotypePresentation;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;

/**
 */
public class MarkerGoEvidencePresentation {
    public static ReferenceDatabase genbankReferenceDatabase;
    public static ReferenceDatabase genpeptReferenceDatabase;
    public static ReferenceDatabase refseqReferenceDatabase;
    public static ReferenceDatabase uniprotReferenceDatabase;
    public static ReferenceDatabase spkwReferenceDatabase;
    public static ReferenceDatabase ecReferenceDatabase;
    public static ReferenceDatabase interproReferenceDatabase;
    public static ReferenceDatabase goReferenceDatabase;

    public static ReferenceDatabase getGenbankReferenceDatabase() {
        if (genbankReferenceDatabase == null) {
            genbankReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
        }
        return genbankReferenceDatabase;
    }

    public static ReferenceDatabase getGenpeptReferenceDatabase() {
        if (genpeptReferenceDatabase == null) {
            genpeptReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENPEPT,
                    ForeignDBDataType.DataType.POLYPEPTIDE,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
        }
        return genpeptReferenceDatabase;
    }

    public static ReferenceDatabase getRefseqReferenceDatabase() {
        if (refseqReferenceDatabase == null) {
            refseqReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.REFSEQ,
                    ForeignDBDataType.DataType.RNA,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
        }
        return refseqReferenceDatabase;
    }

    public static ReferenceDatabase getUniprotReferenceDatabase() {
        if (uniprotReferenceDatabase == null) {
            uniprotReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.UNIPROTKB,
                    ForeignDBDataType.DataType.POLYPEPTIDE,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
        }
        return uniprotReferenceDatabase;
    }

    public static ReferenceDatabase getSpkwReferenceDatabase() {
        if (spkwReferenceDatabase == null) {
            spkwReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.SP_KW,
                    ForeignDBDataType.DataType.GENE_ONTOLOGY,
                    ForeignDBDataType.SuperType.INFERENCE,
                    Species.ZEBRAFISH);
        }
        return spkwReferenceDatabase;
    }

    public static ReferenceDatabase getEcReferenceDatabase() {
        if (ecReferenceDatabase == null) {
            ecReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.EC,
                    ForeignDBDataType.DataType.GENE_ONTOLOGY,
                    ForeignDBDataType.SuperType.INFERENCE,
                    Species.ZEBRAFISH);
        }
        return ecReferenceDatabase;
    }

    public static ReferenceDatabase getInterproReferenceDatabase() {
        if (interproReferenceDatabase == null) {
            interproReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.INTERPRO,
                    ForeignDBDataType.DataType.GENE_ONTOLOGY,
                    ForeignDBDataType.SuperType.INFERENCE,
                    Species.ZEBRAFISH);
        }
        return interproReferenceDatabase;
    }

    public static ReferenceDatabase getGoReferenceDatabase() {
        if (goReferenceDatabase == null) {
            goReferenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.QUICKGO,
                    ForeignDBDataType.DataType.GENE_ONTOLOGY,
                    ForeignDBDataType.SuperType.ONTOLOGY,
                    Species.ZEBRAFISH);
        }
        return goReferenceDatabase;
    }

    /**
     * From gocuration.apg
     *
     * @param inferredFrom Inference to display ;
     * @return Link to be displayed.
     */
    public static String generateInferenceLink(String inferredFrom) {
        InferenceCategory inferenceCategory = InferenceCategory.getInferenceCategoryByValue(inferredFrom);
        String accession = inferredFrom.substring(inferenceCategory.prefix().length());
        switch (inferenceCategory) {
            case ZFIN_MRPH_GENO:
            case ZFIN_GENE:
                if (accession.startsWith("ZDB-MRPHLNO-") || accession.startsWith("ZDB-GENE-")) {
                    Marker morpholino = (Marker) HibernateUtil.currentSession().get(Marker.class, accession);
                    return MarkerPresentation.getLink(morpholino);
                } else if (accession.startsWith("ZDB-GENO-")) {
                    Genotype genotype = (Genotype) HibernateUtil.currentSession().get(Genotype.class, accession);
                    return GenotypePresentation.getLink(genotype);
                } else {
                    return inferredFrom;
                }
            case UNIPROTKB:
                return createLink(accession, getUniprotReferenceDatabase().getForeignDB(), inferenceCategory);
            case GO:
                return createGOLink(accession, getGoReferenceDatabase().getForeignDB(), inferenceCategory);
            case GENBANK:
                return createLink(accession, getGenbankReferenceDatabase().getForeignDB(), inferenceCategory);
            case GENPEPT:
                return createLink(accession, getGenpeptReferenceDatabase().getForeignDB(), inferenceCategory);
            case REFSEQ:
                return createLink(accession, getRefseqReferenceDatabase().getForeignDB(), inferenceCategory);
            case SP_KW:
                return createLink(accession, getSpkwReferenceDatabase().getForeignDB(), inferenceCategory);
            case INTERPRO:
                return createLink(accession, getInterproReferenceDatabase().getForeignDB(), inferenceCategory);
            case EC:
                return createLink(accession, getEcReferenceDatabase().getForeignDB(), inferenceCategory);
            default:
                return inferredFrom;
        }

    }

    public static String createGOLink(String accession, ForeignDB foreignDB, InferenceCategory inferenceCategory) {
        StringBuilder sb = new StringBuilder("");
        sb.append("<a href=\"");

        sb.append(foreignDB.getDbUrlPrefix());
        sb.append(accession);
        if (foreignDB.getDbUrlSuffix() != null) {
            sb.append(foreignDB.getDbUrlSuffix());
        }
        sb.append("\">");
        GenericTerm goTerm =
                RepositoryFactory.getOntologyRepository()
                        .getTermByOboID(inferenceCategory.prefix() + accession);
        if (goTerm != null) {
            sb.append(goTerm.getTermName());
        } else {
            sb.append(inferenceCategory.prefix());
            sb.append(accession);
        }
        sb.append("</a>");
        return sb.toString();
    }

    public static String createLink(String accession, ForeignDB foreignDB, InferenceCategory inferenceCategory) {
        StringBuilder sb = new StringBuilder("");
        sb.append("<a href=\"");

        sb.append(foreignDB.getDbUrlPrefix());
        sb.append(accession);
        if (foreignDB.getDbUrlSuffix() != null) {
            sb.append(foreignDB.getDbUrlSuffix());
        }
        sb.append("\">");

        sb.append(inferenceCategory.prefix());
        sb.append(accession);
        sb.append("</a>");
        return sb.toString();
    }
}
