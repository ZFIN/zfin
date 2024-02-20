import React from 'react';

interface CurateConstructEditProps {
    publicationId: string;
}

const CurateConstructEdit = ({publicationId}: CurateConstructEditProps) => {
    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>EDIT CONSTRUCT: </span>
            <a style={{textDecoration: 'underline'}}>Show</a>
        </div>
    </>;
}

export default CurateConstructEdit;