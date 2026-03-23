A simple Minecraft mod that can play music when players join in the world.

> This mod uses AI to assist in development.
>
> Since mod version 1.1.0, javafx has given place to `com.googlecode.soundlibs:mp3spi`, which provides MP3 support now.
> 
> Since mod version 1.2.0, the music config has changed to JSON type.
> 
> Inspired by TartaricAcid's [Net Music](https://modrinth.com/mod/net-music) !

The logo comes from [here](https://www.flaticon.com/free-icon/music_9325026?term=music&page=1&position=87&origin=search&related_id=9325026), designed by juicy_fish.

## Functions

- [x] Play music
- [x] Supported file types: mp3, wav
- [x] Music Downloading screen
- [x] Allow download switch
- [x] Play different musics based on the player's name (uuid)
- [ ] Show lyrics while playing music

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
    │   ├── music_1.mp3
    │   ├── music_2.wav
    │   └── ...
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
[Basic]
    #Range of music play (a non negative integer)
    #Range: 0 ~ 100
    range = 3
    #Whether to allow downloading music from the internet
    InternetAccess = true
[Lyrics]
    #Whether to show lyrics while playing music
    ShowLyrics = true
```


### Server Side

`login_music-server.toml`:

```toml
[Selection]
    #Keywords for music selection (name/uuid)
    type = "name"
```

**Since mod version 1.2.0, the music config has changed to JSON type, just as shown below.**

If you enter a server with this mod, the config file on the server has higher priority, which will cover your custom settings.

```json
{
    "musics": [
        {
            "id": "Default",
            "name": "Default.mp3",
            "url": "https://www.example.com/example1.mp3"
        }, {
            "id": "Steve",
            "name": "login_music.mp3",
            "url": "https://www.example.com/example2.mp3"
        }
    ]
}
```

> The "Music URL" is available if and only if `InternetAccess` is set "true" (the default setting is "false").

For example, When a player named Steve enters the world, the mod will try to find the music matched the name `Steve` (in this case it's `login_music.mp3` ) in the folder `/LoginMusic` first.
Then try to download it from its related url if it has failed before.

> Tips: If there is a file with the same name in the folder, it will skip the download even if the file does not match the URL.

If more entries need to be configured, please follow the JSON's rule.


## Command

All the commands need permission level 2.

Use `/loginmusic reload` to reload config.

Use `/loginmusic list` to show music config available currently.