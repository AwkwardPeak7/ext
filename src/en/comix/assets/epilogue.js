// Hands secure.js a fake axios instance, captures the request interceptor it
// installs, and exposes comixSign(path) / comixSigner.sign(path).
(function () {
  "use strict";
  var g = globalThis;

  var SEC = g.__SECURE_EXPORTS;
  if (!SEC || typeof SEC.n !== "function") {
    throw new Error("comix-signer: secure.js exports not found (__SECURE_EXPORTS.n)");
  }

  var requestInterceptors = [];
  var fakeAxios = {
    defaults: { headers: { common: {} }, baseURL: "/api/v1" },
    interceptors: {
      request: {
        use: function (onFulfilled /*, onRejected */) {
          requestInterceptors.push(onFulfilled);
          return requestInterceptors.length - 1;
        },
        eject: function () {},
      },
      response: {
        use: function () { return 0; },
        eject: function () {},
      },
    },
    get: function () {}, post: function () {}, put: function () {},
    patch: function () {}, delete: function () {}, request: function () {},
  };

  // Equivalent to the site's `Mi(L)` call.
  SEC.n(fakeAxios);

  g.comixSign = function (path) {
    var cfg = {
      url: String(path),
      method: "get",
      baseURL: "/api/v1",
      params: {},
      headers: {},
    };
    for (var i = 0; i < requestInterceptors.length; i++) {
      var fn = requestInterceptors[i];
      if (typeof fn === "function") {
        var res = fn(cfg);
        if (res && typeof res.then !== "function") cfg = res;
      }
    }
    if (cfg && cfg.params && cfg.params._ != null) return String(cfg.params._);
    if (cfg && cfg.headers && cfg.headers._ != null) return String(cfg.headers._);
    return null;
  };

  g.comixSigner = { sign: g.comixSign };
})();
