import React from 'react';
import Tabs from '../components/Tabs';
import Tab from '../components/Tab';
import PubTrackerStatusTab from './PubTrackerStatusTab';
import PropTypes from 'prop-types';
import PubCorrespondence from './PubCorrespondenceSection';
import {getDetails} from '../api/publication';

class PubTracker extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            pubDetails: null
        };
    }

    componentDidMount() {
        getDetails(this.props.pubId).then(pubDetails => this.setState({pubDetails}));
    }

    render() {
        const { pubId, userId, userName, userEmail } = this.props;
        const { pubDetails } = this.state;

        if (!pubDetails) {
            return null;
        }

        return(
            <Tabs>
                <Tab label='Status'>
                    <PubTrackerStatusTab
                        pubDetails={pubDetails}
                        pubId={pubId}
                        userEmail={userEmail}
                        userId={userId}
                        userName={userName}
                    />
                </Tab>

                <Tab label='Correspondence'>
                    <PubCorrespondence
                        pubDetails={pubDetails}
                        pubId={pubId}
                        userEmail={userEmail}
                        userId={userId}
                        userName={userName}
                    />
                </Tab>
            </Tabs>
        );
    }
}

PubTracker.propTypes = {
    pubId: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
    userEmail: PropTypes.string,
};

export default PubTracker;
