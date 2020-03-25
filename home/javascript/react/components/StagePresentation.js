import React from 'react';

const StagePresentation = ({stages}) => {
    return (<div>
            {stages && stages.map((value) => {
                if (value === 'stage-selected') {
                    return < div className='ontology-ribbon__item' title={'cleavage'}>
                        <div className={`ontology-ribbon__block__tile ${value}`}/>
                    </div>
                } else {
                    return < div className='ontology-ribbon__item'>
                        <div className={`ontology-ribbon__block__tile ${value}`}/>
                    </div>
                }
            })
            }
        </div>
    );
};

export default StagePresentation;
