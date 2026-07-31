import React, {useState}  from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';

const LinkAuthors = ({ pubId }) => {
    //authorStrings is an array of strings of author names
    const authorStrings = useFetch(`/action/publication/${pubId}/author-strings`);

    const [authorFilter, setAuthorFilter] = useState('');

    //suggestedAuthors
    const [suggestedAuthorsUrl, setSuggestedAuthorsUrl] = useState(null);
    const suggestedAuthors = useFetch(suggestedAuthorsUrl);

    //selectedAuthorName is the author name that was clicked on from authorStrings
    const [selectedAuthorName, setSelectedAuthorName] = useState(null);

    //registeredAuthors is an array of objects of registered authors
    const registeredAuthors = useFetch(`/action/publication/${pubId}/registered-authors`);

    //authorZdbID is the zdbID of the author to add manually
    const [authorZdbID, setAuthorZdbID] = useState(null);

    const [errorAddingAuthor, setErrorAddingAuthor] = useState(false);

    const [refreshingListedAuthors, setRefreshingListedAuthors] = useState(false);

    if (authorStrings.pending) {
        return <LoadingSpinner/>;
    }

    if (!authorStrings.value) {
        return null;
    }

    const handleAuthorNameClicked = (author) => {
        setSelectedAuthorName(author);

        setSuggestedAuthorsUrl(`/action/publication/link-author-suggestions?authorString=${author}`);
    }

    const alreadyLinked = (person) => {
        if (!registeredAuthors.value) {
            return false;
        }

        return !registeredAuthors.value.find(author => author.zdbID === person.zdbID);
    }

    const addAuthor = async (person) => {
        const url = `/action/publication/${pubId}/addAuthor/${person.zdbID}`;
        const response = await fetch(url, {method: 'POST'});

        setErrorAddingAuthor(!response.ok);

        //refetch registeredAuthors
        registeredAuthors.refetch();
    }

    const removeAuthor = async (person) => {
        const url = `/action/publication/${pubId}/removeAuthor/${person.zdbID}`;
        await fetch(url, {method: 'POST'});

        //refetch registeredAuthors
        registeredAuthors.refetch();
    }

    const handleManualAuthorAdd = async (event) => {
        event.preventDefault();

        const url = `/action/publication/${pubId}/addAuthor/${authorZdbID}`;
        const response = await fetch(url, {method: 'POST'});

        setErrorAddingAuthor(!response.ok);

        //refetch registeredAuthors
        registeredAuthors.refetch();
    }

    const refreshListedAuthors = async (event) => {
        event.preventDefault();
        setRefreshingListedAuthors(true);
        const url = `/action/publication/${pubId}/refresh-listed-authors`;
        await fetch(url, {method: 'POST'});
        await authorStrings.refetch();
        setRefreshingListedAuthors(false);
    }

    return (
        <>
            {registeredAuthors.rejected && (
                <div className='alert alert-danger'>
                    There was an error on the server and the registered authors could not be loaded.
                </div>
            )}
            {errorAddingAuthor && (
                <div className='alert alert-danger'>
                    There was an error on the server when attempting to add the author
                </div>
            )}
            <div className='row'>
                <div className='col-md-4 filter-authors-container'>
                    <h4>Listed Authors</h4>
                    <form className='form-inline'>
                        <input onChange={(e) => setAuthorFilter(e.target.value)} className='form-control' placeholder='Filter authors'/>
                    </form>
                    <ul className='list-unstyled'>
                        {authorStrings.value.map(authorName => (
                            authorName.toLowerCase().includes(authorFilter.toLowerCase()) && (
                                <li className='author-item' key={authorName}>
                                    <a href='#' onClick={() => handleAuthorNameClicked(authorName)}>{authorName}</a>
                                </li>
                            )
                        ))}
                    </ul>
                    {refreshingListedAuthors ? (<LoadingSpinner/>) : (
                        <button onClick={refreshListedAuthors} className='btn btn-primary mt-3'>Refresh Listed Authors</button>)}
                </div>
                <div className='col-4 suggested-authors-container'>
                    <h4>
                        Suggested Authors {selectedAuthorName && (
                            <span>for {selectedAuthorName}</span>
                        )}
                    </h4>
                    {!selectedAuthorName && (
                        <div className='text-muted'>
                            <i>Select a listed author to view suggestions.</i>
                        </div>
                    )}
                    {suggestedAuthors.value && suggestedAuthors.value.length === 0 && (
                        <div className='alert alert-warning'>
                            <strong>Bummer.</strong> No suggestions found.
                        </div>
                    )}
                    {suggestedAuthors.value && suggestedAuthors.value.length > 0 && (
                        <ul className='list-unstyled'>
                            {suggestedAuthors.value.map(person => (
                                <li className='author-item' key={person.zdbID}>
                                    {alreadyLinked(person) ? (
                                        <a href='#' title='Link this author' onClick={() => addAuthor(person)}><i className='fas fa-fw fa-plus-circle'/></a>
                                    ) : (
                                        <span title='Author already linked'><i className='fas fa-fw fa-check'/></span>
                                    )}
                                    <a href={`/${person.zdbID}`} target='_blank' rel='noreferrer'>{person.display}</a>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                <div className='col-4 linked-authors-container'>
                    <h4>Linked Authors</h4>
                    <div className='form-inline'>
                        <form onSubmit={handleManualAuthorAdd}>
                            <input onChange={(event) => setAuthorZdbID(event.target.value)} className='form-control' placeholder='Link by ZDB ID...'/>
                            <button className='btn btn-success'>Link</button>
                        </form>
                    </div>
                    <ul className='list-unstyled'>
                        {registeredAuthors.value && registeredAuthors.value.map(person => (
                            <li className='author-item' key={person.zdbID}>
                                <a href='#' onClick={() => removeAuthor(person)} title='Unlink this author'><i className='fas fa-trash fa-fw'/></a>
                                <a href={`/${person.zdbID}`} target='_blank' rel='noreferrer'>{person.display}</a>
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
        </>
    );

};

LinkAuthors.propTypes = {
    pubId: PropTypes.string,
}

export default LinkAuthors;
