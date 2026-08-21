# Echo YouTube Extension → TuneSync mapping

TuneSync uses the public Echo extension architecture as an architectural reference. It does not copy Echo or SimpMusic source code.

## What the Echo YouTube extension demonstrates

The upstream Echo YouTube extension is an extension-template project with separate `app` and `ext` modules. Its README documents extension metadata (`extType`, `extId`, `extClass`, name/description/author/update metadata) and explains that Echo discovers supported capabilities from implemented `Client` interfaces. It also documents separate music/tracker/lyrics extension types, local testing, app testing, and GitHub Actions publishing.

## TuneSync mapping

| Echo concept | TuneSync design |
| --- | --- |
| Extension metadata | `MusicExtension` metadata |
| Client capability interfaces | Small provider capability interfaces |
| Extension discovery | `ExtensionManager` |
| Provider model conversion | TuneSync normalized models |
| Playback result | `PlaybackSource` |
| Host/player | TuneSync core + Media3 |

## Planned capability interfaces

- SearchClient
- SongClient
- ArtistClient
- AlbumClient
- PlaylistClient
- LibraryClient
- LyricsClient
- PlaybackClient

Each capability is optional. An extension only implements the interfaces it supports.

## YouTube extension boundary

Provider-specific code belongs under `extensions/youtube`. It will translate provider responses into TuneSync models. UI and Media3 code must not contain YouTube-specific API calls.

The provider may expose metadata/search first. Playback is enabled only when a supported and permitted playback source can be obtained. Authentication/session handling remains isolated from UI code and secrets are never committed.

## Licensing and independence

This document describes architecture and does not copy source code. Any direct reuse of Echo source would require following Echo's applicable license and attribution requirements.
