import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import { useForm } from 'react-form';
import http from '../../utils/http';
import equal from 'fast-deep-equal';
import InputField from '../form/InputField';
import LoadingButton from '../LoadingButton';

const MarkerPublicNoteForm = ({markerId, note, onSave}) => {
    const defaultValues = note || { noteData: '' };
    const {
        Form,
        meta: { isSubmitting, isValid, isSubmitted, serverError},
        setMeta,
        reset,
        values
    } = useForm({
        defaultValues,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/marker/${markerId}/public-note`, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                onSave(updated);
            } catch (error) {
                setMeta({ serverError: error });
                throw error;
            }
        }
    });

    const isPristine = useMemo(() => equal(values, note), [values, note]);

    return (
        <Form>
            <div className='form-group'>
                <InputField
                    tag='textarea'
                    rows='5'
                    field='noteData'
                    id='publicNote'
                />
            </div>

            <div className='form-group row'>
                <div className='col horizontal-buttons'>
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

MarkerPublicNoteForm.propTypes = {
    markerId: PropTypes.string,
    note: PropTypes.shape({
        noteData: PropTypes.string,
    }),
    onSave: PropTypes.func,
};

export default MarkerPublicNoteForm;
