import React from 'react';
import PropTypes from 'prop-types';

import {getDetails, getCuratedEntities, getCurators,  sendAuthorNotification,} from '../api/publication';


import PubTrackerPanel from '../components/PubTrackerPanel';
import PubTrackerAuthorNotification from '../components/PubTrackerAuthorNotification';

class PubTrackerAuthorNotificationSection extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            pubDetails: null,
            curatedEntities: [],
            notificationLoading: false,
            curators: [],
            validationWarnings: [],
        };
        this.handleNotificationEdit = this.handleNotificationEdit.bind(this);
        this.handleNotificationSend = this.handleNotificationSend.bind(this);
    }

    componentDidMount() {

        getDetails(this.props.pubId).then(pubDetails => this.setState({pubDetails}));

        getCurators().then(curators => this.setState({curators}));
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
        const {  userName, userEmail, pubDetails } = this.props;
        const {
            curatedEntities,
            notificationLoading,
        } = this.state;



        return (
            <div>
                <PubTrackerPanel title='Contact Authors'>
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
            </div>
        )
    }
}

PubTrackerAuthorNotificationSection.propTypes = {
    pubDetails: PropTypes.object,
    pubId: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
    userEmail: PropTypes.string,
};

export default PubTrackerAuthorNotificationSection;
