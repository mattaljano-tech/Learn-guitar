# NylonGitarr Studio

Här ska jag lära mig spela gitarr.

Övningsapp för nylongitarr: kompband med reggae och akustiskt groove, Guitar Hero-läge, skank-skola, ackordtabell, ackordbyten, stämmare och en nybörjarväg med XP, nivåer och märken.

Appen är en installerbar webbapp (PWA). Den fungerar offline efter första besöket och sparar dina framsteg på telefonen.

## Installera på telefonen (Android, Chrome)

1. Öppna appens adress i Chrome, till exempel `https://mattaljano-tech.github.io/Learn-guitar/`.
2. Tryck på menyn (tre prickar) och välj **Installera app** eller **Lägg till på startskärmen**.
3. Appen får en egen ikon och öppnas i helskärm.

Mikrofonen (stämmaren, ackordkollen och mikrofonläget i Guitar Hero) fungerar här, eftersom appen ligger på en egen säker adress. Chrome frågar om lov första gången.

## Publicera med GitHub Pages

1. Gå till repots **Settings → Pages**.
2. Under **Build and deployment** väljer du **Deploy from a branch**, grenen **main** och mappen **/ (root)**. Spara.
3. Efter någon minut visas adressen överst på sidan.

## Uppdatera appen

1. Ändra `index.html`.
2. Höj `VERSION` i `sw.js` (till exempel `v1` → `v2`).
3. Pusha till `main`. Telefonen hämtar den nya versionen nästa gång appen öppnas med nät.

## Filer

- `index.html` – hela appen (React via CDN, inga byggsteg)
- `manifest.webmanifest` – namn, ikon och helskärmsläge
- `sw.js` – offline-stöd
- `icons/` – appikoner
