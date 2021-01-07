import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import Section from '../components/layout/Section';
import LoadingSpinner from '../components/LoadingSpinner';
import MarkerPublicNoteForm from '../components/marker-edit/MarkerPublicNoteForm';
import MarkerCuratorNotes from '../components/marker-edit/MarkerCuratorNotes';
import { stringToBool } from '../utils';
import MarkerExternalNotes from '../components/marker-edit/MarkerExternalNotes';

const MarkerEditNotes = ({ currentUserId, markerId, showExternalNotes = 'false' }) => {
    const {
        value: allNotes,
        pending,
        refetch,
    } = useFetch(`/action/marker/${markerId}/notes`);

    const [privateNotes, setPrivateNotes] = useState([]);
    const [externalNotes, setExternalNotes] = useState([]);
    const [publicNote, setPublicNote] = useState(null);
    const showExternalNotesBool = stringToBool(showExternalNotes);

    // a little hack to allow components without a parent-child relationship to communicate
    useEffect(() => {
        document.addEventListener('UpdateMarkerNotesList', refetch);
        return () => document.removeEventListener('UpdateMarkerNotesList', refetch);
    });

    useEffect(() => {
        if (!allNotes) {
            return;
        }
        setPublicNote(allNotes.find(note => note.noteEditMode === 'PUBLIC'));
        setExternalNotes(allNotes.filter(note => note.noteEditMode === 'EXTERNAL'));
        setPrivateNotes(allNotes.filter(note => note.noteEditMode === 'PRIVATE'));
    }, [allNotes, setPrivateNotes, setPublicNote, setExternalNotes]);

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
            {showExternalNotesBool &&
                <Section title='External Notes'>
                    <MarkerExternalNotes
                        markerId={markerId} 
                        notes={externalNotes}
                        setNotes={setExternalNotes}
                        type='antibody'
                    />
                </Section>
            }
        </>
    );
};

MarkerEditNotes.propTypes = {
    currentUserId: PropTypes.string,
    markerId: PropTypes.string,
    showExternalNotes: PropTypes.string,
};

export default MarkerEditNotes;
