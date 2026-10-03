# 03 — Items, recetas y encantamientos

## Jerarquía de items

```
Item
├── LockingItem          (base con id único)
│   ├── LockItem         (el candado)
│   └── KeyItem          (la llave)
├── MasterKeyItem        (abre cualquier candado)
├── KeyRingItem          (llavero, contenedor + Curio)
└── LockPickItem         (ganzúa, abre el minijuego)
```

### `LockingItem` — base con id

`common/item/LockingItem.java`

- `stacksTo(1)`.
- El **id único** se guarda en `DataComponents.CUSTOM_DATA` bajo `"Id"` como un
  `int` aleatorio (`ThreadLocalRandom.nextInt()`).
- `getOrSetId(stack)`: lo crea si no existe; `getId(stack)` (`@Nullable`);
  `copyId(from, to)`: copia el id (lo usa la receta de llaves).
- `inventoryTick`: en servidor garantiza que el id exista.
- Hover text: muestra `ID: <n>` en verde oscuro.

### `LockItem` — el candado

`common/item/LockItem.java`

- Campos: `length` (cantidad de pines), `enchantmentValue`, `resistance`
  (cuánta "fuerza" hace falta para reventarlo; hoy informativo).
- Estado en el item:
  - `"Length"` (longitud, en `CUSTOM_DATA`).
  - `"Open"` — se setea desde `Lockable.update()` con
    `LockItem.setOpen(stack, !locked)`. Sirve para elegir el modelo/textura
    "abierto". `isOpen`/`setOpen` (ojo: `setOpen` muta el tag con `getUnsafe()`).
- `useOn` decide cómo colocar el candado según `LocksServerConfig.EASY_LOCK`:
  - `easyLock`: un solo clic. Detecta **cofres dobles** (`CHEST_TYPE`, busca el
    bloque conectado) y **puertas** (`DOUBLE_BLOCK_HALF` + `DOOR_HINGE`) y arma
    el `Cuboid6i` de los dos bloques automáticamente.
  - `freeLock`: modo selección. El primer clic guarda la posición en el
    attachment `SELECTION` del jugador; el segundo cierra la región
    (`Cuboid6i(pos1, pos)`), coloca el candado y limpia la selección.
  - En ambos, si no es creativo, descuenta el item y avisa si
    `handler.add` falla (volumen/colisión) devolviendo `PASS`.
- `use` (clic en el aire) con el candado abierto: lo "cierra" y suena
  `PIN_MATCH` (detalle cosmético/de estado).
- Es encantable.

### `KeyItem` — la llave

`common/item/KeyItem.java` (hereda de `LockingItem`)

- `useOn`: busca los lockables que intersectan el bloque cuyo `lock.id` coincide
  con el `Id` de la llave, suena `LOCK_OPEN` y **togglea** `lock.setLocked`.
- Hover text: `Press G to Rename` (más el ID heredado).

### `MasterKeyItem` — la maestra

`common/item/MasterKeyItem.java`

- `useOn`: togglea el estado de **todos** los lockables que intersectan el bloque,
  sin comparar id.
- No está en el tag `keys` (por eso el flujo de `handleRightClick` la trata con
  una rama especial, ver doc 04).

### `KeyRingItem` — el llavero

`common/item/KeyRingItem.java`

- `implements ICurioItem`; `rows` define la capacidad (`getSlotCount() = rows*9`).
- Se guarda en el item un `IItemHandler` (data component `ITEM_HANDLER`) con los
  slots internos. `getOrCreateHandler` lo crea del tamaño correcto si falta.
- `canEquip`: sólo si el slot de Curios es `ring`.
- `containsId(stack, id)`: chequea si contiene una llave con ese id.
- `use`: abre `KeyRingContainer` (el menú) en el servidor.
- `useOn`: recorre los slots buscando una llave que matchee algún lockable
  intersectado; si la encuentra, togglea y suena.
- `LocksItemModelsProperties` usa la property `locks:keys` para cambiar el modelo
  según cuántas llaves tiene.

### `LockPickItem` — la ganzúa

`common/item/LockPickItem.java`

- `strength` (0.0–1.0) determina si puede abrir un candado con encantamiento
  `complexity`: `canPick(stack, cmp) = strength > cmp * 0.25f`.
