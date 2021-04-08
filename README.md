### Client-server chat Application

# Members:
* Keegan White WHTKEE004
* Stuart Mesham MSHSTU001
* Luc Hayward HYWLUC001

# Run Process
Navigate to out/artifacts and run either the server or the client jar as needed.
Use the following commands from within the folder containing the jar to run the respective programs:

* <b>Server:</b> java -jar Server.jar [port]
* <b>Client:</b> java -jar Client.jar [host_address] [port]
* Enter no arguments to run on localhost port 9000


# Features

# Login

In order to protect user’s privacy there is a mandatory login that has to be done before messaging can take place. The login requires a username which makes it possible for multiple clients to connect to the server concurrently, as this makes it possible for the server to differentiate between clients.

# Option to decline/accept files

In order to meet bandwidth constraints the user has an option to accept or decline receiving a file being sent to them. The user has the option to accept or decline the file at any point in time so that they can carry on with the conversation while there is a pending file transfer. This makes it possible for a user to get access to WiFi if they have data constraints and accept the file at this time.

# Graphic User Interface (GUI)

The inclusion of a GUI provides comfortable human computer interactions.

# Persisted Messages

If a user is offline and they are sent a message, they will receive this message when they login. This allows users to connect with any other user that is registered even if they are offline.

# Protocol Specification

# Message Format and Structure

Every message that is sent between the client and server or server and client consists of a header message. The header specifies whether there will be body messages to follow, amongst other important information. The header (shown in square brackets in the diagrams below) consists of the following fields: a command, destination, source and length field. The command type specifies what type of message it is, the destination indicates which client the command the message is intended for, the source is the client who initiated the message sequence and the length field is only used when the command type is a file, it indicates how long the byte array will be.

# Client Connection Sequence

The “AUTHENTICATE” command is included here so the server can react to the body messages appropriately. This command type will cause the server to expect two body messages to come through containing the username and password respectively. Once these messages have been received the server checks that the username and password is valid and either accepts or declines the login attempt, returning a message displaying the outcome to the client.

# Connecting to a User Sequence

The “CONNECT_CHAT” command is included here so the server can react to the fact there will not be body messages and thus the server simply checks that the username is valid. A header message containing “CONNECT_RSP” is sent to the client indicating that a body message will be sent with a “SUCCESS” or “FAIL” message, indicating whether the username is valid and whether the connection was successfully created.

# Send Message Sequence

The “MESSAGE” command type in the header is present in order for the server to know that one body message will follow the header message. Once this message has been received by the server it is sent to the intended client with a “MESSAGE” command in the header letting the client know to expect a body message.

# Send File Sequence

The “FILE” command type is sent in the header so that the client knows to expect 3 body messages, the file identification number (the time stamp followed by the sender’s username), the file name and then a byte array (the file data). Once the server has received these messages it will store the file and send a header message to the intended client with the “FILE_REQUEST” command type. The client will know that 2 body messages will follow with the file ID and filename. This allows the client to display the file name and show options to accept or reject the file. The file ID is sent so that when the client accepts or rejects the file, the client’s response message can contain the file ID, thus letting the server know which file to perform actions on. Once the client has accepted or declined the file, a file response message is sent to the server with a “FILE_RSP” command and a body message with the response and the file ID. This allows the server to delete the file if the user has declined the file or send the file to the user if the user accepted the request. Following this the server will delete the file. The file response stage of the sequence can happen at any time, allowing the user to carry on with the conversation even if they have not responded to the request.

# Graceful Disconnect Sequence

When a user clicks the exit button the client will respond by checking whether there are any queued messages to be sent to the server. If there are queued messages they will then be sent and then a header message with the “DISCONNECT” command is sent to the client. It will then receive a body message with the client's username so it knows who is disconnecting. The client will immediately disconnect after sending the disconnect message as there is no response needed from the server.
