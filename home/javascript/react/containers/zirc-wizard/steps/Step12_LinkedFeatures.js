import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import CheckboxGroup from '../components/CheckboxGroup';
import ConditionalField from '../components/ConditionalField';

const YES_NO = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
];

const DISTANCE_UNITS = [
    { value: 'cM', label: 'centimorgans (cM)' },
    { value: 'Mb', label: 'megabases (Mb)' },
];

const Step12_LinkedFeatures = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const line = state.lines[li];
    const linked = line.linkedFeatures;

    const path = (field) => ['lines', li, 'linkedFeatures', field];

    // Build feature options from mutations and transgenes
    const featureOptions = [];
    line.mutations.forEach((m, i) => {
        featureOptions.push({
            value: `mutation_${i}`,
            label: `Mutation ${i + 1}${m.alleleDesignation ? ` (${m.alleleDesignation})` : ''}`,
        });
    });
    line.transgenes.forEach((t, i) => {
        featureOptions.push({
            value: `transgene_${i}`,
            label: `Transgene ${i + 1}${t.designation ? ` (${t.designation})` : ''}`,
        });
    });

    return (
        <div>
            <div className='form-group row'>
                <label className='col-md-6 col-form-label'>
                    Are there any features in the line that do not segregate independently
                    during line propagation because they are linked with each other?
                </label>
                <div className='col-md-4'>
                    <RadioGroup
                        name='areAnyLinked'
                        options={YES_NO}
                        value={linked.areAnyLinked === true ? 'yes' : linked.areAnyLinked === false ? 'no' : ''}
                        onChange={(val) => updateField(path('areAnyLinked'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            <ConditionalField condition={linked.areAnyLinked === true}>
                <h6>Please mark the features that are linked with each other:</h6>
                <div className='mb-3'>
                    <CheckboxGroup
                        name='linkedIndices'
                        options={featureOptions}
                        values={linked.linkedIndices}
                        onChange={(val) => updateField(path('linkedIndices'), val)}
                    />
                </div>

                <div className='form-group row'>
                    <label className='col-md-4 col-form-label'>Is the distance between linked features known?</label>
                    <div className='col-md-6'>
                        <RadioGroup
                            name='distanceKnown'
                            options={YES_NO}
                            value={linked.distanceKnown === true ? 'yes' : linked.distanceKnown === false ? 'no' : ''}
                            onChange={(val) => updateField(path('distanceKnown'), val === 'yes')}
                            inline
                        />
                    </div>
                </div>

                <ConditionalField condition={linked.distanceKnown === true}>
                    <WizardField
                        label='Distance'
                        id='distanceValue'
                        value={linked.distanceValue}
                        onChange={(val) => updateField(path('distanceValue'), val)}
                    />
                    <div className='form-group row'>
                        <label className='col-md-4 col-form-label'>Unit</label>
                        <div className='col-md-6'>
                            <RadioGroup
                                name='distanceUnit'
                                options={DISTANCE_UNITS}
                                value={linked.distanceUnit}
                                onChange={(val) => updateField(path('distanceUnit'), val)}
                                inline
                            />
                        </div>
                    </div>
                </ConditionalField>

                <WizardField
                    label='Additional information'
                    id='linkedAdditionalInfo'
                    value={linked.additionalInfo}
                    onChange={(val) => updateField(path('additionalInfo'), val)}
                    tag='textarea'
                    rows={3}
                />
            </ConditionalField>
        </div>
    );
};

export default Step12_LinkedFeatures;
