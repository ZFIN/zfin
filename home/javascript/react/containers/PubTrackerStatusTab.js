import React from 'react';
import PropTypes from 'prop-types';
import produce from 'immer';

import {
    addNote,
    addTopic,
    deleteNote,
    getCuratedEntities,
    getCurators,
    getIndexed,
    getLocations,
    getNotes,
    getStatus,
    getStatuses,
    getTopics,
    sendAuthorNotification,
    updateIndexed,
    updateNote,
    updateStatus,
    updateTopic,
    validate,
} from '../api/publication';
import intertab from '../utils/intertab';

import PubTrackerPanel from '../components/PubTrackerPanel';
import PubTrackerStatus from '../components/PubTrackerStatus';
import PubTrackerIndexed from '../components/PubTrackerIndexed';
import PubTrackerTopics from '../components/PubTrackerTopics';
import PubTrackerNotes from '../components/PubTrackerNotes';
import PubGeneTable from './PubGeneTable';
import PubAlleleTable from './PubAlleleTable';
import PubSTRTable from './PubSTRTable';
import PubTrackerAuthorNotification from '../components/PubTrackerAuthorNotification';
import Section from '../components/Section';
import PageNav from '../components/PageNav';

const STATUS = 'Status';
const TOPICS = 'Topics';
const NOTES = 'Notes';
const DATAOBJECTS = 'Data Objects';
const CONTACTAUTHORS = 'Contact';


const SECTIONS = [
    {name: STATUS},
    {name: TOPICS},
    {name: NOTES},
    {name: DATAOBJECTS},
    {name: CONTACTAUTHORS},
];

