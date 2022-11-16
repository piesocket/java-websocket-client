# Java WebSockets Client For Android

PieSocket's Android SDK for WebSockets written in Java.


This SDK can be used to communicate with any third-party WebSocket server,
and implements auto-reconnection among other best WebSocket practices.


## Installation
Let's start by adding PieSocket Android SDK as a dependency to your application. 

### Gradle (Kotlin)
```
implementation("com.piesocket:channels-sdk:1.0.4")
```

### Gradle (Java)
```
implementation 'com.piesocket:channels-sdk:1.0.4'
```

### Maven
```
<dependency>
    <groupId>com.piesocket</groupId>
    <artifactId>channels-sdk</artifactId>
    <version>1.0.4</version>
</dependency>
```

## Permissions
Setup manifest permissions as instructed [here](https://www.piesocket.com/docs/3.0/android-websockets#permissions).

## Usage

### Use PieSocket's managed WebSocket server
Get your API key and Cluster ID here: [Create PieSocket account](/app/v4/register)

```
PieSocketOptions options = new PieSocketOptions();
options.setClusterId("demo");
options.setApiKey("VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV");

PieSocket piesocket = new PieSocket(options);
Channel channel = piesocket.join("chat-room-1");
```

### Stand-alone Usage
To use with third-party WebSocket servers, skip the initialization part above and create a Channel instance as shown below.

```
Channel channel = new Channel("wss://example.com", true);
```

[PieSocket Channels](https://piesocket.com/channels) is a managed WebSockets service with following features:
  - Authentication
  - Private Channels
  - Presence Channels
  - Publish messages with REST API
  - Auto-scalability
  - Webhooks
  - Analytics
  - Authentication
  - Upto 60% cost savings

We highly recommend using PieSocket Channels over self hosted WebSocket servers for production applications.

## Documentation
For usage examples and more information, refer to: [Official SDK docs](https://www.piesocket.com/docs/3.0/android-websockets)