import React from 'react';
import PropTypes from 'prop-types';
import { useForm } from 'react-form';
import InputField from '../components/form/InputField';
import LoadingButton from '../components/LoadingButton';
import http from '../utils/http';
import useMutableFetch from '../hooks/useMutableFetch';

const MarkerEditNomenclature = ({markerId}) => {
    const {
        value: nomenclature,
        setValue
    } = useMutableFetch(
        `/action/marker/${markerId}/nomenclature`,
        {
            name: '',
            abbreviation: '',
        }
    );

    const {
        Form,
        reset,
        setMeta,
        meta: { isTouched, isValid, isSubmitting, isSubmitted, serverError }
    } = useForm({
        defaultValues: nomenclature,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/marker/${markerId}/nomenclature`, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setValue(updated);
            } catch (error) {
                setMeta({ serverError: error });
                throw error;
            }
        },
    });

    return (
        <Form>
            <div className='form-group row'>
                <label htmlFor='inputName' className='col-md-2 col-form-label'>Name</label>
                <div className='col-md-4'>
                    <InputField
                        field='name'
                        id='inputName'
                        validate={(value, { debounce }) => debounce(async () => {
                            if (value === nomenclature.name) {
                                return false;
                            }
                            if (!value) {
                                return 'A name is required';
                            }
                            const markerExists = await http.get(`/action/marker/lookup?markerName=${value}`);
                            if (markerExists) {
                                return 'This name is already in use';
                            }
                            return false;
                        }, 300)}
                    />
                </div>
            </div>

            <div className='form-group row'>
                <label htmlFor='inputName' className='col-md-2 col-form-label'>Abbreviation</label>
                <div className='col-md-4'>
                    <InputField
                        field='abbreviation'
                        id='inputName'
                        validate={(value, { debounce }) => debounce(async () => {
                            if (value === nomenclature.abbreviation) {
                                return false;
                            }
                            if (!value) {
                                return 'An abbreviation is required';
                            }
                            const markerExists = await http.get(`/action/marker/lookup?markerAbbrev=${value}`);
                            if (markerExists) {
                                return 'This abbreviation is already in use';
                            }
                            return false;
                        }, 300)}
                    />
                </div>
            </div>

            {isTouched &&
                <>
                    <div className='form-group row'>
                        <label htmlFor='inputReason' className='col-md-2 col-form-label'>Reason</label>
                        <div className='col-md-4'>
                            <InputField
                                tag='select'
                                field='reason'
                                id='inputReason'
                            >
                                <option />
                                {nomenclature.meta.reasons.map(reason => <option key={reason}>{reason}</option>)}
                            </InputField>
                        </div>
                    </div>

                    <div className='form-group row'>
                        <label htmlFor='inputComments' className='col-md-2 col-form-label'>Comments</label>
                        <div className='col-md-4'>
                            <InputField
                                tag='textarea'
                                field='comments'
                                id='inputReason'
                                rows='3'
                            />
                        </div>
                    </div>
                </>
            }

            <div className='form-group row'>
                <div className='offset-md-2 col-md-10 horizontal-buttons'>
                    <button
                        type='button'
                        className='btn btn-outline-secondary'
                        disabled={isSubmitting || !isTouched}
                        onClick={reset}
                    >
                        Reset
                    </button>

                    <LoadingButton
                        loading={isSubmitting}
                        type='submit'
                        className='btn btn-primary'
                        disabled={isSubmitting || !isTouched || !isValid}
                    >
                        Save
                    </LoadingButton>

                    {isSubmitted && <span className='text-success'><i className='fas fa-check'/> Saved</span>}

                    {serverError && <span className='text-danger'>Update not saved. Try again later.</span>}
                </div>
            </div>
        </Form>
    );
};

MarkerEditNomenclature.propTypes = {
    markerId: PropTypes.string.isRequired,
};

export default MarkerEditNomenclature;
