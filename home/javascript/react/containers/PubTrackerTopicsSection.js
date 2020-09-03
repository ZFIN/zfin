import React from 'react';
import PropTypes from 'prop-types';
import produce from 'immer';

import {addTopic, getCurators, getTopics, updateTopic,} from '../api/publication';

import PubTrackerPanel from '../components/PubTrackerPanel';
import PubTrackerTopics from '../components/PubTrackerTopics';

class PubTrackerTopicsSection extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            curatedEntities: [],
            topics: [],
            locations: [],
            curators: [],
            validationWarnings: [],
        };

        this.handleTopicSave = this.handleTopicSave.bind(this);

    }

    componentDidMount() {
        const {pubId} = this.props;
        getCurators().then(curators => this.setState({curators}));
        getTopics(pubId).then(topics => this.setState({topics}));
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

    render() {
        const {
            topics,
        } = this.state;


        return (
            <div>
                <PubTrackerPanel title='Topics'>
                    <PubTrackerTopics
                        onTopicSave={this.handleTopicSave}
                        topics={topics}
                    />
                </PubTrackerPanel>
            </div>
        )
    }
}

PubTrackerTopicsSection.propTypes = {
    pubDetails: PropTypes.object,
    pubId: PropTypes.string,
    userId: PropTypes.string,
    userName: PropTypes.string,
    userEmail: PropTypes.string,
};

export default PubTrackerTopicsSection;
