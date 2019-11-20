import React from 'react';
import PropTypes from 'prop-types';
import Tab from "./Tab";

class Tabs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            selected: 0
        };
        this.showTabFromHash = this.showTabFromHash.bind(this);
    }

    componentDidMount() {
        this.showTabFromHash();
        window.addEventListener('hashchange', this.showTabFromHash);
    }

    componentWillUnmount() {
        window.removeEventListener('hashchange', this.showTabFromHash);
    }

    showTabFromHash() {
        const hash = window.location.hash;
        if (hash) {
            React.Children.forEach(this.props.children, (child, idx) => {
                if (hash === '#' + child.props.label) {
                    this.setState({selected: idx});
                }
            });
        } else {
            this.setState({selected: 0});
        }
    }

    handleTabClick(event, idx, label) {
        event.preventDefault();
        window.history.pushState(null, null, window.location.pathname + window.location.search + '#' + label);
        this.setState({selected: idx});
    }

    render() {
        const { children } = this.props;
        const { selected } = this.state;

        return (
            <div>
                <ul className='nav nav-tabs nav-justified mb-5' role='tablist'>
                    {
                        React.Children.map(children, (child, idx) => {
                            if (!child.type || child.type !== Tab) {
                                return null;
                            }
                            const { label } = child.props;
                            return (
                                <li role='presentation' className='nav-item'>
                                    <a href={'#' + label} className={`nav-link ${idx === selected ? 'active' : ''}`} role='tab' onClick={event => this.handleTabClick(event, idx, label)}>
                                        {label}
                                    </a>
                                </li>
                            );
                        })
                    }
                </ul>

                <div className='tab-content'>
                    {
                        React.Children.map(children, (child, idx) => (
                            React.cloneElement(child, { isActive: idx === selected })
                        ))
                    }
                </div>
            </div>
        );
    }
}

Tabs.propTypes = {
    children: PropTypes.node,
};

export default Tabs