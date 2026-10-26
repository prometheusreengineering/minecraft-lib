# minecraft-lib

Shared code for the four Prometheus Minecraft projects.

**Architecture (in progress, plan: `~/.claude/plans/okay-im-thinking-of-goofy-fern.md`):** the loader becomes the only
way in. Stage 0 and the core are built (`../prometheus-minecraft`, branch `stage0-core`, see its `CLAUDE.md`). The
patches don't use the lib or `PrometheusPatch` yet.

- **Stage 0:** a tiny jar the user installs. It has no dependencies and downloads the **core** from GitHub releases.
- **Core:** the loader logic plus this lib, with its package not renamed. It downloads the patches, adds them to the
  classpath and calls their `PrometheusPatch`.
- **Patches:** they have no entry points or mod metadata of their own, and they compile against this lib as `provided`.

So there is exactly one copy of this lib in a game, and it updates with the core.

| Project | Repo (sibling folder) | Build | Java | Runs on |
|---|---|---|---|---|
| Loader | `../prometheus-minecraft` | Maven, modules `stage0`, `stage0-legacy`, `core` | 8 (needs JDK 21+ to compile) | Fabric, Forge 1.8-1.12 (LaunchWrapper), Forge 1.15+ (ModLauncher), NeoForge |
| Lunar | `../lunar` | Maven | 8 | Lunar Client (Forge 1.8+, Fabric 1.16+) |
| Essential | `../essential` | Maven | 8 | Forge 1.8+, Fabric 1.16+, NeoForge 1.20+ |
| Dawn | `../minecraft-dawn` | Gradle + Loom, Kotlin | 17 | Dawn/Feather on modern Fabric |

The loader downloads each patch from its GitHub release and adds it to the classpath (`patches.json` in the loader repo).
Several patches can end up in the same game.

## Rules

- **Java 8.** No `var`, `List.of`, `Path.of`, `String.isBlank`, records or other post-8 API. Check with
  `javac --release 8` (see Build).
- **The game supplies the dependencies.** Gson and Log4j are `provided`. Compile against the oldest version any project
  runs on, or a call can compile and then fail with `NoSuchMethodError` on 1.8.9:
  - Log4j: `2.0-beta9` (Forge 1.8.9).
  - Gson: `2.2.4` (1.8-1.11). Gson by Minecraft version: 1.8-1.11.2 → 2.2.4, 1.12-1.17 → 2.8.0, 1.18 → 2.8.9,
    1.19-1.20.1 → 2.10, 1.20.5-1.21.1 → 2.10.1, 1.21.8 → 2.11.0, 1.21.11+ → 2.13.2.
  - No Guava: it differs too much between game versions. Use the JDK.
- **Only add API, never change or remove it.** Patches are released separately from the core and run against whatever
  core is newest. A changed signature breaks every user at once with `NoSuchMethodError`. New behavior goes in new
  methods, and old ones can be deprecated.
- **The core ships this lib with its package unchanged.** Nobody renames or bundles it. Patches use it as `provided`.
  Never rename `org.apache.logging` or `com.google.gson` in anything: those must stay the game's own classes.
- **Not Essential's Gson.** Essential's model classes (`Cosmetic`, `CosmeticSetting`, ...) are annotated with
  `gg.essential.lib.gson.annotations.SerializedName` (fields serialize as `a`, `b`, `c`, ...). Minecraft's Gson ignores
  those annotations, so `GsonUtil` must not be used for Essential types. The Essential repo keeps its own `GsonUtil`
  for them.
- **Never crash the game.** Library code that runs in the game catches and logs instead of throwing, unless the caller
  asked for the exception (ex: `PathUtil` methods declare `IOException`).
- **Absolute paths.** Everything goes under `OS.getConfigFolder()`, never a path relative to the working directory, which
  changes with how the game was launched.

## Layout

`src/main/java/ing/prometheusreengineer/minecraft/`

- `util/OS`: current OS and `getConfigFolder()`: `%APPDATA%/.minecraft/prometheus` on Windows,
  `~/Library/Application Support/prometheus` on macOS, `$XDG_DATA_HOME` (or `~/.local/share`)`/prometheus` on Linux.
  Overridden by the `folder` setting.
- `util/Settings`: user overrides. `Settings.get("dawn.port")` reads `-Dprometheus.dawn.port`, then
  `PROMETHEUS_DAWN_PORT`. Blank values count as unset, and booleans accept only `true`/`false`.
- `util/PathUtil`: home and env paths, recursive copy, copy-missing and delete.
- `util/GsonUtil`: 4-space pretty JSON with Minecraft's Gson.
- `util/Env`: writes to `System.getenv()` through `sun.misc.Unsafe` (Dawn uses it to set `DAWN_API_URL`). It can't
  compile with `--release 8`, because that hides `sun.misc`.
- `util/AtomicFiles`: writes to a unique temp file next to the target, then moves it into place. A failed or crashed
  write leaves the old file as it was.
- `util/Hashing`: SHA-256 and SHA-1 as lowercase hex, with the JDK only. `matches()` accepts GitHub's `sha256:<hex>`
  asset digests.
- `util/Http`: built on `HttpURLConnection`:
  - timeouts (`http.timeout` setting, default 10s) and a User-Agent
  - a status of 400 or above is an `IOException`
  - `download()` is atomic and checks the SHA-256 and the byte count against `Content-Length`, because a server
    hanging up early can look like a normal end of stream
- `patch/PrometheusPatch` + `PatchContext`: the only way into a patch, described below.
- `log/`: the logger, described below.

## Patch contract (`patch/`)

