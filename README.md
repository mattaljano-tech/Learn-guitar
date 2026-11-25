# NylonGitarr Studio

Här ska jag lära mig spela gitarr.

Övningsapp för nylongitarr: kompband, en 3D-hand som visar greppen i takt med låten, skank-skola, ackordtabell, ackordbyten, lyssnande träning, stämmare och en nybörjarväg med XP, nivåer och märken.

Appen är en installerbar webbapp (PWA). Den fungerar offline efter första besöket och sparar dina framsteg på telefonen.

## Installera på telefonen (Android, Chrome)

1. Öppna appens adress i Chrome, till exempel `https://mattaljano-tech.github.io/Learn-guitar/`.
2. Tryck på menyn (tre prickar) och välj **Installera app** eller **Lägg till på startskärmen**.
3. Appen får en egen ikon och öppnas i helskärm.

Mikrofonen (stämmaren och ackordkollen) fungerar här, eftersom appen ligger på en egen säker adress. Chrome frågar om lov första gången.

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
- `hand/` – 3D-handens ackordbyten, ett kort WebM-klipp per byte (renderade i Blender)
- `audio/nylon/` – inspelade nylongitarrtoner till kompet (29 toner). Källa: [tonejs-instruments](https://github.com/nbrosowsky/tonejs-instruments) av Nicholaus P. Brosowsky, licens [CC-BY 3.0](https://creativecommons.org/licenses/by/3.0/). Klippta, mono och normaliserade.
- `audio/bass/` (elbas, Karoryfer) och `audio/kontrabas/` (kontrabas, VSO2) – inspelade bastoner till kompbandet. Källa: [tonejs-instruments](https://github.com/nbrosowsky/tonejs-instruments) av Nicholaus P. Brosowsky, licens [CC-BY 3.0](https://creativecommons.org/licenses/by/3.0/). Klippta, mono och normaliserade.
- `audio/drums/` – riktiga trumljud till Beatstudion, klippta, mono och normaliserade:
  - Bastrumma, virvel, hi-hat, ride, crash och pukor från *Pearl Master Studio Pack 1* av enoe, licens [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), via [oramics/sampled](https://github.com/oramics/sampled).
  - Djup och tight bastrumma, kantslag, handklapp, halvöppen hi-hat, ride-klocka, tamburin, koskälla och maracas från *AVL Drumkits 1.1* (Glen MacArthur), licens [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/), via [oramics/sampled](https://github.com/oramics/sampled). De bearbetade ljudfilerna delas under samma licens.
  - Borstvirvel och träkloss från *OLPC Berklee Sound Library*, licens [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), via [Tonejs/audio](https://github.com/Tonejs/audio).
