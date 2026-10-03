# 09 — Mapa de archivos (qué hace cada clase)

Referencia clase por clase. Rutas relativas a
`src/main/java/melonslise/locks/` salvo aclaración.

## Raíz

| Archivo | Rol |
|---------|-----|
| `Locks.java` | Entrypoint `@Mod` común: registra todo, configs y capability de Curios. |
| `LocksClient.java` | Entrypoint `@Mod(dist = CLIENT)`: config cliente, screens, item properties, listener de teclado. |

## `common/util/` — modelo y utilidades

| Archivo | Rol |
|---------|-----|
| `Lock.java` | Estado lógico del candado (id, combinación, trabado). `Observable`. |
| `Lockable.java` | Candado colocado en el mundo (`bb`, `lock`, `tr`, `stack`, `id`), animación, `autoRelock`, cálculo de pose (`State`). |
| `Cuboid6i.java` | Cuboide entero: volumen, intersección, mapeo a chunks, serialización. |
| `Transform.java` | Enum de 12 orientaciones (dirección × `AttachFace`). |
| `LockableInfo.java` | DTO para guardar candados dentro de estructuras (`toNbt`/`fromNbt`). |
| `ILockableProvider.java` | Interfaz (`getLockables()`) implementada en `ChunkAccess` para el worldgen. |
| `LocksPredicates.java` | Predicados `LOCKED` / `NOT_LOCKED`. |
| `LocksUtil.java` | Utilidades: shuffle, chance, transform de estructuras, merge de loot (sin uso), `intersecting`, `locked`, `lockedAndRelated`, `lockWhenGen`, `lockChunk`. |
| `BiIntFunction.java` / `BiIntPredicate.java` | Interfaces funcionales para mapear chunks. |

## `common/components/` — estado persistente

| Archivo | Rol |
|---------|-----|
| `LockableHandler.java` | Registro autoritativo por nivel: mapa de lockables, ids, add/remove, NBT. |
| `LockableStorage.java` | Índice por chunk (no persistido). |
| `ItemHandler.java` | Inventario del llavero (`Container` + codec/stream codec). |
| `Selection.java` | Posición de la primera esquina de una selección (attachment del jugador). |
| `interfaces/ILockableHandler.java` | Contrato del handler. |
| `interfaces/ILockableStorage.java` | Contrato del storage por chunk. |
| `interfaces/IItemHandler.java` | Contrato del inventario interno. |
| `interfaces/ISelection.java` | Contrato de la selección. |

## `common/init/` — registros

| Archivo | Rol |
|---------|-----|
| `LocksComponents.java` | AttachmentTypes (`LOCKABLE_HANDLER/STORAGE`, `SELECTION`), data component `ITEM_HANDLER` y `getLockableHandler`. |
| `LocksItems.java` | Items y pestaña creativa. |
| `LocksItemTags.java` | Tags `keys`, `locks`, `lock_picks`. |
| `LocksSoundEvents.java` | Sonidos. |
| `LocksContainerTypes.java` | Menús `lock_picking` y `key_ring`. |
| `LocksNetwork.java` | Registro de payloads de red. |
| `LocksRecipeSerializers.java` | Serializer `crafting_key`. |
| `LocksEnchantments.java` | Encantamientos `shocking`, `sturdy`, `complexity`. |
| `LocksFeatures.java` | Registra la feature `locks:locks` (sin uso real). |
| `LocksDamageSources.java` | Tipo de daño `locks:shock`. |
| `LocksVillagerTrades.java` | Trades de toolsmith y wandering trader. |

## `common/item/`

| Archivo | Rol |
|---------|-----|
| `LockingItem.java` | Base: `stacksTo(1)` + id único en `CUSTOM_DATA`. |
| `LockItem.java` | El candado: `length`, encantabilidad, colocar (easy/free lock), estado `Open`. |
| `KeyItem.java` | Llave: togglea el candado con id coincidente. |
| `MasterKeyItem.java` | Togglea cualquier candado. |
| `KeyRingItem.java` | Llavero: contenedor `ITEM_HANDLER` + Curio. |
| `LockPickItem.java` | Ganzúa: fuerza y apertura del minijuego. |

