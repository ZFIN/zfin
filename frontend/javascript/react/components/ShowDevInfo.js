import React, {useState} from 'react';
import PropTypes from 'prop-types';

const ShowDevInfo = ({show = false, url, indexer}) => {
    const [hide, setHide] = useState(true);

    const handleShowHide = (hideOption) => {
        setHide(!hideOption)
    }

    //TODO: remove this and figure out why it's cast as a string
    if (show && show === 'true') {
        return <>
            <div>Developer Info: <span onClick={() => handleShowHide(hide)}>show</span></div>
            {!hide && (
                <div>
                    <button type='button' className='btn btn-info'>
                        Endpoint: {url}
                    </button>
                    <p/>
                    <button type='button' className='btn btn-info'>
                        Indexer: {indexer}
                    </button>
                </div>)
            }
            <p/>
        </>;
    }
    return <></>
};

ShowDevInfo.propTypes = {
    show: PropTypes.bool,
    url: PropTypes.string,
    indexer: PropTypes.string,
};

export default ShowDevInfo;
