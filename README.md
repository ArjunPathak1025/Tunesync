# TuneSync

An open-source Android music player built around an extension-based provider architecture and an original, premium artwork-focused UI.

## Vision

TuneSync aims to combine:

- An extension/provider architecture inspired by the general design principles of modern extension-based players
- A premium, artwork-focused Android UI
- Android Media3 for real playback
- Persistent library, playlists, queue and history
- Replaceable music providers

## Architecture

```text
TuneSync UI
    ↓
ViewModels / Use Cases
    ↓
Extension Manager
    ↓
Extension API
    ↓
Provider Extensions
    ↓
Metadata / playback source
    ↓
Media3 Player
```

The first extension will be designed around a compatible YouTube Music provider. Provider implementations remain isolated from the UI and playback core.

## Current status

Milestone 1 foundation is in progress:

- Multi-module Gradle project
- Provider-independent music models
- Extension API
- Extension registry
- Initial Compose UI
- Home/Search/Library/Settings navigation
- Mini-player shell
- GitHub Actions build workflow

Playback, database persistence, real provider integration, lyrics, caching and offline features are subsequent milestones.

## Important

TuneSync is an independent project. It does not copy proprietary code, branding or assets from other applications. Provider integrations must respect applicable service terms, copyright, authentication and platform policies.
