import React, {useState} from 'react';
import ConstructRelationshipsTable from "./ConstructRelationshipsTable";

interface CurateConstructRelationshipsProps {
    publicationId: string;
}

const CurateConstructRelationships = ({publicationId}: CurateConstructRelationshipsProps) => {
    const [displayContents, setDisplayContents] = useState<boolean>(false);

    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>CONSTRUCT RELATIONSHIPS: </span>
            <a style={{textDecoration: 'underline'}} onClick={() => setDisplayContents(!displayContents)}>
                {displayContents ? 'Hide' : 'Show'}
            </a>
            {displayContents && <div className='mt-2'>
                <ConstructRelationshipsTable publicationId={publicationId}/>
            </div>}
        </div>
    </>;
}

export default CurateConstructRelationships;