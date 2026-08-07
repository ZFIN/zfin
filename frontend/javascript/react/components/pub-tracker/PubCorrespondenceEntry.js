import React from 'react';
import PropTypes from 'prop-types';

class PubCorrespondenceEntry extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            open: false,
        };
        this.handleToggleOpen = this.handleToggleOpen.bind(this);
    }

    handleToggleOpen() {
        this.setState(state => ({open: !state.open}));
    }

    render() {
        const { correspondence } = this.props;
        const { open } = this.state;

        return (
            <div>
                <div><strong>{new Date(correspondence.date).toLocaleDateString()}</strong></div>
                <div><strong>From:</strong> {correspondence.from.email}</div>
                <div><strong>To:</strong> {correspondence.to.map(p => p.email).join(', ')}</div>
                <div><strong>Subject:</strong> {correspondence.subject}</div>
                <div className={open ? 'keep-breaks' : 'one-line-preview'}><span
                    className={`icon-toggle ${open ? 'open' : ''}`}
                    onClick={this.handleToggleOpen}
                ><i className='fas fa-fw fa-chevron-right'/></span> {correspondence.message}
                </div>
            </div>
        );
    }
}

PubCorrespondenceEntry.propTypes = {
    correspondence: PropTypes.object,
};

export default PubCorrespondenceEntry;
