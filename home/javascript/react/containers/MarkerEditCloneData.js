import React, {useMemo} from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import {useForm} from 'react-form';
import http from '../utils/http';
import FormGroup from '../components/form/FormGroup';
import LoadingButton from '../components/LoadingButton';
import equal from 'fast-deep-equal';

const CloneData = ({
    cloneId,
    cloningSiteList,
    libraryList,
    vectorList,
    digestList,
    polymeraseList,
    insertSize,
    pcrAmplification,
}) => {
    const {
        value: cloneData,
        setValue: setCloneData,
    } = useFetch(`/action/api/clone/${cloneId}/data`, {
        defaultValue: {
            cloningSite: '',
            library: '',
        }
    });
    const cloningSiteOptions = JSON.parse(cloningSiteList);
    const libraryOptions = JSON.parse(libraryList);
    const vectorOptions = JSON.parse(vectorList);
    const digestOptions = JSON.parse(digestList);
    const polymeraseOptions = JSON.parse(polymeraseList);

    const {
        Form,
        reset,
        setMeta,
        values,
        meta: {isValid, isSubmitting, isSubmitted, serverError}
    } = useForm({
        defaultValues: cloneData,
        onSubmit: async (values) => {
            try {
                const updated = await http.post(`/action/api/clone/${cloneId}`, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setCloneData(updated);
            } catch (error) {
                setMeta({serverError: error});
                throw error;
            }
        },
    });

    const isPristine = useMemo(() => equal(values, cloneData), [values, cloneData]);

    return (
        <Form>
            <FormGroup
                label='Cloning Site'
                field='cloningSite'
                id='cloningSite'
                tag='select'
            >
                <option value=''/>
                {cloningSiteOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Library'
                field='probeLibraryName'
                id='library'
                tag='select'
            >
                <option value=''/>
                {libraryOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Vector'
                field='vectorName'
                id='vector'
                tag='select'
            >
                <option value=''/>
                {vectorOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Digest'
                field='digest'
                id='digest'
                tag='select'
            >
                <option value=''/>
                {digestOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Polymerase'
                field='polymerase'
                id='polymerase'
                tag='select'
            >
                <option value=''/>
                {polymeraseOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Insert Size'
                field='insertSize'
                id='insertSize'
                validate={(value, { debounce }) => debounce(async () => {
                    if (value === insertSize) {
                        return false;
                    }
                    return value === parseInt(value, 10)
                }, 300)}
            />

            <FormGroup
                label='PCR Amplification'
                field='pcrAmplification'
                id='pcrAmplification'
                tag='textarea'
            >
                {pcrAmplification}
            </FormGroup>


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

                    {serverError && <span className='text-danger'>Update not saved. Try again later.</span>}
                </div>
            </div>
        </Form>
    );
};

CloneData.propTypes = {
    cloneId: PropTypes.string,
    cloningSiteList: PropTypes.string,
    libraryList: PropTypes.string,
    vectorList: PropTypes.string,
    digestList: PropTypes.string,
    polymeraseList: PropTypes.string,
    insertSize: PropTypes.string,
    pcrAmplification: PropTypes.string,
};

export default CloneData;
