import React from 'react';

const BlastDropDown = ({dbLink}) => {
    if (dbLink.blastableDatabases.length == 1)
        return <span>Single database</span>
    return (<div className="dropdown show">
        <a className="btn btn-info btn-sm dropdown-toggle" href="#" role="button" id="dropdownMenuLink"
           data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            Select Tool
        </a>
        <div className="dropdown-menu" aria-labelledby="dropdownMenuLink">
            {dbLink.blastableDatabases.map((blast) => (
                <a className="dropdown-item" href={blast.urlPrefix + dbLink.accessionNumber}>{blast.displayName}</a>
            ))}
        </div>
    </div>);
}


export default BlastDropDown;
