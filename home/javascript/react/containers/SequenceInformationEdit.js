import React, {useState} from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from '../components/LoadingSpinner';
import AddSequenceModal from '../components/sequence-edit/AddSequenceModal';
import EditSequenceModal from '../components/sequence-edit/EditSequenceModal';
import DeleteSequenceModal from '../components/sequence-edit/DeleteSequenceModal';
import SequenceInformationTable from '../components/sequence-edit/SequenceInformationTable';
import useFetch from '../hooks/useFetch';

const SequenceInformationEdit = ({markerId}) => {

    const markerLinksHook = useFetch( '/action/marker/' + markerId + '/links?group=marker linked sequence');
    const linkDatabasesHook = useFetch('/action/marker/link/databases?group=dblink adding on marker-edit');
    const [firstLoadComplete, setFirstLoadComplete] = useState(false);

    const [showNewSequenceModal, setShowNewSequenceModal] = useState(false);
    const [editSequenceModalInformation, setEditSequenceModalInformation] = useState(false);
    const [deleteSequenceModalInformation, setDeleteSequenceModalInformation] = useState(false);

    if (!firstLoadComplete) {
        if (markerLinksHook.pending || linkDatabasesHook.pending) {
            return <LoadingSpinner/>;
        }
        if (!markerLinksHook.value || !linkDatabasesHook.value) {
            return null;
        }
        setFirstLoadComplete(true);
    }
    return <>
        {(markerLinksHook.error || linkDatabasesHook.error) && <div className='alert alert-danger'>Error loading sequence information</div>}
        <div className='summaryTitle'>SEQUENCE INFORMATION
            <span className='ml-1' style={{'cursor': 'pointer'}}>{' '}
                <i
                    style={{'color': 'red', 'fontWeight': 'bold'}}
                    title='Create new sequence information'
                    onClick={() => {setShowNewSequenceModal(true)}}
                >New</i>
            </span>
        </div>
        <SequenceInformationTable
            markerLinks={markerLinksHook.value}
            editClicked={(sequence) => {setEditSequenceModalInformation(sequence);}}
            deleteClicked={(sequence) => {setDeleteSequenceModalInformation(sequence);}}
        />
        <AddSequenceModal
            markerId={markerId}
            linkDatabases={linkDatabasesHook.value}
            show={showNewSequenceModal}
            onAdd={() => {markerLinksHook.refetch();}}
            // could this be refactored so that onClose is not needed and the modal is closed by the child component?
            onClose={() => setShowNewSequenceModal(false)}
        />
        <EditSequenceModal
            markerId={markerId}
            sequence={editSequenceModalInformation}
            onEdit={() => {markerLinksHook.refetch()}}
            // could this be refactored so that onClose is not needed and the modal is closed by the child component?
            onClose={() => setEditSequenceModalInformation(false)}
        />
        <DeleteSequenceModal
            sequence={deleteSequenceModalInformation}
            onDelete={() => {markerLinksHook.refetch()}}
            // could this be refactored so that onClose is not needed and the modal is closed by the child component?
            onClose={() => setDeleteSequenceModalInformation(false)}
        />
    </>;
};

SequenceInformationEdit.propTypes = {
    markerId: PropTypes.string,
}

export default SequenceInformationEdit;
