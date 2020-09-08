import React, {Component} from 'react';
import PropTypes from 'prop-types';

import style from '../../../css/datapage.scss';
import {makeId} from '../utils';


class Section extends Component {
    render() {
        const id = this.props.title && makeId(this.props.title);
        const target = <a className={style.target} id={id} />;
        const renderTitle = <h4>{this.props.isMeta && target}{this.props.title}</h4>;


        return (
            <div className={style.subsection}>
                {!this.props.isMeta && target}
                {this.props.title && !this.props.hideTitle && renderTitle}
                {this.props.children}
            </div>
        );
    }
}

Section.propTypes = {
    children: PropTypes.node.isRequired,
    hideTitle: PropTypes.bool,
    isMeta: PropTypes.bool,
    title: PropTypes.string,
};


export default Section;
