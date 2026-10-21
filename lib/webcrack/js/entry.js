import { webcrack } from 'webcrack'

export async function deobfuscate(code) {
  const result = await webcrack(code, {
    jsx: false,
    unpack: false,
    // The default sandbox needs isolated-vm, which only exists in Node; the WebView is isolation enough
    sandbox: src => (0, eval)(src),
  })
  return result.code
}
