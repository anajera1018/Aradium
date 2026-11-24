# Aradium - Music Player for Android

CPSC 411A Final Project

## Team Members
- Anthony I. Najera

## Project Description
Aradium is a simple offline music player for Android. You add audio files that are already on your phone, give them a name, artist, description, date and cover, and play them like you would in Spotify or Apple Music. Music keeps playing when the screen is off or the app is in the background, and you can control it from the notification or lock screen.

## Problem It Solves
A lot of people have music files on their phone (old downloads, recordings, mixes, songs from friends) but most music apps are built around streaming and accounts. Aradium gives those local files a clean home: one library, playlists, favorites and basic playback, with no account, no ads and no internet needed.

## Main Features
- Upload any audio file from the device (mp3, m4a, wav, flac, ogg, ...), one or many at a time
- Reads the song's built-in title, artist, album, genre, year and cover art when it has them
- Edit name, artist, album, genre, date, description and favorite
- Add a cover from the gallery or remove it
- Optional "Search online" button that fills in song info and cover art (iTunes Search API)
- Play, pause, skip, previous, seek, shuffle and repeat (off / all / one)
- Background playback with notification and lock screen controls
- Mini player on every page and a full Now Playing page
- Create, rename and delete playlists
- Add and remove songs in a playlist, move songs up and down, sort by title, artist or date added
- Favorites, Recently Played, Most Played and Recently Added
- Search songs by name, artist, album or genre, and search playlists
- Account name, dark mode and default sort saved on the device
- Works with non-English song names and file names

## Pages
| Page | What it does |
|------|--------------|
| Home | Greeting, Play all / Shuffle, Recently Played, Most Played, Recently Added, Favorites |
| Library | Songs, Playlists and Favorites tabs with sorting |
| Upload | Pick audio files and edit them right after adding |
| Search | Search the whole library |
| General | Account name, settings, library stats, about |
| Edit Song | Change song info and cover |
| Playlist | Play, sort, reorder, add and remove songs |
| Now Playing | Big cover, seek bar and all playback controls |

## Technologies Used
- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- ViewModel + StateFlow
- Kotlin Coroutines and Flow
- Room Database
- DataStore Preferences
- Media3 ExoPlayer + MediaSessionService
- Retrofit + Gson
- Coil

## Architecture Overview
The app follows MVVM.

```
UI (Compose screens)
        |
   MusicViewModel  ---- MediaController ---- PlaybackService (ExoPlayer)
        |
   MusicRepository
     |      |       \
  Room   DataStore   Retrofit
```

- **UI layer**: `HomeScreen`, `LibraryScreen`, `UploadScreen`, `SearchScreen`, `GeneralScreen`, `EditSongScreen`, `PlaylistScreen`, `PlayerScreen`, shared pieces in `Components.kt`, routes in `Navigation.kt`. Screens only show state and send events to the ViewModel.
- **ViewModel**: `MusicViewModel` holds the app state as `StateFlow` (songs, playlists, settings, player, upload and lookup states) and runs all work in `viewModelScope`.
- **Data layer**: `MusicRepository` is the single source of truth. It talks to Room (`Song`, `Playlist`, `PlaylistSong`, `MusicDao`, `MusicDatabase`), DataStore (`SettingsStore`) and Retrofit (`SongApi`).
- **Playback**: `PlaybackService` is a Media3 `MediaSessionService` that owns ExoPlayer, so music keeps playing in the background and shows a media notification.

## Screenshots
| Home | Library | Upload |
|------|---------|--------|
| ![Home](screenshots/home.png) | ![Library](screenshots/library.png) | ![Upload](screenshots/upload.png) |

| Search | General | Now Playing |
|--------|---------|-------------|
| ![Search](screenshots/search.png) | ![General](screenshots/general.png) | ![Player](screenshots/player.png) |

| Edit Song | Playlist |
|-----------|----------|
| ![Edit](screenshots/edit.png) | ![Playlist](screenshots/playlist.png) |

## Installation / Run Instructions
1. Clone the repo: `git clone https://github.com/anajera1018/Aradium.git`
2. Open the project folder in Android Studio or IntelliJ IDEA
3. Wait for Gradle sync to finish (JDK 17 or newer)
4. Run the `app` configuration on an emulator or phone (Android 7.0 / API 24 or newer)
5. Allow notifications when asked so the playback controls show up
6. Go to Upload and pick some audio files. On an emulator, drag an mp3 onto the emulator window first so there is something to pick.

## Team Member Contributions
| Member | Contributions |
|--------|---------------|
| Anthony I. Najera | App idea and design, Room database, DataStore settings, playback service, ViewModel, all Compose screens, navigation, Retrofit lookup, testing, README |