- **Declaring it:** a patch implements `PrometheusPatch` and lists the class in
  `META-INF/services/ing.prometheusreengineer.minecraft.patch.PrometheusPatch`.
- **What the core does:** it adds the jar, registers its mixin configs, then calls `init(PatchContext)` once. An
  exception only disables that patch.
- **`PatchContext`:** gives `project()`, `platform()`, `coreVersion()`, `folder()` (`<config folder>/<project>`) and
  `log(...)` (`Log.get(project, ...)`).
- **It doesn't call `Log.setOwner`.** There is one copy of the lib, so the owner is global. The core sets it to
  `loader`, and lib lines go to `loader.log`.

## Logging (`log/`)

Hosts handle logging differently: Lunar replaces JUL's output, Forge 1.8.9 ships a Log4j beta, and the loader runs before
most of it is set up. So `Log` decides the level and the format itself, and does not depend on the host.

```java
private static final Log log = Log.get("essential", "ECM");   // logs as Prometheus/essential/ECM
log.info("Merged %d cosmetics (%d new)", total, added);        // String.format, done before any backend
log.error("Failed to save cosmetics list", e);
log.warn("failed %s", id, e);                                  // a trailing Throwable arg is the exception
```

- **Code inside this lib uses `Log.lib("Env")`, never a project name.** Its lines belong to whoever claimed the lib
  with `Log.setOwner(...)`.
  - The core calls `Log.setOwner("loader")` first thing. From then on, `Log.lib` loggers write to `loader.log` as
    `Prometheus/loader/lib/Env`.
  - Lines logged before that call go to `lib.log` as `Prometheus/lib/Env`.
  - The owner is read on every line, so `static final` loggers created before the call still follow it.
  - Patches never call `setOwner`. They log through `PatchContext.log()` or `Log.get("<project>")`.

- **The file always gets every line**: `<config folder>/logs/<project>.log` (`LogFile`).
  - It's opened on the first line written, which also moves the previous run to `<project>.1.log`.
  - It rotates once per JVM. The marker is a system property, which every copy of the lib can see, so a project's jar
    loaded twice appends instead of rotating again.
  - Each line is flushed, and each run is capped at about 8 MB.
  - The header lists the jar path (which gives the patch and its version), Java, the OS, the folder, the detected loaders
    and the debug state.
- **The console is best effort** (`Console`):
  - `LOG4J` (the default) uses the game's Log4j when it is present. If Log4j is missing or broken, it falls back to
    `STDOUT`.
  - `Log4jConsole` is the only class that imports Log4j, and it is only touched after a check that Log4j exists. Keep it
    that way.
  - A project picks its own default with `Log.setDefaultConsole(...)`. This is read on every line, so loggers created
    earlier still pick it up. Lunar should use `STDERR`, because its old JUL workaround wrote to `System.err` and that
    showed up. This hasn't been checked in the game yet.
- **Settings**:
  - `debug` / `<project>.debug` (default off).
  - `log.console` / `<project>.log.console` (`log4j|stdout|stderr|off`).
- **It never throws.** If the file can't be opened, there is one warning on stderr, the file output turns off and the
  console keeps working.

## Build and test

There may be no `mvn` on the PATH. IntelliJ's bundled Maven works:
`%APPDATA%/JetBrains/IntelliJIdea<version>/plugins/maven-plugin/lib/maven3/bin/mvn`.

- `mvn test` runs the tests. `mvn install` puts the lib in `~/.m2`, where the loader's core picks it up. **Run it
  after every change the core needs**, because the core build doesn't see this repo otherwise.

Without Maven, use `javac` against the jars in `~/.m2/repository`. On Windows, give `javac` paths like `C:/Users/...`,
because Windows `javac` can't read git-bash `/c/...` paths.

- Check that the code only uses Java 8 API, except `Env` (which needs `sun.misc`). Compile against
  `gson-2.2.4.jar` (not in `~/.m2` by default, so download it from Maven Central and verify its `.sha1`):
  `javac --release 8 -Xlint:all,-options -Werror -cp <annotations;log4j-api-2.0-beta9;gson-2.2.4> <sources except Env.java>`
- Build everything like Maven does: `javac -source 8 -target 8 ...`
- Tests are JUnit 5 in `src/test/java`. Run them with `junit-platform-console-standalone-1.11.4.jar`:
  `java -jar <it> execute -cp <main;test;deps> --select-package ing.prometheusreengineer.minecraft`

- **Logging tests** set `-Dprometheus.folder` to a temp folder and turn the console off. They use a unique project name
  per test, because log files and rotation are keyed by project.
- **`HttpTest`** runs a JDK `com.sun.net.httpserver.HttpServer` on the loopback address. It never touches the network.

## Style

- Commits: `type: message` (`feat:`, `fix:`, `build:`, `docs:`, `chore:`), small, and each one builds on its own.
- `org.jetbrains.annotations`: `@NotNull`, `@Nullable` and `@Contract` on public API.
- Short lowercase `//` comments that explain why. Javadoc on public methods.
- Utility classes are `final` with a private constructor.
- Name threads `Prometheus - <project> - <task>` and make them daemon threads.

## Known issues

- `PathUtil.deleteRecursively` and `copyRecursively` follow symlinks and junctions: `File.isDirectory()` is true for a
  link to a folder.
- `OS`: Windows adds `.minecraft` to the path, but macOS and Linux don't.
- `GsonUtil` uses `new Gson()`, which escapes `<`, `>`, `=` and `'` as `\u003c` and similar codes. Use
  `disableHtmlEscaping()`.
- Not built yet, and planned: read-with-fallback, daemon thread helpers, and helpers for finding obfuscated classes by
  shape (from Lunar's `StaffMods` and Dawn's `FeatherMapping`).
