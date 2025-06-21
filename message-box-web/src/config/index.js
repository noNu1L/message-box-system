
const server = {
    // SERVER_IP: '192.168.67.221',
    SERVER_IP: '127.0.0.1',
    SERVER_PORT: '8675',
    SEND_SERVICE_NAME: 'send',
    WEB_SERVICE_NAME: 'web',

    /**
     *  microservice 服务完整版 ;
     *  simple : 简单版 只有邮件推送
     */
    MODE : 'simple'
};

const getWebServiceUrl = () => {
    if (server.MODE === 'microservice') {
        if (!server.SERVER_IP) {
            return `http://localhost:${server.SERVER_PORT}/${server.WEB_SERVICE_NAME}`;
        } else {
            return `http://${server.SERVER_IP}:${server.SERVER_PORT}/${server.WEB_SERVICE_NAME}`;
        }
    }
    if (server.MODE === 'simple') {
        return `http://${server.SERVER_IP}:${server.SERVER_PORT}`;
    }
};

const getSendServiceUrl = () => {
    if (server.MODE === 'microservice') {
        if (!server.SERVER_IP) {
            return `http://localhost:${server.SERVER_PORT}/${server.SEND_SERVICE_NAME}`;
        } else {
            return `http://${server.SERVER_IP}:${server.SERVER_PORT}/${server.SEND_SERVICE_NAME}`;
        }
    }
    if (server.MODE === 'simple') {
        return `http://${server.SERVER_IP}:${server.SERVER_PORT}`;
    }
};

const WEB_SERVICE_URL = getWebServiceUrl();
const SEND_SERVICE_URL = getSendServiceUrl();

const MODE = server.MODE;
const SERVER_IP = server.SERVER_IP;
const SERVER_PORT = server.SERVER_PORT;

module.exports = {
    WEB_SERVICE_URL,
    SEND_SERVICE_URL,
    MODE,
    SERVER_IP,
    SERVER_PORT
};