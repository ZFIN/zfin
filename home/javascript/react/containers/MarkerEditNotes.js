import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import Section from '../components/layout/Section';
import LoadingSpinner from '../components/LoadingSpinner';
import MarkerPublicNoteForm from '../components/marker-edit/MarkerPublicNoteForm';
import MarkerCuratorNotes from '../components/marker-edit/MarkerCuratorNotes';

const MarkerEditNotes = ({currentUserId, markerId}) => {
    const {
        value: allNotes,
        pending,
    } = useFetch(`/action/marker/${markerId}/notes`);

    const [privateNotes, setPrivateNotes] = useState([]);
    const [publicNote, setPublicNote] = useState(null);

    useEffect(() => {
        if (!allNotes) {
            return;
        }
        setPublicNote(allNotes.find(note => note.noteEditMode === 'PUBLIC'));
        setPrivateNotes(allNotes.filter(note => note.noteEditMode === 'PRIVATE'))
    }, [allNotes, setPrivateNotes, setPublicNote]);

    if (pending) {
        return <LoadingSpinner />;
    }

    if (!allNotes) {
        return null;
    }

    return (
        <>
            <Section title='Public Note'>
                <MarkerPublicNoteForm markerId={markerId} note={publicNote} onSave={setPublicNote} />
            </Section>
            <Section title='Curator Notes'>
                <MarkerCuratorNotes
                    currentUserId={currentUserId}
                    markerId={markerId}
                    notes={privateNotes}
                    setNotes={setPrivateNotes}
                />
            </Section>
        </>
    );
};

MarkerEditNotes.propTypes = {
    currentUserId: PropTypes.string,
    markerId: PropTypes.string,
};

export default MarkerEditNotes;
