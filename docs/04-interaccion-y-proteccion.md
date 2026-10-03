# 04 — Interacción y protección de bloques

Todo lo que hace que un bloque "esté candado" se reparte entre **eventos** y
**mixins**. Los eventos cubren la interacción del jugador; los mixins cubren el
comportamiento del bloque (romper, empujar, abrir, explosiones, contenedores).

## Eventos — `common/event/LocksEvents.java`

Registrados en `NeoForge.EVENT_BUS`:

- `onRightClickBlock` (`PlayerInteractEvent.RightClickBlock`)
- `onBlockBreak` (`BlockEvent.BreakEvent`)
- `LocksVillagerTrades::onVillagerTrades` / `onWanderingTrades`
- `onLootTableLoad` (`LootTableLoadEvent`) — **hoy no-op** (ver doc 08).

### `handleRightClick` — el corazón de la interacción

Es el flujo del clic derecho sobre un bloque. Primero junta los lockables que
**intersectan** la posición; si no hay, `PASS` (el bloque actúa normal). Si el
clic no es con la mano principal, `FAIL`.

**Shift + clic con llave (mano, `MASTER_KEY` o llavero):** antes que nada, si la
llave matchea algún lockable intersectado, **togglea** su estado, limpia
`autoRelock` (sólo en servidor) y devuelve `CONSUME`. Al consumir el evento el
bloque **no se abre**: es la forma de **dejar un cofre/puerta destrabado a
propósito** (para que otros lo abran) o de volver a trabarlo sin abrirlo. No
choca con "quitar candado", que exige mano vacía.

Toma el primer lockable **trabado** y evalúa:

1. **Llave en Curios** (`checkCuriosKeyrings`): un llavero equipado en Curios con
   una llave del id correcto. Desbloquea/togglea los lockables con ese id y
   devuelve `CONSUME`. No marca `autoRelock` (queda abierto).
2. **Llave en mano** (o `MASTER_KEY`): dispara `swing`.
   - Si el bloque es un `BaseContainerBlockEntity` (cofre, cofre trampa, barrica,
     horno, dispensador, shulker, etc.): desbloquea los lockables con ese id,
     marca `autoRelock = true` en servidor y devuelve `PASS`, para que el
     contenedor abra en el mismo clic (ver **agujero negro** más abajo).
   - Si **no** es contenedor (por ejemplo puerta de hierro): devuelve `PASS` sin
     tocar el lock, dejando que `KeyItem.useOn` haga el toggle clásico.
3. **Ganzúa** (`stack.is(LOCK_PICKS)`): en cliente devuelve `CONSUME`; en servidor
   abre `LockPickingContainer`.
4. **Sin llave ni ganzúa**: dispara la animación (`lkb.swing(20)`), suena
   `LOCK_RATTLE`, y devuelve `FAIL`. Si `LocksClientConfig.DEAF_MODE`, muestra
   `locks.status.locked` en la action bar.

Además: si `ALLOW_REMOVING_LOCKS` y el jugador hace **shift + clic con mano
vacía** sobre un candado **destrabado**, lo dropea como item (`ItemEntity`) y lo
remueve vía `handler.remove`.

> **Agujero negro (bug histórico, ya resuelto):** en 1.21.1, el `useOn` del item
> corre recién si el `useWithoutItem` del bloque no consumió. Para cofres, el
> bloque consume y abre el menú, así que `KeyItem.useOn` **nunca** se ejecutaba y
> el cofre abría con el lock todavía trabado. Como `BaseContainerBlockEntityMixin`
> devuelve `EMPTY` en `getItem` y descarta escrituras cuando el lock está trabado,
> el interior se veía vacío y los items desaparecían. La solución es desbloquear
> el lock **en el evento** (que corre antes de armar el menú) y re-trabarlo al
> cerrar con `autoRelock` + `ContainerOpenersCounterMixin`.

### `onBlockBreak` — protección

`canBreakLockable` es `PROTECT_LOCKABLES && !creativo && lockedAndRelated`.
`lockedAndRelated` también considera el bloque **de arriba** si es una puerta,
para no romper la mitad superior de una puerta con candado en la inferior.

## Mixins — `src/main/java/melonslise/locks/mixin/`

### Contenedores

- **`BaseContainerBlockEntityMixin`** — mientras haya un lockable trabado que
  intersecte la posición del block entity: `getItem` y `removeItem` devuelven
  `ItemStack.EMPTY`, y `setItem` redirige `getItems()` a una lista descartable.
  Es lo que impide ver/extraer/insertar items con el candado trabado.
