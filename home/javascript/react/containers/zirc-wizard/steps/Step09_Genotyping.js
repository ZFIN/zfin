import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import NumberInput from '../components/NumberInput';
import EntityIterator from '../components/EntityIterator';
import ConditionalField from '../components/ConditionalField';

const ASSAY_TYPES = [
    { value: 'pcr_gel', label: 'PCR followed by gel electrophoresis' },
    { value: 'pcr_sequencing', label: 'PCR followed by sequencing' },
    { value: 'rflp', label: 'Restriction Fragment Length Polymorphism (RFLP)' },
    { value: 'dcaps', label: 'Derived Cleaved Amplified Polymorphic Sequences (dCAPS)' },
    { value: 'asa', label: 'Allele Specific Amplification (ASA)' },
    { value: 'kasp', label: 'Kompetitive Allele Specific PCR (KASP)' },
    { value: 'hrma', label: 'High-Resolution Melt Analysis (HRMA)' },
    { value: 'other', label: 'Other (please specify)' },
];

const CLEAVES_OPTIONS = [
    { value: 'wt', label: 'WT' },
    { value: 'mut', label: 'MUT' },
];

const AssayFields = ({ assay, basePath }) => {
    const { updateField } = useWizard();
    const path = (field) => [...basePath, field];

    const isRFLP = assay.type === 'rflp';
    const isPCR = assay.type === 'pcr_gel' || assay.type === 'pcr_sequencing';
    const showPrimers = isPCR || isRFLP || assay.type === 'dcaps' || assay.type === 'asa' || assay.type === 'kasp' || assay.type === 'hrma';

    return (
        <div>
            <h6>Type of Assay</h6>
            <div className='mb-3'>
                <RadioGroup
                    name='assayType'
                    options={ASSAY_TYPES}
                    value={assay.type}
                    onChange={(val) => updateField(path('type'), val)}
                />
            </div>

            <ConditionalField condition={showPrimers}>
                <hr />
                <WizardField
                    label='Forward primer'
                    id='forwardPrimer'
                    value={assay.forwardPrimer}
                    onChange={(val) => updateField(path('forwardPrimer'), val)}
                />
                <WizardField
                    label='Reverse primer'
                    id='reversePrimer'
                    value={assay.reversePrimer}
                    onChange={(val) => updateField(path('reversePrimer'), val)}
                />
                <WizardField
                    label='Expected wild-type PCR product(s) (bp)'
                    id='expectedWtProduct'
                    value={assay.expectedWtProduct}
                    onChange={(val) => updateField(path('expectedWtProduct'), val)}
                />
                <WizardField
                    label='Expected mutant PCR product(s) (bp)'
                    id='expectedMutProduct'
                    value={assay.expectedMutProduct}
                    onChange={(val) => updateField(path('expectedMutProduct'), val)}
                />
            </ConditionalField>

            <ConditionalField condition={isRFLP}>
                <WizardField
                    label='Restriction enzyme'
                    id='restrictionEnzyme'
                    value={assay.restrictionEnzyme}
                    onChange={(val) => updateField(path('restrictionEnzyme'), val)}
                />
                <div className='form-group row'>
                    <label className='col-md-4 col-form-label'>The enzyme cleaves WT or MUT template</label>
                    <div className='col-md-6'>
                        <RadioGroup
                            name='enzymeCleavesTemplate'
                            options={CLEAVES_OPTIONS}
                            value={assay.enzymeCleavesTemplate}
                            onChange={(val) => updateField(path('enzymeCleavesTemplate'), val)}
                            inline
                        />
                    </div>
                </div>
                <WizardField
                    label='Expected wild-type product(s) after digest'
                    id='expectedWtAfterDigest'
                    value={assay.expectedWtAfterDigest}
                    onChange={(val) => updateField(path('expectedWtAfterDigest'), val)}
                />
                <WizardField
                    label='Expected mutant product(s) after digest'
                    id='expectedMutAfterDigest'
                    value={assay.expectedMutAfterDigest}
                    onChange={(val) => updateField(path('expectedMutAfterDigest'), val)}
                />
            </ConditionalField>

            <WizardField
                label='Additional information'
                id='assayAdditionalInfo'
                value={assay.additionalInfo}
                onChange={(val) => updateField(path('additionalInfo'), val)}
                tag='textarea'
                rows={2}
            />
        </div>
    );
};

const Step09_Genotyping = () => {
    const { state, dispatch } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const mutation = state.lines[li].mutations[mi];

    const handleAssayCountChange = (count) => {
        const val = count === '' ? '' : Number(count);
        dispatch({ type: 'UPDATE_FIELD', path: ['lines', li, 'mutations', mi, 'genotypingAssayCount'], value: val });
        if (typeof val === 'number' && val > 0) {
            dispatch({ type: 'SET_GENOTYPING_ASSAY_COUNT', lineIndex: li, mutationIndex: mi, count: val });
        }
    };

    return (
        <div>
            <NumberInput
                label='Number of available genotyping assays'
                id='genotypingAssayCount'
                value={mutation.genotypingAssayCount}
                onChange={handleAssayCountChange}
                min={0}
                max={10}
            />

            <ConditionalField condition={mutation.genotypingAssays.length > 0}>
                <hr />
                <EntityIterator
                    items={mutation.genotypingAssays}
                    currentIndex={state.currentAssayIndex}
                    onSelectIndex={(i) => dispatch({ type: 'SET_CURRENT_ASSAY_INDEX', index: i })}
                    label='Assay'
                    renderItem={(assay, index) => (
                        <AssayFields
                            key={index}
                            assay={assay}
                            basePath={['lines', li, 'mutations', mi, 'genotypingAssays', index]}
                        />
                    )}
                />
            </ConditionalField>
        </div>
    );
};

export default Step09_Genotyping;
