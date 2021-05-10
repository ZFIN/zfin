import React from 'react';
import PropTypes from 'prop-types';
import { addCorrespondence, deleteCorrespondence, getCorrespondences } from '../../api/publication';
import PubCorrespondenceEmailForm from './PubCorrespondenceEmailForm';
import Alert from '../Alert';
import { splitEmailRecipientListString } from '../../utils/publication';
import PubCorrespondenceList from './PubCorrespondenceList';
import intertab from '../../utils/intertab';

const prependSubject = (subject) => {
    if (subject.toLowerCase().substr(0, 3) !== 're:') {
        subject = 'Re: ' + subject;
    }
    return subject;
};

class PubCorrespondenceSection extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            correspondences: [],
            email: null,
            loading: false,
            successMessage: '',
            errorMessage: '',
        };

        this.openOutgoingForm = this.openOutgoingForm.bind(this);
        this.openIncomingForm = this.openIncomingForm.bind(this);
        this.handleEmailUpdate = this.handleEmailUpdate.bind(this);
        this.handleEmailCancel = this.handleEmailCancel.bind(this);
        this.handleEmailComplete = this.handleEmailComplete.bind(this);
        this.clearSuccessMessage = this.clearSuccessMessage.bind(this);
        this.clearErrorMessage = this.clearErrorMessage.bind(this);
        this.handleResend = this.handleResend.bind(this);
        this.handleRecordReply = this.handleRecordReply.bind(this);
        this.handleSendReply = this.handleSendReply.bind(this);
        this.handleDeleteCorrespondence = this.handleDeleteCorrespondence.bind(this);
        this.handleTemplateSelect = this.handleTemplateSelect.bind(this);
    }

    componentDidMount() {
        getCorrespondences(this.props.pubId).then(correspondences => this.setState({ correspondences }));
    }

    openOutgoingForm() {
        const { userId, userEmail } = this.props;
        this.setState({
            email: {
                outgoing: true,
                additionalTo: '',
                to: [],
                from: { zdbID: userId, email: userEmail },
                subject: '',
                message: '',
            }
        });
    }

    openIncomingForm() {
        const { userId, userEmail } = this.props;
        this.setState({
            email: {
                outgoing: false,
                additionalTo: '',
                to: [{ zdbID: userId, email: userEmail }],
                from: { email: '' },
                subject: '',
                message: '',
            }
        });
    }

    handleEmailUpdate(email) {
        this.setState({ email });
    }

    handleTemplateSelect(subject, body) {
        const { pubDetails, userName } = this.props;
        this.setState(state => ({
            email: {
                ...state.email,
                subject: subject,
                message: body(pubDetails.citation, userName)
            }
        }));
    }

    handleEmailCancel() {
        this.setState({ email: null });
    }

    addCorrespondence(correspondence) {
        this.setState({ loading: true });
        addCorrespondence(this.props.pubId, correspondence)
            .then(correspondence => this.setState(state => ({
                correspondences: [
                    correspondence,
                    ...state.correspondences
                ],
                email: null,
                successMessage: correspondence.outgoing ? 'Email successfully sent.' : 'Reply saved.',
                errorMessage: '',
            })))
            .then(() => intertab.fireEvent(intertab.EVENTS.PUB_STATUS))
            .fail(() => this.setState({
                successMessage: '',
                errorMessage: correspondence.outgoing ? 'Error sending email.' : 'Error saving reply.',
            }))
            .always(() => this.setState({
                loading: false,
            }));
    }

    handleEmailComplete() {
        const { email } = this.state;
        const combinedRecipients = email.to.concat(
            splitEmailRecipientListString(email.additionalTo).map(email => ({ email }))
        );
        this.addCorrespondence({
            ...email,
            to: combinedRecipients,
        });
    }

    handleResend(correspondence) {
        if (correspondence.to.length === 0) {
            return;
        }
        this.addCorrespondence({
            ...correspondence,
            resend: true,
        });
    }

    handleRecordReply(correspondence) {
        const { userId, userEmail } = this.props;
        this.setState({
            email: {
                outgoing: false,
                additionalTo: '',
                to: [{ zdbID: userId, email: userEmail }],
                from: { email: correspondence.to.map(t => t.email).join(', ') },
                subject: prependSubject(correspondence.subject),
                message: ''
            }
        });
    }

    handleSendReply(correspondence) {
        const { userId, userEmail } = this.props;
        this.setState({
            email: {
                reply: true,
                outgoing: true,
                to: [],
                additionalTo: correspondence.from.email,
                from: { zdbID: userId, email: userEmail },
                subject: prependSubject(correspondence.subject),
                message: ''
            }
        });
    }

    handleDeleteCorrespondence(correspondence) {
        deleteCorrespondence(correspondence.id, correspondence.outgoing)
            .then(() => this.setState(state => ({
                correspondences: state.correspondences.filter(c => c.id !== correspondence.id),
                successMessage: 'Message deleted.',
                errorMessage: '',
            })))
            .fail(() => ({
                successMessage: '',
                errorMessage: 'Error deleting message.',
            }));
    }

    clearSuccessMessage() {
        this.setState({ successMessage: '' });
    }

    clearErrorMessage() {
        this.setState({ errorMessage: '' });
    }

    render() {
        const { pubDetails } = this.props;
        const { correspondences, email, successMessage, errorMessage } = this.state;

        if (!pubDetails) {
            return null;
        }

        return (
            <div>
                <div className='row bottom-buffer'>
                    <div className='col-md-12 horizontal-buttons'>
                        <button
                            className='btn btn-outline-secondary'
                            onClick={this.openOutgoingForm}
                        >
                            Send Email
                        </button>
                        <button
                            className='btn btn-outline-secondary'
                            onClick={this.openIncomingForm}
                        >
                            Record Reply
                        </button>
                    </div>
                </div>

                <PubCorrespondenceEmailForm
                    authors={pubDetails.registeredAuthors}
                    email={email}
                    loading={false}
                    onCancel={this.handleEmailCancel}
                    onUpdate={this.handleEmailUpdate}
                    onComplete={this.handleEmailComplete}
                    onTemplateSelect={this.handleTemplateSelect}
                />

                <Alert color='success' dismissable onDismiss={this.clearSuccessMessage}>
                    {successMessage}
                </Alert>

                <Alert color='danger' dismissable onDismiss={this.clearErrorMessage}>
                    {errorMessage}
                </Alert>

                <PubCorrespondenceList
                    correspondences={correspondences}
                    onResend={this.handleResend}
                    onRecordReply={this.handleRecordReply}
                    onSendReply={this.handleSendReply}
                    onDelete={this.handleDeleteCorrespondence}
                />
            </div>
        );
    }
}

PubCorrespondenceSection.propTypes = {
    pubDetails: PropTypes.shape({
        citation: PropTypes.string,
        registeredAuthors: PropTypes.array,
    }),
    pubId: PropTypes.string,
    userEmail: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
};

export default PubCorrespondenceSection;
