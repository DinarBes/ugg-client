// Grants (or revokes) access to the admin app: `isAdmin: true` in the user's users/{uid}.
// The same as ticking the field in the Firebase console, by phone number instead of uid.
//
//   npm run set-admin -- +79001234567            grant
//   npm run set-admin -- +79001234567 --revoke   revoke
//   npm run set-admin -- --list                  who has it now
//
// The number must have signed in at least once (in either app), so that the account exists.
// The admin app picks the change up with «Проверить ещё раз».
// Needs service-account.json next to this script, the same key as for `npm run seed`.

import { readFileSync, existsSync } from "node:fs";
import { initializeApp, cert } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

const keyPath = new URL("./service-account.json", import.meta.url);
if (!existsSync(keyPath)) {
  console.error("Нет service-account.json рядом со скриптом. Скачай ключ: Project settings → Service accounts → Generate new private key.");
  process.exit(1);
}

initializeApp({ credential: cert(JSON.parse(readFileSync(keyPath, "utf8"))) });
const db = getFirestore();
const args = process.argv.slice(2);

if (args.includes("--list")) {
  const admins = await db.collection("users").where("isAdmin", "==", true).get();
  for (const doc of admins.docs) console.log(`  ✓ ${doc.get("phone") || "(без телефона)"}  ${doc.get("name") || ""}  ${doc.id}`);
  console.log(admins.empty ? "Администраторов нет." : `Всего: ${admins.size}`);
  process.exit(0);
}

const phone = args.find((arg) => !arg.startsWith("--"));
const revoke = args.includes("--revoke");

if (!phone || !/^\+\d{10,15}$/.test(phone)) {
  console.error("Укажи номер в формате +79001234567: npm run set-admin -- +79001234567 [--revoke]");
  process.exit(1);
}

let user;
try {
  user = await getAuth().getUserByPhoneNumber(phone);
} catch (error) {
  if (error.code === "auth/user-not-found") {
    console.error(`Аккаунта с номером ${phone} нет. Сначала войди этим номером в приложение.`);
    process.exit(1);
  }
  throw error;
}

await db.doc(`users/${user.uid}`).set(
  revoke ? { isAdmin: FieldValue.delete() } : { isAdmin: true, phone },
  { merge: true },
);
console.log(`${revoke ? "Доступ снят" : "Доступ выдан"}: ${phone} (${user.uid})`);
