import React from 'react';

const Stage = ({startStage, endStage}) => {

    const startIsEnd = startStage.name === endStage.name
    if (startIsEnd) {
        return <div>{startStage.name}</div>;
    } else {
        return <div>{startStage.name} - {endStage.name}</div>;
    }
}

export default Stage;