class PubTrackerStatusTab extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            curatedEntities: [],
            notificationLoading: false,
            notes: [],
            topics: [],
            status: {},
            statusLoading: false,
            indexed: null,
            indexedLoading: false,
            statuses: [],
            locations: [],
            curators: [],
            validationWarnings: [],
        };
        this.handleStatusSave = this.handleStatusSave.bind(this);
        this.handleCloseValidate = this.handleCloseValidate.bind(this);
        this.handleValidationCancel = this.handleValidationCancel.bind(this);
        this.handleIndexedToggle = this.handleIndexedToggle.bind(this);
        this.handleTopicSave = this.handleTopicSave.bind(this);
        this.handleAddNote = this.handleAddNote.bind(this);
        this.handleEditNote = this.handleEditNote.bind(this);
        this.handleDeleteNote = this.handleDeleteNote.bind(this);
        this.handleNotificationEdit = this.handleNotificationEdit.bind(this);
        this.handleNotificationSend = this.handleNotificationSend.bind(this);
    }

    componentDidMount() {
        const {pubId} = this.props;
        getStatuses().then(statuses => this.setState({statuses}));
        getLocations().then(locations => this.setState({locations}));
        getCurators().then(curators => this.setState({curators}));
        getStatus(pubId).then(status => this.setState({status}));
        getIndexed(pubId).then(indexed => this.setState({indexed}));
        getNotes(pubId).then(notes => this.setState({notes}));
        getTopics(pubId).then(topics => this.setState({topics}));
    }

    handleStatusSave(status, options = {}) {
        const {pubId} = this.props;
        this.setState({statusLoading: true});
        updateStatus(pubId, status, options).then(status => {
            this.setState({
                status,
                statusLoading: false,
                validationWarnings: [],
            });
            intertab.fireEvent(intertab.EVENTS.PUB_STATUS);
            if (status.status.type === 'CLOSED' || options.resetTopics) {
                getTopics(pubId).then(topics => this.setState({topics}));
            }
        });
    }

    handleCloseValidate(status) {
        this.setState({statusLoading: true});
        validate(this.props.pubId).then(validation => {
            if (validation.warnings.length > 0) {
                this.setState({
                    validationWarnings: validation.warnings,
                    statusLoading: false,
                });
            } else {
                this.handleStatusSave(status)
            }
        })
    }

    handleValidationCancel() {
        this.setState({
            validationWarnings: []
        });
    }

    handleIndexedToggle(indexed) {
        this.setState({indexedLoading: true});
        updateIndexed(this.props.pubId, indexed).then(indexed => this.setState({
            indexed,
            indexedLoading: false
        }));
    }

    handleTopicSave(topic) {
        let request;
        if (topic.zdbID) {
            request = updateTopic(topic.zdbID, topic);
        } else {
            request = addTopic(this.props.pubId, topic);
        }
        request.then(topic => {
            const idx = this.state.topics.findIndex(other => other.topic === topic.topic);
            this.setState(produce(state => {
                state.topics[idx] = topic;
            }));
        });
    }

    handleAddNote(note) {
        return addNote(this.props.pubId, note).then(note => {
            this.setState(produce(state => {
                state.notes.unshift(note);
            }));
        });
    }

    handleEditNote(note) {
        const { notes } = this.state;
        const idx = notes.findIndex(other => other.zdbID === note.zdbID);
        return updateNote(note.zdbID, note).then(note => {
            this.setState(produce(state => {
                state.notes[idx] = note;
            }));
        });
    }

    handleDeleteNote(note) {
        const { notes } = this.state;
        const idx = notes.findIndex(other => other.zdbID === note.zdbID);
        return deleteNote(note.zdbID).then(() => {
            this.setState(produce(state => {
                state.notes.splice(idx, 1);
            }));
        });
    }

    handleNotificationEdit() {
        this.setState({notificationLoading: true});
        return getCuratedEntities(this.props.pubId)
            .then((curatedEntities) => this.setState({
                curatedEntities,
                notificationLoading: false,
            }));
    }

    handleNotificationSend(notification, note) {
        this.setState({notificationLoading: true});
        return sendAuthorNotification(this.props.pubId, notification)
            .then(() => this.handleAddNote(note))
            .always(() => this.setState({notificationLoading: false}));
    }

    render() {
        const { pubDetails, pubId, userId, userName, userEmail,  } = this.props;
        const {
            curatedEntities,
            curators,
            indexed,
            indexedLoading,
            locations,
            notes,
            notificationLoading,
            status,
            statuses,
            statusLoading,
            topics,
            validationWarnings
        } = this.state;

        const statusHeader = [
            'Status',
            <span className='float-right' key='history'>
                <small>
                    <a href={`/action/publication/${pubId}/status-history`} target='_blank' rel='noopener noreferrer'>
                        History <i className='fas fa-external-link-alt' />
                    </a>
                </small>
            </span>
        ];

        return (


            <div className='d-flex h-100'>
                <PageNav  sections={SECTIONS} />
                <div className='data-page-content-container'>
                    <Section hideTitle title={STATUS}>
                        <PubTrackerPanel title={statusHeader}>

                            <div className='row clearfix'>
                                <div className='col-6 border-right'>
                                    {curators.length > 0 && statuses.length > 0 && locations.length > 0 &&
                                    <PubTrackerStatus
                                        curators={curators}
                                        defaultLocation={status.location}
                                        defaultOwner={status.owner}
                                        defaultStatus={status.status}
                                        loading={statusLoading}
                                        locations={locations}
                                        onSave={this.handleStatusSave}
                                        onValidate={this.handleCloseValidate}
                                        onValidateCancel={this.handleValidationCancel}
                                        statuses={statuses}
                                        userId={userId}
                                        warnings={validationWarnings}
                                    />
                                    }
                                </div>
                                <div className='col-5'>
                                    <div className='row'>
                                        <div className='offset-1 mt-2'>
                                            <PubTrackerIndexed
                                                indexed={indexed}
                                                onToggle={this.handleIndexedToggle}
                                                saving={indexedLoading}
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </PubTrackerPanel>
                    </Section>
                    <Section hideTitle title={TOPICS}>
                        <PubTrackerPanel title='Topics'>
                            <PubTrackerTopics
                                onTopicSave={this.handleTopicSave}
                                topics={topics}
                            />
                        </PubTrackerPanel>
                    </Section>
                    <Section hideTitle title={NOTES}>
                        <PubTrackerPanel title='Notes'>
                            <PubTrackerNotes
                                notes={notes}
                                onAddNote={this.handleAddNote}
                                onDeleteNote={this.handleDeleteNote}
                                onEditNote={this.handleEditNote}
                            />
                        </PubTrackerPanel>
                    </Section>
                    <Section hideTitle title={DATAOBJECTS}>
                        <PubTrackerPanel title='Genes'>
                            <PubGeneTable
                                pubId={pubId}
                            />
                        </PubTrackerPanel>
                        <PubTrackerPanel title='Alleles'>
                            <PubAlleleTable
                                pubId={pubId}
                            />
                        </PubTrackerPanel>
                        <PubTrackerPanel title='STRs'>
                            <PubSTRTable
                                pubId={pubId}
                            />
                        </PubTrackerPanel>
                    </Section>
                    <Section hideTitle title={CONTACTAUTHORS}>
                        <PubTrackerPanel title='Contact'>
                            <PubTrackerAuthorNotification
                                curatorName={userName}
                                curatorEmail={userEmail}
                                pub={pubDetails}
                                curatedEntities={curatedEntities}
                                loading={notificationLoading}
                                onEditNotification={this.handleNotificationEdit}
                                onSendNotification={this.handleNotificationSend}
                            />
                        </PubTrackerPanel>
                    </Section>
                </div>
            </div>
        )
    }
}

PubTrackerStatusTab.propTypes = {
    pubDetails: PropTypes.object,
    pubId: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
    userEmail: PropTypes.string,
};

export default PubTrackerStatusTab;
