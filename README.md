A simple Minecraft mod that can play music when players join in the world.

> This mod uses AI to assist in development.
> 
> Currently, it uses javafx to play audio. As a result, it may not work well on some platforms.

## Functions

- [x] Play music
- [x] Supported file types: mp3, wav
- [x] Music Downloading screen
- [ ] Play different musics based on the player's name (uuid)

## Files Structure

All music files are stored in the `/LoginMusic` folder.

```
├── {your minecraft version}
    ├── config
    │   └── login_music-common.toml     # Universal config
    │
    ├── LoginMusic                      # Store music files
    │   ├── music_1.mp3
    │   ├── music_2.wav
    │   └── ...
    │
    ├── saves/world
    │   ├── serverconfig
    │   │   ├── login_music.toml        # Configure specific music
    │   │   └ ...
    │   └── ...
    └── ...
```

## Config

Please set your musics at `login_music.toml`.

```
[music]
    # Use 'Default' as the default setting
    entries = ["Default|Default.mp3|https://www.example.com/Example.mp3"]
```

Each entry is a string: `" Target player | Music name | Music URL "`.

For example, 

```
[music]
    entries = ["Steve|login_music.mp3|https://www.example.com/Example.mp3"]
```

When a player named Steve enters the world, the mod will try to find the music matched the name `Steve` (in this case it's `login_music.mp3` ) in the folder `/LoginMusic` first.
Then try to download it from its related url if it has failed before.

> Tips: If there is a file with the same name in the folder, it will skip the download even if the file does not match the URL.

If more entries need to be configured, please separate them with commas. Set `Default` for anyone that  