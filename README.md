# Open Wire

A native Android app that pulls live headlines (World, Politics, Business,
Tech, Science, Sports, Entertainment) directly from public RSS/Atom feeds
on-device, caches them for offline use, and links out to the original
article for each story. No backend of its own — every headline links to
its real source.

## How it's built

There is no local Android SDK involved. A GitHub Actions workflow
(`.github/workflows/build.yml`) does the build on every push to `main`
(and can be run manually from the Actions tab):

1. Installs JDK 17 and the Android SDK on the runner.
2. Decodes the signing keystore from the `OPENWIRE_KEYSTORE_BASE64` secret.
3. Runs `gradle :app:bundleRelease` to produce a signed `.aab` (the format
   Play Console wants) and `gradle :app:assembleDebug` for a quick-install
   `.apk` you can sideload to test on a phone.
4. Both are uploaded as workflow artifacts — open the finished run under
   the **Actions** tab and download them from the **Artifacts** section
   at the bottom of the run page.

## Required repository secrets

Add these under **Settings → Secrets and variables → Actions → New
repository secret**. The actual values are not in this repo — they were
handed to you separately, since a signing key does not belong in git
history even in a private repo.

| Secret name | What it is |
|---|---|
| `OPENWIRE_KEYSTORE_BASE64` | The upload keystore, base64-encoded |
| `OPENWIRE_KEYSTORE_PASSWORD` | Its store password |
| `OPENWIRE_KEY_ALIAS` | `openwire` |
| `OPENWIRE_KEY_PASSWORD` | Its key password (same as the store password — PKCS12 keystores use one password for both) |

**Back up the original `.jks` keystore file somewhere safe outside of
git** (password manager, encrypted drive). If you ever lose it before
enrolling in Play App Signing, or if you aren't enrolled and lose it
later, you cannot publish updates to this app under the same listing
again — Google would require a new app.

## Uploading to Play Console

1. Create (or sign into) your Google Play Developer account at
   play.google.com/console (one-time $25 fee, identity verification).
2. Create a new app, fill in the store listing (see
   `store-listing/` for drafted text), and set its privacy policy URL.
3. Under **Production → Create new release**, upload the `app-release.aab`
   from the latest Actions run.
4. On first upload, opt into **Play App Signing** when prompted — Google
   then holds the real signing key and you only ever need this upload key
   again for future releases.
5. Complete the content rating questionnaire, target audience, data
   safety form, and ads declaration (this app shows no ads and collects
   no personal data beyond what Android's own crash/diagnostics report).
6. Submit for review. First review for a brand-new app/account commonly
   takes anywhere from a few hours to a few days.

## Feed sources

See `app/src/main/java/com/sriramanappindi/openwire/data/Feeds.kt`. Every
feed is public RSS/Atom; the app only ever shows a headline, a short
excerpt, and a link to the original site — it never reproduces full
article text.
