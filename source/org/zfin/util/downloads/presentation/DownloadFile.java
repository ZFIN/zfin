package org.zfin.util.downloads.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All download files, their names and file names.
 */
public enum DownloadFile {
    GENETIC_MARKER("Genetic Marker", "genetic_markers.txt", "Genetic Markers", "ZFIN ID,Symbol,Name,Marker Type"),
    PREVIOUS_ZFIN("Previous ZFIN IDS (includes former and current ZFIN IDs for merged data)", "zdb_history.txt", "Genetic Markers", "Former ZFIN ID,Current ZFIN ID"),
    ANTIBODIES("Antibodies", "antibodies.txt", "Antibodies", "ZFIN ID,Name,Clonal Type,Heavy Chain,Light Chain,Immunogen Organism,Host Organism"),
    ANTIBODY_EXPRESSIONS("Antibody Expressions in wild-type fish", "antibody_expressions.txt", "Antibodies", "ZFIN ID,Anatomy Structure ID,AnatomyStructure Name,Substructure Term ID,Substructure Term Name"),
    PREVIOUS_NAMES("Previous names", "aliases.txt", "Previous Names", "Current ZFIN ID,Current Name,Current Symbol,Previous Name"),
    MAPPING_DATA("Mapping Data from the 6 Zebrafish Mapping Panels", "mappings.txt", "Mapping Data", "ZFIN ID,Symbol,Panel Symbol,Chr,Location,Metric"),
    ZFIN_GENES_ENSEMBL("ZFIN Genes: Ensembl", "E_zfin_gene_alias.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    ZFIN_GENES_VEGA("ZFIN Genes: VEGA", "zfin_gene.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    TC_GENES_ENSEMBL("Transcript and Genes: Ensembl", "E_drerio_vega_transcript.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    TC_GENES_VEGA("Transcript and Genes: VEGA", "drerio_vega_transcript.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_EXPRESSION_ENSEMBL("ZFIN Genes with Expression: Ensembl", "E_expression.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_EXPRESSION_VEGA("ZFIN Genes with Expression: VEGA", "expression.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_PHENO_ENSEMBL("ZFIN Genes with Phenotype: Ensembl", "E_phenotype.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_PHENO_VEGA("ZFIN Genes with Phenotype: VEGA", "phenotype.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_ANTIBODY_ENSEMBL("ZFIN Genes with Antibodies: Ensembl", "E_antibody.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_ANTIBODY_VEGA("ZFIN Genes with Antibodies: VEGA", "antibody.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_MO_ENSEMBL("ZFIN Morpholinos: Ensembl", "E_zfin_morpholino.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_MO_VEGA("ZFIN Morpholinos: VEGA", "zfin_morpholino.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_Assembly_ENSEMBL("Assembly: Ensembl", "E_drerio_backbone.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_Assembly_VEGA("Assembly: VEGA", "vega_clone.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_COMPL_Assembly_ENSEMBL("Complete Assembly Clones: Ensembl", "E_full_zfin_clone.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_COMPL_Assembly_VEGA("Complete Assembly Clones: VEGA", "full_length_clones.gff3", "Sequence Coordinates in gff3 format", "Chromosome ,Source ,Type ,Start ,End ,Score ,Strand ,Phase ,Attributes"),
    GENES_CHROMOSOME_VEGA("Chromosome sequence-region definitions: VEGA", "vega_chromosome.gff3", "Sequence Coordinates in gff3 format", "Sequence-region ,Chromosome ,Start ,End"),
    MARKER_GENBANK_SEQUENCE("ZFIN Markers associations to GenBank sequence data", "genbank.txt", "Sequence Data", "ZFIN ID,Name,GenBank ID"),
    MARKER_REFDEQ_SEQUENCE("ZFIN Markers associations to RefSeq sequence data", "refseq.txt", "Sequence Data", "ZFIN ID,Symbol,RefSeq ID"),
    MARKER_GENE_SEQUENCE("ZFIN Markers associations to Gene sequence data", "gene.txt", "Sequence Data", "ZFIN ID,Symbol,Gene ID"),
    MARKER_UNIGENE_SEQUENCE("ZFIN Markers associations to UniGene sequence data", "unigene.txt", "Sequence Data", "ZFIN ID,Symbol,UniGene ID"),
    MARKER_UNIPROT_SEQUENCE("ZFIN Markers associations to UnitProt protein data", "uniprot.txt", "Sequence Data", "ZFIN ID,Symbol,UniProt ID"),
    MARKER_INTERPROT_SEQUENCE("ZFIN Markers associations to InterProt protein data", "interpro.txt", "Sequence Data", "ZFIN ID,Symbol,InterPro ID"),
    MARKER_GENEPEPT_SEQUENCE("ZFIN Marker associations to GenePept protein data", "genpept.txt", "Sequence Data", "ZFIN ID,Symbol,GenPept ID"),
    MARKER_VEGA_SEQUENCE("ZFIN Markers associations to Sanger Vega data", "vega.txt", "Sequence Data", "ZFIN ID,Symbol,Vega ID"),
    MARKER_ENSEMBL_SEQUENCE("ZFIN Markers associations to Ensembl IDs", "ensembl_1_to_1.txt", "Sequence Data", "ZFIN ID,Symbol,Ensembl ID"),
    MARKER_INDIRECT_SEQUENCE("ZFIN Gene IDs indirectly associated with Sequence Accessions via cDNA & EST", "gene_seq.txt", "Sequence Data", "ZFIN Gene ID,ZFIN Gene Symbol,Sequence Accession"),
    MARKER_INDIRECT_DIRECT_SEQUENCE("ZFIN Gene IDs indirectly and directly associated with Sequence Accessions via cDNA & EST", "all_rna_accessions.txt", "Sequence Data", "ZFIN Gene ID,ZFIN Gene Symbol,Sequence Accession"),
    TRANSCRIPTS_SEQUENCE("Transcripts", "transcripts.txt", "Sequence Data", "ZFIN ID,Name,Gene ZFIN ID,Clone ZFIN ID,Transcript type,Transcript status"),
    HUMAN_ORTHOLOGY("Human and Zebrafish Orthology", "human_orthos.txt", "Orthology Data", "ZFIN ID,ZFIN Symbol,ZFIN Name,Human Symbol,Human Name,OMIM ID,Gene ID"),
    MOUSE_ORTHOLOGY("Mouse and Zebrafish Orthology", "mouse_orthos.txt", "Orthology Data", "ZFIN ID,ZFIN Symbol,ZFIN Name,Mouse Symbol,Mouse Name,MGI ID,Gene ID"),
    FLY_ORTHOLOGY("Drosophila and Zebrafish Orthology", "fly_orthos.txt", "Orthology Data", "ZFIN ID,ZFIN Symbol,ZFIN Name,Fly Symbol,Fly Name,Flybase ID"),
    PHENOTYPIC_GENE_ORTHOLOGY("Phenotypic Zebrafish genes with Human Orthology", "ortho.txt", "Orthology Data", "ZFIN ID,ZFIN ID,ZFIN Symbol,Entrez Zebrafish Gene ID,Human Gene Symbol,Entrez Human Gene ID"),
    GENETIC_MARKER_RELATIONSHIP("Genetic Marker Relationships", "gene_marker_relationship.txt", "Genetic Marker Relationships", "Gene ID,Gene Symbol,Marker ID,Marker Symbol,Relationship"),
    GENE_ONTOLOGY_DATA("Gene Ontology Data", "ftp://ftp.geneontology.org/pub/go/gene-associations/gene_association.zfin.gz", "Gene Ontology (GO) Annotations of Zebrafish Markers \n" +
            "(see also GO Annotation File format at the Gene Ontology Consortium)", "Database Desig-nation,Marker ID,Gene Symbol,Qualifiers,GO Term ID,Reference ID,GO Evidence Code,Inferred From,Ontology: P=Biological Process \n" +
            "F=Molecular Function \n" +
            "C=Cellular Component,Marker Name,Marker Synonyms (if any),Marker Type: \n" +
            "gene\n" +
            "transcript\n" +
            "protein,Taxon,Modification Date,Assigned By"),
    GENES_EXPRESSION("ZFIN Genes with Expression Assay Records", "xpat.txt", "Gene Expression", "Gene ID,Gene Symbol,EST ID(optional),EST Symbol(optional),Expression Type,Expression ID,Publication ID,Genotype ID,Environment ID ,Probe Quality (optional 0 - 5 rating)"),
    ANTIBODY_EXPRESSION("ZFIN Antibody Expression Assay Records", "abxpat.txt", "Gene Expression", "Antibody ID,Antibody name,Gene ID (optional),Gene Symbol (optional),Expression Type,Expression ID,Publication ID,Genotype ID,Environment ID"),
    ENVIRONMENT_EXPRESSION("Expression Environment Description", "xpat_environment.txt", "Gene Expression", "Environment ID,Condition Group,Condition Content,Value,Unit,Comment"),
    EXPERIMENT_FIGURE_EXPRESSION("Expression Experiment-Figure", "xpatfig.txt", "Gene Expression", "Expression ID,Expression Result ID,Figure ID"),
    WT_EXPRESSION("Expression data for wildtype fish", "wildtype-expression.txt", "Gene Expression", "Gene ID,Gene abbreviation,Genotype,Super Structure ID,Super Structure Name,Sub Structure ID,Sub Structure Name,Start Stage,End Stage,Assay"),
    STAGE_EXPRESSION("Zebrafish Gene Expression by Stage and Anatomy Term", "xpat_stage_anatomy.txt", "Gene Expression", "Expression Result ID ,Expression ID,Begin Stage ID,End Stage ID,Anatomy Super Term ID,Anatomy Sub Term ID,Expression Found"),
    GENO_FEAT("Genotype Features", "genotype_features.txt", "Genotype Data", "Genotype ID,Genotype Name,Genotype Unique Name,Allele ID,Allele Name,Allele Abbreviation,Allele Type,Allele Display Type,Gene or Construct Symbol,Corresponding ZFIN Gene ID/Construct ID"),
    GENO_REMOVED_FEAT("Genotype Features (removed or displaced genes)", "genotype_features_missing_markers.txt", "Genotype Data", "Genotype ID,Genotype Name,Genotype Unique Name,Affected Gene Symbol,Corresponding ZFIN Gene ID"),
    GENO_BACKGROUNDS_FEAT("Genotype Backgrounds", "genotype_backgrounds.txt", "Genotype Data", "Genotype ID,Genotype Name,Background"),
    GENO_PUB_FEAT("Genotype-Publication relation", "genotype_publication.txt", "Genotype Data", "Genotype ID,Publication ID"),
    WILDTYPE_LINES("Wildtype Lines", "wildtypes.txt", "Wildtype Lines", "Genotype ID,Genotype Name,Genotype Abbreviation"),
    PUB_IDS("ZFIN Publication IDs: ZFIN IDs to PubMed ids", "pub_to_pubmed_id_translation.txt", "Publications", "Publication ZFIN ID,PubMed ID. None or blank when not available"),
    BIBLIOGRAPHY("ZFIN Bibliography", "zfinpubs.txt", "Publications", "Publication ZFIN ID,PubMed ID. None or blank when not available,Authors,Title,Journal,Year,Volume,Pages"),
    UNIPROTKB("ZFIN Marker IDs / UniProtKB IDs to ZFIN Pub IDs & PubMed IDs", "uniprot-zfinpub.txt", "Publications", "ZFIN Marker ID,UniProtKB ID,ZFIN Pub ID ,PubMedID,ZFIN Annotation(GO/Phenotype/Expression)"),
    GENO_PHENO("Genotypes with Phenotypes", "phenotype.txt", "Phenotype Data", "Genotype ID ,Genotype Name ,Start Stage ID ,Start Stage Name ,End Stage ID ,End Stage Name ,Affected Structure or Process 1 superterm ID ,Affected Structure or Process 1 superterm Name ,Affected Structure or Process 1 subterm ID ,Affected Structure or Process 1 subterm Name ,Phenotype Keyword ID ,Phenotype Keyword Name ,Phenotype Modifier ,Affected Structure or Process 2 superterm ID ,Affected Structure or Process 2 superterm Name ,Affected Structure or Process 2 subterm ID ,Affected Structure or Process 2 subterm Name ,Publication ID ,Environment ID"),
    GENO_PHENO_OBO("Genotypes with Phenotypes (obo format)", "pheno_obo.txt", "Phenotype Data", "Genotype ID ,Genotype Name ,Start Stage Obo ID ,End Stage Obo ID ,Affected Structure or Process 1 superterm obo ID ,Affected Structure or Process 1 subterm obo ID ,Phenotype Keyword obo ID ,Phenotype Modifier ,Affected Structure or Process 2 superterm obo ID ,Affected Structure or Process 2 subterm obo ID ,Publication ZFIN ID ,Environment ZFIN ID"),
    GENO_ENVIRONMENT("Phenotype Environment Description", "pheno_environment.txt", "Phenotype Data", "Environment ID,Condition Group,Condition Content,Value,Unit,Comment"),
    GENO_FIG("Genotype-Figure relation", "genofig.txt", "Phenotype Data", "Genotype ID,Figure ID"),
    GENO_HUMAN("Phenotype for Zebrafish genes with Human Orthology", "pheno.txt", "Phenotype Data", "ZFIN Gene ID ,Entrez Zebrafish Gene ID ,Entrez Human Gene ID ,ZFIN Gene Symbol ,Affected Structure or Process 1 superterm obo ID ,Affected Structure or Process 1 superterm name ID ,Affected Structure or Process 1 subterm obo ID ,Affected Structure or Process 1 subtrem name ID ,Affected Structure or Process 2 superterm obo ID ,Affected Structure or Process 2 superterm name ,Affected Structure or Process 2 subterm obo ID ,Affected Structure or Process 2 subterm name ,Phenotype Keyword obo ID ,Phenotype Quality ,Phenotype Tag"),
    AO_TERMS("Zebrafish Anatomical Terms", "staged_anatomy.other", "Anatomical Ontologies", "Entire Staged Anatomy (tab indented)"),
    AO_RELS("Zebrafish Anatomy Term Relationships", "anatomy_relationship.txt", "Anatomical Ontologies", "Parent Item ID,Child Item ID,Relationship Type ID"),
    STAGE("Zebrafish Stage Series", "stage_ontology.txt", "Anatomical Ontologies", "Stage ID,Stage OBO ID,Stage Name,Begin Hours,End Hours"),
    TERM_STAGE("Zebrafish Anatomy Term Stages", "anatomy_item.txt", "Anatomical Ontologies", "Anatomy Item ID,Anatomy Item Name,Begin Stage ID,End Stage ID"),
    AO_SYNON("Zebrafish Anatomy Term Synonyms", "anatomy_synonyms.txt", "Anatomical Ontologies", "Anatomy Item ID,Anatomy Item Name,Anatomy Item Synonym"),
    MOS("Morpholino", "Morpholinos.txt", "Morpholino Data", "Gene ID,Gene Symbol,MO ID,MO Symbol,MO Sequence,Note"),
    IMAGES("Image-Figure translations: Note: <i>Images are not provided due to copyright restrictions</i>", "ImageFigures.txt", "Images", "Image ID,Figure ID,Image Preparation"),
    FEATURE("All Genomic Features", "features.txt", "Genomic Feature Data", "Genomic Feature ID,Genomic Feature Abbreviation,Genomic Feature Name,Genomic Feature Type"),
    FEATURE_AFFECTED_GENES("Genomic Features and their affected genes (alleles, deficiencies, translocations)", "features-affected-genes.txt", "Genomic Feature Data", "Genomic Feature ID,Gene Abbreviation,Gene ID"),;

    private String name;
    private String category;
    private String fileName;
    private String columnHeaderList;

    private static List<String> categories = new ArrayList<String>(20);

    static {
        categories.add("Genetic Markers");
        categories.add("Antibodies");
        categories.add("Previous Names");
        categories.add("Mapping Data");
        categories.add("Sequence Coordinates in gff3 format");
        categories.add("Sequence Data");
        categories.add("Orthology Data");
        categories.add("Genetic Marker Relationships");
        categories.add("Gene Ontology Data");
        categories.add("Gene Expression");
        categories.add("Phenotype Data");
        categories.add("Genotype Data");
        categories.add("Genomic Feature Data");
        categories.add("Wildtype Lines");
        categories.add("Publications");
        categories.add("Anatomical Ontologies");
        categories.add("Morpholino Data");
        categories.add("Images");
    }

    private DownloadFile(String name, String fileName, String category, String columnHeaderList) {
        this.name = name;
        this.category = category;
        this.fileName = fileName;
        this.columnHeaderList = columnHeaderList;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCategory() {
        return category;
    }

    public String getColumnHeaderList() {
        return columnHeaderList;
    }

    public static DownloadFile getDownloadFileByFileName(String fileName) {
        if (fileName == null)
            return null;
        for (DownloadFile downloadFile : values())
            if (downloadFile.fileName.equals(fileName))
                return downloadFile;
        return null;
    }

    public static int compare(DownloadFile o1, DownloadFile o2) {
        String category = o1.getCategory();
        String category1 = o2.getCategory();
        int indexOne = categories.indexOf(category);
        int indexTwo = categories.indexOf(category1);
        if (indexOne == indexTwo)
            return o1.getName().compareToIgnoreCase(o2.getName());
        return indexOne - indexTwo;
    }

    public String getFileType() {
        String[] tokens = fileName.split("\\.");
        return tokens[1];
    }

    public Type getFileFormatType() {
        String fileType = getFileType();
        return Type.getTypeByName(fileType);
    }

    public static List<String> getSortedCategories() {
        return categories;
    }

    public List<String> getHeaderColumns() {
        String[] header = columnHeaderList.split(",");
        List<String> headerColumns = new ArrayList<String>(header.length);
        Collections.addAll(headerColumns, header);
        return headerColumns;
    }

    public String getHeaderLine(String delimiter) {
        return columnHeaderList.replace(",", delimiter);
    }

    public String getFormattedHeaderLine(String delimiter, String... names) {
        int numberOfColumns = names.length;
        int numberOfDelimiters = columnHeaderList.split(",").length;
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < numberOfDelimiters; index++) {
            if (numberOfColumns > index) {
                builder.append(names[index]);
                builder.append(delimiter);
            } else {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    public String getFormattedFileName() {
        String filePrefix = fileName.split("\\.")[0];
        return getFileFormatType().getFormattedFileName(filePrefix);
    }

    enum Type {
        CSV, GFF3, TSV, OTHER;

        public static Type getTypeByName(String name) {
            if (name == null)
                return null;
            if (name.equalsIgnoreCase("txt"))
                name = "tsv";
            for (Type type : values())
                if (type.toString().equalsIgnoreCase(name.toUpperCase()))
                    return type;
            return null;
        }

        public String getFormattedFileName(String filePrefix) {
            return filePrefix + "." + name().toLowerCase();
        }
    }
}
