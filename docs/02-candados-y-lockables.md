# 02 — Candados y lockables

Esta es la capa más importante del mod. Un **lockable** es un candado colocado en
el mundo que abarca uno o más bloques; un **lock** es el estado lógico (id,
combinación de pines, trabado/destrabado) que comparte ese lockable.

## `Lock` — estado lógico

`common/util/Lock.java`

```java
public class Lock extends Observable {
    public record LockRecord(int id, List<Byte> combo, boolean locked) {}
    public LockRecord lockRecord;
    public final Random rng;   // sembrado con el id
}
```

- `id`: entero único. Se guarda en el item (`CustomData."Id"`) y en NBT.
- `combo`: lista de bytes. Es **el orden en que hay que tirar los pines**: el
  índice es la posición del pin en el lock y el valor es qué pin hay que elegir.
  Se genera con `shuffle(length)` usando el `Random` sembrado por `id`
  (`new Random(id)`), así que **la combinación es determinística para un id**.
- `locked`: si está trabado.
- Es `Observable`: al cambiar con `setLocked`, notifica a sus observers (el
  `Lockable` y el `LockableHandler`).

Conversión item ↔ lock:
- `Lock.gen(stack)` / `Lock.from(stack)`: leen el id y la longitud del item.
- `Lock.fromNbt` / `toNbt`: serialización clásica.
- `CODEC` / `STREAM_CODEC`: serialización Mojang (persistencia y red).

`LockRecord` es simplemente el record inmutable que se pasa por NBT/red para no
tener que serializar el `Random` ni el grafo de observers.

> TODO en el código: si una combinación se barajara fuera de la creación, al
> recargar se reconstruiría con la combinación inicial (sembrada por id).

## `Lockable` — el candado en el mundo

`common/util/Lockable.java`

```java
public class Lockable extends Observable implements Observer {
    public final Cuboid6i bb;      // región de bloques cubierta
    public final Lock lock;
    public final Transform tr;     // orientación (dirección + cara)
    public final ItemStack stack;  // item que representa el candado
    public final int id;
    public int oldSwingTicks, swingTicks, maxSwingTicks; // animación
    public boolean autoRelock;     // transitorio, ver auto-relock
    public Map<List<BlockState>, State> cache; // cálculo de pose
}
```

- Es **`Observer` del `Lock`**: cuando el lock cambia, `update()` marca
  `setChanged()/notifyObservers()` (para el handler) y actualiza
  `LockItem.setOpen(stack, !locked)`.
- `getLockState(Level)`: calcula dónde y cómo se dibuja el candado. Recorre los
  bloques cubiertos, une sus `VoxelShape` y elige el punto medio de la cara más
  cercana al centro del `bb`. Cachea por lista de `BlockState` (se invalida al
  cambiar los bloques).
- `Lockable.State`: pose (`pos`, `tr`, `bb`) con helpers cliente `inView` y
  `inRange`.
- `swing(ticks)` / `tick()`: animación del candado al intentar abrirlo.
- `LockableRecord`: versión inmutable para red/NBT (`toRecord()`).

Constructores: uno pide `Level` y asigna un id con `nextId()`; otro recibe el id
explícito (para deserializar).

## `Cuboid6i` — región de bloques

`common/util/Cuboid6i.java` es un cuboide entero normalizado (`x1<x2`, etc.).

- `volume()` — cantidad de bloques (se compara contra `MAX_LOCKABLE_VOLUME`).
- `intersects(pos/other)`, `getContainedPos()`, `intersection`, `offset`.
- `containedChunksTo(f, endEarly)` / `getContainedChunks(p)` — mapean la región
  a chunks (con manejo de bordes negativos y positivos por `>>4`).
- `center()`, `sideCenter(dir)`, `isLoaded(level)`.

## `Transform` — orientación

`common/util/Transform.java` es un enum de 12 valores: combinación de
`Direction` cardinal (N/S/E/W) y `AttachFace` (`CEILING`, `WALL`, `FLOOR`). Se
resuelve con `fromDirection`/`fromDirectionAndFace`, y `getCuboidFace()` devuelve
la cara sobre la que apoyar el candado.

## `LockableHandler` — registro autoritativo por nivel

`common/components/LockableHandler.java`

