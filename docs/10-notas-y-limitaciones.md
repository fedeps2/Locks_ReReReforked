# 10 — Notas, deuda técnica y limitaciones

Este documento lista lo que el código **todavía no hace**, lo que está a medias y
los atajos conocidos, para no confundir "existe en el repo" con "funciona".

## Funcionalidad no cableada

| Tema | Estado | Detalle |
|------|--------|---------|
| **Inyección de loot** | ❌ No implementada | `data/locks/loot_table/inject/chests/*.json` existe pero nada las referencia. `LocksUtil.mergeEntries` no se llama; `LocksEvents.onLootTableLoad` es un no-op. |
| **Feature de worldgen `locks:locks`** | ⚠️ Registrada, sin uso | `LocksFeature.place()` devuelve `true` con el cuerpo comentado. No hay `biome_modifier` que use el placed feature. Los candados de mundo se generan por `WorldGenRegionMixin`, no por la feature. |
| **`EnumModifier`** | ❌ Muerto | Todo el cuerpo comentado. No se usa. |
| **`ClientNet` / `ServerNet`** | ❌ Muertos | `@Deprecated`, cuerpos vacíos. Residuo del port. |
| **`StructurePieceMixin`** | ❌ Comentado | Sus tres inyecciones están comentadas. El camino activo es `WorldGenRegionMixin`. |
| **`LocksClientConfig.OVERLAY`** | ⚠️ Definido, sin leer | `GuiMixin` dibuja el tooltip del candado sin consultar `Show Overlay`. |
| **`LocksUtil.resourceManager`** | ❌ Sin uso | Campo y código de parseo de loot tables comentados. |
| **`LootTableAccessor` / `LootPoolAccessor`** | ⚠️ Sin uso real | Sólo los usaría `mergeEntries`. |

## Bugs/quircks conocidos en el código

- **Combinación determinística por id** (`Lock`): la `combo` se genera sembrando
  `Random` con el id, así que un mismo id siempre da la misma combinación. Hay un
  `TODO` sobre qué pasa si se re-baraja una combinación ya cargada.
- **`LockItem.setOpen`** muta el `CustomData` con `getUnsafe().putBoolean(...)`:
  funciona pero es frágil y evita el chequeo de component.
- **`getOrSetLength`** arma un `CompoundTag` local y lee `Length` de ahí, **no**
  escribe el valor en el stack: si falta la longitud, siempre devuelve el `length`
  del item (no persiste). El hover text lee del tag, así que puede discrepar.
- **`freeLock`** tiene un `FIXME`: no repite los chequeos de `add` (volumen,
  colisión) que sí hace `LockItem.useOn`, así que una selección libre puede
  intentar colocar un candado que después se rechaza.
- **`tooltipLockable`** es un campo estático que se setea en el render y se
  limpia en el HUD: depende del orden de render/HUD de vanilla.
- **`LocksClient.onScreenKeyPressed`** obtiene `hoveredSlot` por reflexión
  (`setAccessible`), lo que puede romperse con cambios/obfusción de vanilla.
- **`LockPickingContainer.tryBreakPick`**: la fórmula
  `random < ex + ch` puede dar probabilidades altas de romper con ganzúas
  fuertes; está documentada pero conviene revisarla si se balancea.
- **`LocksRenderTypes.OVERLAY_LINES`** conserva depth test pese a
  `NO_DEPTH_TEST` (`FIXME` en el archivo).
- **Performance cliente**: los `FIXME` de `LocksClient`/`Sprite` marcan la
  creación de `Quaternionf` por frame y las rotaciones por candado; es deuda de
  optimización, no un bug funcional.

## Limitaciones de comportamiento

- **Auto-relock** sólo aplica a la ruta "clic con llave en mano sobre un
  `BaseContainerBlockEntity`". Candados abiertos con ganzúa o con llavero en
  Curios quedan abiertos. Contenedores lockeables sin `ContainerOpenersCounter`
  (hornos, tolvas, dispensadores, brewing stand) abiertos con llave quedan
  desbloqueados. Para destrabar/trabar a propósito sin abrir está el
  **shift + clic con llave** (ver doc 04).
