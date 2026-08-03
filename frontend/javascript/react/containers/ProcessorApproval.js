import React from 'react';
import PropTypes from 'prop-types';
import {addChecklistEntry, deleteChecklistEntry, getChecklist} from '../api/publication';

const approvalMessage = {
    ADD_PDF: 'Original article PDF upload complete',
    ADD_FIGURES: 'Figure addition complete',
    LINK_AUTHORS: 'Author linking complete',
};

class ProcessorApproval extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            checklistEntry: undefined,
            loading: false,
            open: true,
        };
        this.toggleOpen = this.toggleOpen.bind(this);
        this.handleSignoff = this.handleSignoff.bind(this);
        this.handleUndo = this.handleUndo.bind(this);
    }

    componentDidMount() {
        const { pubId, task } = this.props;
        this.setState({loading: true});
        getChecklist(pubId).then(checklist => {
            const checklistEntry = checklist.find(entry => entry.task === task);
            this.setState({
                checklistEntry,
                loading: false,
                open: checklistEntry === undefined
            })
        });
    }

    toggleOpen() {
        this.setState(state => {
            return {
                open: !state.open
            };
        });
    }

    handleSignoff() {
        const { pubId, task } = this.props;
        addChecklistEntry(pubId, task).then(checklistEntry => this.setState({checklistEntry}));
    }

    handleUndo() {
        const { checklistEntry } = this.state;
        deleteChecklistEntry(checklistEntry.id).then(() => this.setState({checklistEntry: undefined}));
    }

    render() {
        const { checklistEntry, loading, open } = this.state;

        if (loading) {
            return null;
        }

        return (
            <div style={{marginBottom: '20px'}}>
                <h5 onClick={this.toggleOpen} style={{cursor: 'pointer'}}>
                    <span className={`fa-animation-container ${open ? 'fa-rotate-90' : ''}`}>
                        <i className='fas fa-caret-right fa-fw' />
                    </span> Processor Sign-off
                </h5>
                {
                    open && ( checklistEntry ?
                        <span>
                            Approved by {checklistEntry.person.name} on {(new Date(checklistEntry.date)).toLocaleDateString()}.
                            <button className='btn btn-outline-secondary' onClick={this.handleUndo} style={{marginLeft: '20px'}}>Undo</button>
                        </span> :
                        <button className='btn btn-primary' onClick={this.handleSignoff}>
                            {approvalMessage[this.props.task]}
                        </button>
                    )
                }

            </div>
        )
    }
}

ProcessorApproval.propTypes = {
    pubId: PropTypes.string.isRequired,
    task: PropTypes.string.isRequired,
};

export default ProcessorApproval;
