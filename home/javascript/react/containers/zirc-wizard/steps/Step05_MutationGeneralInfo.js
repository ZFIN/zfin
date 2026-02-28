import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';

const YES_NO = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
];

const Step05_MutationGeneralInfo = () => {
    const { state, dispatch, updateField } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const mutation = state.lines[li].mutations[mi];

    const path = (field) => ['lines', li, 'mutations', mi, field];

    const handleGeneCountChange = (val) => {
        const count = val === '' ? '' : Number(val);
        updateField(path('geneCount'), count);
        if (typeof count === 'number' && count > 0) {
            dispatch({ type: 'SET_GENE_COUNT', lineIndex: li, mutationIndex: mi, count });
        }
    };

    return (
        <div>
            <WizardField
                label='Allele designation assigned to the mutation'
                id='alleleDesignation'
                value={mutation.alleleDesignation}
                onChange={(val) => updateField(path('alleleDesignation'), val)}
            />
            <WizardField
                label='Mutagenesis protocol'
                id='mutagenesisProtocol'
                value={mutation.mutagenesisProtocol}
                onChange={(val) => updateField(path('mutagenesisProtocol'), val)}
            />

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Is the mutation molecularly characterized/cloned?</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='isMolecularlyCharacterized'
                        options={YES_NO}
                        value={mutation.isMolecularlyCharacterized === true ? 'yes' : mutation.isMolecularlyCharacterized === false ? 'no' : ''}
                        onChange={(val) => updateField(path('isMolecularlyCharacterized'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            <WizardField
                label='How many genes does the mutation directly affect?'
                id='geneCount'
                type='number'
                value={mutation.geneCount}
                onChange={handleGeneCountChange}
            />

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Is ZFIN record established for this mutation?</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='hasZfinRecord'
                        options={YES_NO}
                        value={mutation.hasZfinRecord === true ? 'yes' : mutation.hasZfinRecord === false ? 'no' : ''}
                        onChange={(val) => updateField(path('hasZfinRecord'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            {mutation.hasZfinRecord && (
                <WizardField
                    label='ZDB genomic feature # (ZFIN ID)'
                    id='zdbFeatureId'
                    value={mutation.zdbFeatureId}
                    onChange={(val) => updateField(path('zdbFeatureId'), val)}
                />
            )}

            <WizardField
                label='Mutation discoverer/creator'
                id='discoverer'
                value={mutation.discoverer}
                onChange={(val) => updateField(path('discoverer'), val)}
            />
            <WizardField
                label='Institution where the mutation was discovered/generated'
                id='institution'
                value={mutation.institution}
                onChange={(val) => updateField(path('institution'), val)}
            />
            <WizardField
                label='Publications (please list)'
                id='publications'
                value={mutation.publications}
                onChange={(val) => updateField(path('publications'), val)}
                tag='textarea'
                rows={3}
            />
        </div>
    );
};

export default Step05_MutationGeneralInfo;