## `common/container/`

| Archivo | Rol |
|---------|-----|
| `LockPickingContainer.java` | Menú del minijuego: estado, `tryPin`, rotura de ganzúa, shock, `removed`. |
| `KeyRingContainer.java` | Menú del llavero + slots especiales (`KeyRingSlot`, `StorageSlot`). |

## `common/event/`

| Archivo | Rol |
|---------|-----|
| `LocksEvents.java` | Click derecho (`handleRightClick`), protección de rotura, registro de trades. `onLootTableLoad` no-op. |

## `common/network/toclient/`

| Archivo | Rol |
|---------|-----|
| `AddLockablePacket.java` | Alta de un lockable en el cliente. |
| `AddLockableToChunkPacket.java` | Alta de un lockable en el índice de un chunk. |
| `RemoveLockablePacket.java` | Baja de un lockable. |
| `UpdateLockablePacket.java` | Cambio de estado trabado. |
| `TryPinResultPacket.java` | Resultado de un intento de pin. |
| `ClientNet.java` | Legacy vacío (`@Deprecated`). |

## `common/network/toserver/`

| Archivo | Rol |
|---------|-----|
| `TryPinPacket.java` | Intento de pin del cliente. |
| `RenameKeyPacket.java` | Renombrar llave (inventario / llavero / Curios). |
| `ServerNet.java` | Legacy vacío (`@Deprecated`). |

## `common/recipe/`

| Archivo | Rol |
|---------|-----|
| `KeyRecipe.java` | Receta custom: lockable + key_blank → llaves con el mismo id. |

## `common/config/`

| Archivo | Rol |
|---------|-----|
| `LocksConfig.java` | Config común: generación, weights, randomización. |
| `LocksServerConfig.java` | Config de servidor: bloques lockeables, volumen, protección, easy lock. |
| `LocksClientConfig.java` | Config cliente: deaf mode, overlay. |

## `common/worldgen/feature/`

| Archivo | Rol |
|---------|-----|
| `LocksFeature.java` | Feature registrada pero con `place()` vacío (scaffolding). |

## `common/addenchant/`

| Archivo | Rol |
|---------|-----|
| `EnumModifier.java` | Modificación reflectiva de enums (totalmente comentada; no usada). |

## `client/gui/`

| Archivo | Rol |
|---------|-----|
| `LockPickingScreen.java` | Pantalla del minijuego: sprites, input, animaciones. |
| `KeyRingScreen.java` | Pantalla del llavero. |
| `RenameKeyScreen.java` | Diálogo de renombrado de llave. |

## `client/gui/sprite/`

| Archivo | Rol |
|---------|-----|
| `Sprite.java` | Sprite animable (posición, rotación, alpha, cola de acciones). |
| `TextureInfo.java` | Región de textura y `draw`. |
| `SpringSprite.java` | Sprite del resorte que cambia de frame. |
| `action/IAction.java` | Interfaz de acción de sprite. |
| `action/SingleCallbackAction.java` | Acción con callback final. |
| `action/TimedAction.java` | Acción con duración en ticks. |
| `action/MoveAction.java` | Traslación lineal. |
| `action/AccelerateAction.java` | Traslación acelerada. |
| `action/FadeAction.java` | Cambio de alpha. |
| `action/WaitAction.java` | Espera. |

## `client/init/`

| Archivo | Rol |
|---------|-----|
| `LocksScreens.java` | Registra las pantallas de los menús. |
| `LocksItemModelsProperties.java` | Item properties `keys` y `open`. |
| `LocksRenderTypes.java` | `RenderType` de líneas para la selección. |
| `LocksNetworkClient.java` | Registro cliente (vacío). |

## `client/util/`

