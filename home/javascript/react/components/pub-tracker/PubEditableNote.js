import React, {Component} from 'react';
import PropTypes from 'prop-types';

class PubEditableNote extends Component {
    constructor(props) {
        super(props);
        this.state = {
            editing: false,
            editText: '',
        };
        this.handleDeleteClick = this.handleDeleteClick.bind(this);
        this.handleEditClick = this.handleEditClick.bind(this);
        this.handleNoteChange = this.handleNoteChange.bind(this);
        this.handleEditSave = this.handleEditSave.bind(this);
        this.handleEditCancel= this.handleEditCancel.bind(this);
    }

    handleEditClick(event) {
        event.preventDefault();
        this.setState({
            editing: true,
            editText: this.props.note.text,
        });
    }

    handleDeleteClick(event) {
        const { onDeleteNote, note } = this.props;
        event.preventDefault();
        onDeleteNote(note);
    }

    handleNoteChange(event) {
        this.setState({
            editText: event.target.value,
        });
    }

    handleEditSave() {
        const note = {
            ...this.props.note,
            text: this.state.editText
        };
        this.props.onSaveNote(note).then(() => this.setState({
            editing: false
        }));
    }

    handleEditCancel() {
        this.setState({
            editing: false
        });
    }

    render() {
        const { note } = this.props;
        const { editing, editText } = this.state;

        return (
            <div className='media mb-3'>
                <div className='mr-3'>
                    <div className='thumb-container'>
                        <img className='thumb-image' src={note.curator.imageURL} />
                    </div>
                </div>
                <div className='media-body'>
                    <h5 className='mb-0'>{note.curator.name} <small className='text-muted'>{new Date(note.date).toLocaleDateString()}</small></h5>

                    {note.editable &&
                    <ul className='list-inline mb-2'>
                        <li className='list-inline-item'>
                            <small><a href='#' onClick={this.handleEditClick}>Edit</a></small>
                        </li>
                        <li className='list-inline-item'>
                            <small><a href='#' onClick={this.handleDeleteClick}>Delete</a></small>
                        </li>
                    </ul>}

                    {editing ?
                        <div>
                            <div className='form-group'>
                                <textarea className='form-control' value={editText} onChange={this.handleNoteChange} />
                            </div>
                            <div className='horizontal-buttons'>
                                <button onClick={this.handleEditSave} type='button' className='btn btn-primary'>
                                    Done Editing
                                </button>
                                <button onClick={this.handleEditCancel} type='submit' className='btn btn-outline-secondary'>
                                    Cancel
                                </button>
                            </div>
                        </div> :
                        <p className='keep-breaks'>{note.text}</p>
                    }
                </div>
            </div>
        );
    }
}

PubEditableNote.propTypes = {
    note: PropTypes.object,
    onSaveNote: PropTypes.func,
    onDeleteNote: PropTypes.func,
};

export default PubEditableNote;
