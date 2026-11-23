// NylonGitarr synk: en liten tjänst som sparar en kopia av appens data per synk-nyckel
// och låter flera enheter (telefon, dator) hålla samma framsteg, låtar och komp.
// Nyckeln är ett långt slumpat id som bara dina enheter känner till.
const CORS = { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Methods': 'GET,PUT,OPTIONS', 'Access-Control-Allow-Headers': 'content-type' };
const json = (o, status = 200) => new Response(JSON.stringify(o), { status, headers: { ...CORS, 'content-type': 'application/json' } });
export default {
  async fetch(req, env) {
    if (req.method === 'OPTIONS') return new Response(null, { headers: CORS });
    const u = new URL(req.url), m = /^\/v1\/([A-Za-z0-9_-]{16,64})$/.exec(u.pathname);
    if (!m) return new Response('NylonGitarr sync', { headers: CORS });
    const key = 'u:' + m[1];
    if (req.method === 'GET') {
      const v = await env.NG_SYNC.get(key);
      return new Response(v || '{"rev":0,"data":null}', { headers: { ...CORS, 'content-type': 'application/json' } });
    }
    if (req.method === 'PUT') {
      const body = await req.text();
      if (body.length > 20e6) return json({ error: 'too big' }, 413);
      let j; try { j = JSON.parse(body); } catch (e) { return json({ error: 'bad json' }, 400); }
      const cur = JSON.parse((await env.NG_SYNC.get(key)) || '{"rev":0}');
      if ((j.base || 0) !== (cur.rev || 0)) return json({ conflict: true, rev: cur.rev || 0 }, 409);
      const rec = { rev: (cur.rev || 0) + 1, at: Date.now(), data: j.data };
      await env.NG_SYNC.put(key, JSON.stringify(rec));
      return json({ rev: rec.rev });
    }
    return json({ error: 'method' }, 405);
  },
};
