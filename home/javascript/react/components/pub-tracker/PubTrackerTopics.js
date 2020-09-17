import React from 'react';
import PropTypes from 'prop-types';

import RelativeDate from '../RelativeDate';


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
        onTopicSave({
            ...topic,
            dataFound,
        });
    };


    const onOpenTopic = (topic) => {
        onTopicSave({
            ...topic,
            openedDate: Date.now(),
            closedDate: null,
            dataFound: true,
        });
    };

    const onCloseTopic = (topic) => {
        onTopicSave({
            ...topic,
            closedDate: Date.now(),
            dataFound: true,
        });
    };

    const onReturnTopicToNew = (topic) => {
        onTopicSave({
            ...topic,
            openedDate: null,
        });
    };

    return (
        <table className='table table-hover'>
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
                            <input type='checkbox' checked={topic.dataFound} onChange={event => onToggleDataFound(topic, event.target.checked)} /> {topic.topic}
                        </td>
                        <td>
                            {getTopicStatus(topic)}
                        </td>
                        <td>{!isNewTopic(topic) ? topic.curator.name : ''}</td>
                        <td>
                            {isNewTopic(topic) &&
                            <button className='btn btn-outline-secondary btn-dense' onClick={() => onOpenTopic(topic)}>
                                Open
                            </button>}

                            {isOpenTopic(topic) &&
                            <div className='btn-group'>
                                <button type='button' className='btn btn-outline-secondary btn-dense' onClick={() => onCloseTopic(topic)}>
                                    Close
                                </button>
                                <button type='button' className='btn btn-outline-secondary dropdown-toggle btn-dense' data-toggle='dropdown'>
                                    <span className='sr-only'>Toggle Dropdown</span>
                                </button>
                                <div className='dropdown-menu' role='menu'>
                                    <a className='dropdown-item' href='#' onClick={(e) => {e.preventDefault(); onReturnTopicToNew(topic)}}>Back to New</a>
                                </div>
                            </div>}

                            {isClosedTopic(topic) &&
                            <button className='btn btn-outline-secondary btn-dense' onClick={() => onOpenTopic(topic)}>
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