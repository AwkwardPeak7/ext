// Minimal browser environment for running comix.to's secure.js inside QuickJS.
// The host (Signer.kt) prepends `var __COMIX_CFG__ = "..."` before this file.
// The `nativeify` helper is load-bearing: secure.js reads `.toString()` on the
// DOM functions it reaches (e.g. document.querySelector) and folds the source
// string into its cipher input. Real browsers return
// `function name() { [native code] }`; this shim must too.
(function () {
  "use strict";
  var g =
    typeof globalThis !== "undefined"
      ? globalThis
      : typeof global !== "undefined"
      ? global
      : this;

  function nativeify(fn, name) {
    try {
      Object.defineProperty(fn, "toString", {
        value: function () { return "function " + name + "() { [native code] }"; },
        writable: true, configurable: true,
      });
    } catch (e) {}
    try {
      Object.defineProperty(fn, "name", { value: name, configurable: true });
    } catch (e) {}
    return fn;
  }
  function tagged(name, obj) {
    try { Object.defineProperty(obj, Symbol.toStringTag, { value: name, configurable: true }); } catch (e) {}
    return obj;
  }
  function n(name, body) { return nativeify(body || function () {}, name); }

  if (typeof g.console === "undefined") {
    g.console = {
      log: n("log"), info: n("info"), warn: n("warn"),
      error: n("error"), debug: n("debug"), trace: n("trace"),
    };
  }

  if (typeof g.setTimeout === "undefined") g.setTimeout = n("setTimeout", function () { return 0; });
  if (typeof g.clearTimeout === "undefined") g.clearTimeout = n("clearTimeout");
  if (typeof g.setInterval === "undefined") g.setInterval = n("setInterval", function () { return 0; });
  if (typeof g.clearInterval === "undefined") g.clearInterval = n("clearInterval");
  if (typeof g.queueMicrotask === "undefined")
    g.queueMicrotask = n("queueMicrotask", function (f) { Promise.resolve().then(f); });
  if (typeof g.requestAnimationFrame === "undefined")
    g.requestAnimationFrame = n("requestAnimationFrame", function () { return 0; });
  if (typeof g.cancelAnimationFrame === "undefined")
    g.cancelAnimationFrame = n("cancelAnimationFrame");

  var B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  if (typeof g.btoa === "undefined") {
    g.btoa = nativeify(function (input) {
      input = String(input);
      var out = "", i = 0, nlen = input.length;
      while (i < nlen) {
        var c1 = input.charCodeAt(i++) & 0xff;
        var c2 = i < nlen ? input.charCodeAt(i++) & 0xff : NaN;
        var c3 = i < nlen ? input.charCodeAt(i++) & 0xff : NaN;
        var e1 = c1 >> 2;
        var e2 = ((c1 & 3) << 4) | (isNaN(c2) ? 0 : c2 >> 4);
        var e3 = isNaN(c2) ? 64 : ((c2 & 15) << 2) | (isNaN(c3) ? 0 : c3 >> 6);
        var e4 = isNaN(c3) ? 64 : c3 & 63;
        out +=
          B64.charAt(e1) +
          B64.charAt(e2) +
          (e3 === 64 ? "=" : B64.charAt(e3)) +
          (e4 === 64 ? "=" : B64.charAt(e4));
      }
      return out;
    }, "btoa");
  }
  if (typeof g.atob === "undefined") {
    g.atob = nativeify(function (input) {
      input = String(input).replace(/[^A-Za-z0-9+/=]/g, "");
      var out = "", i = 0, nlen = input.length;
      while (i < nlen) {
        var e1 = B64.indexOf(input.charAt(i++));
        var e2 = B64.indexOf(input.charAt(i++));
        var e3 = B64.indexOf(input.charAt(i++));
        var e4 = B64.indexOf(input.charAt(i++));
        var c1 = (e1 << 2) | (e2 >> 4);
        var c2 = ((e2 & 15) << 4) | (e3 >> 2);
        var c3 = ((e3 & 3) << 6) | e4;
        out += String.fromCharCode(c1);
        if (e3 !== 64 && e3 !== -1) out += String.fromCharCode(c2 & 0xff);
        if (e4 !== 64 && e4 !== -1) out += String.fromCharCode(c3 & 0xff);
      }
      return out;
    }, "atob");
  }

  if (typeof g.navigator === "undefined") {
    g.navigator = tagged("Navigator", {
      appCodeName: "Mozilla",
      appName: "Netscape",
      appVersion:
        "5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36",
      product: "Gecko",
      productSub: "20030107",
      userAgent:
        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36",
      platform: "Linux armv8l",
      language: "en-US",
      languages: ["en-US", "en"],
      vendor: "Google Inc.",
      vendorSub: "",
      hardwareConcurrency: 8,
      deviceMemory: 8,
      maxTouchPoints: 5,
      cookieEnabled: true,
      onLine: true,
      doNotTrack: null,
      webdriver: false,
    });
  } else if (!g.navigator.appCodeName) {
    try { g.navigator.appCodeName = "Mozilla"; } catch (e) {}
  }

  function makeStorage() {
    var data = {};
    return tagged("Storage", {
      getItem: nativeify(function (k) {
        return Object.prototype.hasOwnProperty.call(data, k) ? data[k] : null;
      }, "getItem"),
      setItem: nativeify(function (k, v) { data[k] = String(v); }, "setItem"),
      removeItem: nativeify(function (k) { delete data[k]; }, "removeItem"),
      clear: nativeify(function () { data = {}; }, "clear"),
      key: nativeify(function (i) { return Object.keys(data)[i] || null; }, "key"),
      get length() { return Object.keys(data).length; },
    });
  }
  if (typeof g.localStorage === "undefined") g.localStorage = makeStorage();
  if (typeof g.sessionStorage === "undefined") g.sessionStorage = makeStorage();

  if (typeof g.location === "undefined") {
    g.location = tagged("Location", {
      href: "https://comix.to/",
      origin: "https://comix.to",
      protocol: "https:",
      host: "comix.to",
      hostname: "comix.to",
      port: "",
      pathname: "/",
      search: "",
      hash: "",
      ancestorOrigins: {},
      assign: nativeify(function () {}, "assign"),
      replace: nativeify(function () {}, "replace"),
      reload: nativeify(function () {}, "reload"),
      toString: nativeify(function () { return this.href; }, "toString"),
    });
  }

  // The cfg meta is read at sign time and folded into the cipher keystream —
  // expose it via a fake meta element that document.querySelector returns.
  var CFG = typeof g.__COMIX_CFG__ === "string" ? g.__COMIX_CFG__ : "";
  function metaEl(name, content) {
    var attrs = { name: name, content: content };
    var el = tagged("HTMLMetaElement", {
      tagName: "META", nodeName: "META", nodeType: 1, localName: "meta",
      get name() { return attrs.name; },
      get content() { return attrs.content; },
      getAttribute: nativeify(function (n) {
        return Object.prototype.hasOwnProperty.call(attrs, n) ? attrs[n] : null;
      }, "getAttribute"),
      hasAttribute: nativeify(function (n) {
        return Object.prototype.hasOwnProperty.call(attrs, n);
      }, "hasAttribute"),
      getAttributeNames: nativeify(function () { return Object.keys(attrs); }, "getAttributeNames"),
      removeAttribute: nativeify(function (n) { delete attrs[n]; }, "removeAttribute"),
      setAttribute: nativeify(function (n, v) { attrs[n] = String(v); }, "setAttribute"),
      attributes: Object.keys(attrs).map(function (k) { return { name: k, value: attrs[k] }; }),
      style: {},
      addEventListener: nativeify(function () {}, "addEventListener"),
      removeEventListener: nativeify(function () {}, "removeEventListener"),
    });
    return el;
  }
  var metaCfg = metaEl("cfg", CFG);
  var metaCharset = tagged("HTMLMetaElement", {
    tagName: "META", nodeName: "META", nodeType: 1, localName: "meta",
    getAttribute: nativeify(function (n) { return n === "charset" ? "utf-8" : null; }, "getAttribute"),
    hasAttribute: nativeify(function (n) { return n === "charset"; }, "hasAttribute"),
    attributes: [{ name: "charset", value: "utf-8" }],
  });
  var metas = [metaCharset, metaCfg];

  if (typeof g.document === "undefined") {
    function stubEl() {
      return tagged("HTMLElement", {
        tagName: "DIV", nodeName: "DIV", nodeType: 1,
        style: {}, attributes: [], children: [], childNodes: [],
        setAttribute: nativeify(function () {}, "setAttribute"),
        getAttribute: nativeify(function () { return null; }, "getAttribute"),
        appendChild: nativeify(function () {}, "appendChild"),
        removeChild: nativeify(function () {}, "removeChild"),
        addEventListener: nativeify(function () {}, "addEventListener"),
        removeEventListener: nativeify(function () {}, "removeEventListener"),
        getContext: nativeify(function () { return null; }, "getContext"),
      });
    }
    var bodyEl = stubEl();
    var headEl = tagged("HTMLHeadElement", Object.assign(stubEl(), { children: metas, childNodes: metas }));
    var htmlEl = tagged("HTMLHtmlElement", Object.assign(stubEl(), { children: [headEl, bodyEl], childNodes: [headEl, bodyEl] }));

    g.document = tagged("HTMLDocument", {
      cookie: "", referrer: "", title: "comix", readyState: "complete",
      visibilityState: "visible", hidden: false, URL: g.location && g.location.href,
      baseURI: g.location && g.location.href, domain: "comix.to",
      documentElement: htmlEl, head: headEl, body: bodyEl, scripts: [], defaultView: g, currentScript: null,
      createElement: nativeify(function () { return stubEl(); }, "createElement"),
      createElementNS: nativeify(function () { return stubEl(); }, "createElementNS"),
      createTextNode: nativeify(function (t) { return { nodeType: 3, textContent: String(t) }; }, "createTextNode"),
      getElementById: nativeify(function () { return null; }, "getElementById"),
      getElementsByTagName: nativeify(function (t) {
        return String(t).toLowerCase() === "meta" ? metas : [];
      }, "getElementsByTagName"),
      getElementsByClassName: nativeify(function () { return []; }, "getElementsByClassName"),
      getElementsByName: nativeify(function () { return []; }, "getElementsByName"),
      querySelector: nativeify(function (s) {
        var sel = String(s);
        if (/cfg/.test(sel)) return metaCfg;
        if (/^meta/i.test(sel)) return metaCharset;
        return null;
      }, "querySelector"),
      querySelectorAll: nativeify(function (s) {
        return /^meta/i.test(String(s)) ? metas : [];
      }, "querySelectorAll"),
      addEventListener: nativeify(function () {}, "addEventListener"),
      removeEventListener: nativeify(function () {}, "removeEventListener"),
      dispatchEvent: nativeify(function () { return true; }, "dispatchEvent"),
      createEvent: nativeify(function () { return { initEvent: function () {} }; }, "createEvent"),
      hasFocus: nativeify(function () { return true; }, "hasFocus"),
    });
  }

  try { Object.defineProperty(g, Symbol.toStringTag, { value: "Window", configurable: true }); } catch (e) {}
  g.window = g;
  g.self = g;
  if (typeof g.screen === "undefined")
    g.screen = tagged("Screen", { width: 412, height: 915, availWidth: 412, availHeight: 915, colorDepth: 24, pixelDepth: 24 });
  if (typeof g.history === "undefined")
    g.history = tagged("History", { length: 1, state: null,
      pushState: nativeify(function () {}, "pushState"),
      replaceState: nativeify(function () {}, "replaceState"),
      back: nativeify(function () {}, "back"),
      forward: nativeify(function () {}, "forward"),
      go: nativeify(function () {}, "go"),
    });
  if (typeof g.addEventListener === "undefined") g.addEventListener = nativeify(function () {}, "addEventListener");
  if (typeof g.removeEventListener === "undefined") g.removeEventListener = nativeify(function () {}, "removeEventListener");
  if (typeof g.dispatchEvent === "undefined") g.dispatchEvent = nativeify(function () { return true; }, "dispatchEvent");
  if (typeof g.performance === "undefined")
    g.performance = tagged("Performance", {
      now: nativeify(function () { return 0; }, "now"),
      timeOrigin: 0,
      getEntriesByType: nativeify(function () { return []; }, "getEntriesByType"),
      mark: nativeify(function () {}, "mark"),
      measure: nativeify(function () {}, "measure"),
    });
  if (typeof g.MutationObserver === "undefined")
    g.MutationObserver = function MutationObserver() { return { observe: function () {}, disconnect: function () {}, takeRecords: function () { return []; } }; };
  if (typeof g.matchMedia === "undefined")
    g.matchMedia = nativeify(function () { return { matches: false, addEventListener: function () {}, removeEventListener: function () {} }; }, "matchMedia");
  if (typeof g.getComputedStyle === "undefined")
    g.getComputedStyle = nativeify(function () { return { getPropertyValue: function () { return ""; } }; }, "getComputedStyle");
})();