| Archivo | Rol |
|---------|-----|
| `LocksClient.java` | Render de candados, selección, tooltip HUD, tecla G. |
| `LocksClientUtil.java` | Cámara, frustum, `worldToScreen`, helpers de dibujo/matemática. |

## `mixin/`

| Archivo | Rol |
|---------|-----|
| `BaseContainerBlockEntityMixin.java` | Bloquea get/set/remove de items si el contenedor está trabado. |
| `ChestBlockMixin.java` | Impide fusionar cofres si uno está trabado. |
| `ChunkCacheMixin.java` | Limpia lockables del cliente al descargar chunk. |
| `ChunkManagerMixin.java` | Manda los lockables del chunk al jugador que entra a verlo. |
| `ChunkMixin.java` | Al crear `LevelChunk`: carga worldgen + reconstruye índice desde el handler. |
| `ChunkPrimerMixin.java` | Da a `ChunkAccess` una lista de lockables (`ILockableProvider`). |
| `ContainerOpenersCounterMixin.java` | Auto-relock al cerrar el último jugador. |
| `DoorMixin.java` | Cancela abrir puertas trabadas. |
| `ExplosionContextMixin.java` | Protege bloques trabados de explosiones. |
| `HopperTileEntityMixin.java` | Tolvas no ven contenedores trabados. |
| `PistonBlockMixin.java` | Pistones no empujan bloques trabados. |
| `PistonBlockStructureHelperMixin.java` | Resolver de pistón no mueve bloques trabados. |
| `PlayerTickMixin.java` | Limpia la selección si no hay candado en mano. |
| `ServerWorldMixin.java` | Dropea el candado si el bloque cambia. |
| `WorldMixin.java` | `hasNeighborSignal` falso en trabados + `strongCheck`. |
| `accessor/LootTableAccessor.java` | Acceso a `LootTable.pools`. |
| `accessor/LootPoolAccessor.java` | Acceso a `LootPool.Builder.entries`. |
| `addlock/StructureTemplateMixin.java` | Guarda/recrea candados en estructuras. |
| `addlock/StructurePieceMixin.java` | Histórico, todo comentado. |
| `addlock/WorldGenRegionMixin.java` | Genera candados durante el worldgen. |
| `compact/LootrMixin.java` | Compat opcional con Lootr (redirect no-op). |
| `client/ClientTickMixin.java` | Tick de animación de candados. |
| `client/GuiMixin.java` | Tooltip del candado en el HUD. |
| `client/WorldRendererMixin.java` | Render de candados en el mundo. |

## Recursos (`src/main/resources/`)

| Ruta | Rol |
|------|-----|
| `META-INF/neoforge.mods.toml` | Metadatos, deps (NeoForge, Curios, Lootr), mixins y AT. |
| `META-INF/accesstransformer.cfg` | Acceso a miembros vanilla. |
| `locks.mixins.json`, `locks.mixins.compact.json` | Listas de mixins. |
| `pack.mcmeta` | `pack_format` 15. |
| `assets/locks/lang/*.json` | Traducciones. |
| `assets/locks/models/item/*.json` | Modelos (incluye `open_*` y llaveros con overrides). |
| `assets/locks/textures/{item,gui,slot}/*` | Texturas. |
| `assets/locks/sounds.json`, `assets/locks/sounds/*.ogg` | Sonidos. |
| `data/locks/recipe/*.json` | Recetas. |
| `data/locks/tags/item/*.json` | Tags de items. |
| `data/locks/damage_type/shock.json` | Tipo de daño. |
| `data/locks/enchantment/{shocking,sturdy,complexity}.json` | Encantamientos (datapack registry). |
| `data/locks/worldgen/{configured,placed}_feature/locks_block.json` | Feature (sin uso). |
| `data/locks/loot_table/inject/chests/*.json` | Tablas de loot (sin cablear). |
| `data/locks/curios/slots/ring.json`, `data/curios/tags/item/ring.json` | Integración Curios. |
| `data/locks/c/tags/item/{ingots,nuggets}/steel.json` | Tags comunes de acero. |
