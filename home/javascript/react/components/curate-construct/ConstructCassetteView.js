import React from 'react';
import PropTypes from 'prop-types';

const ConstructCassetteView = ({cassette}) => {
    return <>
        <b>Promoter: </b>
        {cassette.promoter.map((item) => {
            return <>
                <span className='promoter'>{item.value}</span>
                <span className='separator'>{item.separator}</span>
            </>})}
        <b> Coding: </b>
        {cassette.coding.map((item) => {
            return <>
                <span className='coding'>{item.value}</span>
                <span className='separator'>{item.separator}</span>
            </>})}
    </>
}

ConstructCassetteView.propTypes = {
    cassette: PropTypes.object,
}

export default ConstructCassetteView;
