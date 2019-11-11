import React from 'react';
import PropTypes from 'prop-types';
import update from 'immutability-helper';

import RelativeDate from "./RelativeDate";

const PubTrackerTopics = ({onTopicSave, topics}) => {

    const isNewTopic = (topic) => !topic.openedDate && !topic.closedDate;
    const isOpenTopic = (topic) => topic.openedDate && !topic.closedDate;
    const isClosedTopic = (topic) => topic.closedDate;
    const getTopicStatus = (topic) => {
        if (isOpenTopic(topic)) {
            return <span>Opened <RelativeDate ago date={topic.openedDate}/></span>;
        } else if (isClosedTopic(topic)) {
            return 'Closed';
        } else {
            return '';
        }
    };

    const onToggleDataFound = (topic, dataFound) => {
        topic = update(topic, {
            dataFound: {$set: dataFound}
        });
        onTopicSave(topic);
    };

    const onOpenTopic = (topic) => {
        topic = update(topic, {
            openedDate: {$set: Date.now()},
            closedDate: {$set: null},
            dataFound: {$set: true},
        });
        onTopicSave(topic);
    };

    const onCloseTopic = (topic) => {
        topic = update(topic, {
            closedDate: {$set: Date.now()},
            dataFound: {$set: true},
        });
        onTopicSave(topic);
    };

    const onReturnTopicToNew = (topic) => {
        topic = update(topic, {
            openedDate: {$set: null}
        });
        onTopicSave(topic);
    };

    return (
        <table className="table table-hover">
            <thead>
            <tr>
                <th>Topic</th>
                <th>Status</th>
                <th>Curator</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            {topics && topics.map(topic => (
                <tr key={topic.topic}>
                    <td>
                        <input type="checkbox" checked={topic.dataFound} onChange={event => onToggleDataFound(topic, event.target.checked)} /> {topic.topic}
                    </td>
                    <td>
                        {getTopicStatus(topic)}
                    </td>
                    <td>{!isNewTopic(topic) ? topic.curator.name : ""}</td>
                    <td>
                        {isNewTopic(topic) &&
                        <button className="btn btn-outline-secondary btn-dense" onClick={() => onOpenTopic(topic)}>
                            Open
                        </button>}

                        {isOpenTopic(topic) &&
                        <div className="btn-group">
                            <button type="button" className="btn btn-outline-secondary btn-dense" onClick={() => onCloseTopic(topic)}>
                                Close
                            </button>
                            <button type="button" className="btn btn-outline-secondary dropdown-toggle btn-dense" data-toggle="dropdown">
                                <span className="caret" />
                                <span className="sr-only">Toggle Dropdown</span>
                            </button>
                            <ul className="dropdown-menu" role="menu">
                                <li><a href='#' onClick={(e) => {e.preventDefault(); onReturnTopicToNew(topic)}}>Back to New</a></li>
                            </ul>
                        </div>}
                        
                        {isClosedTopic(topic) &&
                        <button className="btn btn-outline-secondary btn-dense" onClick={() => onOpenTopic(topic)}>
                            Re-open
                        </button>}
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};

PubTrackerTopics.propTypes = {
    onTopicSave: PropTypes.func,
    topics: PropTypes.array,
};

export default PubTrackerTopics;