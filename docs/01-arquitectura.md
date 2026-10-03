# 01 — Arquitectura

## Entrypoints

### `Locks` (lado común)

`melonslise/locks/Locks.java` es el punto de entrada `@Mod(Locks.ID)`. En su
constructor hace, en este orden:

1. Registra los `DeferredRegister`:
   - `LocksComponents` — attachments de nivel/chunk y data components de item.
   - `LocksItems` — items y la pestaña creativa.
   - `LocksSoundEvents` — sonidos.
   - `LocksContainerTypes` — menús (`lock_picking`, `key_ring`).
   - `LocksRecipeSerializers` — receta custom `crafting_key`.
   - `LocksFeatures` — feature de worldgen `locks:locks`.
   - `LocksEnchantments` — `shocking`, `sturdy`, `complexity`.
   - `LocksEvents` — listeners del bus de juego (`NeoForge.EVENT_BUS`).
   - `LocksNetwork` — registro de payloads.
2. Registra las configs: `COMMON` (`LocksConfig`) y `SERVER` (`LocksServerConfig`).
3. Registra la capability de **Curios** para los llaveros (`KEY_RING`,
   `KEY_RING_LARGE`): expone el propio item como `ICurio`, de modo que se puedan
   equipar en el slot `ring`.

### `LocksClient` (lado cliente, `Dist.CLIENT`)

`melonslise/locks/LocksClient.java`:

- Registra la config `CLIENT` (`LocksClientConfig`).
- `LocksNetworkClient.register` (hoy vacío; la red se registra en el lado común).
- En `FMLClientSetupEvent` (con `enqueueWork`):
  - `LocksScreens.register()` → asocia menús con pantallas.
  - `LocksItemModelsProperties.register()` → item model properties.
- Agrega `LocksClient::onScreenKeyPressed` al bus de juego (tecla `G` para
  renombrar llaves).

## Convención de paquetes

| Paquete | Rol |
|---------|-----|
| `melonslise.locks` | Entrypoints (`Locks`, `LocksClient`) |
| `common.components` | Estado persistente: `Lockable`, `LockableHandler`, `LockableStorage`, `ItemHandler`, `Selection` + interfaces |
| `common.components.interfaces` | Contratos (`ILockableHandler`, `ILockableStorage`, `IItemHandler`, `ISelection`) |
| `common.config` | Las tres `ModConfigSpec` |
| `common.container` | `AbstractContainerMenu` de lockpicking y llavero |
| `common.event` | Listeners de eventos |
| `common.init` | `DeferredRegister` y constantes de registro |
| `common.item` | Clases de item |
| `common.network.toclient` / `toserver` | Payloads |
| `common.recipe` | `KeyRecipe` |
| `common.util` | `Lock`, `Lockable`, `Cuboid6i`, `Transform`, `LocksUtil`, predicados |
| `common.worldgen.feature` | `LocksFeature` (ver doc 08, hoy sin uso real) |
| `client.gui` | Pantallas (`LockPickingScreen`, `KeyRingScreen`, `RenameKeyScreen`) |
| `client.gui.sprite` | Sistema de sprites y acciones para el minijuego |
| `client.init` | Registro cliente (screens, item properties, render types) |
| `client.util` | Render y utilidades 3D→2D |
| `mixin` | Mezclas sobre vanilla/NeoForge; `accessor`, `addlock`, `client`, `compact` |

## Modelo de datos (attachments y components)

Todo el estado del mod vive en **attachments de NeoForge** y **data components**,
registrados en `LocksComponents`:

| Tipo | Registro | Ámbito | ¿Persistido? |
|------|----------|--------|--------------|
| `LOCKABLE_HANDLER` | `AttachmentType` serializable | `Level` | **Sí** (`level.dat`, vía `INBTSerializable`) |
| `LOCKABLE_STORAGE` | `AttachmentType` | `ChunkAccess` | **No** (índice en memoria, reconstruido) |
| `SELECTION` | `AttachmentType` con `Selection.CODEC`, `copyOnDeath()` | `Player` | Sí, muere con el jugador |
| `ITEM_HANDLER` | `DataComponentType<ItemHandler>` | `ItemStack` | **Sí**, persistente + sincronizado por red |

`LocksComponents.getLockableHandler(Level)` es el accesor central:
- Mantiene un `ConcurrentHashMap<Level, LockableHandler>` para lookups rápidos.
- Para un `ServerLevel` devuelve el attachment de nivel (única fuente de verdad).
- Para el cliente crea un `LockableHandler` efímero por instancia de `Level`.

Ver [02 — Candados y lockables](02-candados-y-lockables.md) para el detalle de
persistencia y del índice por chunk.

## Bus de eventos

- **Bus de mod** (`IEventBus`): registros y `RegisterCapabilitiesEvent`,
  `RegisterPayloadHandlersEvent`.
- **Bus de juego** (`NeoForge.EVENT_BUS`): `LocksEvents` (click derecho, romper
  bloque, trades), `LocksClient` (tecla G).

## Dependencias externas

- **NeoForge** — requerido (`[[dependencies.locks]] modId="neoforge"`).
- **Curios** — opcional, `ordering="AFTER"`. `compileOnly` + `localRuntime` en
  Gradle. Se usa para llaveros equipables y para la capability de item. Si no
  está presente, el mod igual compila, pero las clases que lo referencian
  (`KeyRingItem implements ICurioItem`, `LocksEvents`, `RenameKeyPacket`) se
  cargarían solo si el mod está; el `mods.toml` lo marca opcional.
- **Lootr** — opcional, mixin `@Pseudo` en `locks.mixins.compact.json`
  (compatibilidad, hoy un redirect no-op).

## Recursos

- `assets/locks/lang/{en_us,zh_cn}.json` — traducciones.
- `assets/locks/models/item/*.json` — modelos; algunos usan las properties
  `locks:keys` (llavero) y `locks:open` (candado abierto).
- `assets/locks/textures/{item,gui,slot}/…` — texturas.
- `assets/locks/sounds*.ogg` + `sounds.json`.
- `data/locks/…` — recetas, tags, tipos de daño, worldgen y loot (ver doc 08).
- `META-INF/accesstransformer.cfg` — abre acceso a miembros vanilla usados por
  mixins y registro (por ejemplo `MenuScreens.register`, `Materials`, etc.).
- `locks.mixins.json` y `locks.mixins.compact.json`.
