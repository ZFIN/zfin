import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import { useForm } from 'react-form';
import http from '../../utils/http';
import equal from 'fast-deep-equal';
import InputField from '../form/InputField';
import LoadingButton from '../LoadingButton';
import useFetch from '../../hooks/useFetch';
import LoadingSpinner from '../LoadingSpinner';
import useCurationTabLoadEvent from '../../hooks/useCurationTabLoadEvent';

const EditOrthologyNote = ({markerId}) => {
    const {
        value: note,
        pending,
        setValue: setNote,
    } = useFetch(`/action/api/marker/${markerId}/orthology-note`);

    useCurationTabLoadEvent('ORTHOLOGY', pending);

    const {
        Form,
        meta: { isSubmitting, isValid, isSubmitted, serverError},
        setMeta,
        reset,
        values
    } = useForm({
        defaultValues: note,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/api/marker/${markerId}/orthology-note`, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setNote(updated);
            } catch (error) {
                setMeta({ serverError: error });
                throw error;
            }
        }
    });

    const isPristine = useMemo(() => equal(values, note), [values, note]);

    if (pending) {
        return <LoadingSpinner />
    }

    if (!note) {
        return null;
    }

    return (
        <Form>
            <div className='form-group'>
                <InputField
                    tag='textarea'
                    rows='3'
                    field='note'
                    id='orthologyNote'
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

EditOrthologyNote.propTypes = {
    markerId: PropTypes.string,
};

export default EditOrthologyNote;
