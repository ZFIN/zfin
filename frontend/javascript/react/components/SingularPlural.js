import React from 'react';

const SingularPlural = ({singular, plural, value}) => {
    if (value === 1) {
        return <span>{value} {singular}</span>;
    } else {
        {
            return <>{value} {plural}</>
        }
    }

};

export default SingularPlural;

