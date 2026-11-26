/* NylonGitarr Studio – service worker
   Höj VERSION när appen uppdateras, så hämtar telefonen den nya versionen. */
const VERSION = 'v157';
const CACHE = 'nylongitarr-' + VERSION;
const SHELL = ['./', 'index.html', 'video/scene.jpg', 'manifest.webmanifest', 'icons/icon-192.png', 'icons/icon-512.png', 'icons/maskable-512.png', 'icons/apple-touch-icon.png',
  ...['clap','cowbell','crash','hat-semi','hat','kick-deep','kick-tight','kick','ohat','ride-bell','ride','rim','shaker','snare-brush','snare-tight','snare','tamb','tom-hi','tom-lo','wood-hi','wood-lo'].map((n) => 'audio/drums/' + n + '.mp3'),
  ...['A2','A3','A4','A5','As5','B1','B2','B3','B4','Cs3','Cs4','Cs5','D2','D3','D5','Ds4','E2','E3','E4','E5','Fs2','Fs3','Fs4','Fs5','G3','G5','Gs2','Gs4','Gs5'].map((n) => 'audio/nylon/' + n + '.mp3'),
  ...['As1','As2','As3','Cs2','Cs3','E1','E2','E3','G1','G2','G3'].map((n) => 'audio/bass/' + n + '.mp3'),
  ...['A2','As1','C2','Cs3','D2','E2','E3','Fs1','Fs2','G1','Gs2','Gs3'].map((n) => 'audio/kontrabas/' + n + '.mp3'),
  ...['A2','A3','A4','C3','C4','C5','Ds3','Ds4','F4','Fs3'].map((n) => 'audio/harmonium/' + n + '.mp3'),
  ...['A1','A2','A3','A4','A5','C2','C3','C4','C5','C6','Ds2','Ds3','Ds4','Ds5','Fs2','Fs3','Fs4','Fs5'].map((n) => 'audio/piano/' + n + '.mp3'),
  ...['A3','A4','A5','C4','C5','E4','E5','G3','G4','G5'].map((n) => 'audio/violin/' + n + '.mp3'),
  ...['A2','A3','C2','C3','C4','D2','D3','E2','E3','G2','G3'].map((n) => 'audio/cello/' + n + '.mp3'),
  ...['A2','A3','A4','C3','C4','C5','Ds3','Ds4','E2','Fs2','Fs3','Fs4'].map((n) => 'audio/elgitarr/' + n + '.mp3'),
  ...['G','C','D','Em','Am','E','A','Dm','F','Bm','Cadd9','Fmaj7'].flatMap((c) => ['mjuk','hard','upp','dampat'].map((t) => 'audio/strums/' + c + '-' + t + '.mp3'))];
const CDN = ['cdnjs.cloudflare.com', 'fonts.googleapis.com', 'fonts.gstatic.com'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE).then((c) => c.addAll(SHELL.map((u) => new Request(u, { cache: 'reload' })))).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k)))).then(() => self.clients.claim()));
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);

  // Själva sidan: hämta ny version om nätet finns, annars den sparade
  if (req.mode === 'navigate') {
    e.respondWith(fetch(req).then((res) => { const copy = res.clone(); caches.open(CACHE).then((c) => c.put('index.html', copy)); return res; })
      .catch(() => caches.match('index.html')));
    return;
  }

  // React och typsnitt: använd sparad kopia direkt, uppdatera i bakgrunden
  if (CDN.includes(url.hostname)) {
    e.respondWith(caches.open(CACHE).then((c) => c.match(req).then((hit) => {
      const net = fetch(req).then((res) => { if (res.ok || res.type === 'opaque') c.put(req, res.clone()); return res; }).catch(() => hit);
      return hit || net;
    })));
    return;
  }

  // Egna filer: sparad kopia först
  if (url.origin === self.location.origin) {
    e.respondWith(caches.match(req).then((hit) => hit || fetch(req).then((res) => { const copy = res.clone(); caches.open(CACHE).then((c) => c.put(req, copy)); return res; })));
  }
});
