# Sharing file to chat via http upload

## User story
As a user I want to share file in chat.


## XEP-xxxx
[omemo-media-sharing](https://xmpp.org/extensions/inbox/omemo-media-sharing.html)

## Modules
```

```

## Command
```kotlin

```

## Handler
[handleUploadFile](../../core/domain/src/main/java/cc/cryptopunks/crypton/handler/UploadFile.kt)

## Requires
```kotlin

```

## Returns
```kotlin

```

## Errors
```kotlin

```

## Flow
1. Generate secure random 256 bit key.
1. Encrypt local file using AES protocol.
1. Upload encrypted file to server, server should return link to file.
1. Replace `https` protocol in line to `aesgcm`.
1. Add secure iv + key to link after right after symbol `#`.
1. Share formatted link via regular encrypted communication channel.