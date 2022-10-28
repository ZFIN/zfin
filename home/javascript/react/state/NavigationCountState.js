
let navigationCounts = {};
let listeners = {};

const getCounts = () => {
    return {...navigationCounts};
};

const setCounts = (keyName, count) => {
    navigationCounts[keyName] = count;
    const currentListeners = listeners[keyName] || [];
    currentListeners.forEach(listener => listener(count));
};

const subscribe = (keyName, callback) => {
    const currentListeners = listeners[keyName] || [];
    listeners[keyName] = [...currentListeners, callback];
};

export { getCounts, subscribe, setCounts };


