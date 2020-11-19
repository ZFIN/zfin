import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import AddEditList from '../components/AddEditList';
import { EntityLink } from '../components/entity';
import LoadingSpinner from '../components/LoadingSpinner';

const MarkerEditMarkerRelationships = ({markerId, relationshipTypes}) => {
    const {
        value: relationships,
        pending,
    } = useFetch(`/action/api/marker/${markerId}/editableRelationships?relationshipTypes=${relationshipTypes}`);
    // eslint-disable-next-line no-unused-vars
    const [modalRelationship, setModalRelationship] = useState(null);

    const formatRelationship = ({ markerRelationshipType, firstMarker, secondMarker, publications }, editLink) => {
        const is1to2 = firstMarker.zdbID === markerId;
        const typeLabel = is1to2 ? markerRelationshipType.firstToSecondLabel : markerRelationshipType.secondToFirstLabel;
        const displayMarker = is1to2 ? secondMarker : firstMarker;
        return (
            <>
                {typeLabel} <EntityLink entity={displayMarker} />
                {publications.length > 0 && <> ({publications.length})</>}
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
                newItem={{}}
                setModalItem={setModalRelationship}
                itemKeyProp='markerRelationshipZdbId'
                formatItem={formatRelationship}
            />
        </>
    );
};

MarkerEditMarkerRelationships.propTypes = {
    markerId: PropTypes.string,
    relationshipTypes: PropTypes.string,
}

export default MarkerEditMarkerRelationships;
