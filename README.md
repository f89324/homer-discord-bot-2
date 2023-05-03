# Homer-bot

![homer-logo](https://github.com/f89324/homer-discord-bot/blob/develop/resources/homer.png)  
Exclusive bot for Donut Hole server.


### Setup
#### Running the bot
```
chcp 65001
java ^
-Dspring.config.location=homer-config.yml ^
-jar untitled-0.0.1.jar
```

### Usage

#### Common behavior
* The bot will automatically connect to the audio channel if it is idle and someone enters the audio channel.
* The bot will automatically disconnect from the audio channel if the last person left.
* The bot will play personal intro music when specific people enter an active audio channel.

#### Basic commands
* `join` - Joins a voice channel.
* `leave` - Leaves a voice channel.
* `react` - Broadcasts a reaction to the voice channel.

#### Permissions bot need to work
* `VIEW_CHANNEL` - To read text channels & see voice channels.
* `READ_MESSAGE_HISTORY` - To read command messages.
* `SEND_MESSAGES` - To answer your commands and send notification messages.
* `CONNECT` - To join to a voice channel.
* `SPEAK` - To play audio in a voice channel.