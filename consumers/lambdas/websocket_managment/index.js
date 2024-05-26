
const AWS = require('aws-sdk');
const apiGatewayManagementApi = new AWS.ApiGatewayManagementApi({
    endpoint: process.env.API_GATEWAY_URL
});

const dynamoDb = new AWS.DynamoDB.DocumentClient();


exports.handler = async (event) => {
  const connectionId = event.requestContext.connectionId;
  const routeKey = event.requestContext.routeKey;
  let response;

  switch (routeKey) {
    case '$connect':
      await dynamoDb.put({
        TableName: 'websocket_connections',
        Item: { connectionId, status: 'connected' }
      }).promise();
      response = { statusCode: 200, body: 'Connected.' };
      break;
    case '$disconnect':
      await dynamoDb.delete({
        TableName: 'websocket_connections',
        Key: { connectionId }
      }).promise();
      response = { statusCode: 200, body: 'Disconnected.' };
      break;
    case '$default':
      const message = JSON.parse(event.body);
      console.log("Started parsed message received from client: ", message);
      await sendToClient(connectionId, JSON.stringify({ message: 'Message received' }));
      response = { statusCode: 200, body: 'Message received.' };
      break;
    default:
      response = { statusCode: 400, body: 'Invalid route.' };
  }

  return response;
};

const sendToClient = async (connectionId, payload) => {
  await apiGatewayManagementApi.postToConnection({ ConnectionId: connectionId, Data: payload }).promise();
};
