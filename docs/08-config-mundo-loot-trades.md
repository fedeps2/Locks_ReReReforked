# 08 — Config, mundo, loot y trades

## Configs

Tres specs `ModConfigSpec`, registradas en los entrypoints.

### `LocksConfig` — `COMMON`

`common/config/LocksConfig.java`

| Opción | Default | Qué hace |
|--------|---------|----------|
| `Generation Chance` | 0.85 | Probabilidad de generar un candado en un cofre al generar el mundo (0 = off). |
| `Generation Enchant Chance` | 0.4 | Probabilidad de encantar ese candado generado. |
| `Lockable Generated Blocks` | `minecraft:chest`, `minecraft:barrel`, `lootr:.*`, `quark:.*_chest` | Regex de bloques lockeables en worldgen. |
| `Generated Locks` | `locks:wood_lock …diamond_lock` | Items que pueden salir como candado (deben ser `LockItem`). |
| `Generated Lock Chances` | `5, 4, 2, 3, 1` | Pesos relativos (misma cantidad que `Generated Locks`). |
| `Randomize Loaded Locks` | true | Al cargar una estructura, re-randomiza id y combinación (si no, usa la del template). |

`init()` (invocado perezosamente) compila las regex y arma
`weightedGeneratedLocks` (un `NavigableMap` acumulado por peso) y `weightTotal`.
`getRandomLock(rng, registries)` elige por peso y, con `canEnchant`, encanta el
candado con `shocking`/`complexity`/`sturdy`. Los `Holder<Enchantment>` se
resuelven desde `registries.lookupOrThrow(Registries.ENCHANTMENT)` (no desde un
`DeferredRegister`, porque `ENCHANTMENT` es un datapack registry y se define en
`data/locks/enchantment/*.json`).

### `LocksServerConfig` — `SERVER`

`common/config/LocksServerConfig.java`

| Opción | Default | Qué hace |
|--------|---------|----------|
| `Max Lockable Volume` | 6 | Máximo de bloques por selección. |
| `Lockable Blocks` | `.*chest`, `.*barrel`, `.*hopper`, `.*door`, `.*trapdoor`, `.*fence_gate`, `.*shulker_box` | Regex de bloques lockeables. |
| `Allow Removing Locks` | true | Shift + mano vacía quita candados abiertos. |
| `Protect Lockables` | true | Los bloques trabados no se rompen en supervivencia. |
| `Easy Lock` | true | Un clic vs. selección. |
| `Stronger Prevention` | false | Chequeo extra en `Level.setBlock` (`WorldMixin.strongCheck`). |

`canLock(level, pos)` compila las regex en `lockableBlocks` perezosamente y
responde si el bloque matchea.

### `LocksClientConfig` — `CLIENT`

`common/config/LocksClientConfig.java`

| Opción | Default | Qué hace |
|--------|---------|----------|
| `Deaf Mode` | true | Feedback visual (`locks.status.locked`) al interactuar con un bloque trabado. |
| `Show Overlay` | true | Mostrar el tooltip del candado al sostener una ganzúa. |

> `Show Overlay` está definido pero el código que lo consulta no aparece hoy:
> `GuiMixin` dibuja el tooltip sin chequear esta config. Queda como pendiente.

## Worldgen

### Camino activo: `WorldGenRegionMixin`

`WorldGenRegion` envuelve la generación de terreno. El mixin inyecta al final de
`setBlock` y llama a `LocksUtil.lockChunk(levelAccessor, level, pos, randomSource, chunk)`:

1. `lockWhenGen` verifica `LocksConfig.canGen` (chance + regex).
2. Deduce dirección, maneja cofres dobles (sólo se genera en el lado izquierdo)
   y puertas (sólo en la mitad superior; calcula el bloque par).
3. Elige un candado (`LocksConfig.getRandomLock`) y crea `Lockable` + `Lock.gen`.
4. `lockChunk` agrega el lockable a la lista del `ChunkAccess`
   (`ILockableProvider` → `ChunkPrimerMixin`), que luego consume `ChunkMixin`.

### `LocksFeature`

`common/worldgen/feature/LocksFeature.java` está registrado como `locks:locks`
y hay `configured_feature/locks_block.json` + `placed_feature/locks_block.json`,
pero:

- `place()` devuelve `true` y **todo el cuerpo está comentado**.
- No hay `biome_modifier` que referencie el placed feature.

Es decir, la feature **no se usa hoy**. La generación real es la del
`WorldGenRegionMixin`. Los JSON quedan como scaffolding.

### Estructuras

`StructureTemplateMixin` (ver doc 04) guarda los lockables al copiar una
estructura (`fillFromWorld`) y los recrea al colocarla (`placeInWorld`),
respetando rotación/mirror y `Randomize Loaded Locks`. `LockableInfo` es el DTO
para eso.

## Loot

Estado actual: **la inyección de loot no está implementada**.

- Existen tablas en `data/locks/loot_table/inject/chests/*.json` (simple_dungeon,
  stronghold, desert_pyramid, etc.) con ganzúas y mecanismos. En 1.21 ese path
  las registra como tablas con id `locks:inject/chests/...`, pero **nada las
  referencia**.
- `LocksUtil.mergeEntries` (que fusiona pools usando los accesores) existe pero
  **no se llama desde ningún lado**.
- `LocksEvents.onLootTableLoad` sólo chequea que el path empiece con `chests` y
  no hace nada más: es un no-op.

Conclusión: los candados/items de loot en cofres **no se inyectan** en la
generación actual. Las tablas quedan listas para cuando se cablee el
`LootTableLoadEvent`/`LootModifier`.

## Villager trades

`common/init/LocksVillagerTrades.java`, registrado por `LocksEvents`:

- **Toolsmith**: vende ganzúas y mecanismos por esmeraldas en los niveles 1–5
  (madera → hierro → oro → acero → diamante).
- **Wandering trader**: desde hierro hasta diamante, incluyendo `steel_lock` y
  `diamond_lock` encantados.

## Daño

`data/locks/damage_type/shock.json` define `locks:shock`
(`scaling: when_caused_by_living_non_player`, `exhaustion: 0.1`). Lo aplica el
lockpicking con el encantamiento `shocking`.

## Sonidos

`LocksSoundEvents` registra `key_ring`, `lock.close`, `lock.open`,
`lock.rattle`, `pin.fail`, `pin.match`, `shock`; `assets/locks/sounds.json` los
mapea a los `.ogg` y define subtítulos (`locks.subtitle.*`).

## Idiomas

`assets/locks/lang/en_us.json` y `zh_cn.json`: nombres de items, encantamientos,
death messages, títulos de GUI, tooltips (`locks.tooltip.*`) y estados
(`locks.status.*`).
