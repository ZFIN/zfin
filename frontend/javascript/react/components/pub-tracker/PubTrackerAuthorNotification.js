import React, {Component} from 'react';
import PropTypes from 'prop-types';

import LoadingButton from '../LoadingButton';
import {buildRecipientList, splitEmailRecipientListString} from '../../utils/publication';
import AuthorEmailCheckboxList from './AuthorEmailCheckboxList';

class PubTrackerAuthorNotification extends Component {
    constructor(props) {
        super(props);
        this.state = {
            registeredRecipients: [],
            additionalRecipients: '',
            editing: false,
            previewing: false,
            names: '',
            customNote: '',
            sendError: false,
            sendSuccess: false,
        };
        this.handleEditNotificationClick = this.handleEditNotificationClick.bind(this);
        this.previewNotification = this.previewNotification.bind(this);
        this.cancelEdit = this.cancelEdit.bind(this);
        this.sendNotification = this.sendNotification.bind(this);
        this.cancelPreview = this.cancelPreview.bind(this);
        this.letterRef = React.createRef();
    }

    componentDidMount() {
        this.addRegisteredAuthorsToRecipientList();
    }

    componentDidUpdate(prevProps) {
        if (!prevProps.pub) {
            this.addRegisteredAuthorsToRecipientList();
        }
    }

    addRegisteredAuthorsToRecipientList() {
        const { pub } = this.props;
        if (pub) {
            this.setState({ registeredRecipients: buildRecipientList(pub.registeredAuthors) });
        }
    }

    hasRecipients() {
        const { registeredRecipients, additionalRecipients } = this.state;
        return registeredRecipients.length > 0 || additionalRecipients;
    }

    handleEditNotificationClick() {
        this.props.onEditNotification().then(() => this.setState({
            editing: true,
            sendError: false,
            sendSuccess: false,
        }));
    }

    getRecipientList() {
        const { registeredRecipients, additionalRecipients } = this.state;
        return registeredRecipients
            .map(r => r.email)
            .concat(splitEmailRecipientListString(additionalRecipients));
    }

    previewNotification() {
        this.setState({
            previewing: true,
            editing: false,
        });
    }

    cancelEdit() {
        this.setState({
            previewing: false,
            editing: false,
        });
    }

    sendNotification() {
        const { registeredRecipients, additionalRecipients } = this.state;

        const notification = {
            message: this.letterRef.current.innerHTML,
            recipients: this.getRecipientList(),
        };

        const recipients = registeredRecipients
            .map(r => `${r.name} (${r.email})`)
            .concat(splitEmailRecipientListString(additionalRecipients));
        const note = { text: `Notified authors: ${recipients.join(', ')}` };

        this.props.onSendNotification(notification, note)
            .then(() => this.setState({
                sendSuccess: true,
                sendError: false,
                editing: false,
                previewing: false,
                names: '',
                customNote: '',
                additionalRecipients: '',
            }))
            .fail(() => {
                this.setState({
                    sendSuccess: false,
                    sendError: true,
                });
            });
    }

    cancelPreview() {
        this.setState({
            editing: true,
            previewing: false,
        })
    }

    render() {
        const { curatorName, curatorEmail, curatedEntities, loading, pub } = this.props;
        const {
            additionalRecipients,
            customNote,
            editing,
            names,
            previewing,
            registeredRecipients,
            sendError,
            sendSuccess,
        } = this.state;

        if (!pub) {
            return null;
        }

        return (
            <div>
                { !editing && !previewing &&
                    <div>
                        <div className='form-group'>
                            <label>Registered Authors</label>
                            <AuthorEmailCheckboxList
                                id='notification-authors'
                                value={registeredRecipients}
                                authors={pub.registeredAuthors}
                                onChange={registeredRecipients => this.setState({registeredRecipients})}
                            />
                        </div>

                        <div className='form-group'>
                            <label>Additional Recipients</label>
                            <input
                                className='form-control'
                                value={additionalRecipients}
                                onChange={event => this.setState({additionalRecipients: event.target.value})}
                                placeholder='alice@example.com, bob@example.net'
                            />
                        </div>

                        <LoadingButton
                            className='btn btn-primary'
                            type='button'
                            disabled={loading || !this.hasRecipients()}
                            onClick={this.handleEditNotificationClick}
                            loading={loading}
                        >
                            Edit Notification
                        </LoadingButton>
                    </div>
                }

                { (editing || previewing) &&
                    <div>
                        <b>Recipients: </b> {this.getRecipientList().join(', ')}
                    </div>
                }

                { (editing || previewing) &&
                    <div>
                        <div className='notif-letter' ref={this.letterRef}>
                            <p>Dear {previewing ? names : <input className='form-control d-inline' style={{width: '500px'}} value={names} onChange={e => this.setState({names: e.target.value})} />},</p>
                            <p>I am pleased to report that information about your paper has been entered into ZFIN, the Zebrafish Information Network.</p>
                            <p><a href={`https://${window.location.hostname}/${pub.zdbID}`} dangerouslySetInnerHTML={{__html: pub.citation}} /></p>
                            <p>Please notify me if you have corrections, comments about the data associated with your paper:</p>
                            <div>
                                <ul>
                                    {curatedEntities.map(entity => (
                                        <li key={entity.label}>
                                            <a href={`https://${window.location.hostname}${entity.path}`}>
                                                {entity.label} {entity.count ? `(${entity.count})` : ''}
                                            </a>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                            <p>{previewing ? customNote : <textarea className='form-control d-inline w-auto' cols='80' rows='4' value={customNote} onChange={e => this.setState({customNote: e.target.value})} />}</p>
                            <p>ZFIN is The Zebrafish Information Network, a centralized community
                                resource for zebrafish genetic, genomic, and developmental data. We encourage you to
                                share this message with your co-authors and appreciate any feedback that you are able to
                                offer. Community input is vital to our success and value as a public resource. If you
                                have corrections, comments, or additional data that you would like to submit to ZFIN,
                                please contact me.</p>
                            <p>Thank you,</p>
                            <p>{curatorName}<br />
                                Scientific Curation Group<br />
                                {curatorEmail}</p>
                            <p>Zebrafish Information Network<br />
                                5291 University of Oregon<br />
                                Eugene, Oregon, USA 97403-5291</p>
                        </div>
                        <div className='horizontal-buttons'>
                            <LoadingButton
                                className='btn btn-primary'
                                type='button'
                                onClick={previewing ? this.sendNotification : this.previewNotification}
                                loading={loading}
                            >
                                {previewing ? 'Send Notification' : 'Preview Notification'}
                            </LoadingButton>
                            <button className='btn btn-outline-secondary' type='button' onClick={previewing ? this.cancelPreview : this.cancelEdit}>
                                Cancel
                            </button>
                        </div>
                    </div>
                }

                <div className='notif-letter-alert'>
                    {sendSuccess && <div className='alert alert-success'>Message has been sent.</div>}
                    {sendError && <div className='alert alert-danger'>Something went wrong while sending the message.</div>}
                </div>
            </div>
        );
    }
}

PubTrackerAuthorNotification.propTypes = {
    curatorName: PropTypes.string,
    curatorEmail: PropTypes.string,
    pub: PropTypes.shape({
        citation: PropTypes.string,
        registeredAuthors: PropTypes.array,
        zdbID: PropTypes.string,
    }),
    loading: PropTypes.bool,
    curatedEntities: PropTypes.array,
    onEditNotification: PropTypes.func,
    onSendNotification: PropTypes.func,
};

export default PubTrackerAuthorNotification;
