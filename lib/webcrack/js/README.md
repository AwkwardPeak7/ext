# webcrack bundle

Builds `../assets/webcrack.js` from the [`webcrack`](https://www.npmjs.com/package/webcrack) npm
package. `Webcrack.kt` loads it into a WebView and calls the global `webcrack.deobfuscate(code)`,
which returns a promise of the deobfuscated code.

## Updating

1. Bump `webcrack` in `package.json`. Pick a release that is at least two weeks old.
2. Run:

   ```bash
   npm install
   npm run build
   ```

3. Commit `package.json`, `package-lock.json` and `../assets/webcrack.js`, and bump `versionCode`
   of every extension that uses `:lib:webcrack`.

## Build notes

webcrack targets Node, so the bundle replaces a few Node-only pieces (see the `build` script):

- `isolated-vm` is stubbed with `empty.js`. It's webcrack's default sandbox for running the
  obfuscator's string decoder, and `entry.js` passes a plain `eval` sandbox instead, since the
  WebView is isolation enough.
- `@babel/core` is stubbed too. `@codemod/utils` only re-exports it, and nothing uses it.
- `node:fs/promises` (only used to save results to disk) is stubbed, and `node:path` is replaced
  with `path-browserify`.
- `process.env` is defined as `{}`, since Babel reads a few flags from it.

If a new version fails to build with `Could not resolve "..."`, it needs another of these aliases.
