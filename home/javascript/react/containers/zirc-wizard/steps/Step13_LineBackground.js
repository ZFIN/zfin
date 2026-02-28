import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import ConditionalField from '../components/ConditionalField';

const YES_NO = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
];

const Step13_LineBackground = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const bg = state.lines[li].background;

    const path = (field) => ['lines', li, 'background', field];

    return (
        <div>
            <WizardField
                label='Maternal background'
                id='maternal'
                value={bg.maternal}
                onChange={(val) => updateField(path('maternal'), val)}
            />
            <WizardField
                label='Paternal background'
                id='paternal'
                value={bg.paternal}
                onChange={(val) => updateField(path('paternal'), val)}
            />

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Can the background be changed?</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='canBeChanged'
                        options={YES_NO}
                        value={bg.canBeChanged === true ? 'yes' : bg.canBeChanged === false ? 'no' : ''}
                        onChange={(val) => updateField(path('canBeChanged'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            <ConditionalField condition={bg.canBeChanged === false}>
                <WizardField
                    label='Please specify the potential concerns if the background needs to be changed'
                    id='changeConcerns'
                    value={bg.changeConcerns}
                    onChange={(val) => updateField(path('changeConcerns'), val)}
                    tag='textarea'
                    rows={3}
                />
            </ConditionalField>

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>
                    Are there any unreported genetic/genomic features present in the line background?
                </label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='unreportedFeatures'
                        options={YES_NO}
                        value={bg.unreportedFeatures === true ? 'yes' : bg.unreportedFeatures === false ? 'no' : ''}
                        onChange={(val) => updateField(path('unreportedFeatures'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            <ConditionalField condition={bg.unreportedFeatures === true}>
                <WizardField
                    label='Please specify'
                    id='unreportedFeaturesDetail'
                    value={bg.unreportedFeaturesDetail}
                    onChange={(val) => updateField(path('unreportedFeaturesDetail'), val)}
                    tag='textarea'
                    rows={3}
                />
            </ConditionalField>
        </div>
    );
};

export default Step13_LineBackground;
