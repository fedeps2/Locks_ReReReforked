# Locks — documentación del port no oficial a NeoForge 1.21.1

Locks es un mod que agrega **candados colocables** sobre casi cualquier bloque
(cofres, puertas, tolvas, barricas, cofres dobles, bloques de otros mods, etc.),
un **minijuego de ganzúa**, **llaves** con IDs únicos, **llaveros**, encantamientos
propios y generación de candados en estructuras/cofres del mundo.

- Mod id: `locks`
- Versión: `1.0-beta`
- Loader: NeoForge `21.1.x` sobre Minecraft `1.21.1`
- Dependencias: **Curios** (opcional, para llaveros), **Lootr** (opcional, mixin pseudo)
- Grupo: `melonslise.locks`

## Índice

| Documento | Contenido |
|-----------|-----------|
| [01 — Arquitectura](01-arquitectura.md) | Entrypoints, registros, paquetes, ciclo de vida, attachments |
| [02 — Candados y lockables](02-candados-y-lockables.md) | `Lock`, `Lockable`, `LockableHandler`, persistencia, auto-relock |
| [03 — Items y recetas](03-items-y-recetas.md) | Llaves, ganzúas, llaveros, recetas, encantamientos, tags |
| [04 — Interacción y protección](04-interaccion-y-proteccion.md) | Eventos y mixins que traban/protegen bloques |
| [05 — Lockpicking](05-lockpicking.md) | Minijuego de ganzúa: server, cliente, animaciones |
| [06 — Cliente: render y GUI](06-cliente-render-y-gui.md) | Render 3D de candados, HUD, selección, pantallas |
| [07 — Red](07-red.md) | Paquetes cliente/servidor y sus flujos |
| [08 — Config, mundo, loot y trades](08-config-mundo-loot-trades.md) | Configs, worldgen, estructuras, loot, aldeanos |
| [09 — Mapa de archivos](09-mapa-de-archivos.md) | Referencia archivo por archivo (qué hace cada clase) |
| [10 — Notas y limitaciones](10-notas-y-limitaciones.md) | FIXMEs, código muerto, gaps conocidos |

## Compilar

```bash
./gradlew build          # jar en build/libs/
./gradlew runClient      # cliente de desarrollo
./gradlew runServer      # servidor de desarrollo
```

Requiere JDK 21. Las verificación de compilación no se ejecutó al escribir esta
documentación: todo lo aquí descrito sale de leer el código y los recursos.

## Cómo leer el código

```
src/main/java/melonslise/locks/
├── Locks.java            # entrypoint común
├── LocksClient.java      # entrypoint cliente
├── common/               # lógica de juego (corre en ambos lados)
│   ├── components/       # Lockable, Handler, Storage, ItemHandler, Selection
│   ├── config/           # 3 configs (common/server/client)
│   ├── container/        # menús (lockpicking, llavero)
│   ├── event/            # eventos de Forge/NeoForge
│   ├── init/             # registros (items, menus, red, sonidos, etc.)
│   ├── item/             # items
│   ├── network/          # paquetes
│   ├── recipe/           # receta custom de llave
│   ├── util/             # Lock, Lockable, Cuboid6i, Transform, utilidades
│   └── worldgen/         # feature (registrada, no usada hoy)
├── client/               # render, GUI, sprites
└── mixin/                # patches a vanilla
```