- Vive en el **nivel** (`Level`), uno por mundo/dimensión.
- `Int2ObjectMap<Lockable> lockables` — todos los lockables **cargados**.
- `AtomicInteger lastId` — contador de ids, persistido junto al handler.
- `nextId()` incrementa y devuelve.
- `getInChunk(pos)` — devuelve el `ILockableStorage` del chunk (índice).
- `add(lkb, level)`:
  - rechaza si `bb.volume() > MAX_LOCKABLE_VOLUME`;
  - agrega el lockable a todos los storages de chunk que intersecta, verificando
    que no se solape con otro (`allMatch`); con `endEarly`;
  - lo guarda en el mapa, se registra como observer;
  - en cliente dispara el swing; en servidor envía `AddLockablePacket` a todos.
- `remove(id)` — lo saca de los chunks y del mapa, desregistra observer y manda
  `RemoveLockablePacket`.
- `update(...)` (observer del lockable) — en servidor manda `UpdateLockablePacket`
  a todos los jugadores.
- Implementa `INBTSerializable<CompoundTag>`: `serializeNBT`/`deserializeNBT`
  escriben `last_id` + la lista `Lockables`.

**Es la única fuente de verdad persistida.** Ver más abajo.

## `LockableStorage` — índice por chunk

`common/components/LockableStorage.java` es un attachment del chunk con otro
`Int2ObjectMap<Lockable>` (subconjunto del handler que toca ese chunk). **No se
serializa**: se reconstruye al cargar el chunk (ver `ChunkMixin` en el doc 04).
Existe para consultas O(1) por posición sin recorrer todo el nivel.

## `LocksComponents` — registro y accesor

`common/init/LocksComponents.java` registra los attachments y el data component,
y ofrece `getLockableHandler(Level)`:

```java
private static final Map<Level, LockableHandler> lockableHandlers = new ConcurrentHashMap<>();

public static ILockableHandler getLockableHandler(Level world) {
    return lockableHandlers.computeIfAbsent(world, w ->
        w instanceof ServerLevel sl ? sl.getData(LOCKABLE_HANDLER.get())
                                    : new LockableHandler(w));
}
```

## Persistencia (diseño y por qué)

Diseño: **el handler del nivel es la única fuente de verdad**; el storage por
chunk es sólo un índice en memoria que se reconstruye.

- `LOCKABLE_HANDLER` se registra con `AttachmentType.serializable(...)`, así que
  se guarda en el `level.dat` de cada dimensión vía `INBTSerializable`.
- `LOCKABLE_STORAGE` se registra **sin** serializer, no se persiste.
- Al construirse un `LevelChunk` (`ChunkMixin.init`), se agrega lo que venga de
  worldgen (lista del `ProtoChunk`, vía `ILockableProvider`/`ChunkPrimerMixin`) y
  después se **reconstruye** el índice del chunk a partir de `handler.getLoaded()`,
  deduplicando por id y mandando `AddLockableToChunkPacket` al cliente.
- Al descargar un chunk en el cliente (`ChunkCacheMixin.drop`), los lockables se
  sacan del mapa cargado del cliente.

Esto evita el bug clásico de "resurrección": un lockable multi-chunk se habría
guardado en N chunks y podría reaparecer desde un chunk con datos viejos tras un
`remove`.

## Auto-relock

`Lockable.autoRelock` (transitorio, no se serializa) marca un candado que se
abrió **con la llave correcta sobre un contenedor** para poder abrirlo en el
mismo clic. `ContainerOpenersCounterMixin` lo vuelve a trabar cuando el
**último** jugador cierra el contenedor (`openCount == 0`). Cubre cofres,
cofres trampa, barricas y shulker boxes (todos usan `ContainerOpenersCounter`).

Dos matices:

- **No re-traba si la llave quedó adentro:** antes de trabar, revisa si el
  contenedor (`instanceof Container`) guarda una `MASTER_KEY`, una llave del id o
  un llavero que contenga esa llave. Si es así, lo deja destrabado para no
  encerrar al jugador afuera de su propio cofre.
- **Destrabado persistente:** un **shift + clic con llave** togglea el candado sin
  abrir el bloque y limpia `autoRelock`, dejándolo así hasta que alguien lo
  cambie. Es la vía explícita para "dejar el cofre abierto para otros".

Se detalla en [04 — Interacción y protección](04-interaccion-y-proteccion.md).
