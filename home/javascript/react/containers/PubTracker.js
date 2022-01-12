import React from 'react';
import PropTypes from 'prop-types';
import {
    addNote,
    addTopic, deleteNote, getCuratedEntities,
    getCurators,
    getDetails,
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
    validate
} from '../api/publication';
import intertab from '../utils/intertab';
import produce, {setAutoFreeze} from 'immer';
setAutoFreeze(false);

import DataPage from '../components/layout/DataPage';
import Section from '../components/layout/Section';
import {
    PubTrackerStatus,
    PubTrackerIndexed,
    PubTrackerTopics,
    PubTrackerNotes,
    PubGeneTable,
    PubAlleleTable,
    PubSTRTable,
    PubTrackerAuthorNotification,
    PubCorrespondenceSection,
} from '../components/pub-tracker';
import PubPDFLink from '../components/PubPDFLink';

const STATUS = 'Status';
const TOPICS = 'Topics';
const NOTES = 'Notes';
const DATA_OBJECTS = 'Data Objects';
const CONTACT_AUTHORS = 'Contact Authors';
const CORRESPONDENCE = 'Correspondence';

const SECTIONS = [
    STATUS,
    TOPICS,
    NOTES,
    DATA_OBJECTS,
    CONTACT_AUTHORS,
    CORRESPONDENCE,
];


class PubTracker extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            pubDetails: null,
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
        const { pubId } = this.props;
        getDetails(pubId).then(pubDetails => this.setState({pubDetails}));
        getStatuses().then(statuses => this.setState({ statuses }));
        getLocations().then(locations => this.setState({ locations }));
        getCurators().then(curators => this.setState({ curators }));
        getStatus(pubId).then(status => this.setState({ status }));
        getIndexed(pubId).then(indexed => this.setState({ indexed }));
        getNotes(pubId).then(notes => this.setState({ notes }));
        getTopics(pubId).then(topics => this.setState({ topics }));

        // if there is a section id in the url hash, scroll it into view. can't rely on the
        // default browser behavior to do this since sections are loaded after the page load
        // event fires
        if (location.hash) {
            const section = document.getElementById(location.hash.substring(1));
            if (section) {
                // ideally we'd wait for all the async requests to settle before doing this scroll
                // too make sure we don't bounce around too much, but this is a cheap, good-enough solution.
                setTimeout(() => section.scrollIntoView(), 300);
            }
        }
    }

    handleStatusSave(status, options = {}) {
        const { pubId } = this.props;
        this.setState({ statusLoading: true });
        updateStatus(pubId, status, options).then(status => {
            this.setState({
                status,
                statusLoading: false,
                validationWarnings: [],
            });
            intertab.fireEvent(intertab.EVENTS.PUB_STATUS);
            if (status.status.type === 'CLOSED' || options.resetTopics) {
                getTopics(pubId).then(topics => this.setState({ topics }));
            }
        });
    }

    handleCloseValidate(status) {
        this.setState({ statusLoading: true });
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
        this.setState({ indexedLoading: true });
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
        this.setState({ notificationLoading: true });
        return getCuratedEntities(this.props.pubId)
            .then((curatedEntities) => this.setState({
                curatedEntities,
                notificationLoading: false,
            }));
    }

    handleNotificationSend(notification, note) {
        this.setState({ notificationLoading: true });
        return sendAuthorNotification(this.props.pubId, notification)
            .then(() => this.handleAddNote(note))
            .always(() => this.setState({ notificationLoading: false }));
    }

    render() {
        const { pubId, userId, userName, userEmail } = this.props;
        const {
            pubDetails,
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

        return (
            <DataPage sections={SECTIONS}>
                {pubDetails && (
                    <h4 className='mb-5'>
                        <a href={`/${pubDetails.zdbID}`} dangerouslySetInnerHTML={{__html: pubDetails.title}} />{' '}
                        <PubPDFLink publication={pubDetails} />
                    </h4>
                )}
                <Section title={STATUS}>
                    <div className='row clearfix'>
                        <div className='col-6 border-right'>
                            {curators.length > 0 && statuses.length > 0 && locations.length > 0 && (
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
                            )}
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
                    <small>
                        <a href={`/action/publication/${pubId}/status-history`} target='_blank' rel='noopener noreferrer'>
                            History <i className='fas fa-external-link-alt' />
                        </a>
                    </small>
                </Section>

                <Section title={TOPICS}>
                    <PubTrackerTopics
                        onTopicSave={this.handleTopicSave}
                        topics={topics}
                    />
                </Section>

                <Section title={NOTES}>
                    <PubTrackerNotes
                        notes={notes}
                        onAddNote={this.handleAddNote}
                        onDeleteNote={this.handleDeleteNote}
                        onEditNote={this.handleEditNote}
                    />
                </Section>

                <Section title={DATA_OBJECTS}>
                    <Section title='Genes'>
                        <PubGeneTable pubId={pubId} />
                    </Section>
                    <Section title='Alleles'>
                        <PubAlleleTable pubId={pubId} />
                    </Section>
                    <Section title='Sequence Targeting Reagents'>
                        <PubSTRTable pubId={pubId} />
                    </Section>
                </Section>

                <Section title={CONTACT_AUTHORS}>
                    <PubTrackerAuthorNotification
                        curatorName={userName}
                        curatorEmail={userEmail}
                        pub={pubDetails}
                        curatedEntities={curatedEntities}
                        loading={notificationLoading}
                        onEditNotification={this.handleNotificationEdit}
                        onSendNotification={this.handleNotificationSend}
                    />
                </Section>

                <Section title={CORRESPONDENCE}>
                    <PubCorrespondenceSection
                        pubDetails={pubDetails}
                        pubId={pubId}
                        userEmail={userEmail}
                        userId={userId}
                        userName={userName}
                    />
                </Section>
            </DataPage>
        )
    }
}

PubTracker.propTypes = {
    pubId: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
    userEmail: PropTypes.string,
};

export default PubTracker;
