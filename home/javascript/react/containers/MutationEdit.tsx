import React, {useCallback} from 'react';
import SaveToast from '../components/zirc/SaveToast';
import FormRenderer from '../components/form-renderer/FormRenderer';
import {useAutosavedForm} from '../components/form-renderer/useAutosavedForm';
import {emptyMutationDTO, MUTATION_EDIT_SCHEMA} from './zirc/mutationEditSchema';

// ─── Wire types (consumed by the schema + ZircAssayFileUpload) ────────────

export interface GeneDTO {
    id: number | null;
    sortOrder: number | null;
    mutatedGeneZdbId: string | null;
    mutatedGeneAbbreviation: string | null;
    linkageGroup: string | null;
    genbankGenomicDna: string | null;
    genbankCdna: string | null;
}

export interface LesionDTO {
    id: number | null;
    sortOrder: number | null;
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

export interface GenotypingAssayDTO {
    id: number | null;
    sortOrder: number | null;
    assayType: string | null;
    forwardPrimer: string | null;
    reversePrimer: string | null;
    expectedWtPcr: string | null;
    expectedMutPcr: string | null;
    restrictionEnzymeName: string | null;
    restrictionEnzymeCatalog: string | null;
    enzymeCleaves: string[] | null;
    expectedWtDigest: string | null;
    expectedMutDigest: string | null;
    additionalInfo: string | null;
    sequencingPrimer: string | null;
    dcapsMismatchPrimer: string | null;
    wtSpecificPrimer: string | null;
    mutSpecificPrimer: string | null;
    commonPrimer: string | null;
    kaspGenomicSequence: string | null;
    sslpMarkerName: string | null;
    sslpDistance: string | null;
    sslpGenomicLocation: string | null;
    sslpInducedBackground: string | null;
    sslpOutcrossedBackground: string | null;
    sslpInducedPcr: string | null;
    sslpOutcrossedPcr: string | null;
    files: GenotypingAssayFileDTO[] | null;
}

export interface GenotypingAssayFileDTO {
    id: number;
    kind: string;
    originalFilename: string;
    contentType: string | null;
    fileSize: number | null;
    uploadedAt: string | null;
    uploadedBy: string | null;
}

export interface PhenotypeDTO {
    id: number | null;
    sortOrder: number | null;
    description: string | null;
    hpfStart: number | null;
    hpfEnd: number | null;
    /** Server-managed cache derived from hpfStart. Read-only on the client. */
    stage: string | null;
    zfinImagePermission: boolean | null;
    zircImagePermission: boolean | null;
    nonMendelianPercentage: number | null;
    nonMendelianComment: string | null;
    segregation: string[] | null;
    type: string[] | null;
}

export interface MutationDTO {
    id: number;
    lineSubmissionId: string;
    sortOrder: number | null;
    alleleDesignation: string | null;
    alleleInZfin: boolean | null;
    mutagenesisStage: string | null;
    mutagenesisProtocol: string | null;
    mutagenesisProtocolOther: string | null;
    molecularlyCharacterized: boolean | null;
    mutationType: string | null;
    homozygousLethal: boolean | null;
    lethalityStageTypical: string | null;
    lethalitySpecificTimepoint: string | null;
    lethalityWindowStart: string | null;
    lethalityWindowEnd: string | null;
    lethalityAdditionalInfo: string | null;
    zfinRecordEstablished: boolean | null;
    cellGenomicFeature: string | null;
    mutationDiscoverer: string | null;
    mutationInstitution: string | null;
    genes?: GeneDTO[] | null;
    lesions?: LesionDTO[] | null;
    genotypingAssays?: GenotypingAssayDTO[] | null;
    phenotypes?: PhenotypeDTO[] | null;
    publications?: string[] | null;
}

interface MutationEditProps {
    mutationId: string;
}

// ─── Path label map for SaveToast ─────────────────────────────────────────

const PATH_LABELS: Record<string, string> = {
    alleleInZfin: 'Allele in ZFIN',
    alleleDesignation: 'Allele Designation',
    mutagenesisStage: 'Mutagenesis Stage',
    mutagenesisProtocol: 'Mutagenesis Protocol',
    mutagenesisProtocolOther: 'Mutagenesis Protocol (other)',
    molecularlyCharacterized: 'Molecularly Characterized',
    mutationType: 'Mutation Type',
    zfinRecordEstablished: 'ZFIN Record Established',
    cellGenomicFeature: 'ZDB Genomic Feature #',
    mutationDiscoverer: 'Discoverer',
    mutationInstitution: 'Institution',
    homozygousLethal: 'Homozygous Lethal',
    lethalityStageTypical: 'Lethality Stage',
    lethalitySpecificTimepoint: 'Lethality Timepoint',
    lethalityWindowStart: 'Lethality Window Start',
    lethalityWindowEnd: 'Lethality Window End',
    lethalityAdditionalInfo: 'Lethality Info',
    genes: 'Genes',
    lesions: 'Lesions',
    genotypingAssays: 'Genotyping Assays',
    phenotypes: 'Phenotypes',
    publications: 'Publications',
};

function labelForPath(path: string): string {
    return PATH_LABELS[path] ?? path;
}

// ─── Save dispatch ────────────────────────────────────────────────────────

async function callPatch(id: string, path: string, value: unknown): Promise<MutationDTO> {
    const resp = await fetch(`/action/zirc/mutation/${id}/patch`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({path, value}),
    });
    if (!resp.ok) {
        throw new Error(`HTTP ${resp.status}`);
    }
    return resp.json() as Promise<MutationDTO>;
}

// ─── Container ────────────────────────────────────────────────────────────

const MutationEdit = ({mutationId}: MutationEditProps) => {
    const save = useCallback(async (
        id: string,
        _dto: MutationDTO,
        path: string,
        value: unknown,
    ): Promise<MutationDTO> => {
        // Single unified endpoint; the server dispatches on path internally.
        return callPatch(id, path, value);
    }, []);

    const load = useCallback(async (id: string): Promise<MutationDTO> => {
        const r = await fetch(`/action/zirc/mutation/${id}.json`);
        if (!r.ok) {
            throw new Error(`HTTP ${r.status}`);
        }
        return r.json() as Promise<MutationDTO>;
    }, []);

    const form = useAutosavedForm<MutationDTO>({
        initialId: mutationId,
        // Mutations are created by the parent line submission's "+ Add
        // mutation" action, never on this page -- emptyDto is required by
        // the hook's API but won't be exercised.
        emptyDto: () => emptyMutationDTO(''),
        load,
        save,
        extractId: dto => String(dto.id),
        labelForPath,
    });

    // CustomNode action: file upload's response is the full updated DTO;
    // replace local state so the row's `files` list reflects the new state.
    const applyMutationDTO = useCallback((dtoFromServer: unknown) => {
        form.setDto(dtoFromServer as MutationDTO);
    }, [form]);

    if (form.loadError) {
        return <div className='alert alert-danger'>Failed to load mutation: {form.loadError}</div>;
    }
    if (!form.dto) {
        return <div className='text-muted'>Loading…</div>;
    }

    const dto = form.dto;
    return <>
        <FormRenderer
            schema={MUTATION_EDIT_SCHEMA}
            value={dto}
            onChange={(path, value) => {
                form.setDto({...dto, [path]: value} as MutationDTO);
            }}
            onCommit={form.commit}
            actions={{
                applyMutationDTO: applyMutationDTO as unknown as (...args: unknown[]) => unknown,
            }}
        />
        <SaveToast event={form.saveEvent}/>
    </>;
};

export default MutationEdit;
