import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import InputField from '../form/InputField';
import SequenceInput from '../form/SequenceInput';
import InputCheckbox from '../form/InputCheckbox';
import FormGroup from '../form/FormGroup';

function reverseString(str) {
    return str.split('').reverse().join('');
}

function complementString(str) {
    return str.replace(/[ATCG]/gi, match => {
        switch (match.toUpperCase()) {
        case 'A':
            return 'T';
        case 'T':
            return 'A';
        case 'C':
            return 'G';
        case 'G':
            return 'C';
        default:
            return match.toUpperCase();
        }
    });
}

const SequenceTargetingReagentSequenceFields = ({
    validBases,
    reportedLabel,
    displayedLabel,
    values,
    reportedSequenceField,
    reversedField,
    complementedField,
    displayedSequenceField,
    setDisplayedSequence,
}) => {
    useEffect(() => {
        let sequence = values[reportedSequenceField].toUpperCase();
        if (values[reversedField]) {
            sequence = reverseString(sequence);
        }
        if (values[complementedField]) {
            sequence = complementString(sequence)
        }
        setDisplayedSequence(sequence);
    }, [values[reportedSequenceField], values[reversedField], values[complementedField]]);

    return (
        <>
            <div className='form-group row'>
                <label htmlFor='reportedSequence1' className='col-md-2 col-form-label'>{reportedLabel}</label>
                <div className='col-md-8'>
                    <InputField
                        field={reportedSequenceField}
                        id={reportedSequenceField}
                        tag={SequenceInput}
                        validate={value => {
                            if (!value) {
                                return 'A sequence is required';
                            }
                            if (value.match(new RegExp('[^' + validBases + ']', 'i')) !== null) {
                                return 'Invalid sequence string. Only ' + validBases + ' allowed.';
                            }
                            return false;
                        }}
                    />
                    <div className='form-check form-check-inline'>
                        <InputCheckbox
                            field={reversedField}
                            id={reversedField}
                        /><label className='form-check-label' htmlFor='reversed1'>Reverse</label>
                    </div>
                    <div className='form-check form-check-inline'>
                        <InputCheckbox
                            field={complementedField}
                            id={complementedField}
                        /><label className='form-check-label' htmlFor='complemented1'>Complement</label>
                    </div>
                    {(values[reversedField] || values[complementedField]) &&
                        <div className='text-muted'>
                            A curator note will be added to describe alteration
                        </div>
                    }
                </div>
            </div>

            <FormGroup
                label={displayedLabel}
                field={displayedSequenceField}
                id={displayedSequenceField}
                tag={SequenceInput}
                inputClassName='col-md-8'
                disabled
            />
        </>
    );
};

SequenceTargetingReagentSequenceFields.propTypes = {
    validBases: PropTypes.string,
    reportedLabel: PropTypes.string,
    displayedLabel: PropTypes.string,
    values: PropTypes.object,
    reportedSequenceField: PropTypes.string,
    reversedField: PropTypes.string,
    complementedField: PropTypes.string,
    displayedSequenceField: PropTypes.string,
    setDisplayedSequence: PropTypes.func,
};

export default SequenceTargetingReagentSequenceFields;