import React, { useState } from 'react';
import PropTypes from 'prop-types';
import AddEditList from '../AddEditList';
import useAddEditDeleteForm from '../../hooks/useAddEditDeleteForm';
import AddEditDeleteModal from '../AddEditDeleteModal';
import InputField from '../form/InputField';

const MarkerCuratorNotes = ({currentUserId, markerId, notes, setNotes}) => {
    const [modalNote, setModalNote] = useState(null);
    const isEdit = modalNote && !!modalNote.zdbID;

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/curator-notes`,
        editUrl: isEdit ? `/action/marker/${markerId}/curator-notes/${modalNote.zdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/${markerId}/curator-notes/${modalNote.zdbID}` : '',
        onSuccess: () => setModalNote(null),
        items: notes,
        setItems: setNotes,
        defaultValues: modalNote
    });

    const formatListItem = (note, editLink) => {
        const noteDate = new Date(note.date);
        return (
            <>
                <div className='mt-2'>
                    <b className='mr-1'>{note.curator.firstName} {note.curator.lastName}</b>
                    {noteDate.toLocaleString()}
                    {note.curator.zdbID === currentUserId && editLink}
                </div>
                <div className='keep-breaks'>{note.noteData}</div>
            </>
        )
    }

    return (
        <>
            <AddEditList
                setModalItem={setModalNote}
                newItem={{ noteData: '' }}
                items={notes}
                itemKeyProp='zdbID'
                formatItem={formatListItem}
            />
            <AddEditDeleteModal {...modalProps} header='Curator Note'>
                {values && (
                    <div className='form-group'>
                        <InputField
                            tag='textarea'
                            rows='5'
                            field='noteData'
                            id='curatorNoteEdit'
                            validate={value => value.trim() ? false : 'Note cannot be blank'}
                        />
                    </div>
                )}
            </AddEditDeleteModal>
        </>
    );
};

MarkerCuratorNotes.propTypes = {
    currentUserId: PropTypes.string,
    markerId: PropTypes.string,
    notes: PropTypes.arrayOf(PropTypes.shape({
        zdbID: PropTypes.string,
        noteData: PropTypes.string,
    })),
    setNotes: PropTypes.func,
};

export default MarkerCuratorNotes;
