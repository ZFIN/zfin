
let listeners = {};

const subscribe = (channelName, callback) => {
    const currentListeners = listeners[channelName] || [];
    listeners[channelName] = [...currentListeners, callback];
};

const publish = (channelName, eventDetails) => {
    const currentListeners = listeners[channelName] || [];
    currentListeners.forEach(listener => listener(eventDetails));
};

export { subscribe, publish };
