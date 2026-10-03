# 05 — Lockpicking (minijuego de ganzúa)

El lockpicking es un minijuego 2D tipo "tumbler": hay que elegir, en orden, el
pin correcto de cada columna. El servidor tiene la combinación
(`Lock.combo`); el cliente sólo anima y manda intentos.

## Apertura del menú

El gate de la ganzúa vive **en el evento**, no en `useOn`: `onRightClickBlock`
consume el clic antes de que corra `LockPickItem.useOn`, así que la rama de
ganzúa de `LocksEvents.handleRightClick` es la que decide:

1. Verifica `LockPickItem.canPick(stack, complexity)` (fuerza de la ganzúa vs.
   encantamiento `complexity` del candado). Si no alcanza, muestra
   `locks.status.too_complex` (cliente) y devuelve `FAIL` sin abrir nada.
2. Si alcanza, en servidor abre `LockPickingContainer.Provider(hand, lkb)` con:
   - extra data: `hand` (enum) + `id` del lockable.
   - el registro `LockPickingContainer` reconstruye el container desde
     `getLockableHandler(level).getLoaded().get(id)`.

`LockPickItem.useOn` conserva el mismo chequeo, pero en la práctica **no se
ejecuta** para un lockable trabado (el evento ya fue consumido).

## Servidor — `common/container/LockPickingContainer.java`

Estado por sesión:

- `currIndex`: índice del próximo pin a tirar.
- `shocking`, `sturdy`, `complexity`: niveles de encantamiento leídos del item
  del candado.
- `pos`: posición media del candado (para sonidos).
- Slots del inventario del jugador como `HiddenSlot` (no se muestran) para que
  el menú sea válido.

Métodos clave:

- `isValidPick(stack)` — debe ser del tag `LOCK_PICKS` y alcanzar la complejidad.
- `stillValid` — `lock.isLocked() && isValidPick(item en mano)`.
- `isOpen()` — `currIndex == lock.getLength()`.
- `tryPin(int currPin)`:
  - si `checkPin(currIndex, currPin)`: avanza, suena `PIN_MATCH`, `correct=true`.
  - si no: `tryBreakPick`. Si se rompe la ganzúa, `reset()` (vuelve al primer
    pin) y, si `shocking > 0`, aplica daño `locks:shock` (`shocking * 1.5`) y
    suena `SHOCK`. Si no se rompe, suena `PIN_FAIL`.
  - Manda `TryPinResultPacket(correct, reset)` al jugador.
- `tryBreakPick(player, pin)` — decide si se rompe la ganzúa:
  ```
  sturdyModifier = sturdy == 0 ? 1 : 0.75 + sturdy*0.5
  ch = strength / sturdyModifier            // mayor = más resistente
  ex = (1 - ch) * (1 - breakChanceMult(pin))
  no se rompe si  random < ex + ch
  => chance de romper = breakChanceMult(pin) * (1 - ch)
  ```
  `breakChanceMult`: si el pin elegido es adyacente al correcto (`|real-pin| == 1`),
  es `0.33` (perdona el error de "un pin al lado"); en cualquier otro caso `1`.
  `chance de romper = breakChanceMult(pin) * (1 - ch)`. Una ganzúa más fuerte
  (`ch` alto) reduce la chance de rotura; `sturdy` alto **la aumenta** (baja `ch`).
  Si el stack **no** está en `LOCK_PICKS`, igual se rompe.
  Al romperse, si se queda sin ganzúas busca otra válida en el inventario y la
  pone en la mano.
- `removed(player)` — al cerrar el menú, si el lock quedó **abierto**, lo
  desbloquea (`setLocked(false)`) y suena `LOCK_OPEN`.
- `quickMoveStack` — deshabilitado (devuelve vacío).

> Nota: el lock queda trabado mientras se juega; recién se destraba al cerrar el
> menú con todos los pines resueltos (`isOpen()`). No hay auto-relock acá.

## Cliente — `client/gui/LockPickingScreen.java`

- Elegida por `LocksScreens` para el menú `lock_picking`.
- Carga texturas según el candado:
  `textures/gui/<registry_path>.png` (p. ej. `iron_lock.png`), y la ganzúa
  `…_lock_pick.png`.
- Arma la escena con sprites:
  - pared frontal, columnas, paredes internas, pared trasera y manija;
  - por pin: `pinTumbler` (3 variantes), `upperPin` y `SpringSprite` (resorte);
  - la ganzúa `lockPick` y sus dos mitades (`leftPickPart`/`rightPickPart`) para
    el efecto de "entrar por el ojo de la cerradura".
- `renderBg`: dibuja la textura a escala 2× y luego todos los sprites.
- Input (`keyPressed`/`keyReleased`):
  - izquierda/derecha mueven la ganzúa (`speedX = ±4`);
  - arriba tira el pin seleccionado (`pullPin(getSelectedPin())`), envía
    `TryPinPacket` y reproduce la animación.
- `handlePin(correct, reset)` (invocado por `TryPinResultPacket`): anima el pin,
  marca `pins[i]`, y si `reset` limpia todo.
- `reset()`: deshace pines, hace fade de la ganzúa y congela (`frozen = true`).
- `resetPick()` / `resetSpeed()` / `boundLockPick()` / `updatePickParts()`:
  gestión de la ganzúa y su animación.
- `renderLabels`: título y, si el lock está abierto, el hint
  `locks.gui.lockpicking.open`.

## Sistema de sprites y acciones

`client/gui/sprite/`:

- **`TextureInfo`** — región de textura (`startX/Y`, `width/height`, canvas) y
  `draw` con `GuiGraphics.blit`.
- **`Sprite`** — posición, velocidad, rotación y alpha con interpolación
  (`old*` vs `*`), más una cola de acciones.
- **`SpringSprite`** — elige el frame del resorte según la posición del upper pin.
- **Acciones** (`sprite/action/`):
  - `IAction` — interfaz (`isFinished`, `update`, `finish`, `then`).
  - `SingleCallbackAction` — soporte de callback al terminar.
  - `TimedAction` — base con contador de ticks.
  - `MoveAction` / `AccelerateAction` — traslación lineal / con aceleración.
  - `FadeAction` — variación de alpha.
  - `WaitAction` — espera N ticks.
  - Los callbacks `unfreezeCb` (descongela al terminar de meter la ganzúa) y
    `resetPickCb` (vuelve a preparar la ganzúa) están en la pantalla.

## Red del minijuego

- Cliente → servidor: `TryPinPacket(byte pin)`.
- Servidor → cliente: `TryPinResultPacket(boolean correct, boolean reset)`.
- `LockPickingContainer.getExtraData(hand, lkb)` describe la apertura del menú.

Ver [07 — Red](07-red.md) para los detalles de los payloads.
