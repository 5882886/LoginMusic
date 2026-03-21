A simple Minecraft mod that can play music when players join in the world.

> This mod uses AI to assist in development.
>
> Since mod version 1.1.0, javafx has given place to `com.googlecode.soundlibs:mp3spi`, which provides MP3 support now.
> 
> Inspired by TartaricAcid's [Net Music](https://modrinth.com/mod/net-music) !

## Functions

- [x] Play music
- [x] Supported file types: mp3, wav
- [x] Music Downloading screen
- [x] Allow download switch
- [ ] Play different musics based on the player's name (uuid)

## Files Structure

All music files are stored in the `/LoginMusic` folder.

```
├── {your minecraft version}
    ├── config
    │   ├── login_music-common.toml     # Universal config
    │   └── login_music.toml        # Configure specific music
    │
    ├── LoginMusic                      # Store music files
    │   ├── music_1.mp3
    │   ├── music_2.wav
    │   └── ...
    └ ...
```

## Config

### Client Side

`login_music-client.toml` is effective only for the client:

```toml
["Basic"]
    #Range of music play (a non negative integer)
    #Range: 0 ~ 100
    range = 3
    #Whether to allow downloading music from the internet
    InternetAccess = true
```


### Server Side

`login_music-server.toml` defines all the players' musics:

```toml
[Selection]
    #Keywords for music selection (name/uuid)
    type = "name"

[Music]
    # Use 'Default' as the default setting
    entries = ["Default|Default.mp3|https://www.example.com/Example.mp3"]
```

Each entry is a string like: `" Target player | Music name | Music URL "`.

> The "Music URL" is available if and only if `InternetAccess` is set "true" (the default setting is "false").

For example, 

```toml
[music]
    entries = ["Steve|login_music.mp3|https://www.example.com/login_music.mp3", "Default|Default.mp3|https://www.example.com/Default.mp3"]
```

When a player named Steve enters the world, the mod will try to find the music matched the name `Steve` (in this case it's `login_music.mp3` ) in the folder `/LoginMusic` first.
Then try to download it from its related url if it has failed before.

> Tips: If there is a file with the same name in the folder, it will skip the download even if the file does not match the URL.

If more entries need to be configured, please separate them with commas. 