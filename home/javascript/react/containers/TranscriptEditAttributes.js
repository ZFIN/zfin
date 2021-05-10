import React, {useMemo} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import {useForm} from 'react-form';
import http from '../utils/http';
import FormGroup from '../components/form/FormGroup';
import LoadingButton from '../components/LoadingButton';
import equal from 'fast-deep-equal';
import PublicationInput from '../components/form/PublicationInput';
import InputField from '../components/form/InputField';

const TranscriptEditAttributes = ({
    transcriptId,
    transcriptTypes,
    transcriptStatus
}) => {
    const {
        value: transcriptAttributes,
        setValue: setTranscriptAttributes,
    } = useFetch(`/action/api/transcript/${transcriptId}/details`, {
        defaultValue: {
            transcriptType: '',
            transcriptStatus: '',
            references: [{zdbID: ''}],
        }
    });


    const transcriptTypeOptions = JSON.parse(transcriptTypes);
    const transcriptStatusOptions = JSON.parse(transcriptStatus);


    const {
        Form,
        reset,
        setMeta,
        references,
        values,
        pushFieldValue,
        removeFieldValue,
        meta: {isValid, isSubmitting, isSubmitted, serverError}
    } = useForm({
        defaultValues: transcriptAttributes, references,
        validate: values => {
            if (values && values.references.length === 0) {
                return 'At least one reference is required';
            }
            return false;
        },
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/api/transcript/${transcriptId}/details`, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setTranscriptAttributes(updated);
            } catch (error) {
                setMeta({serverError: error});
                throw error;
            }
        },
    });

    const isPristine = useMemo(() => equal(values, transcriptAttributes), [values, transcriptAttributes]);
    const labelClass = 'col-md-3 col-form-label';
    const inputClass = 'offset-md-2 col-md-10';


    return (
        <Form>

            <FormGroup
                label='Transcript Type'
                field='transcriptType'
                id='transcriptType'
                tag='select'
            >
                <option value=''/>
                {transcriptTypeOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Transcript Status'
                field='transcriptStatus'
                id='status'
                tag='select'
            >
                <option value=''/>
                {transcriptStatusOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>


            <div className='form-group row'>
                <label className={labelClass}>Citations</label>
                <div className={inputClass}>
                    {
                        values && values.references.map((reference, idx) => (
                            <div key={idx} className={`d-flex align-items-baseline ${idx > 0 ? 'mt-2' : ''}`}>
                                <div className='flex-grow-1'>
                                    <InputField
                                        tag={PublicationInput}
                                        field={`references.${idx}.zdbID`}
                                        validate={value => {
                                            if (!value) {
                                                return 'A publication ZDB ID is required';
                                            }
                                            return false
                                        }}
                                    />
                                </div>
                                <button
                                    type='button'
                                    onClick={() => removeFieldValue('references', idx)}
                                    className='btn btn-link'
                                >
                                    <i className='fas fa-times'/>
                                </button>
                            </div>
                        ))
                    }

                    <button
                        type='button'
                        className='btn btn-link px-0'
                        onClick={() => pushFieldValue('references', {zdbID: ''})}
                    >
                        Add Citation
                    </button>
                </div>
            </div>


            <div className='form-group row'>
                <div className='offset-md-2 col-md-10 horizontal-buttons'>
                    <button
                        type='button'
                        className='btn btn-outline-secondary'
                        disabled={isSubmitting || isPristine}
                        onClick={reset}
                    >
                        Reset
                    </button>

                    <LoadingButton
                        loading={isSubmitting}
                        type='submit'
                        className='btn btn-primary'
                        disabled={isSubmitting || isPristine || !isValid}
                    >
                        Save
                    </LoadingButton>

                    {isSubmitted && isPristine &&
                    <span className='text-success'><i className='fas fa-check'/> Saved</span>}

                    {serverError && <span className='text-danger'>Update not saved. Try again later. Check Transcript Status-Type mismatch</span>}

                </div>
            </div>
        </Form>
    );
};

TranscriptEditAttributes.propTypes = {
    transcriptId: PropTypes.string,
    transcriptTypes: PropTypes.string,
    transcriptStatus: PropTypes.string,
};

export default TranscriptEditAttributes;