- `getOrSetStrength` guarda la fuerza en `CUSTOM_DATA` (por si se modificara).
- `useOn`: si el candado está trabado y la ganzúa alcanza, abre el menú de
  lockpicking; si no alcanza muestra `locks.status.too_complex` (action bar) y
  devuelve `PASS`.

## Registro y valores

`common/init/LocksItems.java`

| Item | Clase | Parámetros |
|------|-------|------------|
| `spring`, `*_lock_mechanism`, `key_blank` | `Item` | materiales |
| `wood_lock` | `LockItem` | length 5, ench 15, resist 4 |
| `iron_lock` | `LockItem` | 7, 14, 12 |
| `steel_lock` | `LockItem` | 9, 12, 20 |
| `gold_lock` | `LockItem` | 6, 22, 6 |
| `diamond_lock` | `LockItem` | 11, 10, 100 |
| `key` | `KeyItem` | — |
| `master_key` | `MasterKeyItem` | — |
| `key_ring` | `KeyRingItem(1)` | 9 slots |
| `key_ring_large` | `KeyRingItem(2)` | 18 slots |
| `wood_lock_pick` | `LockPickItem` | 0.20 |
| `iron_lock_pick` | `LockPickItem` | 0.35 |
| `gold_lock_pick` | `LockPickItem` | 0.25 |
| `steel_lock_pick` | `LockPickItem` | 0.70 |
| `diamond_lock_pick` | `LockPickItem` | 0.85 |

La pestaña creativa `main` agrega todos los items registrados.

## Recetas

- **`KeyRecipe`** (`common/recipe/KeyRecipe.java`, serializador
  `locks:crafting_key`): receta custom sin forma. Requiere **un** item lockable
  (candado o llave, cualquiera con `"Id"`) más uno o más `key_blank`; produce
  tantas `key` como blanks, copiando el id (`LockingItem.copyId`). El item
  lockable no se consume (`getRemainingItems` lo devuelve). Necesita mesa 3×3.
- **Recetas JSON** en `data/locks/recipe/`: locks (shaped con mecanismo), pick
  con palo/material, mechanism, spring, `key_blank`, `key_ring` (8 nuggets de
  hierro), `key_ring_large`, y `key.json` (referencia a `locks:crafting_key`).
- `data/locks/c/tags/item/{ingots,nuggets}/steel.json` — acero común.

## Encantamientos

`common/init/LocksEnchantments.java` — sólo aplicables a los items de
`key_blank` + los 5 candados. En 1.21.1 el registro de encantamientos es un
**datapack registry**, así que **no** se crean con `DeferredRegister`: se definen
en `data/locks/enchantment/{shocking,sturdy,complexity}.json` y `LocksEnchantments`
sólo guarda los `ResourceKey` y resuelve los `Holder` desde el registry access del
nivel (`getEnchantmentHolders` / `getEnchantmentLevel`). Por eso aparecen en
`/enchant`.

| Encantamiento | Máx | Efecto en el juego |
|---------------|-----|--------------------|
| `locks:shocking` | 5 | Al fallar un pin, electrocuta al jugador (daño `locks:shock`, `1.5 * nivel`). |
| `locks:sturdy` | 3 | Endurece el candado: sube la chance de **romper** la ganzúa. |
| `locks:complexity` | 3 | Exige ganzúas más fuertes (`strength > nivel*0.25`). |

El tipo de daño `locks:shock` está en `data/locks/damage_type/shock.json`, con
traducciones `death.attack.locks.shock[.player]`.

## Tags de item

`data/locks/tags/item/`:

- `keys.json` → `locks:key`.
- `locks.json` → los 5 candados.
- `lock_picks.json` → las 5 ganzúas.

`LocksItemTags` expone `KEYS`, `LOCKS`, `LOCK_PICKS`. Se usan para decidir
interacciones (`stack.is(...)`), no para el modelo.

## Integración con Curios

- `common/init/LocksComponents` (en `Locks`) registra la capability de item
  `CuriosCapability.ITEM` para los dos llaveros, devolviendo el item como
  `ICurio`.
- `data/curios/tags/item/ring.json` incluye ambos llaveros.
- `data/locks/curios/slots/ring.json` define `size: 2`.
- `LocksEvents` y `RenameKeyPacket` buscan llaveros equipados en Curios para
  abrir candados y renombrar llaves sin sacarlos.
