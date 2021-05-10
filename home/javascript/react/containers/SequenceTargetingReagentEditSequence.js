import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import { useForm } from 'react-form';
import http from '../utils/http';
import LoadingButton from '../components/LoadingButton';
import equal from 'fast-deep-equal';
import SequenceTargetingReagentSequenceFields from '../components/marker-edit/SequenceTargetingReagentSequenceFields';

const SequenceTargetingReagentEditSequence = ({ strId }) => {
    const {
        value: strDetails,
        setValue: setStrDetails,
    } = useFetch(
        `/action/str/${strId}/details`,
        {
            defaultValue: {
                reportedSequence1: '',
                sequence1: '',
                reversed1: false,
                complemented1: false,
                reportedSequence2: '',
                sequence2: '',
                reversed2: false,
                complemented2: false,
            }
        }
    );

    const {
        Form,
        reset,
        setFieldValue,
        setMeta,
        values,
        meta: { isValid, isSubmitting, isSubmitted, serverError }
    } = useForm({
        defaultValues: strDetails,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/str/${strId}/details`, {
                    ...values,
                    reportedSequence1: values.reportedSequence1.toUpperCase(),
                });
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setStrDetails(updated);

                // notify the un-related marker notes component
                document.dispatchEvent(new Event('UpdateMarkerNotesList'));
            } catch (error) {
                setMeta({ serverError: error });
                throw error;
            }
        },
    });

    const isTalen = strDetails.type === 'TALEN';
    let validBases = 'ATGC';
    if (isTalen) {
        validBases += 'R';
    }
    const reportedLabel = isTalen ? 'Target Sequence 1 Reported' : 'Reported';

    const isPristine = useMemo(() => equal(values, strDetails), [values, strDetails]);

    return (
        <Form>
            <SequenceTargetingReagentSequenceFields
                complementedField='complemented1'
                displayedSequenceField='sequence1'
                reportedLabel={reportedLabel}
                displayedLabel='Displayed'
                reportedSequenceField='reportedSequence1'
                reversedField='reversed1'
                validBases={validBases}
                values={values}
                setDisplayedSequence={value => setFieldValue('sequence1', value)}
            />

            {isTalen &&
            <div className='mt-4'>
                <SequenceTargetingReagentSequenceFields
                    complementedField='complemented2'
                    displayedSequenceField='sequence2'
                    reportedLabel='Target Sequence 2 Reported'
                    displayedLabel='Displayed'
                    reportedSequenceField='reportedSequence2'
                    reversedField='reversed2'
                    validBases={validBases}
                    values={values}
                    setDisplayedSequence={value => setFieldValue('sequence2', value)}
                />
            </div>
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

                    {isSubmitted && isPristine &&
                    <span className='text-success'><i className='fas fa-check' /> Saved</span>}

                    {serverError && <span className='text-danger'>Update not saved. Try again later.</span>}
                </div>
            </div>
        </Form>
    );
};

SequenceTargetingReagentEditSequence.propTypes = {
    strId: PropTypes.string,
};

export default SequenceTargetingReagentEditSequence;
