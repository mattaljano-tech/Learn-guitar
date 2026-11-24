#!/usr/bin/env bash
# Bygger mappen www/ som läggs in i Android-appen: webbappen + React och typsnitt lokalt, så att allt fungerar utan nät.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf www && mkdir -p www/vendor/fonts
cp ../index.html ../manifest.webmanifest www/
[ -f ../sync-url.json ] && cp ../sync-url.json www/ || true
cp -r ../icons ../audio ../hand ../video www/
cp node_modules/react/umd/react.production.min.js node_modules/react-dom/umd/react-dom.production.min.js www/vendor/
css=www/vendor/fonts.css; : > "$css"
add() { # paket familj vikt
  local f="node_modules/@fontsource/$1/files/$1-latin-$3-normal.woff2"
  cp "$f" www/vendor/fonts/
  printf "@font-face{font-family:'%s';font-style:normal;font-weight:%s;font-display:swap;src:url(fonts/%s) format('woff2')}\n" "$2" "$3" "$(basename "$f")" >> "$css"
}
for w in 400 500 600 700 800; do add outfit Outfit $w; done
for w in 500 700; do add jetbrains-mono "JetBrains Mono" $w; done
for w in 500 600 700; do add fraunces Fraunces $w; done
add pinyon-script "Pinyon Script" 400
python3 - <<'PY'
import re
p='www/index.html'; s=open(p, encoding='utf-8').read()
n0=len(s)
s=s.replace('https://cdnjs.cloudflare.com/ajax/libs/react/18.3.1/umd/react.production.min.js','vendor/react.production.min.js')
s=s.replace('https://cdnjs.cloudflare.com/ajax/libs/react-dom/18.3.1/umd/react-dom.production.min.js','vendor/react-dom.production.min.js')
s=re.sub(r'<link rel="stylesheet" href="https://fonts\.googleapis\.com/[^"]*">','<link rel="stylesheet" href="vendor/fonts.css">',s)
s=re.sub(r'<link rel="preconnect"[^>]*>\n?','',s)
s=s.replace('<link rel="manifest" href="manifest.webmanifest">\n','')
assert 'cdnjs.cloudflare.com' not in s and 'fonts.googleapis.com' not in s, 'externa länkar kvar'
open(p,'w', encoding='utf-8').write(s)
print('www/index.html klar')
PY
