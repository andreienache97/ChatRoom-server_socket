# ChatRoom-server_socket

Create a chat server that can connect to clients. Using Java, you are required to create Server and Client programs. The main scope of each program should be as follows.
•	The Server program should have the following features:
-	Listen for new connections from clients.
-	Handle connections from multiple clients.
-	Respond to client requests.
-	Broadcast chat messages to all clients.
•	The Client program should have the following features:
-	Send new connections to a server.
-	Accept user input and handle sending to the server.
-	Handle responses from the server
Along with the features above, the following features can be also implemented.
-	A client can ask the server how long the server has been running for and receive the correct response.
-	A client can ask the server how long the client has been in the chat room for and receive the correct response.
-	A client can ask the server what the server’s IP Address is and receive the correct response.
-	A client can ask the server how many clients in total are currently connected to the chat room and receive the correct response.
-	A client can ask the server for a list of request commands that can be sent and receive the correct response.
-	The broadcasting of messages from each client should be handled by the server.
-	After connecting to the server, the client should be asked for a username by the server.
-	The client should only be able to chat in the chat room once a username has been selected.
-	The username that a client selects should be unique to the chat room.
-	The client should be able to send and listen for messages concurrently.
-	The client should handle the server going offline abruptly in a graceful way.
-	The server should handle the client disconnecting abruptly in a graceful way.
-	The client should be able to send a disconnect request to the server and the server should then handle this gracefully.
-	When a client connects or disconnects from the chat room, all other clients that are connected should be sent a notification about this action of the client.
-	All input/output should be handled accordingly through the correct use of Exception Handling.
-	Before connecting to a server a client must first be asked what the address is of the server they wish to connect to.
-	When a client sends the server specific requests, these interactions should not be broadcasted to other clients in the chat room and should therefore only be visible by the client sending the request and the server.
