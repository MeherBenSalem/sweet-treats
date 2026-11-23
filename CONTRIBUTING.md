# Contributing

Thanks for helping improve Sweet Treats.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Make focused changes in the relevant Minecraft version workspace (`1.20.1/`, `1.21.1/`, `26.2/` or `26.3/`).
   A gameplay change usually belongs in all four: the `common/` code is nearly identical between them, and only the
   version-specific APIs differ.
3. Build the workspace you changed (`./gradlew build` from that folder: Gradle on Java 21 for 1.20.1 and 1.21.1,
   Java 25 for 26.x), or all four with `./gradlew buildAll` from the root (the 8 loader jars land in `releases/`).
4. Open a pull request describing the change and how you tested it.

## Layout

Each workspace is a MultiLoader Gradle build:

- `common/`: blocks, block entities, items, recipes and their serializers, the stations' menus and screens, the
  Recipe Book and discovery tracking, config, network packets and renderers. Nearly all changes happen here.
- `fabric/` and `neoforge/` (`forge/` on 1.20.1): entry points that register content, packets, the creative tab,
  renderers, loot and world generation hooks and events through the `platform/` services. The 1.20.1 Forge module also
  holds the GameTests, the visual self test and the marketing showcase (development only, left out of the jar).

## Resources

The 1.20.1 workspace is the source for the generated resources; the other workspaces carry the same data in their
version's format (folder names, recipe and loot formats, item model definitions, world generation).

```bash
python3 tools/generate_resources.py   # regenerate JSON data, lang and docs/RECIPES.md in 1.20.1/common
python3 tools/validate_resources.py   # check models, textures, lang and loot against the 1.20.1 registry
```

When you change data, carry the change into `1.21.1/`, `26.2/` and `26.3/` too.

## Testing in game

```bash
cd 1.20.1 && ./gradlew :forge:runGameTestServer        # the automated in-game tests
cd 1.20.1 && ./gradlew :forge:runVisualTestClient      # builds a demo bakery, screenshots every screen and quits
scripts/server-smoke-test.sh 26.3 fabric               # a dev server that places every block
```

If you change gameplay, try it on every workspace you touched, for Fabric and NeoForge/Forge.

## Coding expectations

- Prefer small, reviewable PRs over large mixed changes.
- Match existing naming, package layout, and MultiLoader patterns.
- Keep all state on the server; the client only shows it.
- Do not commit build output, IDE metadata, or local secrets.
- When fixing a bug, say which version/loaders you verified.

## Issues

Use GitHub Issues for bugs and feature requests. Include Minecraft version, mod loader, mod version, and steps to
reproduce when reporting bugs.

## Licensing

By contributing, you agree that your contributions are licensed under the Apache License, Version 2.0, unless you
state otherwise in the pull request.
