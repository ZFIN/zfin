import React from 'react';

const StagePresentation = ({stages}) => {
    return (<div>
            {stages && Object.entries(stages).map(([key, value]) => {
                if (value === 'stage-selected') {
                    return < div className='ontology-ribbon__item' title={key}>
                        <a href={''}>
                            <div className={`ontology-ribbon__block__tile ${value}`}/>
                        </a>
                    </div>
                } else {
                    return < div className='ontology-ribbon__item' title={key}>
                        <div className={`ontology-ribbon__block__tile ${value}`}/>
                    </div>
                }
            })
            }
        </div>
    );
};

export default StagePresentation;
