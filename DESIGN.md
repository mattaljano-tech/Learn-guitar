# DESIGN.md — NylonGitarr Studio

> Den gemensamma källan för gränssnittets värden. Läs hela filen före UI-arbete.
> Använd värden via roll/token. Saknas en roll: lägg till den här och i koden i samma ändring.
> Skapad med better-design (`.claude/skills/better-design`), härledd ur koden som den ser ut, inte påhittad.

Uppdaterad: 2026-11-25

## Riktning (ärvd, inte ny)

- **Koncept:** en klassisk nylongitarr på ett köksbord: cedertopp, mörkt trä och pappas gitarrband.
- **Kvaliteter:** lugn, varm, inbjudande att plocka upp gitarren.
- **Undvik:** många boxar och ramar, neon och gradienter, ytor som tävlar med 3D-handen.
- **Rörelse:** som ett anslag på en sträng: snabbt ner, mjuk fjädrande retur. Inga pulser som aldrig slutar.
- **Signaturdetaljer:** gitarrbandet (`--band`) som logga och avdelare; 3D-handen som byter grepp i takt.
- **Effekter ur poolen:** inga.

## Kodtokens

| Plattform | Fil | Namn |
| --- | --- | --- |
| Webb + Android (Capacitor) | `index.html`, `:root` och `:root[data-skin=…]` | `--bg`, `--surface`, `--text`, `--ember` … |
| Rörelse | `index.html`, `<style id="bd-system">` | `--m-press-down`, `--m-spring-pop` … |

Teman: `ceder` (standard, ljust), `valnot` (mörkt, används vid `auto` + mörkt läge), `sol` (mörkt). Grundvärdena i `:root` utan tema är en äldre neonpalett som bara syns innan temat satts.

## Typsnitt

| Roll | Familj | Reserv | Vikter | Laddning |
| --- | --- | --- | --- | --- |
| Logga | Corinthia | Snell Roundhand, cursive | 700 | Google Fonts / `@fontsource` i appen |
| Rubriker (h1–h3) | Fraunces (`--f-serif`) | Georgia, serif | 500–700, optisk storlek | samma |
| Text / UI | Outfit (`--f-body`) | system-ui | 400–800 | samma |
| Siffror | JetBrains Mono (`--f-mono`, bara i `sol`) | ui-monospace | 500, 700 | samma |

Inga andra familjer.

## Typskala (som den används)

| Roll | Storlek | Vikt | Användning |
| --- | --- | --- | --- |
| display (`.home-h1`) | 46 dator / ~32 mobil | 600 serif | en gång per skärm |
| h2 (`.h2-mission h2`) | ~38 dator / 26 mobil | 600 serif | dagens mål |
| sektionsrubrik / fällbar rad | 16–22 | 700 | "Jamma direkt", "Mixer och känsla" |
| brödtext | 15 | 400 | |
| sekundär | 13–14 | 400–600 | artist, hjälptext |
| etikett / kicker | 12–13 | 700, ingen versal, ingen spärrning | "Dagens mål", "Veckans utmaning" |

## Färg (Ceder)

| Roll | Token | Ceder | Valnöt | Användning |
| --- | --- | --- | --- | --- |
| bakgrund | `--bg` | #f6efe4 | #1c1511 | |
| yta | `--surface` / `-2` / `-3` | #fbf7f0 / #ede3d3 / #e1d4bf | #241b16 / #2e231c / #3a2d24 | |
| text | `--text` | #3b2a1e | #f1e6d2 | |
| dämpad | `--muted` | #6b5646 | #c9b8a0 | |
| svag | `--faint` | **#756354** (var #8a7563) | #9c8a74 | klara steg, tidsstämplar |
| linje | `--line` | #d3c3ab | #4a3a2e | dekorativa linjer, aldrig ensam kontroll-kant |
| handling | `--ember` | **#985527** (var #b5652e) | #d4a24c | huvudknapp, ackordnamn, kicker |
| på handling | `--ember-ink` | #fff8ef | #2a1c08 | |
| fokus | `--brass` | #9a5426 | #e3bd6e | fokusring 2 px |

Uppmätta kontraster (WCAG, beräknade på tokenparen):

| Par | Tema | Före | Efter | Krav |
| --- | --- | --- | --- | --- |
| text / bg | ceder | 11.97 | 11.97 | 4.5 |
| ember (liten text) / bg | ceder | 3.78 | 5.02 | 4.5 |
| ember / surface-2 | ceder | — | 4.51 | 4.5 |
| ember-ink / ember (knapptext) | ceder | 4.10 | 5.44 | 4.5 |
| faint / bg | ceder | 3.83 | 5.01 | 4.5 |
| fokus / bg | ceder | 5.01 | 5.01 | 3 |
| ember-ink / ember | valnöt | 7.15 | 7.15 | 4.5 |

Tillstånd visas aldrig bara med färg (klara steg har bock, aktuellt steg fet text).

## Avstånd

Skala som används: 2, 4, 6, 8, 10, 12, 14, 16, 18, 22, 24, 28, 32, 40, 48.
- Inuti en grupp: 4–8. Mellan objekt: 10–14. Mellan grupper: 22–32. Kolumngap på dator: 40.
- Idag på dator: hela sidan följer två kolumner `1.25fr | 1fr` med 40 px gap, så att hand/låtlista och dagens mål/veckans utmaning delar kanter.

## Former

Radier: 3 (bandet), 10–12 (små knappar, rader), 14 (`--r` i ceder/valnöt, huvudknapp), 18 (handvideo dator), 999 (chips).
Skuggor: inga på knappar i teman (borttagna medvetet).

## Rörelse

| Token | Värde | Roll |
| --- | --- | --- |
| `--m-press-down` | 80 ms, `--m-ease-out` | knapp trycks ner |
| `--m-release` | 520 ms, `--m-spring-pop` | fjädrande retur (~1 % överslag i skala) |
| `--m-hover` | 200 ms, `--m-ease-color` | hover med mus |
| `--m-focus` | 160 ms | fokusringen glider in |
| `--m-spring-snap` | ≈3 % överslag | indikatorer, reglage |

- Alla knappar, `summary` och länkar får tryckrespons från skriptet i `index.html` (better-design-blocket): djup ≈ 4 px oavsett storlek (`1 − 8/(b+h)`, 0.90–0.985), ljusa ytor mörknar till 0.9, fyllda knappar ljusnar till 1.12, minst 90 ms. Det använder Web Animations, så komponenternas egna `transition` behöver inte röras.
- Minskad rörelse: ingen skalning, fyllningssteget finns kvar.
- Aldrig `transition: all`.

## Kända motsägelser (inte rättade i den här grenen)

- `.h2-hero`, `.h2-mission` m.fl. definieras om i 3–4 lager (gamla omdesigner ligger kvar under nyare). Nya värden ska läggas i ett ställe; städa lagren vid nästa större ändring.
- Toppmenyn (`nav.tabs`) använder `role="tab"`/`aria-selected` fast den är navigering; borde vara länkar/knappar med `aria-current="page"`, som bottenmenyn.
- Grundpaletten i `:root` (neon) används inte av något tema.
