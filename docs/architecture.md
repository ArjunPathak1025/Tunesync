# TuneSync architecture

TuneSync separates the player and UI from external music providers.

```text
UI -> ViewModels -> Domain/Core -> Extension API -> Provider
                         |
                         +-> Media3 playback
                         +-> Room persistence
```

Providers expose normalized models such as songs, artists, albums and playlists. The core player remains provider-agnostic.

## Extension boundary

An extension may provide metadata, search, artwork, lyrics, and a permitted playback source. It must not own the Android media service or bypass provider access controls. The core application remains responsible for playback, queue state, notifications, and lifecycle.

## Planned milestones

1. Core project and extension contracts
2. Media3 playback service and queue
3. Room library/history/favorites
4. Extension manager
5. Provider integration
6. Lyrics and caching
7. Testing and CI
