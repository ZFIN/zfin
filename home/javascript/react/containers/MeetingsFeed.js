import React from 'react';
import {getPosts} from "../api/wiki";
import WikiPostList from "../components/WikiPostList";

function MeetingsFeed() {
    return <WikiPostList onInit={() => getPosts('meetings')} showAll='/display/news' />
}

export default MeetingsFeed;
