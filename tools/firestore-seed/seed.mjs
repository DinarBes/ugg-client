// Fills Firestore with the app's catalog and screen texts (the same data as the mocks).
//
//   1. Firebase console → Project settings → Service accounts → Generate new private key,
//      save the file next to this script as service-account.json.
//   2. npm install
//   3. npm run seed            (add --dry-run to only print what would be written)
//
// Every document listed in data.mjs is overwritten as a whole; nothing else is touched.

import { readFileSync, existsSync } from "node:fs";
import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore, GeoPoint } from "firebase-admin/firestore";
import { documents } from "./data.mjs";

const dryRun = process.argv.includes("--dry-run");
const keyPath = new URL("./service-account.json", import.meta.url);

if (dryRun) {
  for (const [path, data] of Object.entries(documents)) console.log(path, JSON.stringify(data, null, 2));
  process.exit(0);
}

if (!existsSync(keyPath)) {
  console.error("Нет service-account.json рядом со скриптом. Скачай ключ: Project settings → Service accounts → Generate new private key.");
  process.exit(1);
}

initializeApp({ credential: cert(JSON.parse(readFileSync(keyPath, "utf8"))) });
const db = getFirestore();

// `{ lat, lng }` in data.mjs becomes a Firestore GeoPoint.
const toFirestore = (value) => {
  if (Array.isArray(value)) return value.map(toFirestore);
  if (value && typeof value === "object") {
    const keys = Object.keys(value);
    if (keys.length === 2 && "lat" in value && "lng" in value) return new GeoPoint(value.lat, value.lng);
    return Object.fromEntries(keys.map((k) => [k, toFirestore(value[k])]));
  }
  return value;
};

const batch = db.batch();
for (const [path, data] of Object.entries(documents)) batch.set(db.doc(path), toFirestore(data));
await batch.commit();

console.log(`Готово: записано документов — ${Object.keys(documents).length}`);
for (const path of Object.keys(documents)) console.log("  ✓", path);
