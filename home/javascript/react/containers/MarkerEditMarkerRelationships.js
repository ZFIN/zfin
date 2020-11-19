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

const MarkerEditMarkerRelationships = ({markerId, relationshipTypes}) => {
    const {
        value: relationships,
        setValue,
        pending,
    } = useFetch(`/action/api/marker/${markerId}/editableRelationships?relationshipTypes=${relationshipTypes}`);
    const [modalRelationship, setModalRelationship] = useState(null);
    const isEdit = modalRelationship && !!modalRelationship.zdbID;

    const {
        pushFieldValue,
        removeFieldValue,
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/api/marker/${markerId}/relationship`,
        editUrl: isEdit ? `/action/marker/relationship/${modalRelationship.zdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/relationship/${modalRelationship.zdbID}` : '',
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
                {typeLabel} <EntityLink entity={displayMarker} />
                {references.length > 0 && <> ({references.length})</>}
                {editLink}
            </>
        );
    };

    if (pending) {
        return <LoadingSpinner />;
    }

    if (!relationships) {
        return null;
    }

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
                        inputClassName='col-md-10'
                        label='Relationship'
                        id='relationship-type'
                        field='markerRelationshipType.name'
                        tag='select'
                        validate={value => value ? false : 'A relationship type is required'}
                    >
                        <option value='' />
                        {relationshipTypes.split(',').map(type => (
                            <option value={type} key={type}>{type}</option>
                        ))}
                    </FormGroup>

                    <FormGroup
                        inputClassName='col-md-10'
                        label='Related Marker'
                        id='first-marker'
                        field='firstMarker.abbreviation'
                    />

                    <FormGroup
                        inputClassName='col-md-10'
                        label='Related Marker'
                        id='second-marker'
                        field='secondMarker.abbreviation'
                    />

                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Citations</label>
                        <div className='col-md-10'>
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
    markerId: PropTypes.string,
    relationshipTypes: PropTypes.string,
}

export default MarkerEditMarkerRelationships;
