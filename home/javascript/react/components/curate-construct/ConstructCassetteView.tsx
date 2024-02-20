import React from 'react';
import {Cassette} from "./ConstructTypes";

interface ConstructCassetteViewProps {
    cassette: Cassette;
}

const ConstructCassetteView = ({cassette}: ConstructCassetteViewProps) => {
    return <>
        <b>Promoter: </b>
        {cassette.promoter.map((item, index) => {
            return <React.Fragment key={index}>
                <span className='promoter'>{item.value}</span>
                <span className='separator'>{item.separator}</span>
            </React.Fragment>})}
        <b> Coding: </b>
        {cassette.coding.map((item, index) => {
            return <React.Fragment key={index}>
                <span className='coding'>{item.value}</span>
                <span className='separator'>{item.separator}</span>
            </React.Fragment>})}
    </>
}

export default ConstructCassetteView;
