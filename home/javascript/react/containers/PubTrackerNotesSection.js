import React from 'react';
import PropTypes from 'prop-types';
import produce from 'immer';
import {addNote, deleteNote, getNotes, updateNote} from '../api/publication';
import PubEditableNote from '../components/PubEditableNote';


class PubTrackerNotesSection extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            notes: [],
            newNote: ''
        };
        this.handleAddNote = this.handleAddNote.bind(this);
        this.handleEditNote = this.handleEditNote.bind(this);
        this.handleDeleteNote = this.handleDeleteNote.bind(this);
        this.handleNewNoteChange = this.handleNewNoteChange.bind(this);
        this.handleNewNoteSave = this.handleNewNoteSave.bind(this);
    }

    componentDidMount() {
        const {pubId} = this.props;
        getNotes(pubId).then(notes => this.setState({notes}));

    }

    handleAddNote(note) {
        return addNote(this.props.pubId, note).then(note => {
            this.setState(produce(state => {
                state.notes.unshift(note);
            }));
        });
    }

    handleEditNote(note) {
        const {notes} = this.state;
        const idx = notes.findIndex(other => other.zdbID === note.zdbID);
        return updateNote(note.zdbID, note).then(note => {
            this.setState(produce(state => {
                state.notes[idx] = note;
            }));
        });
    }

    handleDeleteNote(note) {
        const {notes} = this.state;
        const idx = notes.findIndex(other => other.zdbID === note.zdbID);
        return deleteNote(note.zdbID).then(() => {
            this.setState(produce(state => {
                state.notes.splice(idx, 1);
            }));
        });
    }

    handleNewNoteChange(event) {
        this.setState({
            newNote: event.target.value,
        })
    }

    handleNewNoteSave() {
        this.props.onAddNote({text: this.state.newNote}).then(() => this.setState({newNote: ''}));
    }

    render() {
        const {onDeleteNote, onEditNote} = this.props;
        const {notes, newNote} = this.state;
        return (
            <div>
                <form role='form'>
                    <div className='form-group'>
                        <label>New Note</label>
                        <textarea
                            value={newNote}
                            className='form-control'
                            rows='3'
                            onChange={this.handleNewNoteChange}
                        />
                    </div>
                    <button onClick={this.handleNewNoteSave} type='button' className='btn btn-primary'>Post</button>
                </form>
                <hr/>
                {notes.length === 0 && <p className='text-muted text-center'>No notes yet</p>}
                {notes.map(note => (
                    <PubEditableNote
                        key={note.zdbID}
                        note={note}
                        onDeleteNote={onDeleteNote}
                        onSaveNote={onEditNote}
                    />
                ))}
            </div>
        );
    }
}

PubTrackerNotesSection.propTypes = {
    pubId: PropTypes.string,
    onAddNote: PropTypes.func,
    onEditNote: PropTypes.func,
    onDeleteNote: PropTypes.func
};

export default PubTrackerNotesSection;