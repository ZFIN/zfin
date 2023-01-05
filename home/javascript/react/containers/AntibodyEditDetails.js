import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import { useForm } from 'react-form';
import http from '../utils/http';
import FormGroup from '../components/form/FormGroup';
import LoadingButton from '../components/LoadingButton';
import equal from 'fast-deep-equal';
import PublicationInput from '../components/form/PublicationInput';
import DEFAULT_PUB_SUGGESTIONS from '../constants/AntibodyPublications';


const AntibodyEditDetails = ({
    antibodyId,
    hostSpeciesList,
    immunogenSpeciesList,
    heavyChainIsotypes,
    lightChainIsotypes,
    clonalTypes
}) => {
    const {
        value: antibodyDetails,
        setValue: setAntibodyDetails,
    } = useFetch(`/action/api/antibody/${antibodyId}/details`, {
        defaultValue: {
            name: '',
            abregistryID: '',
            hostSpecies: '',
            immunogenSpecies: '',
            heavyChainIsotype: '',
            lightChainIsotype: '',
            clonalType: '',
        }
    });
    const hostSpeciesOptions = JSON.parse(hostSpeciesList);
    const immunogenSpeciesOptions = JSON.parse(immunogenSpeciesList);
    const heavyChainIsotypeOptions = JSON.parse(heavyChainIsotypes);
    const lightChainIsotypeOptions = JSON.parse(lightChainIsotypes);
    const clonalTypeOptions = JSON.parse(clonalTypes);

    const {
        Form,
        reset,
        setMeta,
        values,
        meta: { isValid, isSubmitting, isSubmitted, serverError }
    } = useForm({
        defaultValues: antibodyDetails,
        onSubmit: async (values) => {
            try {
                const url = `/action/api/antibody/${antibodyId}/details` + (values.publicationID ? `?publicationID=${values.publicationID}` : '');
                const updated = await http.post(url, values);
                setMeta({
                    isTouched: false,
                    serverError: null,
                });
                setAntibodyDetails(updated);
            } catch (error) {
                setMeta({ serverError: error });
                throw error;
            }
        },
    });

    const isPristine = useMemo(() => equal(values, antibodyDetails), [values, antibodyDetails]);
    const isNamePristine = useMemo(() => values.name === antibodyDetails.name, [values, antibodyDetails]);

    return (
        <Form>
            <FormGroup
                label='Name'
                field='name'
                id='name'
            />

            {!isNamePristine &&
            <>
                <FormGroup
                    id={`${antibodyId}-pub-id`}
                    label='Publication for Name Change'
                    tag={PublicationInput}
                    field='publicationID'
                    // inputClassName='col-md-10'
                    // onChange={handlePubInputChange}
                    validate={value => {
                        if (!value) {
                            return 'A publication ZDB ID is required for name changes';
                        }
                        return false;
                    }}
                    defaultPubs={DEFAULT_PUB_SUGGESTIONS}
                />
            </>
            }

            <FormGroup
                label='Antibody Registry ID'
                field='abregistryID'
                id='abregistryID'
            />

            <FormGroup
                label='Host Organism'
                field='hostSpecies'
                id='hostSpecies'
                tag='select'
            >
                <option value='' />
                { hostSpeciesOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Immunogen Organism'
                field='immunogenSpecies'
                id='immunogenSpecies'
                tag='select'
            >
                <option value='' />
                { immunogenSpeciesOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Isotype Heavy Chain'
                field='heavyChainIsotype'
                id='heavyChainIsotype'
                tag='select'
            >
                <option value='' />
                { heavyChainIsotypeOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Isotype Light Chain'
                field='lightChainIsotype'
                id='lightChainIsotype'
                tag='select'
            >
                <option value='' />
                { lightChainIsotypeOptions.map(option => <option key={option}>{option}</option>)}
            </FormGroup>

            <FormGroup
                label='Type'
                field='clonalType'
                id='clonalType'
                tag='select'
            >
                <option value='' />
                { clonalTypeOptions.map(option => <option key={option}>{option}</option>)}
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

                    {isSubmitted && isPristine && <span className='text-success'><i className='fas fa-check'/> Saved</span>}

                    {serverError && <span className='text-danger'>Update not saved. Try again later.</span>}
                </div>
            </div>
        </Form>
    );
};

AntibodyEditDetails.propTypes = {
    antibodyId: PropTypes.string,
    hostSpeciesList: PropTypes.string,
    immunogenSpeciesList: PropTypes.string,
    heavyChainIsotypes: PropTypes.string,
    lightChainIsotypes: PropTypes.string,
    clonalTypes: PropTypes.string,
};

export default AntibodyEditDetails;
