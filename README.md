A simple Minecraft mod that can play music when players join in the world.

> This mod uses AI to assist in development.
>
> Inspired by TartaricAcid's [Net Music](https://modrinth.com/mod/net-music) !

| Milestone | Big changes                                                                                  |
|-----------|----------------------------------------------------------------------------------------------|
| 1.1.0     | javafx has given place to `com.googlecode.soundlibs:mp3spi`, which provides MP3 support now. |
| 1.2.0     | the music config has changed to JSON type.                                                   |
| 1.2.1     | Music can be played directly through the Internet without downloading; lyrics support!       |
| 1.2.2     | Audio backend loading, Players can receive audio from other players on the server.           |                                                                                          |

The logo comes from [here](https://www.flaticon.com/free-icon/music_9325026?term=music&page=1&position=87&origin=search&related_id=9325026), designed by juicy_fish.

## Functions

- [x] Play music
- [x] Supported file types: mp3, wav
- [x] Music Downloading screen
- [x] Allow download switch
- [x] Play different musics based on the player's name (uuid)
- [x] Show lyrics while playing music

## File Structure

All music files are stored in the `/LoginMusic` folder.

```
├── {your minecraft version}
    ├── config
    │   ├── login_music-client.toml     # Client config
    │   └── login_music
    │       └── music.json               # Configure specific music
    │
    ├── LoginMusic                      # Store music files
    │   ├── Lyrics
    │   │   ├── music_1.lrc
    │   │   ├── music_2.lrc
    │   │   └── ...
    │   └── MusicCache
    │       ├── music_1.mp3
    │       ├── music_2.wav
    │       └── ...
    │ 
    ├── saves/world
    │   ├── serverconfig
    │   │   ├── login_music.toml        
    │   │   └ ...
    │   └── ...
    └── ...
```

## Configuration

### Client Side

`login_music-client.toml` is effective only for the client:

```toml
[Lyrics]
    #Whether to show lyrics while playing music
    ShowLyrics = true
    #Defines the position of the lyrics
    #Allowed Values: UP, MIDDLE, DOWN
    LyricsPosition = "DOWN"
    #Defines the color of the lyrics you want to use
    LyricsColor = "#FFFFFF"

[Music]
    #Range of music play (a non negative integer)
    # Default: 3
    # Range: 0 ~ 100
    range = 3
    #Whether to allow downloading music from the internet
    InternetAccess = false
    #Whether to play musics from other players
    AllowOthersMusic = true
```

### Server Side

`login_music-server.toml`:

```toml
[Selection]
    #Keywords for music selection (name/uuid)
    #Allowed Values: NAME, UUID
    type = "NAME"
```

**Since mod version 1.2.0, the music config has changed to JSON type, just as shown below.**

If you enter a server with this mod, the config file on the server has higher priority, which will cover your custom settings.

```json
{
    "musics": [
        {
            "id": "Default",
            "music": "Default.mp3",
            "musicUrl": "https://www.example.com/example.mp3",
            "lyric": "Default.lrc",
            "lyricUrl": "https://www.example.com/example.lrc" 
        }, {
            "id": "Steve",
            "music": "login_music.mp3",
            "musicUrl": "https://www.example.com/login_music.mp3",
            "lyric": "login_music.lrc",
            "lyricUrl": "https://www.example.com/login_music.lrc"
        }
    ]
}
```

> The music will be downloaded if `InternetAccess` is set "true" (the default setting is "false"), otherwise, the music is played through network audio streams, which may cause a short pause when entering a world.

For example, When a player named Steve enters the world, the mod will try to find the music matched the name `Steve` (in this case it's `login_music.mp3` ) in the folder `/LoginMusic` first.
Then try to download it from its related url if it has failed before.

> Tips: If there is a file with the same name in the folder, it will skip the download even if the file does not match the URL.

If more entries need to be configured, please follow the JSON's rule.


## Command

All the commands need permission level 2.

| Command                           | Function                              |
|-----------------------------------|---------------------------------------|
| `/loginmusic reload`              | reload config                         |
| `/loginmusic list`                | show music config available currently |
| `/loginmusic show <targetPlayer>` | show targetPlayer's music config      |
| `/loginmusic play`                | replay your login music               |