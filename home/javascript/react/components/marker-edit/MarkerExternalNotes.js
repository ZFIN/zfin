import React, { useState } from 'react';
import PropTypes from 'prop-types';
import useAddEditDeleteForm from '../../hooks/useAddEditDeleteForm';
import AddEditList from '../AddEditList';
import AddEditDeleteModal from '../AddEditDeleteModal';
import FormGroup from '../form/FormGroup';
import PublicationInput from '../form/PublicationInput';

const MarkerExternalNotes = ({markerId, notes, setNotes, type}) => {
    const [modalNote, setModalNote] = useState(null);
    const isEdit = modalNote && !!modalNote.zdbID;

    const {
        values,
        modalProps
    } = useAddEditDeleteForm({
        addUrl: `/action/marker/${markerId}/external-notes?type=${type}`,
        editUrl: isEdit ? `/action/marker/external-notes/${modalNote.zdbID}` : '',
        deleteUrl: isEdit ? `/action/marker/external-notes/${modalNote.zdbID}` : '',
        onSuccess: () => setModalNote(null),
        items: notes,
        setItems: setNotes,
        defaultValues: modalNote
    });

    const formatListItem = (note, editLink) => {
        return (
            <>
                <div className='mt-2'>
                    <a href={`/${note.publicationZdbID}`} className='mr-1'>{note.publicationZdbID}</a>
                    {editLink}
                </div>
                <div className='keep-breaks'>{note.noteData}</div>
            </>
        );
    };

    return (
        <>
            <AddEditList
                setModalItem={setModalNote}
                newItem={{ noteData: '' }}
                items={notes}
                itemKeyProp='zdbID'
                formatItem={formatListItem}
            />

            <AddEditDeleteModal {...modalProps} header='External Note'>
                {values && <>
                    <FormGroup
                        inputClassName='col-md-10'
                        tag={PublicationInput}
                        label='Citation'
                        id='externalNotePub'
                        field='publicationZdbID'
                        validate={value => value ? false : 'A publication ZDB ID is required'}
                    />

                    <FormGroup
                        inputClassName='col-md-10'
                        tag='textarea'
                        rows='5'
                        label='Note'
                        id='externalNoteNote'
                        field='noteData'
                        validate={value => value.trim() ? false : 'Note cannot be blank'}
                    />
                </>}
            </AddEditDeleteModal>
        </>
    );
};

MarkerExternalNotes.propTypes = {
    markerId: PropTypes.string,
    notes: PropTypes.array,
    setNotes: PropTypes.func,
    type: PropTypes.string,
};

export default MarkerExternalNotes;
