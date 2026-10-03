# 06 — Cliente: render y GUI

## `client/util/LocksClient.java`

Núcleo del render y de la interacción cliente.

### `renderLocks(...)` — candados en el mundo

Llamado desde `WorldRendererMixin` al comienzo de `LevelRenderer.renderLevel`.
Para cada `Lockable` del handler del cliente:

1. Calcula su `Lockable.State` (`getLockState`).
2. Culling: `inRange` (distancia según render distance) y `inView` (frustum).
3. Detección de tooltip: si el candado está a ≤5 bloques y cerca de la línea de
   vista, lo guarda en `tooltipLockable` (lo consume `GuiMixin`).
4. Dibuja el `stack` del candado como modelo 3D:
   - traslada al punto en coordenadas relativas a la cámara,
   - orienta según `Transform.tr` (ceiling/wall → rotaciones en X/Y),
   - aplica la animación de `swing` con una curva Bézier cúbica
     (`LocksClientUtil.cubicBezier1d`),
   - escala a 0.5 y renderiza con `ItemRenderer.renderStatic`.

### `renderSelection(...)` — caja de selección

Dibuja el rectángulo de selección del modo `freeLock` usando el attachment
`SELECTION` del jugador y el bloque apuntado (`mc.hitResult`). El color cambia si
la selección es válida (volumen ≤ `MAX_LOCKABLE_VOLUME` y bloque lockeable).
Usa `LocksRenderTypes.OVERLAY_LINES`.

### `renderHudTooltip(...)` — tooltip custom del candado

Dibuja a mano (con `BufferBuilder` POSITION_COLOR) el panel con las líneas de
tooltip del item del candado, con borde verde y una "guía". Se llama desde
`GuiMixin.render` si el jugador sostiene una ganzúa.

### `onScreenKeyPressed(...)` — renombrar llaves

Listener `ScreenEvent.KeyPressed.Pre` registrado desde `LocksClient`. Con la
tecla **G** sobre un slot que tenga una `KeyItem` (o una llave dentro de un
llavero en pantalla), abre `RenameKeyScreen`. Obtiene el slot con reflexión
sobre `AbstractContainerScreen.hoveredSlot` (campo privado, vía `setAccessible`).
Cierra el menú actual antes de abrir la pantalla de renombrado.

## `client/util/LocksClientUtil.java`

Utilidades de dibujo y matemática:

- `getCamera()` / `getFrustum(...)` — cámara y frustum de culling (reutiliza el
  del `LevelRenderer` si está disponible).
- `worldToScreen(pos, partialTicks)` — proyección 3D→2D con manejo de view
  bobbing; la usa el tooltip del HUD.
- `distanceToLineSq` — distancia punto-recta.
- Helpers de geometría: `line`, `square`, `vGradient`, `texture`, `lerp`,
  `cubicBezier1d`.

## `client/init/`

- **`LocksScreens`** — asocia `LOCK_PICKING_TYPE` con `LockPickingScreen` y
  `KEY_RING_TYPE` con `KeyRingScreen` vía `MenuScreens.register`.
- **`LocksItemModelsProperties`** — registra item properties:
  - `locks:keys` en los llaveros: 0, 0.1, 0.21, 0.32 según 0/1/2/3+ llaves
    (cambia el modelo).
  - `locks:open` en los 5 candados: 1 si `LockItem.isOpen`, para el modelo
    abierto.
- **`LocksRenderTypes`** — `locks.overlay_lines`, un `RenderType` de líneas sin
  depth test para la caja de selección.
- **`LocksNetworkClient`** — vacío (la red se registra en `LocksNetwork`, común).

## Pantallas

### `LockPickingScreen`
Ver [05 — Lockpicking](05-lockpicking.md).

### `KeyRingScreen` — `client/gui/KeyRingScreen.java`

`AbstractContainerScreen<KeyRingContainer>`. Usa la textura genérica
`minecraft:textures/gui/container/generic_54.png`. `imageHeight` se ajusta a la
cantidad de filas del llavero (`114 + rows*18`). No tiene lógica propia más allá
de dibujar el fondo y los slots que arma `KeyRingContainer`.

### `RenameKeyScreen` — `client/gui/RenameKeyScreen.java`

- `Screen` liviana con un `EditBox` (máx. 50) precargado con el nombre actual.
- Enter o botón "Rename" → manda `RenameKeyPacket(slotIndex, keyId, name)` al
  servidor. Escape cierra.
- Sólo cliente (`@OnlyIn(Dist.CLIENT)`).

## Menús cliente-relevantes

- **`KeyRingContainer`** (`common/container/KeyRingContainer.java`) define sus
  propios slots:
  - `KeyRingSlot`: sólo acepta `KeyItem`; suena `KEY_RING` al poner/sacar.
  - `StorageSlot`: envuelve el inventario del jugador y protege el stack del
    llavero (no se puede mover el propio llavero desde dentro).
  - `stillValid`: el llavero sigue en la mano correcta.
- `KeyRingContainer.getExtraData(hand)` manda la mano al abrir.

## Mixins de cliente

- **`client/WorldRendererMixin`** — inyecta `renderLocks` en `renderLevel`
  (antes del primer `checkPoseStack`).
- **`client/GuiMixin`** — inyecta en `Gui.render` (TAIL) el dibujo del tooltip
  del candado apuntado si el jugador sostiene una ganzúa.
- **`client/ClientTickMixin`** — en `Minecraft.tick()` (TAIL) llama
  `Lockable.tick()` a todos los lockables cargados (avanza la animación de swing).
- **`ChunkCacheMixin`** (listado como cliente) — al descargar un chunk borra sus
  lockables del mapa del cliente.
