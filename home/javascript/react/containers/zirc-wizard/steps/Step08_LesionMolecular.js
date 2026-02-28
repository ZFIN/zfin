import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import ConditionalField from '../components/ConditionalField';

const LESION_TYPES = [
    { value: 'point_mutation', label: 'Point mutation' },
    { value: 'deletion', label: 'Deletion (deficiency)' },
    { value: 'insertion', label: 'Insertion' },
    { value: 'indel', label: 'Indel (delins)' },
    { value: 'duplication', label: 'Duplication' },
    { value: 'inversion', label: 'Inversion' },
    { value: 'translocation', label: 'Translocation' },
    { value: 'transgene', label: 'Transgene' },
    { value: 'other', label: 'Other (please specify)' },
    { value: 'unknown', label: 'Unknown' },
];

const Step08_LesionMolecular = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const lsi = state.currentLesionIndex;
    const lesion = state.lines[li].mutations[mi].lesions[lsi];

    const path = (field) => ['lines', li, 'mutations', mi, 'lesions', lsi, field];

    const isPointMutation = lesion.type === 'point_mutation';
    const isIndel = lesion.type === 'indel';
    const isDeletion = lesion.type === 'deletion';
    const isInsertion = lesion.type === 'insertion';
    const showSizeBp = isPointMutation || isDeletion || isInsertion || lesion.type === 'duplication';

    return (
        <div>
            <h6>Type of Lesion</h6>
            <div className='mb-3'>
                <RadioGroup
                    name='lesionType'
                    options={LESION_TYPES}
                    value={lesion.type}
                    onChange={(val) => updateField(path('type'), val)}
                />
            </div>

            <ConditionalField condition={lesion.type === 'other'}>
                <WizardField
                    label='Please specify'
                    id='lesionTypeOther'
                    value={lesion.additionalInfo}
                    onChange={(val) => updateField(path('additionalInfo'), val)}
                />
            </ConditionalField>

            <ConditionalField condition={!!lesion.type && lesion.type !== 'unknown' && lesion.type !== 'other'}>
                <hr />
                <h6>More Information About the Molecular Nature of the Lesion</h6>

                <ConditionalField condition={showSizeBp}>
                    <WizardField
                        label='Lesion size (bp)'
                        id='sizeBp'
                        value={lesion.sizeBp}
                        onChange={(val) => updateField(path('sizeBp'), val)}
                    />
                </ConditionalField>

                <ConditionalField condition={isIndel}>
                    <WizardField
                        label='Deletion size (bp)'
                        id='deletionSize'
                        value={lesion.deletionSize}
                        onChange={(val) => updateField(path('deletionSize'), val)}
                    />
                    <WizardField
                        label='Insertion size (bp)'
                        id='insertionSize'
                        value={lesion.insertionSize}
                        onChange={(val) => updateField(path('insertionSize'), val)}
                    />
                    <WizardField
                        label='Deleted base pairs'
                        id='deletedBases'
                        value={lesion.deletedBases}
                        onChange={(val) => updateField(path('deletedBases'), val)}
                    />
                    <WizardField
                        label='Inserted base pairs'
                        id='insertedBases'
                        value={lesion.insertedBases}
                        onChange={(val) => updateField(path('insertedBases'), val)}
                    />
                </ConditionalField>

                <ConditionalField condition={isPointMutation}>
                    <WizardField
                        label='Nucleotide(s) WT'
                        id='nucleotideWt'
                        value={lesion.nucleotideWt}
                        onChange={(val) => updateField(path('nucleotideWt'), val)}
                    />
                    <WizardField
                        label='Nucleotide(s) mutant'
                        id='nucleotideMut'
                        value={lesion.nucleotideMut}
                        onChange={(val) => updateField(path('nucleotideMut'), val)}
                    />
                </ConditionalField>

                <WizardField
                    label='Location of mutated nucleotide(s) in WT genomic sequence'
                    id='flankingSequenceWt'
                    value={lesion.flankingSequenceWt}
                    onChange={(val) => updateField(path('flankingSequenceWt'), val)}
                    tag='textarea'
                    rows={2}
                />
                <p className='text-muted small ml-4'>
                    Provide at least 5 nucleotides that directly precede and at least 5 nucleotides
                    that directly follow the mutated nucleotide(s) in the genomic DNA.
                </p>

                <ConditionalField condition={isIndel || isPointMutation}>
                    <WizardField
                        label='Mutant flanking sequence'
                        id='flankingSequenceMut'
                        value={lesion.flankingSequenceMut}
                        onChange={(val) => updateField(path('flankingSequenceMut'), val)}
                        tag='textarea'
                        rows={2}
                    />
                </ConditionalField>

                <WizardField
                    label='Mutated amino acid(s) (effect on the protein)'
                    id='mutatedAminoAcids'
                    value={lesion.mutatedAminoAcids}
                    onChange={(val) => updateField(path('mutatedAminoAcids'), val)}
                />

                <WizardField
                    label='Additional information'
                    id='lesionAdditionalInfo'
                    value={lesion.additionalInfo}
                    onChange={(val) => updateField(path('additionalInfo'), val)}
                    tag='textarea'
                    rows={3}
                />
            </ConditionalField>
        </div>
    );
};

export default Step08_LesionMolecular;
