import React, {Component} from 'react';
import PropTypes from 'prop-types';
import PubEditableNote from './PubEditableNote';

class PubTrackerNotes extends Component {
    constructor(props) {
        super(props);
        this.state = {
            newNote: '',
        };
        this.handleNewNoteChange = this.handleNewNoteChange.bind(this);
        this.handleNewNoteSave = this.handleNewNoteSave.bind(this);
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
        const { notes, onDeleteNote, onEditNote } = this.props;
        const { newNote } = this.state;
        return (
            <div>
                <form role='form'>
                    <div className='form-group'>
                        <label>New Note</label>
                        <textarea value={newNote} className='form-control' rows='3' onChange={this.handleNewNoteChange}/>
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

PubTrackerNotes.propTypes = {
    onAddNote: PropTypes.func,
    onEditNote: PropTypes.func,
    onDeleteNote: PropTypes.func,
    notes: PropTypes.array,
};

export default PubTrackerNotes;
