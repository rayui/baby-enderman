# Releasing Baby Enderman

CI (`.github/workflows/build.yml`) builds the jar on every push/PR. On a **`v*` tag**, it also
publishes the jar to **Modrinth** and a **GitHub Release**.

## One-time setup (do this once)

1. **Modrinth project** — at <https://modrinth.com>:
   - Create a project: type **Mod**, name **Baby Enderman**, **slug `baby-enderman`** (must match
     `modrinth-id` in the workflow *and* the `baby-enderman` entry in sunnydale-infra).
   - Loader **NeoForge**, Minecraft **1.21.11**, license **MIT**, environment **client and server**.
   - Save it. (You can publish/submit for review now; it needs at least one version, which step 3
     creates. New projects can sit "under review" briefly before they're publicly listed.)

2. **Modrinth API token** — Modrinth → your avatar → **Settings → PATs** → create a token with the
   **Create versions** scope. Copy it, then add it as a GitHub Actions secret on the mod repo:
   ```bash
   gh secret set MODRINTH_TOKEN -R rayui/baby-enderman
   # paste the token when prompted
   ```
   (Or: repo **Settings → Secrets and variables → Actions → New repository secret**, name
   `MODRINTH_TOKEN`.)

## Cutting a release

1. Bump `mod_version` in `gradle.properties` (e.g. `1.0.7`).
2. Commit and push to `main`.
3. Tag it and push the tag (the tag drives publishing):
   ```bash
   git tag v1.0.7
   git push origin v1.0.7
   ```
4. Watch it: `gh run watch -R rayui/baby-enderman` — the tag build publishes to Modrinth + creates a
   GitHub Release. Name/version/dependencies are auto-read from the jar's `neoforge.mods.toml`.

## After the first successful Modrinth publish

The infra switch is staged as a **draft PR** in `sunnydale-infra`
(`minecraft-baby-enderman-modrinth`). Once `baby-enderman` is live on Modrinth with a 1.21.11
NeoForge version, mark that PR **ready** and merge it — Flux rolls the survival2 pod and it pulls the
mod straight from Modrinth (no more committed jar).

## Updating later

Bump `mod_version`, push, tag `v<version>`. Players must update their **client** jar to the matching
version (download from the Modrinth page or the GitHub Release) — it's a both-side mod.