- **Broadcast global**: `LockableHandler` manda los paquetes de alta/baja/update
  a **todos** los jugadores, sin tracking por dimensión. Con muchos candados y
  jugadores esto escala mal.
- **Persistencia = handler del nivel**: el `LOCKABLE_STORAGE` por chunk no se
  guarda; se reconstruye al cargar. Un cambio entre autosaves de `level.dat` se
  pierde ante un crash duro.
- **`add` durante worldgen** agrega a la lista del chunk pero la validación de
  solapamiento de `LockableHandler.add` no corre igual en ese camino; los candados
  generados no se pueden superponer en la práctica, pero el camino difiere.
- **Puertas de madera**: `KeyItem.useOn` no se ejecuta si el bloque consume el
  `useWithoutItem`, así que con llave la puerta abre pero el lock queda trabado
  (quirk pre-existente, fuera de alcance).

## Bugs ya resueltos (contexto histórico)

1. **Agujero negro al abrir cofres con llave**: el lock no se destrababa antes de
   abrir el menú, y `BaseContainerBlockEntityMixin` ocultaba/descartaba los items.
   Resuelto desbloqueando en `LocksEvents.handleRightClick` + `autoRelock`.
2. **Candados que desaparecían tras descargar chunk/reiniciar**: no había
   persistencia. Resuelto haciendo `LockableHandler` `INBTSerializable` como
   attachment del nivel y reconstruyendo el índice del chunk en `ChunkMixin`.
3. **Crash al crear mundo (`Registry not present` para `locks:shocking`)**:
   `LocksConfig.getRandomLock` pasaba los `DeferredHolder` de encantamientos
   (datapack registry) a `EnchantmentHelper.enchantItem`, que llama `holder.value()`
   y tiraba `IllegalStateException` durante `WorldGenRegionMixin`/estructuras.
   Resuelto resolviendo los `Holder<Enchantment>` desde
   `level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)`
   (`LocksEnchantments.getEnchantmentHolders`/`getEnchantmentLevel`) y usándolos
   también en `LockPickItem` y `LockPickingContainer`.
4. **Encantamientos ausentes del registro (`/enchant` no listaba `shocking`)**:
   `Registries.ENCHANTMENT` es datapack registry en 1.21.1, así que el
   `DeferredRegister` no los inyectaba. Resuelto pasándolos a JSON en
   `data/locks/enchantment/*.json` y eliminando el `DeferredRegister`.
5. **Llave encerrada / cofre imposible de dejar abierto**: el auto-relock trababa
   el contenedor al cerrarlo, incluso si la única llave había quedado adentro, y
   no había forma de dejarlo destrabado. Resuelto con el chequeo de llave dentro
   en `ContainerOpenersCounterMixin` y el toggle persistente con shift + clic en
   `LocksEvents`.
6. **`complexity` no se aplicaba**: `LocksEvents.handleRightClick` abría el menú
   de ganzúa sin mirar el encantamiento, y `LockPickItem.useOn` (que sí lo
   chequeaba) nunca corría para un lockable trabado. Resuelto moviendo el
   chequeo `canPick` al evento, con mensaje `too_complex`.

## Sugerencias de próximo paso

- Verificar en runtime que `locks:shocking`, `locks:sturdy` y `locks:complexity`
  cargan desde JSON y aparecen en el registro de encantamientos (`/enchant`).
- Cablear la inyección de loot (evento o `LootModifier`) y decidir si se usa
  `mergeEntries` o se migran las tablas a `GlobalLootModifier`.
- Decidir el destino de `LocksFeature`: implementarla o eliminarla junto con sus
  JSON para no confundir.
- Consultar `OVERLAY` en `GuiMixin`.
- Reducir allocations del render (reutilizar `Quaternionf`, cachear transforms).
- Escribir tests de `Cuboid6i` (bordes de chunk) y de `LockPickingContainer`
  (probabilidades) — son la lógica más delicada y hoy no tienen cobertura.
