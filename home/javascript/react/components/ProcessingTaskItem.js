import React, {Component} from 'react';
import PropTypes from 'prop-types';

class ProcessingTaskItem extends Component {
    constructor(props) {
        super(props);
        this.linkElement = React.createRef();
    }

    componentDidMount() {
        const { disabledMessage } = this.props;
        if (disabledMessage) {
            $(this.linkElement.current).tooltip({
                title: disabledMessage,
                placement: 'bottom',
                html: true,
            });
        }
    }

    render() {
        const {children, href, isComplete, disabledMessage} = this.props;
        const linkClass = disabledMessage ? 'disabled' : (isComplete ? 'complete' : '');
        const iconClass = disabledMessage ? 'fas fa-minus-circle' : (isComplete ? 'far fa-check-circle' : 'far fa-circle');
        return (
            <li>
                <a href={disabledMessage ? undefined : href} className={linkClass} ref={this.linkElement}>
                    <i className={`${iconClass} fa-lg fa-fw`}/> {children}
                </a>
            </li>
        );
    }
}

ProcessingTaskItem.propTypes = {
    children: PropTypes.node,
    href: PropTypes.string,
    isComplete: PropTypes.bool,
    disabledMessage: PropTypes.string,
};

export default ProcessingTaskItem;
