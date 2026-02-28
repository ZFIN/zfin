export function createDefaultGene() {
    return {
        mutatedGene: '',
        linkageGroup: '',
        genbankGenomic: '',
        genbankCdna: '',
        zdbGeneId: '',
    };
}

export function createDefaultLesion() {
    return {
        type: '',
        sizeBp: '',
        // Point mutation
        nucleotideWt: '',
        nucleotideMut: '',
        flankingSequenceWt: '',
        flankingSequenceMut: '',
        // Indel
        deletionSize: '',
        insertionSize: '',
        deletedBases: '',
        insertedBases: '',
        // Common
        mutatedAminoAcids: '',
        additionalInfo: '',
    };
}

export function createDefaultGenotypingAssay() {
    return {
        type: '',
        forwardPrimer: '',
        reversePrimer: '',
        expectedWtProduct: '',
        expectedMutProduct: '',
        restrictionEnzyme: '',
        enzymeCleavesTemplate: '',
        expectedWtAfterDigest: '',
        expectedMutAfterDigest: '',
        additionalInfo: '',
    };
}

export function createDefaultPhenotype() {
    return {
        description: '',
        optimalStageHpf: '',
        optimalStageName: '',
        imageFiles: [],
        imagePermission: false,
        segregation: '',
        segregationPercent: '',
        phenotypeType: '',
        phenotypeTypeOther: '',
    };
}

export function createDefaultMutation() {
    return {
        alleleDesignation: '',
        mutagenesisProtocol: '',
        isMolecularlyCharacterized: null,
        geneCount: 1,
        hasZfinRecord: null,
        zdbFeatureId: '',
        discoverer: '',
        institution: '',
        publications: '',

        genes: [createDefaultGene()],

        hasMultipleLesions: null,
        lesionCount: 1,
        lesions: [createDefaultLesion()],

        genotypingAssayCount: 1,
        genotypingAssays: [createDefaultGenotypingAssay()],

        mutationType: '',
        mutationTypeOther: '',
        phenotypeCount: 1,
        phenotypes: [createDefaultPhenotype()],

        isHomozygousLethal: null,
        stageOfDeathType: '',
        stageOfDeathSpecific: '',
        stageOfDeathFrom: '',
        stageOfDeathTo: '',
        lethalityAdditionalInfo: '',
    };
}

export function createDefaultTransgene() {
    return {
        designation: '',
        constructName: '',
        hasZfinRecord: null,
        zdbFeatureId: '',
        creator: '',
        institution: '',
        publications: '',
    };
}

export function createDefaultLine() {
    return {
        name: '',
        abbreviation: '',
        previousNames: '',
        acceptanceReasons: [],

        featureCount: 1,
        mutationCount: 1,
        transgeneCount: 0,

        mutations: [createDefaultMutation()],
        transgenes: [],

        linkedFeatures: {
            areAnyLinked: null,
            linkedIndices: [],
            distanceKnown: null,
            distanceValue: '',
            distanceUnit: 'cM',
            additionalInfo: '',
        },

        background: {
            maternal: '',
            paternal: '',
            canBeChanged: null,
            changeConcerns: '',
            unreportedFeatures: null,
            unreportedFeaturesDetail: '',
        },

        additionalInfo: {
            hasAdditional: null,
            detail: '',
        },
    };
}

export const initialState = {
    currentStep: 1,
    currentLineIndex: 0,
    currentMutationIndex: 0,
    currentGeneIndex: 0,
    currentLesionIndex: 0,
    currentAssayIndex: 0,
    currentPhenotypeIndex: 0,

    lineCount: 1,
    lines: [createDefaultLine()],
};
