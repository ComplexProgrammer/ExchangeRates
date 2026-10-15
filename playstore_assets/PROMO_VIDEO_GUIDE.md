# Google Play Promo Video Qo'llanmasi & YouTube Sozlamalari

Google Play Console'ning **"Store presence" -> "Main store listing" -> "Video"** bo'limida ilovangiz uchun YouTube video havolasini kiritish talabi va tavsiyalari mavjud.

---

## 📌 1. Google Play Console Rasmiy Talablari

> **Google Play talabi:**
> *"Add a video by entering a YouTube URL. This video must be public or unlisted, ads must be turned off, and it must not be age restricted."*

1. **Format & Havola**: Video faqat YouTube platformasiga yuklangan bo'lishi va standart havola shaklida kiritilishi lozim (masalan: `https://youtu.be/VIDEO_ID` yoki `https://www.youtube.com/watch?v=VIDEO_ID`).
2. **Maxfiylik (Visibility)**: Video **Public** (Ommaviy) yoki **Unlisted** (Havola orqali ochiq) bo'lishi shart. *Private (Maxfiy)* qilib qo'yilsa, Google Play xatolik beradi.
3. **Reklamalar (Monetization)**: Videoda monetizatsiya (reklamalar) mutlaqo **o'chirilgan (OFF)** bo'lishi kerak.
4. **Yosh chegarasi (Age Restriction)**: Videoga yosh chegarasi qo'yilmagan bo'lishi shart (*"Not age-restricted"*).
5. **Namoyish etilishi**: Do'kon sahifasida video Asosiy grafik (Feature graphic) o'rnida yoki skrinshotlar qatorining eng boshida "Play ▶" tugmasi bilan chiroyli ko'rinishda namoyish etiladi.

---

## 🎬 2. Tayyorlangan Video Assetlari

| Asset | Fayl formati | O'lchami | Saqlangan joyi |
| :--- | :--- | :--- | :--- |
| **Promo Video** | MP4 (H.264 / Full HD) | `1920 x 1080 px` (16:9, 30fps) | [`playstore_assets/promo_video_1080p.mp4`](file:///d:/My%20Projects/ExchangeRates/playstore_assets/promo_video_1080p.mp4) |
| **YouTube Muqovasi (Thumbnail)** | PNG | `1280 x 720 px` (16:9) | [`playstore_assets/youtube_thumbnail_1280x720.png`](file:///d:/My%20Projects/ExchangeRates/playstore_assets/youtube_thumbnail_1280x720.png) |

---

## 🚀 3. YouTube'ga Yuklash Bo'yicha Bosqichma-bosqich Ko'rsatma

### 1-qadam: Videoni yuklash
1. [YouTube Studio](https://studio.youtube.com) sahifasiga kiring.
2. **"Create" (Yaratish) -> "Upload videos" (Video yuklash)** tugmasini bosing.
3. [`playstore_assets/promo_video_1080p.mp4`](file:///d:/My%20Projects/ExchangeRates/playstore_assets/promo_video_1080p.mp4) faylini tanlang.

### 2-qadam: Sarlavha va Tavsifni to'ldirish
Quyidagi tayyor matnlardan nusxa oling:

#### Video Sarlavhasi (Title):
```text
Valyuta Kurslari: O'zbekiston — Valyuta Kurslari & Konverter
```
*(Yoki Inglizcha: `Exchange Rates: Uzbekistan — Rates & Currency Converter`)*

#### Video Tavsifi (Description):
```text
"Valyuta Kurslari: O'zbekiston" ilovasi — O'zbekistonda so'mga nisbatan barcha xalqaro valyutalar kurslarini kuzatish, tezkor konvertatsiya qilish va dinamik grafiklar orqali tahlil qilish uchun qulay yordamchingiz.

📲 Google Play Store'dan yuklab oling:
https://play.google.com/store/apps/details?id=exchangerates.complexprogrammer.uz

Asosiy imkoniyatlar:
• O'zbekiston Markaziy bankining kunlik rasmiy kurslari (USD, EUR, RUB va 70+ valyutalar)
• Tezkor va aniq valyuta kalkulyatori / konverteri
• 7 kunlik, 30 kunlik va 1 yillik interaktiv tarixiy grafiklar
• Belgilangan narx bo'yicha aqlli kurs signallari (push xabarnomalar)
• Qulay asosiy ekran vidjeti va oflayn rejim

⚠️ OGOHLANTIRISH (DISCLAIMER):
Ushbu ilova O'zbekiston Respublikasi Markaziy banki yoki boshqa davlat organlari nomidan ish olib bormaydi va davlat tashkilotini ifodalamaydi. Ilova Markaziy bankning ommaviy ochiq ma'lumotlar bazasidan foydalangan holda yaratilgan mustaqil vositadir.
Rasmiy manbalar: https://cbu.uz | https://gov.uz
```

#### Video Teglari (Tags):
```text
valyuta kursi, markaziy bank, dollar kursi, kurslar, cbu uz, uzbekistan exchange rates, valyuta konverter, dollar kursi bugun, valyuta kurslari uzbekistan
```

### 3-qadam: Muqova (Thumbnail) qo'yish
* **"Upload thumbnail"** tugmasini bosib, [`playstore_assets/youtube_thumbnail_1280x720.png`](file:///d:/My%20Projects/ExchangeRates/playstore_assets/youtube_thumbnail_1280x720.png) faylini yuklang.

### 4-qadam: Majburiy Sozlamalar (Muhim!)
* **Audience (Auditoriya)**: `No, it's not made for kids` (Bolalar uchun emas) ni tanlang.
* **Age restriction**: `No, don't restrict my video to viewers over 18 only` (18 yosh chegarasi qo'yilmasin).
* **Monetization (Monetizatsiya)**: Agar kanalingizda monetizatsiya yoqilgan bo'lsa, ushbu video uchun uni **OFF** holatiga o'tkazing (reklamalar ko'rinmasligi shart).
* **Visibility (Ko'rinishi)**: **Unlisted** (Faqat havola orqali) yoki **Public** (Ommaviy) ni tanlang.

### 5-qadam: Google Play Console'ga ulash
1. YouTube videosi yuklangach, uning havolasini nusxalang (masalan: `https://youtu.be/AbCdEf12345`).
2. Google Play Console -> **Grow -> Store presence -> Main store listing** sahifasiga kiring.
3. **"Video"** bo'limidagi **"YouTube video"** qatoriga havolani joylashtiring.
4. **"Save" (Saqlash)** tugmasini bosing!
