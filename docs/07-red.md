# 07 — Red

Registro en `common/init/LocksNetwork.java` con `RegisterPayloadHandlersEvent`
y versión de protocolo `"1"`. Cada payload implementa `CustomPacketPayload`,
define un `Type`, un `StreamCodec` y un handler estático.

## Cliente ← Servidor (`playToClient`)

| Payload | Campos | Handler |
|---------|--------|---------|
| `AddLockablePacket` | `LockableRecord` | agrega un `Lockable` nuevo al handler del cliente (`LockableHandler.add`) |
| `AddLockableToChunkPacket` | `LockableRecord`, `x`, `z` | mete el lockable en el storage del chunk; si no estaba en el mapa cargado, lo crea y registra el observer. Deduplica por id |
| `RemoveLockablePacket` | `id` | remueve el lockable del handler cliente |
| `UpdateLockablePacket` | `id`, `locked` | cambia el estado trabado del lockable cargado |
| `TryPinResultPacket` | `correct`, `reset` | reenvía el resultado al `LockPickingScreen` activo |

Los handlers corren en `context.enqueueWork(...)` (main thread) y castean
`context.player()` a `LocalPlayer`.

## Servidor ← Cliente (`playToServer`)

| Payload | Campos | Handler |
|---------|--------|---------|
| `TryPinPacket` | `pin` | llama `LockPickingContainer.tryPin(pin)` si el menú abierto es de lockpicking |
| `RenameKeyPacket` | `slotIndex`, `keyId`, `name` | renombra una llave del inventario, de un llavero en pantalla o de un llavero en Curios |

### Detalles de `RenameKeyPacket`

- Si `keyId != NO_KEY_ID` (`Integer.MIN_VALUE`): busca una `KeyItem` con ese id
  en el inventario, dentro de llaveros del inventario o dentro de llaveros
  equipados en Curios, y le aplica el nombre.
- Si `keyId == NO_KEY_ID`: usa `slotIndex` sobre el menú abierto
  (`player.containerMenu`) para renombrar el stack del slot.
- `sanitize`: quita los códigos de color (`§`) y caracteres de control, y corta
  a 50 caracteres.
- `applyName`: setea o remueve `DataComponents.CUSTOM_NAME`.
- `sync`: `broadcastChanges()` del menú actual y del inventario para que el
  cliente vea el cambio.

## Serialización

Cada payload tiene **dos** códecs:

- `Codec<...> CODEC` — formato Mojang (datapack / registro).
- `StreamCodec<..., ...> STREAM_CODEC` — el que usa el registro de payloads.

Los records de datos usan `record … (…)` con factory methods
(`AddLockableToChunkPacket(Lockable, ChunkPos)`, etc.) para construir desde el
lado servidor.

## Objetos de transferencia

- `Lockable.LockableRecord` — `bb`, `LockRecord`, `tr` (ordinal), `stack`, `id`.
- `Lock.LockRecord` — `id`, `combo`, `locked`.
- `Cuboid6i` — 6 enteros.
- `ItemStack` — `ItemStack.STREAM_CODEC` de vanilla.

## Payloads legacy

`toclient/ClientNet` y `toserver/ServerNet` existen pero están marcados
`@Deprecated` y con cuerpo vacío: no hacen nada. Son residuo del port.

## Broadcast de lockables

El servidor no usa tracking de dimensiones: `LockableHandler.add`, `remove` y
`update` mandan el paquete a **todos** los jugadores
(`level.getServer().getPlayerList().players`). El cliente resuelve por ids; los
`AddLockableToChunkPacket` se mandan también en `ChunkMap.onChunkReadyToSend`
para quien entra a ver un chunk. El cliente deduplica por id.
