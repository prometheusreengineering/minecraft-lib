# minecraft-lib
Shared code for the Prometheus Minecraft projects: the [loader](https://github.com/prometheusreengineering/prometheus-minecraft) and its patches (Lunar, Essential, Dawn).

## How it reaches the game
There is exactly one copy of this lib in a game. The loader's **core** includes it, with its package unchanged, and
puts it on the game's class path before it loads any patch. The core updates itself from GitHub releases, and the lib
updates with it.

- **Patches** depend on it as `provided`. They never bundle or rename it.
- **Minecraft supplies Gson and Log4j.** They're `provided` here too, and compiled against the oldest versions any
  project runs on: Gson 2.2.4 and Log4j 2.0-beta9 (Minecraft 1.8.9). That way a call can't compile and then fail with
  `NoSuchMethodError` on an old version. There's no Guava.
- **Java 8.** It runs on everything from Forge 1.8.9 (Java 8) to Minecraft 26.x (Java 25).
- **Only additions.** Patches are released separately from the core and run against whatever core is newest, so
  public API is never changed or removed. New behavior goes in new methods.

## Contents
| Package | Class | What |
|---|---|---|
| `log` | `Log` | A logger that behaves the same on every platform: a file per project, plus the game's console |
| `patch` | `PrometheusPatch`, `PatchContext` | How the core calls into a patch |
| `util` | `Settings` | User overrides: `-Dprometheus.<key>` or `PROMETHEUS_<KEY>` |
| | `OS` | The current OS and the prometheus folder, where everything is stored |
| | `Http` | GET and verified, atomic downloads, with timeouts |
| | `Hashing` | SHA-256 and SHA-1, with GitHub's `sha256:` digests |
| | `AtomicFiles` | Writes that never leave a half-written file |
| | `PathUtil` | Home and env paths, recursive copy and delete |
| | `GsonUtil` | Pretty JSON with Minecraft's Gson |
| | `Env` | Sets environment variables of the running JVM |

## Usage

### Logging
```java
private static final Log log = Log.get("essential", "ECM");   // Prometheus/essential/ECM

log.info("Merged %d cosmetics (%d new)", total, added);        // String.format
log.error("Failed to save cosmetics list", e);
log.warn("failed %s", id, e);                                  // a trailing Throwable is the exception
log.debug("only with -Dprometheus.essential.debug=true");
```

- **The file:** every line goes to `<prometheus folder>/logs/<project>.log`. The previous run moves to
  `<project>.1.log`.
- **The console:** lines also go to the game's Log4j, or to stdout or stderr. Choose with `-Dprometheus.log.console`
  (`log4j|stdout|stderr|off`) or `Log.setDefaultConsole(...)`.

### Settings and folders
```java
String port = Settings.get("dawn.port", "8080");               // -Dprometheus.dawn.port, then PROMETHEUS_DAWN_PORT
boolean offline = Settings.getBoolean("offline", false);
Path folder = OS.getConfigFolder();                            // %APPDATA%/.minecraft/prometheus on Windows
```

The prometheus folder is `%APPDATA%/.minecraft/prometheus` on Windows, `~/Library/Application Support/prometheus` on
macOS, and `$XDG_DATA_HOME/prometheus` (or `~/.local/share/prometheus`) on Linux. `-Dprometheus.folder` overrides it.

### Downloads
```java
// throws on a status of 400 or above, a wrong SHA-256, or a download cut short
Http.download(url, folder.resolve("cache/thing.jar"), "sha256:<hex>");
String json = Http.getString(url, Collections.singletonMap("Accept", "application/json"));
AtomicFiles.writeString(folder.resolve("state.json"), json);
```

### Patches
A patch implements `PrometheusPatch` and lists it in
`META-INF/services/ing.prometheusreengineer.minecraft.patch.PrometheusPatch`:

```java
public final class EssentialPatch implements PrometheusPatch {
    @Override
    public void init(@NotNull PatchContext context) throws Exception {
        context.log().info("on %s, core %s", context.platform(), context.coreVersion());
        Path data = context.folder();                          // <prometheus folder>/essential
    }
}
```

The core will call `init` once, after adding the patch jar and its mixin configs. If `init` throws, only that patch is
disabled. The loader doesn't call `init` yet: patches still start from their own mod metadata.

## Building
Requires Maven and a JDK 8+. If `mvn` isn't on your PATH, IntelliJ's bundled Maven works.

```
mvn test       # JUnit 5, no network needed
mvn install    # into ~/.m2, where the loader and the patches find it
```

The loader's core build uses the copy in `~/.m2`, so run `mvn install` here before building the loader. A Gradle
project (Dawn) needs `mavenLocal()` in its repositories, and the dependency as `compileOnly`.
