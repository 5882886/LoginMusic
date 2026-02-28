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