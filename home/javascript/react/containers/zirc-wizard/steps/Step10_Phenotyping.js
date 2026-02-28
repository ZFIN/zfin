import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import NumberInput from '../components/NumberInput';
import EntityIterator from '../components/EntityIterator';
import ConditionalField from '../components/ConditionalField';
import ImageUpload from '../components/ImageUpload';

const MUTATION_TYPES = [
    { value: 'null', label: 'Null (complete loss of function)' },
    { value: 'hypomorphic', label: 'Hypomorphic (partial loss of function)' },
    { value: 'hypermorphic', label: 'Hypermorphic (gain of function, increased activity)' },
    { value: 'neomorphic', label: 'Neomorphic (gain of function, new function)' },
    { value: 'other', label: 'Other (please specify)' },
    { value: 'unknown', label: 'Unknown' },
];

const SEGREGATION_TYPES = [
    { value: 'mendelian_recessive', label: 'Mendelian recessive' },
    { value: 'mendelian_dominant', label: 'Mendelian dominant' },
    { value: 'non_mendelian', label: 'Non-Mendelian (enter %)' },
];

const PHENOTYPE_TYPES = [
    { value: 'zygotic', label: 'Zygotic (Z)' },
    { value: 'maternal', label: 'Maternal (M)' },
    { value: 'maternal_zygotic', label: 'Maternal-Zygotic (M-Z)' },
    { value: 'other', label: 'Other (please specify)' },
];

const PhenotypeFields = ({ phenotype, basePath }) => {
    const { updateField } = useWizard();
    const path = (field) => [...basePath, field];

    return (
        <div>
            <WizardField
                label='Description of the phenotype'
                id='phenoDescription'
                value={phenotype.description}
                onChange={(val) => updateField(path('description'), val)}
                tag='textarea'
                rows={3}
            />
            <WizardField
                label='Optimal stage to ID by phenotype (hpf)'
                id='optimalStageHpf'
                value={phenotype.optimalStageHpf}
                onChange={(val) => updateField(path('optimalStageHpf'), val)}
            />
            <WizardField
                label='Stage name'
                id='optimalStageName'
                value={phenotype.optimalStageName}
                onChange={(val) => updateField(path('optimalStageName'), val)}
            />

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Images of the phenotype</label>
                <div className='col-md-6'>
                    <ImageUpload
                        files={phenotype.imageFiles}
                        onFilesChange={(files) => updateField(path('imageFiles'), files)}
                        permissionGranted={phenotype.imagePermission}
                        onPermissionChange={(val) => updateField(path('imagePermission'), val)}
                    />
                </div>
            </div>

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Segregation</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='segregation'
                        options={SEGREGATION_TYPES}
                        value={phenotype.segregation}
                        onChange={(val) => updateField(path('segregation'), val)}
                    />
                    <ConditionalField condition={phenotype.segregation === 'non_mendelian'}>
                        <WizardField
                            label='Percentage (%)'
                            id='segregationPercent'
                            value={phenotype.segregationPercent}
                            onChange={(val) => updateField(path('segregationPercent'), val)}
                            labelClassName='col-md-4 col-form-label'
                            inputClassName='col-md-3'
                        />
                    </ConditionalField>
                </div>
            </div>

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Phenotype type</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='phenotypeType'
                        options={PHENOTYPE_TYPES}
                        value={phenotype.phenotypeType}
                        onChange={(val) => updateField(path('phenotypeType'), val)}
                    />
                    <ConditionalField condition={phenotype.phenotypeType === 'other'}>
                        <WizardField
                            label='Please specify'
                            id='phenotypeTypeOther'
                            value={phenotype.phenotypeTypeOther}
                            onChange={(val) => updateField(path('phenotypeTypeOther'), val)}
                        />
                    </ConditionalField>
                </div>
            </div>
        </div>
    );
};

const Step10_Phenotyping = () => {
    const { state, dispatch, updateField } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const mutation = state.lines[li].mutations[mi];

    const mPath = (field) => ['lines', li, 'mutations', mi, field];

    const handlePhenotypeCountChange = (count) => {
        const val = count === '' ? '' : Number(count);
        updateField(mPath('phenotypeCount'), val);
        if (typeof val === 'number' && val > 0) {
            dispatch({ type: 'SET_PHENOTYPE_COUNT', lineIndex: li, mutationIndex: mi, count: val });
        }
    };

    return (
        <div>
            <h6>Mutation Type</h6>
            <div className='mb-3'>
                <RadioGroup
                    name='mutationType'
                    options={MUTATION_TYPES}
                    value={mutation.mutationType}
                    onChange={(val) => updateField(mPath('mutationType'), val)}
                />
                <ConditionalField condition={mutation.mutationType === 'other'}>
                    <WizardField
                        label='Please specify'
                        id='mutationTypeOther'
                        value={mutation.mutationTypeOther}
                        onChange={(val) => updateField(mPath('mutationTypeOther'), val)}
                    />
                </ConditionalField>
            </div>

            <hr />
            <h6>Specific Phenotypes</h6>
            <NumberInput
                label='Number of phenotypes characteristic for the mutation'
                id='phenotypeCount'
                value={mutation.phenotypeCount}
                onChange={handlePhenotypeCountChange}
                min={0}
                max={20}
            />

            <ConditionalField condition={mutation.phenotypes.length > 0}>
                <EntityIterator
                    items={mutation.phenotypes}
                    currentIndex={state.currentPhenotypeIndex}
                    onSelectIndex={(i) => dispatch({ type: 'SET_CURRENT_PHENOTYPE_INDEX', index: i })}
                    label='Phenotype'
                    renderItem={(phenotype, index) => (
                        <PhenotypeFields
                            key={index}
                            phenotype={phenotype}
                            basePath={['lines', li, 'mutations', mi, 'phenotypes', index]}
                        />
                    )}
                />
            </ConditionalField>
        </div>
    );
};

export default Step10_Phenotyping;
