# Nubi (Forge 1.20.1) — "Pequeñas nubes, grandes aventuras"

## Compilar sin instalar nada (GitHub Actions)
Al subir el repo, GitHub compila solo el mod. Ve a la pestaña **Actions**, abre la última ejecución "Build" y descarga el `.jar` desde **Artifacts → nubi-mod**. Sube a CurseForge el `nubi-1.0.0.jar` (no el `-sources`).

## Compilar en tu PC
Necesitas JDK 17 y Gradle 8.1.1 (o copia `gradlew`, `gradlew.bat` y la carpeta `gradle` del MDK de Forge 1.20.1). Ejecuta `gradle build`; el mod queda en `build/libs/`.

## Probarlo en el juego
- `/give @s nubi:nubi_spawn_egg` o `/summon nubi:nubi`
- Tormenta: `/weather thunder` (las Nubi salvajes se ponen grises y atacan)
- Mundo Nubi: `/execute in nubi:mundo_nubi run tp @s 0 120 0`
- Flotar: mantén **G** con la armadura de nube completa (se cambia en Controles).
- Domar: clic derecho con un cubo de agua. Lazo: clic derecho con el lazo, luego clic derecho con la mano vacía para montar. Agachado + clic = quedarse quieta / seguir.
- Servidores dedicados: pon `allow-flight=true` en server.properties.

## Si el Mundo Nubi (dimensión) da error
Borra `src/main/resources/data/nubi/dimension` y `.../dimension_type`; el resto del mod funciona igual.
