// GENERATED FILE — do not edit.
//
// Source: org.zfin.zirc.dto records. Regenerate via
//   gradle generateZircTypes
//
// See reference/zirc-architecture.md §3 for why this
// mirror exists and what the generator covers.

export interface LineSubmissionDTO {
    zdbID: string;
    name: string | null;
    abbreviation: string | null;
    previousNames: string[];
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
    createdAt: string | null;
    updatedAt: string | null;
    submitterNames: string | null;
}

export interface MutationDTO {
    id: number;
    lineSubmissionId: string;
    sortOrder: number;
    alleleDesignation: string | null;
    alleleInZfin: boolean | null;
    mutationType: string | null;
    zfinRecordEstablished: boolean | null;
    cellGenomicFeature: string | null;
    mutationDiscoverer: string | null;
    mutationInstitution: string | null;
    mutagenesisStage: string | null;
    mutagenesisProtocol: string | null;
    molecularlyCharacterized: boolean | null;
    homozygousLethal: boolean | null;
    lethalityStageTypical: string | null;
    lethalitySpecificTimepoint: string | null;
    lethalityWindowStart: string | null;
    lethalityWindowEnd: string | null;
    lethalityAdditionalInfo: string | null;
    publications: string[];
    assays: AssaySummaryDTO[];
    genes: GeneDTO[];
    lesions: LesionSummaryDTO[];
    phenotypes: PhenotypeSummaryDTO[];
}

export interface LinkedFeatureDTO {
    mutationAId: number;
    mutationBId: number;
    distanceKnown: boolean | null;
    distanceCentimorgans: number | null;
    distanceMegabases: number | null;
    additionalInfo: string | null;
}

export interface LesionSummaryDTO {
    id: number;
    sortOrder: number;
    lesionType: string | null;
}

export interface LesionDTO {
    id: number;
    mutationId: number;
    sortOrder: number;
    lesionType: string | null;
    lesionSizeBp: number | null;
    insertionSizeBp: number | null;
    nucleotideChange: string | null;
    deletedSequence: string | null;
    insertedSequence: string | null;
    transgeneSequence: string | null;
    locationInline: string | null;
    fivePrimeFlank: string | null;
    threePrimeFlank: string | null;
    hasLargeVariant: boolean | null;
    mutatedAminoAcids: string | null;
    mutatedAminoAcidsHgvs: string | null;
    additionalInfo: string | null;
}

export interface GeneDTO {
    id: number;
    mutationId: number;
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

export interface AssayDTO {
    id: number;
    mutationId: number;
    sortOrder: number;
    assayType: string | null;
    forwardPrimer: string | null;
    reversePrimer: string | null;
    expectedWtPcr: string | null;
    expectedMutPcr: string | null;
    sequencingPrimer: string | null;
    dcapsMismatchPrimer: string | null;
    wtSpecificPrimer: string | null;
    mutSpecificPrimer: string | null;
    commonPrimer: string | null;
    kaspGenomicSequence: string | null;
    restrictionEnzymeName: string | null;
    restrictionEnzymeCatalog: string | null;
    enzymeCleaves: string[];
    expectedWtDigest: string | null;
    expectedMutDigest: string | null;
    sslpMarkerName: string | null;
    sslpDistance: string | null;
    sslpGenomicLocation: string | null;
    sslpInducedBackground: string | null;
    sslpOutcrossedBackground: string | null;
    sslpInducedPcr: string | null;
    sslpOutcrossedPcr: string | null;
    additionalInfo: string | null;
    attachments: AssayFileDTO[];
}

export interface AssayFileDTO {
    id: number;
    originalFilename: string;
    contentType: string | null;
    fileSize: number | null;
    uploadedAt: string | null;
}

export interface PhenotypeSummaryDTO {
    id: number;
    sortOrder: number;
    description: string | null;
}

export interface PhenotypeDTO {
    id: number;
    mutationId: number;
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
