import React from 'react';

const GenericErrorMessage = () => {
    return (
        <span className='text-danger'>
            Something went wrong fetching data. Try again later or <a href={`mailto:${process.env.ZFIN_ADMIN}`}>contact us</a>.
        </span>
    );
};

export default GenericErrorMessage;