- **`ContainerOpenersCounterMixin`** — auto-relock. Sobre `decrementOpeners` y
  `recheckOpeners`: si `openCount == 0` y estamos en servidor, busca lockables
  intersectados con `autoRelock`, les limpia el flag y los vuelve a trabar.
  **Excepción:** si el contenedor (block entity `instanceof Container`) todavía
  tiene adentro una llave que abre ese candado (`MASTER_KEY`, llave del id, o
  llavero que la contenga), **no** lo traba. Así guardar la única llave dentro no
  deja al jugador afuera.

### Física / redstone / entorno

- **`WorldMixin`** — `hasNeighborSignal` devuelve `false` para bloques trabados
  (redstone no los acciona); y en `setBlock`, si `STRONG_PREVENTION`, cancela el
  cambio de bloques trabados (chequeo extra de protección, compatible con menos
  mods).
- **`DoorMixin`** — cancela `DoorBlock.setOpen` en puertas trabadas.
- **`HopperTileEntityMixin`** — la tolva no ve contenedores trabados
  (`getAttachedContainer` → `null`).
- **`PistonBlockMixin`** / **`PistonBlockStructureHelperMixin`** — un pistón no
  puede empujar ni resolver una estructura con un bloque trabado (ni relacionados).
- **`ExplosionContextMixin`** — quita de la lista de bloques a destruir los
  trabados (y relacionados).
- **`ServerWorldMixin.sendBlockUpdated`** — si el bloque cambió (no sólo estado),
  dropea el staff-item del lockable y lo remueve del handler.
- **`ChestBlockMixin`** — no permite fusionar un cofre con otro trabado
  (`candidatePartnerFacing`).

### Jugador y worldgen/estructuras

- **`PlayerTickMixin`** — si el jugador no tiene ningún `LOCKS` en las manos,
  limpia el attachment `SELECTION` (cancela la selección a medio hacer).
- **`ChunkPrimerMixin`** (`ChunkAccess implements ILockableProvider`) — agrega
  una `List<Lockable>` al chunk para que el worldgen deposite candados durante la
  generación, antes de que el `LevelChunk` exista.
- **`WorldGenRegionMixin`** — en cada `setBlock` durante el worldgen llama a
  `LocksUtil.lockChunk(...)`, que con probabilidad `Generation Chance` genera un
  candado sobre cofres/barricas/etc. (según las regex de config).
- **`ChunkMixin`** (`LevelChunk` constructor) — toma los lockables del
  `ProtoChunk`, los mete en storage+handler, avisa al cliente, y **reconstruye**
  el índice del chunk desde `handler.getLoaded()` (persistencia; ver doc 02).
- **`ChunkManagerMixin`** (`ChunkMap.onChunkReadyToSend`) — envía
  `AddLockableToChunkPacket` a los jugadores que empiezan a ver el chunk.
- **`StructureTemplateMixin`** — al copiar/pegar estructuras (`fillFromWorld` /
  `placeInWorld`) guarda y recrea los lockables incluidos (con `LockableInfo`),
  rotando la `Transform` y respetando `Randomize Loaded Locks`.
- **`StructurePieceMixin`** — todo el cuerpo comentado (histórico; el camino
  activo es `WorldGenRegionMixin`).

### Accesores

- **`accessor/LootTableAccessor`** y **`accessor/LootPoolAccessor`** — abren
  `LootTable.pools` / `LootPool.Builder.entries`. Se usan en
  `LocksUtil.mergeEntries`, que hoy **no se llama** (ver doc 08).

### Compatibilidad

- **`compact/LootrMixin`** (`@Pseudo`) — redirect no-op sobre
  `noobanudis.mods.lootr…BlockEntityTicker.onServerTick` para evitar crash de
  compatibilidad. No cambia comportamiento.

## Config que gobierna esto

- `LOCKABLE_BLOCKS` (regex) — qué bloques se pueden candar.
- `MAX_LOCKABLE_VOLUME` — tamaño máximo de una selección.
- `PROTECT_LOCKABLES` — inmune a romper en supervivencia.
- `STRONG_PREVENTION` — chequeo extra en `WorldMixin.setBlock`.
- `EASY_LOCK` — un clic vs. selección de dos esquinas.
- `ALLOW_REMOVING_LOCKS` — quitar candados abiertos con shift+mano vacía.
- `DEAF_MODE` (cliente) — feedback visual al toparse con un bloque trabado.

Detalle de valores en [08 — Config, mundo, loot y trades](08-config-mundo-loot-trades.md).
