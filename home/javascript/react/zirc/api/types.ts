// Hand-typed mirror of org.zfin.zirc.dto.LineSubmissionDTO, used by the
// React Query cache and as the seed for the schema-driven form's initial data.

export interface LineSubmissionDTO {
    zdbID: string;
    name: string | null;
    abbreviation: string | null;
    previousNames: string | null;
    singleAllelic: boolean | null;
    maternalBackground: string | null;
    paternalBackground: string | null;
    backgroundChangeable: boolean | null;
    backgroundChangeConcerns: string | null;
    unreportedFeaturesDetails: string | null;
    husbandryInfo: string | null;
    additionalInfo: string | null;
    reasons: string[];
    reasonsOther: string | null;
    mutations: MutationDTO[];
    linkedFeatures: LinkedFeatureDTO[];
    draft: boolean;
}

// One pairwise linkage between two mutations on the same submission.
// distance is stored across two underlying columns (centimorgans vs
// megabases) — exactly one is non-null at a time. The renderer
// combines them into a single (value, unit) widget.
export interface LinkedFeatureDTO {
    mutationAId: number;
    mutationBId: number;
    distanceKnown: boolean | null;
    distanceCentimorgans: number | null;
    distanceMegabases: number | null;
    additionalInfo: string | null;
}

export interface MutationDTO {
    id: number;
    lineSubmissionId: string;
    sortOrder: number;
    // General
    alleleDesignation: string | null;
    alleleInZfin: boolean | null;
    mutationType: string | null;
    mutationDiscoverer: string | null;
    mutationInstitution: string | null;
    // Mutagenesis
    mutagenesisStage: string | null;
    mutagenesisProtocol: string | null;
    molecularlyCharacterized: boolean | null;
    // Lethality
    homozygousLethal: boolean | null;
    lethalityStageTypical: string | null;
    lethalitySpecificTimepoint: string | null;
    lethalityWindowStart: string | null;
    lethalityWindowEnd: string | null;
    lethalityAdditionalInfo: string | null;
    // Publications
    publications: string[];
    // Genotyping assays — summary rows only, surfaced as collapsed cards.
    assays: AssaySummaryDTO[];
    // Per-mutation genes — full records (small enough that there's no
    // separate summary type).
    genes: GeneDTO[];
    // Per-mutation lesions — summary rows only, surfaced as collapsed
    // cards. Full per-lesion fields fetched via /api/zirc/lesions/{id}.
    lesions: LesionSummaryDTO[];
    // Per-mutation phenotypes — summary rows only, surfaced as collapsed
    // cards. Full per-phenotype fields fetched via /api/zirc/phenotypes/{id}.
    phenotypes: PhenotypeSummaryDTO[];
}

export interface LesionSummaryDTO {
    id: number;
    sortOrder: number;
    lesionType: string | null;
}

export interface PhenotypeSummaryDTO {
    id: number;
    sortOrder: number;
    description: string | null;
}

// Full per-phenotype payload. segregation and type are PostgreSQL text[]
// columns surfaced as JSON string arrays. Wire format for timing is
// always integer hpf; the hpf/dpf unit toggle in the renderer is UI
// state only.
export interface PhenotypeDTO {
    id: number;
    mutationId: number | null;
    sortOrder: number;
    description: string | null;
    hpfStart: number | null;
    hpfEnd: number | null;
    stage: string | null;
    zfinImagePermission: boolean | null;
    zircImagePermission: boolean | null;
    nonMendelianPercentage: number | null;
    nonMendelianComment: string | null;
    segregation: string[];
    type: string[];
}

// Full per-lesion payload. Field visibility is decided by the uiSchema
// rules on lesionType, not by which fields are populated.
export interface LesionDTO {
    id: number;
    mutationId: number | null;
    sortOrder: number;
    lesionType: string | null;
    // Sizing
    lesionSizeBp: number | null;
    insertionSizeBp: number | null;
    // Sequence specifics
    nucleotideChange: string | null;
    deletedSequence: string | null;
    insertedSequence: string | null;
    transgeneSequence: string | null;
    // Location
    locationInline: string | null;
    fivePrimeFlank: string | null;
    threePrimeFlank: string | null;
    hasLargeVariant: boolean | null;
    // Protein-level
    mutatedAminoAcids: string | null;
    mutatedAminoAcidsHgvs: string | null;
    // Catch-all
    additionalInfo: string | null;
}

// One per-mutation gene record. mutatedGeneZdbID is the marker FK; the
// abbreviation is denormalized for display.
export interface GeneDTO {
    id: number;
    mutationId: number | null;
    sortOrder: number;
    mutatedGeneZdbID: string | null;
    mutatedGeneAbbreviation: string | null;
    linkageGroup: string | null;
    genbankGenomicDna: string | null;
    genbankCdna: string | null;
}

export interface AssaySummaryDTO {
    id: number;
    sortOrder: number;
    assayType: string | null;
}

// Full per-assay payload — used by the inline assay editor (M4.2). Field
// visibility is decided by the uiSchema's conditional rules, not by which
// fields are populated, so every column shows up here regardless of type.
export interface AssayDTO {
    id: number;
    mutationId: number | null;
    sortOrder: number;
    assayType: string | null;
    // PCR core
    forwardPrimer: string | null;
    reversePrimer: string | null;
    expectedWtPcr: string | null;
    expectedMutPcr: string | null;
    // Sequencing
    sequencingPrimer: string | null;
    // dCAPS
    dcapsMismatchPrimer: string | null;
    // Allele-specific PCR
    wtSpecificPrimer: string | null;
    mutSpecificPrimer: string | null;
    commonPrimer: string | null;
    // KASP
    kaspGenomicSequence: string | null;
    // RFLP
    restrictionEnzymeName: string | null;
    restrictionEnzymeCatalog: string | null;
    enzymeCleaves: string[];
    expectedWtDigest: string | null;
    expectedMutDigest: string | null;
    // SSLP
    sslpMarkerName: string | null;
    sslpDistance: string | null;
    sslpGenomicLocation: string | null;
    sslpInducedBackground: string | null;
    sslpOutcrossedBackground: string | null;
    sslpInducedPcr: string | null;
    sslpOutcrossedPcr: string | null;
    // Catch-all
    additionalInfo: string | null;
    // Attachments (M4.3) — summary; content streamed separately.
    attachments: AssayFileDTO[];
}

export interface AssayFileDTO {
    id: number;
    originalFilename: string;
    contentType: string | null;
    fileSize: number | null;
    uploadedAt: string | null;
}

// One row in an autocomplete result. label is the display string with
// the ZDB-ID in parens; value is the canonical id stored back in the form.
export interface AutocompleteItemDTO {
    label: string;
    value: string;
}

// RFC 7807 problem detail returned by ZircApiExceptionHandler.
export interface ProblemDetail {
    type?: string;
    title?: string;
    status?: number;
    detail?: string;
    instance?: string;
    errors?: Record<string, string>;
}
