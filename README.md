# Locks — port no oficial a NeoForge 1.21.1

Port del mod **Locks** de Melonslise (y colaboradores) a **NeoForge 1.21.1**.
Agrega candados colocables sobre casi cualquier bloque, llaves con ID único,
llaveros, minijuego de ganzúa, encantamientos y candados generados en estructuras.

- Mod id: `locks` · versión `1.0-beta`
- Minecraft `1.21.1` · NeoForge `21.1.x`
- Opcionales: **Curios** (llaveros equipables), **Lootr** (compatibilidad)
- Licencia: CC-BY-NC-3.0

## Documentación

Toda la documentación técnica está en [`docs/`](docs/README.md):

- [Arquitectura y entrypoints](docs/01-arquitectura.md)
- [Candados y lockables](docs/02-candados-y-lockables.md)
- [Items y recetas](docs/03-items-y-recetas.md)
- [Interacción y protección](docs/04-interaccion-y-proteccion.md)
- [Lockpicking](docs/05-lockpicking.md)
- [Cliente: render y GUI](docs/06-cliente-render-y-gui.md)
- [Red](docs/07-red.md)
- [Config, mundo, loot y trades](docs/08-config-mundo-loot-trades.md)
- [Mapa de archivos](docs/09-mapa-de-archivos.md)
- [Notas y limitaciones](docs/10-notas-y-limitaciones.md)

## Compilar

```bash
./gradlew build     # jar en build/libs/
```

Requiere JDK 21. Créditos originales en `gradle.properties` (`mod_author`,
`credits`).
