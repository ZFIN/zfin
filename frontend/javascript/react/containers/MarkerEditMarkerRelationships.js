import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import AddEditList from '../components/AddEditList';
import { EntityLink } from '../components/entity';
import LoadingSpinner from '../components/LoadingSpinner';
import useAddEditDeleteForm from '../hooks/useAddEditDeleteForm';
import AddEditDeleteModal from '../components/AddEditDeleteModal';
import FormGroup from '../components/form/FormGroup';
import InputField from '../components/form/InputField';
import PublicationInput from '../components/form/PublicationInput';
import MarkerInput from '../components/form/MarkerInput';
import { stringToBool } from '../utils';

const MarkerEditMarkerRelationships = ({markerAbbreviation, markerId, relationshipTypeData, showRelationshipType = 'true'}) => {

    relationshipTypeData = JSON.parse(relationshipTypeData);

    const relationshipTypeNameList = relationshipTypeData.map(d => d.type).join(',');
    const {
        value: relationships,
        setValue,
        pending,
    } = useFetch(`/action/api/marker/${markerId}/editableRelationships?relationshipTypes=${relationshipTypeNameList}`);
    const [modalRelationship, setModalRelationship] = useState(null);
    const isEdit = modalRelationship && !!modalRelationship.zdbID;

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        setValues,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/marker/${markerId}/relationships`,
        editUrl: isEdit ? `/action/api/marker/relationships/${modalRelationship.zdbID}` : '',
        deleteUrl: isEdit ? `/action/api/marker/relationships/${modalRelationship.zdbID}` : '',
        onSuccess: () => setModalRelationship(null),
        items: relationships,
        setItems: setValue,
        defaultValues: modalRelationship,
        validate: values => {
            if (values && values.references.length === 0) {
                return 'At least one reference is required';
            }
            return false;
        },
    });

    const formatRelationship = ({ markerRelationshipType, firstMarker, secondMarker, references }, editLink) => {
        const is1to2 = firstMarker.zdbID === markerId;
        const typeLabel = is1to2 ? markerRelationshipType.firstToSecondLabel : markerRelationshipType.secondToFirstLabel;
        const displayMarker = is1to2 ? secondMarker : firstMarker;
        return (
            <>
                {stringToBool(showRelationshipType) && typeLabel} <EntityLink entity={displayMarker} />
                {references.length > 0 && <> ({references.length})</>}
                {editLink}
            </>
        );
    };

    // this change needs to be handled manually because the first and second marker values
    // need to be reset when the relationship changes
    const handleRelationshipTypeChange = (event) => {
        const value = event.target.value;
        if (!value) {
            return;
        }
        const selectedType = relationshipTypeData.find(d => d.type === value);
        // these setValues calls would be better as updater functions instead of replacing
        // the whole object, but: https://github.com/tannerlinsley/react-form/issues/376
        if (selectedType['1to2']) {
            setValues({
                ...values,
                markerRelationshipType: { name: value },
                firstMarker: {
                    zdbID: markerId,
                    abbreviation: markerAbbreviation,
                },
                secondMarker: {
                    zdbID: '',
                    abbreviation: '',
                },
            });
        } else {
            setValues({
                ...values,
                markerRelationshipType: { name: value },
                firstMarker: {
                    zdbID: '',
                    abbreviation: '',
                },
                secondMarker: {
                    zdbID: markerId,
                    abbreviation: markerAbbreviation,
                },
            });
        }
    }

    if (pending) {
        return <LoadingSpinner />;
    }

    if (!relationships) {
        return null;
    }

    let isModalRelationship1to2 = false;
    let relatedMarkerTypeGroup = '';
    if (values && values.markerRelationshipType.name) {
        const selectedType = relationshipTypeData.find(d => d.type === values.markerRelationshipType.name);
        isModalRelationship1to2 = selectedType['1to2'];
        relatedMarkerTypeGroup = selectedType.relatedMarkerTypeGroup;
    }

    // because the labels on this form are sort of long
    const labelClass = 'col-md-3 col-form-label';
    const inputClass = 'col-md-9';

    return (
        <>
            <AddEditList
                items={relationships}
                newItem={{
                    markerRelationshipType: { name: '' },
                    firstMarker: { abbreviation: ''},
                    secondMarker: { abbreviation: ''},
                    references: [{ zdbID: '' }],
                }}
                setModalItem={setModalRelationship}
                itemKeyProp='zdbID'
                formatItem={formatRelationship}
            />

            <AddEditDeleteModal {...modalProps} header='Marker Relationship'>
                {values && <>
                    <FormGroup
                        inputClassName={inputClass}
                        labelClassName={labelClass}
                        label='Relationship Type'
                        id='relationship-type'
                        field='markerRelationshipType.name'
                        tag='select'
                        validate={value => value ? false : 'A relationship type is required'}
                        onChange={handleRelationshipTypeChange}
                    >
                        <option value='' />
                        {relationshipTypeData.map(d => (
                            <option value={d.type} key={d.type}>{d.type}</option>
                        ))}
                    </FormGroup>

                    {!isModalRelationship1to2 &&
                        <FormGroup
                            inputClassName={inputClass}
                            labelClassName={labelClass}
                            label='Related Marker'
                            id='first-marker'
                            field='firstMarker.abbreviation'
                            tag={MarkerInput}
                            typeGroup={relatedMarkerTypeGroup}
                            validate={value => value ? false : 'A related marker is required'}
                        />
                    }

                    {isModalRelationship1to2 &&
                        <FormGroup
                            inputClassName={inputClass}
                            labelClassName={labelClass}
                            label='Related Marker'
                            id='second-marker'
                            field='secondMarker.abbreviation'
                            tag={MarkerInput}
                            typeGroup={relatedMarkerTypeGroup}
                            validate={value => value ? false : 'A related marker is required'}
                        />
                    }

                    <div className='form-group row'>
                        <label className={labelClass}>Citations</label>
                        <div className={inputClass}>
                            {
                                values.references.map((reference, idx) => (
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
                                            <i className='fas fa-times' />
                                        </button>
                                    </div>
                                ))
                            }
                            <button
                                type='button'
                                className='btn btn-link px-0'
                                onClick={() => pushFieldValue('references', { zdbID: '' })}
                            >
                                Add Citation
                            </button>
                        </div>
                    </div>
                </>}
            </AddEditDeleteModal>
        </>
    );
};

MarkerEditMarkerRelationships.propTypes = {
    markerAbbreviation: PropTypes.string,
    markerId: PropTypes.string,
    relationshipTypeData: PropTypes.string,
    showRelationshipType: PropTypes.string,
}

export default MarkerEditMarkerRelationships;
