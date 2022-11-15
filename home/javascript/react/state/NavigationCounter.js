
let navigationCounts = {};
let listeners = {};

const subscribe = (keyName, callback) => {
    const currentListeners = listeners[keyName] || [];
    listeners[keyName] = [...currentListeners, callback];
};

const setCounts = (keyName, count) => {
    navigationCounts[keyName] = count;
    const currentListeners = listeners[keyName] || [];
    currentListeners.forEach(listener => listener(count));
};

export { subscribe, setCounts };
