import React from 'react';
import Tabs from "../components/Tabs";
import Tab from "../components/Tab";
import PubTrackerStatusTab from "./PubTrackerStatusTab";
import PropTypes from "prop-types";

class PubTracker extends React.Component {
    render() {
        const { pubId, userId, userName, userEmail } = this.props;
        return(
            <Tabs>
                <Tab label='Status'>
                    <PubTrackerStatusTab
                        pubId={pubId}
                        userId={userId}
                        userName={userName}
                        userEmail={userEmail}
                    />
                </Tab>

                <Tab label='Correspondence'>
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
