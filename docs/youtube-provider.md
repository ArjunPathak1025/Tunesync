# YouTube Music provider

TuneSync keeps provider-specific code behind the extension API. The first provider is `youtube-music`.

## Current capabilities

- Runtime discovery of the YouTube Music web client's API configuration
- Search request through the web music client surface
- Normalization into TuneSync `Song` models
- Provider isolation through `YouTubeMusicTransport`
- Extension registration through `ExtensionManager`
- Search UI wired to the provider

## Deliberate boundaries

Playback URL resolution is not represented as a fake working implementation. YouTube Music web playback can return temporary/signed media URLs and the upstream protocol can change. The transport therefore exposes a replaceable `resolvePlaybackSource` boundary which currently returns `null` until it can be implemented and validated against the current provider behavior.

Likewise, song/artist/album/playlist detail and lyrics endpoints remain explicit extension capabilities rather than silently returning fabricated data.

## Architecture

```text
Compose UI
    |
ExtensionManager
    |
MusicExtension
    |
YouTubeMusicExtension
    |
YouTubeMusicTransport
    |
YouTube Music web client
```

This structure follows the public architectural idea used by Echo's extension template: an extension advertises capabilities by implementing client interfaces, while provider-specific HTTP logic remains inside the extension. TuneSync is an independent implementation and does not copy Echo source code.
