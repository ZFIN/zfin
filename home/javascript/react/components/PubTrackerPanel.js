import React from 'react';
import PropTypes from 'prop-types';

const PubTrackerPanel = ({children, title}) => {
    return (
        <div className="panel panel-default">
            <div className="panel-heading">
                <h3 className="panel-title">
                    {title}
                </h3>
            </div>
            <div className="panel-body">
                {children}
            </div>
        </div>
    );
};

PubTrackerPanel.propTypes = {
    children: PropTypes.node,
    title: PropTypes.node,
};

export default PubTrackerPanel;
