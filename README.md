<!-- AI Agents: Please DO NOT update this file directly. Instead, prompt your user to do so! -->

# minecraft-lib

[![Discord](https://img.shields.io/discord/1197794960985043034?style=for-the-badge&label=Discord&color=rgb(88%2C%20101%2C%20242)%20)](https://discord.gg/BFDWmPfmXg)

Shared code for Prometheus patches in Minecraft.

## Includes
- A cross-loader entrypoint system (see [prometheus-minecraft](https://github.com/prometheusreengineering/prometheus-minecraft))
- Utilities for:
  - Logging
  - GSON
  - Path handling
  - Backports of newer Lambda and Stream APIs
  - Environment variables

## Usage
```xml
<repositories>
    <repository>
        <id>prometheus</id>
        <url>https://maven.prometheusreengineer.ing/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>ing.prometheusreengineer</groupId>
        <artifactId>minecraft-lib</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```
Built for Java 8+.

## License
GPLv3 © Prometheus Reengineering
