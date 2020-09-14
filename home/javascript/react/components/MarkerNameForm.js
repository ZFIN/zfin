import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import useMutableFetch from '../hooks/useMutableFetch';
import equal from 'fast-deep-equal';
import { useForm } from 'react-form';
import http from '../utils/http';
import InputField from './form/InputField';
import LoadingButton from './LoadingButton';

const MarkerNameForm = ({markerId}) => {
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
        values,
        meta: { isValid, isSubmitting, isSubmitted, serverError }
    } = useForm({
        debugForm: true,
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

    // not sure if we'll want to do this on other forms. if we do, it may be worth looking at packages
    // other than react-form because it doesn't have a strong distinction between "touched" and "changed"
    const isPristine = useMemo(() => equal(values, nomenclature), [values, nomenclature]);

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
                            const validation = await http.get(`/action/marker/validate?name=${value}`);
                            return validation.errors[0] || false;
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
                            const validation = await http.get(`/action/marker/validate?abbreviation=${value}`);
                            return validation.errors[0] || false;
                        }, 300)}
                    />
                </div>
            </div>

            {!isPristine &&
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

                    {isSubmitted && <span className='text-success'><i className='fas fa-check'/> Saved</span>}

                    {serverError && <span className='text-danger'>Update not saved. Try again later.</span>}
                </div>
            </div>
        </Form>
    );
};

MarkerNameForm.propTypes = {
    markerId: PropTypes.string,
};

export default MarkerNameForm;
