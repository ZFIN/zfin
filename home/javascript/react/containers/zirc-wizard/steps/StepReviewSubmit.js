import React from 'react';
import { useWizard } from '../state/WizardContext';

const LESION_TYPE_LABELS = {
    point_mutation: 'Point mutation',
    deletion: 'Deletion',
    insertion: 'Insertion',
    indel: 'Indel (delins)',
    duplication: 'Duplication',
    inversion: 'Inversion',
    translocation: 'Translocation',
    transgene: 'Transgene',
    other: 'Other',
    unknown: 'Unknown',
};

const Section = ({ title, children }) => (
    <div className='mb-3'>
        <h6 className='text-primary'>{title}</h6>
        <div className='pl-3'>{children}</div>
    </div>
);

const Field = ({ label, value }) => {
    if (!value && value !== 0 && value !== false) {return null;}
    const display = typeof value === 'boolean' ? (value ? 'Yes' : 'No') : String(value);
    return (
        <div className='row mb-1'>
            <div className='col-md-4 text-muted'>{label}</div>
            <div className='col-md-8'>{display}</div>
        </div>
    );
};

const StepReviewSubmit = () => {
    const { state } = useWizard();

    return (
        <div>
            <p>Please review your submission before submitting.</p>

            <Field label='Number of lines' value={state.lineCount} />
            <hr />

            {state.lines.map((line, li) => (
                <div key={li} className='mb-4 border-left pl-3'>
                    <h5>Line {li + 1}: {line.name || '(unnamed)'}</h5>

                    <Section title='General Information'>
                        <Field label='Name' value={line.name} />
                        <Field label='Abbreviation' value={line.abbreviation} />
                        <Field label='Previous names' value={line.previousNames} />
                        <Field label='Acceptance reasons' value={line.acceptanceReasons.join(', ')} />
                    </Section>

                    <Section title='Features'>
                        <Field label='Total features' value={line.featureCount} />
                        <Field label='Mutations' value={line.mutationCount} />
                        <Field label='Transgenes' value={line.transgeneCount} />
                    </Section>

                    {line.mutations.map((mutation, mi) => (
                        <div key={mi} className='mb-3 border-left pl-3'>
                            <Section title={`Mutation ${mi + 1}: ${mutation.alleleDesignation || '(unnamed)'}`}>
                                <Field label='Allele designation' value={mutation.alleleDesignation} />
                                <Field label='Mutagenesis protocol' value={mutation.mutagenesisProtocol} />
                                <Field label='Molecularly characterized' value={mutation.isMolecularlyCharacterized} />
                                <Field label='Gene count' value={mutation.geneCount} />
                                <Field label='ZFIN record' value={mutation.hasZfinRecord} />
                                <Field label='ZDB Feature ID' value={mutation.zdbFeatureId} />
                                <Field label='Discoverer' value={mutation.discoverer} />
                                <Field label='Institution' value={mutation.institution} />
                                <Field label='Publications' value={mutation.publications} />
                            </Section>

                            {mutation.genes.map((gene, gi) => (
                                <Section key={gi} title={`Gene ${gi + 1}`}>
                                    <Field label='Mutated gene' value={gene.mutatedGene} />
                                    <Field label='Linkage group' value={gene.linkageGroup} />
                                    <Field label='GenBank genomic' value={gene.genbankGenomic} />
                                    <Field label='GenBank cDNA' value={gene.genbankCdna} />
                                    <Field label='ZDB gene ID' value={gene.zdbGeneId} />
                                </Section>
                            ))}

                            {mutation.lesions.map((lesion, lsi) => (
                                <Section key={lsi} title={`Lesion ${lsi + 1}`}>
                                    <Field label='Type' value={LESION_TYPE_LABELS[lesion.type] || lesion.type} />
                                    <Field label='Size (bp)' value={lesion.sizeBp} />
                                    <Field label='Nucleotide WT' value={lesion.nucleotideWt} />
                                    <Field label='Nucleotide mutant' value={lesion.nucleotideMut} />
                                    <Field label='Deletion size' value={lesion.deletionSize} />
                                    <Field label='Insertion size' value={lesion.insertionSize} />
                                    <Field label='Deleted bases' value={lesion.deletedBases} />
                                    <Field label='Inserted bases' value={lesion.insertedBases} />
                                    <Field label='Flanking sequence (WT)' value={lesion.flankingSequenceWt} />
                                    <Field label='Flanking sequence (mut)' value={lesion.flankingSequenceMut} />
                                    <Field label='Mutated amino acids' value={lesion.mutatedAminoAcids} />
                                </Section>
                            ))}

                            <Section title='Genotyping'>
                                <Field label='Number of assays' value={mutation.genotypingAssayCount} />
                                {mutation.genotypingAssays.map((assay, ai) => (
                                    <div key={ai} className='ml-3 mb-2'>
                                        <Field label={`Assay ${ai + 1} type`} value={assay.type} />
                                        <Field label='Forward primer' value={assay.forwardPrimer} />
                                        <Field label='Reverse primer' value={assay.reversePrimer} />
                                        <Field label='WT product' value={assay.expectedWtProduct} />
                                        <Field label='Mutant product' value={assay.expectedMutProduct} />
                                        <Field label='Restriction enzyme' value={assay.restrictionEnzyme} />
                                    </div>
                                ))}
                            </Section>

                            <Section title='Phenotyping'>
                                <Field label='Mutation type' value={mutation.mutationType} />
                                {mutation.phenotypes.map((pheno, pi) => (
                                    <div key={pi} className='ml-3 mb-2'>
                                        <Field label={`Phenotype ${pi + 1}`} value={pheno.description} />
                                        <Field label='Stage (hpf)' value={pheno.optimalStageHpf} />
                                        <Field label='Stage name' value={pheno.optimalStageName} />
                                        <Field label='Segregation' value={pheno.segregation} />
                                        <Field label='Phenotype type' value={pheno.phenotypeType} />
                                        <Field label='Images' value={pheno.imageFiles.length > 0 ? `${pheno.imageFiles.length} file(s)` : ''} />
                                    </div>
                                ))}
                            </Section>

                            <Section title='Lethality'>
                                <Field label='Homozygous lethal' value={mutation.isHomozygousLethal} />
                                <Field
                                    label='Stage of death'
                                    value={
                                        mutation.stageOfDeathType === 'specific' ? mutation.stageOfDeathSpecific :
                                            mutation.stageOfDeathType === 'window' ? `${mutation.stageOfDeathFrom} to ${mutation.stageOfDeathTo}` : ''
                                    }
                                />
                            </Section>
                        </div>
                    ))}

                    <Section title='Linked Features'>
                        <Field label='Any linked' value={line.linkedFeatures.areAnyLinked} />
                        <Field label='Linked features' value={line.linkedFeatures.linkedIndices.join(', ')} />
                        <Field label='Distance known' value={line.linkedFeatures.distanceKnown} />
                        <Field label='Distance' value={line.linkedFeatures.distanceValue ? `${line.linkedFeatures.distanceValue} ${line.linkedFeatures.distanceUnit}` : ''} />
                    </Section>

                    <Section title='Background'>
                        <Field label='Maternal' value={line.background.maternal} />
                        <Field label='Paternal' value={line.background.paternal} />
                        <Field label='Can be changed' value={line.background.canBeChanged} />
                        <Field label='Concerns' value={line.background.changeConcerns} />
                        <Field label='Unreported features' value={line.background.unreportedFeatures} />
                        <Field label='Details' value={line.background.unreportedFeaturesDetail} />
                    </Section>

                    <Section title='Additional Info'>
                        <Field label='Has additional info' value={line.additionalInfo.hasAdditional} />
                        <Field label='Details' value={line.additionalInfo.detail} />
                    </Section>
                </div>
            ))}
        </div>
    );
};

export default StepReviewSubmit;
