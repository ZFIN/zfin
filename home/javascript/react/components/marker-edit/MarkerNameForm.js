import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import equal from 'fast-deep-equal';
import { useForm } from 'react-form';
import http from '../../utils/http';
import LoadingButton from '../LoadingButton';
import FormGroup from '../form/FormGroup';

const MarkerNameForm = ({
    markerId,
    nomenclature,
    setNomenclature,
    onSave,
    showAbbreviationField = true,
    showReasonFields = true
}) => {
    const {
        Form,
        reset,
        setMeta,
        values,
        meta: { isValid, isSubmitting, isSubmitted, serverError }
    } = useForm({
        defaultValues: nomenclature,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/marker/${markerId}/nomenclature`, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setNomenclature(updated);
                onSave();
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
            <FormGroup
                label='Name'
                field='name'
                id='inputName'
                validate={(value, { debounce }) => debounce(async () => {
                    if (value === nomenclature.name) {
                        return false;
                    }
                    if (!value) {
                        return 'A name is required';
                    }
                    const validation = await http.get(`/action/marker/${markerId}/validate?name=${value}`);
                    return validation.errors[0] || false;
                }, 300)}
            />

            { showAbbreviationField &&
                <FormGroup
                    label='Abbreviation'
                    field='abbreviation'
                    id='inputAbbreviation'
                    validate={(value, { debounce }) => debounce(async () => {
                        if (value === nomenclature.abbreviation) {
                            return false;
                        }
                        if (!value) {
                            return 'An abbreviation is required';
                        }
                        const validation = await http.get(`/action/marker/${markerId}/validate?abbreviation=${value}`);
                        return validation.errors[0] || false;
                    }, 300)}
                />
            }

            {!isPristine && showReasonFields &&
            <>
                <FormGroup
                    label='Reason'
                    tag='select'
                    field='reason'
                    id='inputReason'
                >
                    <option />
                    {nomenclature.meta.reasons.map(reason => <option key={reason}>{reason}</option>)}
                </FormGroup>

                <FormGroup
                    label='Comments'
                    tag='textarea'
                    field='comments'
                    id='inputComments'
                    rows='3'
                />
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

                    {isSubmitted && isPristine && <span className='text-success'><i className='fas fa-check'/> Saved</span>}

                    {serverError && <span className='text-danger'>Update not saved. Try again later.</span>}
                </div>
            </div>
        </Form>
    );
};

MarkerNameForm.propTypes = {
    markerId: PropTypes.string,
    nomenclature: PropTypes.object,
    setNomenclature: PropTypes.func,
    onSave: PropTypes.func,
    showAbbreviationField: PropTypes.bool,
    showReasonFields: PropTypes.bool,
};

export default MarkerNameForm;
