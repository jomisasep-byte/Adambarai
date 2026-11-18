# MUSIC GUARD AI — MASTER PROJECT BLUEPRINT
## Pembangunan Ulang dari Nol di Google AI Studio

**Versi dokumen:** 1.0  
**Tanggal:** 18 November 2026  
**Platform pembangunan:** Google AI Studio — Build mode — Android  
**Target aplikasi:** Android native, Kotlin, Jetpack Compose  
**Nama produk:** MUSIC GUARD AI

---

# BAGIAN I — ARAH PROYEK BARU

## 1. Keputusan reset proyek

Proyek MUSIC GUARD AI akan dimulai dari nol di Google AI Studio.

Artinya:
- Jangan mengimpor atau melanjutkan repositori Daman.
- Jangan memakai starter Android terdahulu sebagai basis.
- Jangan menyalin struktur proyek lama secara otomatis.
- Jangan menganggap database, API, token, atau fitur dari percakapan lama sudah tersedia.
- Bangun satu proyek Android baru yang bersih.
- Buat fondasi UI dan navigasi terlebih dahulu, lalu tambahkan fungsi secara bertahap.
- Simpan source project hasil AI Studio dan lakukan ekspor ZIP sebagai backup berkala.

**Catatan data:** karena proyek dimulai dari nol, data dari aplikasi lama tidak otomatis terbawa. Jika pengguna kelak ingin mengimpor data lama, lakukan sebagai pekerjaan migrasi terpisah setelah format data diperiksa. Jangan mengklaim data lama sudah dipindahkan.

## 2. Ringkasan produk

MUSIC GUARD AI adalah aplikasi pendamping kreator konten untuk:
1. memilih file video atau audio;
2. memeriksa metadata dan keberadaan audio;
3. menyiapkan sampel audio untuk pengenalan musik;
4. mengirim sampel ke penyedia pengenalan musik resmi yang dikonfigurasi;
5. menampilkan kandidat lagu yang ditemukan;
6. membantu pengguna mencatat dan meninjau bukti lisensi;
7. menyimpan riwayat pemeriksaan;
8. menghasilkan laporan yang bisa disimpan atau dibagikan.

Tujuan utamanya adalah membantu pengguna melakukan pemeriksaan awal sebelum video dipublikasikan.

## 3. Batasan yang tidak boleh dilanggar

MUSIC GUARD AI:
- bukan YouTube Content ID;
- bukan sistem resmi pemeriksaan copyright YouTube;
- tidak bisa menjamin video bebas klaim, blokir, demonetisasi, atau sengketa;
- tidak dapat memastikan hak penggunaan hanya dari pengenalan judul lagu;
- tidak boleh menyebut hasil “100% aman” atau “pasti lolos copyright”.

Pengenalan musik seperti AudD hanya mengidentifikasi kemungkinan lagu dari sampel yang dikirim. Hasil pengenalan tidak sama dengan izin lisensi.

Tidak ditemukannya lagu pada sampel bukan bukti audio bebas hak cipta. Pengguna tetap harus memeriksa sumber musik, bukti lisensi, syarat penggunaan, dan YouTube Studio Checks.

Jangan membuat fitur manipulasi pitch, tempo, noise, equalizer, atau karakter audio dengan tujuan menghindari fingerprinting atau deteksi copyright. Fitur audio hanya untuk identifikasi dan pemeriksaan yang sah.

---

# BAGIAN II — GOOGLE AI STUDIO

## 4. Cara memulai

1. Buka Google AI Studio.
2. Masuk ke **Build mode**.
3. Pilih platform **Android** pada platform picker, bukan Web.
4. Tempel PROMPT UTAMA pada Bagian XII dokumen ini.
5. Biarkan agent membuat proyek native Android baru.
6. Periksa hasil di emulator.
7. Lanjutkan menggunakan prompt tahap demi tahap di Bagian XIII.
8. Ekspor source project ZIP secara berkala untuk backup dan pengembangan lanjutan.

Dokumentasi Google yang relevan:
- Build mode: https://ai.google.dev/gemini-api/docs/aistudio-build-mode
- Android di AI Studio: https://ai.google.dev/gemini-api/docs/aistudio-android
- Panduan Android native di AI Studio: https://developer.android.com/blog/posts/build-native-android-apps-in-google-ai-studio

**Penting:** kemampuan dan ketersediaan fitur Google AI Studio dapat berubah atau diluncurkan bertahap. Minta agent memeriksa pilihan platform dan kemampuan yang benar-benar tersedia di workspace pengguna. Jangan mengarang tombol atau fitur yang tidak terlihat.

## 5. Arsitektur yang dituju

Target utama adalah aplikasi Android native:
- Kotlin;
- Jetpack Compose;
- Material 3;
- satu Activity utama;
- navigasi antarhalaman Compose;
- ViewModel dan repository seperlunya;
- penyimpanan lokal untuk riwayat dan metadata;
- Android Storage Access Framework untuk memilih dan menyimpan file.

Gunakan arsitektur sesederhana mungkin namun terpisah tanggung jawabnya.

### Catatan backend dan keamanan

Aplikasi Android native tidak boleh menyimpan secret provider pihak ketiga secara terbuka di APK.

Untuk integrasi yang memerlukan credential rahasia:
- gunakan backend HTTPS atau layanan backend terkelola yang benar-benar tersedia dan telah dikonfigurasi;
- atau gunakan integrasi resmi Firebase AI Logic/App Check jika fitur tersebut tersedia di workspace dan cocok untuk panggilan Gemini;
- jangan menganggap Firebase AI Logic otomatis dapat memanggil AudD atau API pengenalan musik pihak ketiga;
- untuk AudD atau provider lain, gunakan backend proxy yang menyimpan token secara server-side;
- jika backend belum tersedia, buat UI konfigurasi placeholder yang aman dan jangan pura-pura request API sudah berjalan.

Jika Google AI Studio menyediakan kemampuan full-stack Android/Firebase baru pada akun pengguna, agent harus memverifikasi dan menjelaskan apa yang dapat dipakai untuk kebutuhan ini. Jangan menaruh credential pihak ketiga di client.

---

# BAGIAN III — IDENTITAS VISUAL

## 6. Tema desain

**Gaya:** futuristic cyber security / AI studio dashboard.

Palet:
- background utama: midnight navy atau hampir hitam;
- surface: navy gelap bertingkat;
- aksen primer: cyan elektrik;
- aksen sekunder: violet/ungu;
- warna sukses: hijau lembut;
- peringatan: amber/kuning;
- error: merah yang jelas;
- teks utama: putih lembut;
- teks sekunder: abu-abu kebiruan.

### Prinsip UI
- modern, rapi, profesional, dan mudah dibaca;
- hindari terlalu banyak glow;
- hindari font kecil;
- kartu dan tombol konsisten;
- animasi halus, tidak berlebihan;
- tidak mengorbankan performa;
- kontras teks memadai;
- semua status memiliki label teks, jangan mengandalkan warna saja;
- dukung ukuran layar ponsel yang berbeda;
- sediakan empty state, loading state, error state, dan success state.

### Identitas merek
- Nama: MUSIC GUARD AI
- Tagline: “Know the music. Check the rights.”
- Deskripsi Indonesia: “Kenali musik, periksa izin, dan dokumentasikan hasil sebelum publikasi.”

---

# BAGIAN IV — FITUR APLIKASI

## 7. Navigasi utama

Gunakan bottom navigation atau pola navigasi native yang sesuai dengan desain:
1. Dashboard
2. Scan
3. History
4. Reports
5. Settings

Jangan membuat tombol yang tidak bekerja. Jika fitur belum diimplementasikan, tampilkan status “Segera tersedia” dengan jelas atau jangan tampilkan sebagai fitur aktif.

## 8. Dashboard

Tampilkan:
- logo/nama MUSIC GUARD AI;
- tagline;
- status layanan pengenalan musik;
- tombol “Scan Video”;
- tombol “Scan Audio”;
- jumlah pemeriksaan;
- jumlah hasil yang perlu diverifikasi;
- pemeriksaan terbaru;
- informasi batasan pemeriksaan copyright.

Jangan tampilkan statistik palsu. Jika belum ada data, gunakan nilai kosong/0 yang benar.

## 9. Scan Video / Scan Audio

Alur:
1. Pengguna memilih file menggunakan Android picker.
2. Aplikasi menampilkan nama, ukuran, tipe MIME, dan durasi bila tersedia.
3. Aplikasi memeriksa apakah file dapat dibaca.
4. Untuk video, aplikasi memeriksa apakah ada track audio.
5. Aplikasi menyiapkan sampel atau segmen audio.
6. Aplikasi meminta konfirmasi sebelum mengirim sampel ke layanan eksternal jika diperlukan.
7. Aplikasi menjalankan request API.
8. Aplikasi menampilkan progress berdasarkan tahap yang benar-benar berjalan.
9. Pengguna dapat membatalkan proses.
10. Hasil disimpan setelah proses selesai.

Validasi:
- URI tidak dapat diakses;
- file kosong;
- format tidak didukung;
- video tanpa audio;
- file terlalu besar;
- durasi tidak terbaca;
- codec tidak didukung;
- jaringan tidak tersedia;
- API timeout;
- API menolak request.

Jangan mengubah file sumber. Gunakan cache privat untuk file sementara dan bersihkan setelah proses.

## 10. Ekstraksi audio dan segmentasi

Untuk video:
- gunakan API media Android yang sesuai, misalnya MediaExtractor/MediaMuxer bila cocok;
- baca track audio;
- jangan mengubah video asli;
- siapkan sampel audio dalam format yang diterima provider.

Untuk audio:
- baca URI melalui ContentResolver;
- jangan mengasumsikan URI adalah filesystem path;
- validasi ukuran dan format;
- siapkan file sementara bila diperlukan.

Untuk media panjang:
- jangan hanya mengambil bagian awal lalu menyebut seluruh video telah diperiksa;
- gunakan segmentasi sesuai dokumentasi provider;
- simpan timestamp mulai/akhir setiap segmen;
- tampilkan cakupan pemeriksaan yang sebenarnya;
- jika hanya satu sampel diperiksa, tampilkan dengan jelas “Sampel yang diperiksa: ...”.

Batas ukuran, durasi, format, dan request harus mengikuti dokumentasi provider terkini, bukan angka tebakan.

## 11. Pengenalan musik

Provider awal yang direncanakan: **AudD**, tetapi integrasi hanya boleh diaktifkan setelah token dan konfigurasi yang sah tersedia.

Dokumentasi:
- https://docs.audd.io/
- Endpoint API yang tercatat: https://api.audd.io/

Agent harus memeriksa dokumentasi terbaru sebelum membuat request. Jangan mengarang parameter, batas ukuran, format, atau skema JSON.

Rancang adapter agar provider dapat diganti:

```text
RecognitionProvider
  ├── recognize(sample)
  └── RecognitionResult

AuddRecognitionProvider
  ├── request
  ├── parse response
  └── map error
```

Informasi kandidat lagu yang dapat ditampilkan jika benar-benar diberikan API:
- judul;
- artis;
- album;
- label;
- tautan penyedia musik;
- timestamp segmen yang cocok;
- confidence jika provider memang mengembalikannya.

Status API wajib dibedakan:
- berhasil menemukan kandidat;
- tidak ada kecocokan;
- token belum dikonfigurasi;
- token salah;
- kuota/rate limit;
- timeout;
- jaringan terputus;
- file tidak didukung;
- respons tidak valid;
- kesalahan server.

API gagal atau no-match tidak boleh dianggap sebagai hasil “aman”.

## 12. Pemeriksaan lisensi

Pengenalan lagu tidak membuktikan izin. Sediakan formulir pencatatan lisensi manual:

- sumber musik;
- jenis lisensi;
- pemilik/pemberi lisensi bila diketahui;
- platform yang diizinkan;
- izin monetisasi;
- wilayah;
- masa berlaku;
- kewajiban atribusi;
- URL lisensi;
- file/dokumen bukti;
- catatan pengguna.

Aplikasi hanya boleh menyebut bukti “dicatat” atau “ditinjau pengguna” kecuali memang ada integrasi verifikasi resmi yang dapat membuktikannya.

## 13. Kategori hasil

Gunakan label yang transparan:

| Label | Arti | Tindakan |
|---|---|---|
| PERLU VERIFIKASI | Kandidat lagu ditemukan, izin belum dibuktikan | Periksa lisensi dan cakupan izin |
| BELUM DIKETAHUI | Tidak ada kecocokan pada sampel atau data tidak cukup | Periksa sumber audio secara manual |
| PERLU PERHATIAN | Ada indikasi izin tidak tersedia/tidak sesuai berdasarkan informasi yang dimasukkan | Jangan gunakan sebelum hak penggunaan jelas |
| PEMERIKSAAN GAGAL | Proses atau API gagal | Perbaiki penyebab lalu ulangi |

Jangan menggunakan label “AMAN 100%”. Jika pengguna memasukkan bukti lisensi, tampilkan “Bukti lisensi dicatat” dan rincian cakupannya. Tetap beri catatan bahwa ini bukan jaminan bebas klaim YouTube.

## 14. History

Simpan:
- ID pemeriksaan;
- nama file;
- tipe media;
- ukuran dan durasi;
- tanggal/waktu pemeriksaan;
- cakupan sampel/segmen;
- kandidat lagu;
- timestamp;
- status;
- catatan lisensi;
- detail error jika ada.

Fitur:
- daftar pemeriksaan terbaru;
- detail pemeriksaan;
- pencarian/filter sederhana;
- hapus satu catatan;
- hapus seluruh riwayat dengan dialog konfirmasi;
- ekspor/cadangkan data bila memungkinkan.

Gunakan Room/SQLite atau opsi penyimpanan lokal yang cocok dengan proyek. Jangan simpan file media asli tanpa kebutuhan yang jelas. Riwayat lokal tidak boleh hilang saat aplikasi ditutup.

## 15. Reports

Sediakan:
- ekspor Markdown `.md`;
- ekspor JSON `.json`;
- share sheet Android;
- simpan file melalui ACTION_CREATE_DOCUMENT/Storage Access Framework.

Isi laporan:
- nama aplikasi;
- ID pemeriksaan;
- waktu pemeriksaan;
- nama file dan tipe media;
- durasi media;
- segmen yang benar-benar diperiksa;
- kandidat lagu;
- timestamp;
- metadata API;
- status lisensi;
- bukti/URL yang dicatat;
- alasan status;
- error bila ada;
- rekomendasi langkah selanjutnya;
- disclaimer bahwa laporan bukan keputusan YouTube Content ID.

Jangan membuat laporan yang menyatakan proses berhasil jika API gagal.

## 16. Settings

Sediakan:
- status provider;
- konfigurasi koneksi yang aman;
- petunjuk token/backend;
- preferensi tema jika diperlukan;
- kebijakan privasi;
- tentang aplikasi;
- versi aplikasi;
- batasan hasil.

Jangan menyimpan secret di source code atau BuildConfig client untuk aplikasi yang akan dibagikan secara publik.

---

# BAGIAN V — MODEL DATA MINIMAL

## 17. Entitas data

Rancangan konseptual (agent boleh menyesuaikan):

### ScanRecord
- id: String
- fileName: String
- mimeType: String?
- fileSizeBytes: Long?
- durationMs: Long?
- createdAt: Long
- scanStatus: String
- scanCoverageDescription: String
- errorMessage: String?

### TrackMatch
- id: String
- scanId: String
- title: String?
- artist: String?
- album: String?
- label: String?
- externalUrl: String?
- segmentStartMs: Long?
- segmentEndMs: Long?
- providerName: String

### LicenseEvidence
- id: String
- scanId: String
- sourceName: String?
- licenseType: String?
- allowedPlatforms: String?
- monetizationAllowed: Boolean?
- territory: String?
- validUntil: Long?
- attributionRequired: Boolean?
- licenseUrl: String?
- notes: String?

### ScanReport
- id: String
- scanId: String
- format: String
- createdAt: Long
- exportedUri: String?

Jangan menyimpan file media besar di database. Simpan metadata dan URI yang relevan, dengan memperhatikan izin akses URI.

---

# BAGIAN VI — STRUKTUR PROYEK YANG DIHARAPKAN

## 18. Struktur konseptual

Struktur akhir harus mengikuti batasan generator AI Studio. Jika Android Build mode menghasilkan single-activity/single-module, pertahankan bentuk itu dan organisasikan package di dalamnya.

Contoh:

```text
app/
└── src/main/
    ├── AndroidManifest.xml
    ├── java/<package-name>/
    │   ├── MainActivity.kt
    │   ├── navigation/
    │   ├── ui/
    │   │   ├── dashboard/
    │   │   ├── scan/
    │   │   ├── result/
    │   │   ├── history/
    │   │   ├── reports/
    │   │   ├── settings/
    │   │   └── theme/
    │   ├── data/
    │   │   ├── local/
    │   │   ├── model/
    │   │   └── repository/
    │   ├── media/
    │   ├── recognition/
    │   ├── licensing/
    │   └── export/
    └── res/
```

Ini adalah gambaran, bukan kewajiban untuk membuat file kosong berlebihan. Utamakan kode yang dapat dibangun dan mudah dirawat.

---

# BAGIAN VII — PRIVASI DAN KEAMANAN

## 19. Aturan wajib

- Minta tindakan pengguna sebelum mengirim sampel ke pihak ketiga.
- Jelaskan sampel apa yang dikirim, untuk tujuan apa, dan ke provider mana.
- Gunakan HTTPS.
- Jangan mencatat token atau audio ke log.
- Jangan menaruh API secret provider pihak ketiga dalam APK publik.
- Gunakan backend proxy/secret manager untuk token pihak ketiga.
- Terapkan batas ukuran dan durasi.
- Terapkan timeout dan pembatalan.
- Bersihkan file cache sementara.
- Jangan mengunggah file penuh jika sampel cukup.
- Jangan menyimpan media pengguna tanpa alasan dan persetujuan.
- Sediakan kebijakan retensi data yang jelas.

Jika menggunakan Firebase:
- gunakan hanya layanan yang benar-benar tersedia dan cocok;
- Firebase AI Logic untuk akses model Gemini bukan pengganti otomatis API pengenalan musik AudD;
- periksa Authentication, App Check, Security Rules, dan akses database;
- jangan membuat database publik tanpa aturan keamanan.

---

# BAGIAN VIII — PENGUJIAN DAN KRITERIA SELESAI

## 20. Test case wajib

### UI
- Dashboard terbuka.
- Semua navigasi bekerja.
- Empty state benar.
- Loading dan error state jelas.
- Layout responsif.

### Media
- Pilih audio valid.
- Pilih video dengan audio.
- Pilih video tanpa audio.
- File kosong/rusak.
- File tidak didukung.
- URI akses ditolak.
- File besar.
- Batalkan proses.

### API
- Token belum diatur.
- Token salah.
- Jaringan mati.
- Timeout.
- No-match.
- Match ditemukan.
- Rate limit.
- Respons rusak.

### Data
- Simpan hasil.
- Tutup dan buka ulang aplikasi.
- Riwayat tetap ada.
- Hapus satu item.
- Hapus semua dengan konfirmasi.
- Ekspor laporan.
- Buka kembali laporan hasil ekspor.

### Privasi
- Tidak ada secret dalam APK/source client.
- Tidak ada upload tanpa tindakan pengguna.
- File sementara dibersihkan.
- Log tidak memuat credential atau isi audio.

## 21. Definisi selesai

Proyek dianggap siap untuk tahap pengujian ketika:
1. proyek Android native berhasil dibuat di AI Studio;
2. UI dan navigasi berfungsi;
3. pemilih file berfungsi;
4. validasi media berfungsi;
5. integrasi API benar-benar berjalan jika credential/backend tersedia;
6. hasil no-match dan error ditampilkan dengan benar;
7. history persisten;
8. ekspor laporan berfungsi;
9. build berhasil;
10. source project dapat diekspor;
11. keterbatasan yang belum diselesaikan ditulis dengan jujur.

Jangan menyebut aplikasi siap produksi sebelum keamanan, privasi, kompatibilitas, dan pengujian perangkat nyata diselesaikan.

---

# BAGIAN IX — TAHAPAN KERJA

## 22. Roadmap

### Tahap 1 — Fondasi aplikasi
- Buat proyek Android native baru.
- Buat tema cyber.
- Buat navigasi.
- Buat Dashboard, Scan, History, Reports, Settings.
- Gunakan data contoh hanya untuk preview dan beri label demo.

### Tahap 2 — Media picker
- Integrasi Android picker.
- Metadata file.
- Validasi video/audio.
- Tangani file tanpa audio.
- Progress proses.

### Tahap 3 — Media preparation
- Ekstraksi audio video.
- Persiapan sampel.
- Segmentasi bila diperlukan.
- Timestamp.
- Cleanup cache.

### Tahap 4 — Recognition provider
- Implementasi adapter.
- Backend/credential aman.
- Request API.
- Parsing respons.
- Error handling.

### Tahap 5 — Lisensi
- Form bukti lisensi.
- Status dan penjelasan.
- Detail hasil.

### Tahap 6 — History dan Reports
- Database lokal.
- Persistensi.
- Ekspor Markdown/JSON.
- Share/save.

### Tahap 7 — QA dan APK
- Build.
- Uji emulator.
- Uji perangkat Android.
- Perbaikan crash.
- Ekspor ZIP.
- APK/AAB sesuai opsi yang tersedia.

---

# BAGIAN X — REFERENSI

## 23. Dokumentasi resmi

- Google AI Studio Build mode: https://ai.google.dev/gemini-api/docs/aistudio-build-mode
- Google AI Studio Android: https://ai.google.dev/gemini-api/docs/aistudio-android
- Android native apps in AI Studio: https://developer.android.com/blog/posts/build-native-android-apps-in-google-ai-studio
- AudD API docs: https://docs.audd.io/
- YouTube Content ID overview: https://support.google.com/youtube/answer/2797370
- YouTube copyright claims: https://support.google.com/youtube/answer/6013276

Dokumentasi dan kemampuan platform dapat berubah. Periksa informasi resmi terbaru saat implementasi.

---

# BAGIAN XI — INSTRUKSI PENGGUNA

## 24. Cara menggunakan file ini

1. Buka Google AI Studio.
2. Masuk ke Build mode.
3. Pilih Android.
4. Salin PROMPT UTAMA dari Bagian XII.
5. Tempel prompt ke kolom instruksi.
6. Jalankan pembuatan proyek baru.
7. Setelah proyek muncul, gunakan prompt lanjutan pada Bagian XIII satu per satu.
8. Simpan/export ZIP setelah fondasi berhasil dan setiap milestone besar.

Tidak perlu mengunggah proyek Daman atau starter Android lama. Kita memang memulai proyek baru. Jika suatu saat ingin memakai data lama, lakukan migrasi secara terpisah.

---

# BAGIAN XII — PROMPT UTAMA GOOGLE AI STUDIO

**Salin seluruh teks di dalam blok prompt berikut ke Google AI Studio Build mode setelah memilih Android.**

```text
Bertindaklah sebagai Senior Android Engineer, Kotlin Developer, Jetpack Compose UI/UX Designer, Software Architect, dan QA Engineer.

BANGUN APLIKASI ANDROID NATIVE BARU DARI NOL bernama MUSIC GUARD AI.

PENTING:
- Ini adalah proyek baru. Jangan mengimpor atau melanjutkan proyek lama.
- Jangan membuat aplikasi web. Saya memilih platform Android.
- Gunakan Kotlin dan Jetpack Compose sesuai kemampuan Google AI Studio Android Build mode.
- Buat source code nyata yang dapat dibangun, bukan hanya desain mockup.
- Jangan mengklaim fitur aktif jika belum diimplementasikan.
- Jika ada integrasi yang membutuhkan token atau backend, buat arsitektur dan UI yang benar, lalu jelaskan konfigurasi yang masih diperlukan.
- Jangan menaruh API secret pihak ketiga di APK.
- Jangan membuat fitur untuk menghindari copyright detection.

NAMA:
MUSIC GUARD AI

TAGLINE:
Know the music. Check the rights.

TUJUAN:
Aplikasi membantu kreator YouTube melakukan pemeriksaan awal musik pada video/audio. Pengguna memilih media, aplikasi memeriksa metadata dan audio, menyiapkan sampel, mengenali kandidat lagu melalui API resmi yang dikonfigurasi, mencatat bukti lisensi, menyimpan riwayat, dan mengekspor laporan.

BATASAN:
Aplikasi ini bukan YouTube Content ID dan tidak menjamin bebas copyright, bebas klaim, atau lolos monetisasi. Pengenalan lagu tidak membuktikan lisensi. No-match bukan bukti bebas hak cipta. Selalu tampilkan disclaimer dan anjurkan verifikasi lisensi serta YouTube Studio Checks.

DESAIN:
Buat tampilan cyber futuristik yang premium, profesional, dan nyaman digunakan:
- background midnight navy/hitam;
- aksen cyan elektrik dan violet;
- kartu gelap dengan border halus;
- typography jelas;
- icon konsisten;
- animasi transisi dan loading yang ringan;
- efek glow secukupnya;
- layout responsif;
- status selalu memiliki teks, bukan warna saja.
Jangan membuat UI berlebihan atau sulit dibaca.

NAVIGASI:
1. Dashboard
2. Scan
3. History
4. Reports
5. Settings

DASHBOARD:
- branding MUSIC GUARD AI;
- status layanan pengenalan;
- tombol Scan Video;
- tombol Scan Audio;
- total pemeriksaan;
- hasil yang perlu diverifikasi;
- riwayat terbaru;
- disclaimer ringkas.
Jangan tampilkan angka statistik palsu.

SCAN:
- gunakan Android Storage Access Framework/document picker;
- pengguna dapat memilih file video/audio;
- tampilkan nama file, ukuran, tipe, dan durasi bila tersedia;
- validasi file dan akses URI;
- untuk video periksa keberadaan track audio;
- jangan menganggap URI adalah path file;
- jangan mengubah file asli;
- sediakan progress, cancel, loading, error, dan retry.

MEDIA PROCESSING:
- gunakan Android media APIs yang kompatibel untuk membaca track audio video;
- siapkan sampel audio sesuai spesifikasi provider;
- untuk media panjang, gunakan segmentasi bila diperlukan;
- simpan timestamp tiap segmen;
- tampilkan bagian yang benar-benar diperiksa;
- jangan mengklaim seluruh video diperiksa jika hanya sampel awal;
- file sementara disimpan di cache privat dan dibersihkan setelah selesai/gagal/batal.

MUSIC RECOGNITION:
Provider awal yang direncanakan adalah AudD.
Dokumentasi resmi: https://docs.audd.io/
Endpoint yang tercatat: https://api.audd.io/

Sebelum coding integrasi, periksa dokumentasi AudD terkini. Jangan menebak parameter, batas file, format, atau struktur JSON.
Buat interface RecognitionProvider dan adapter AudD terpisah.
Tangani:
- match;
- no match;
- token belum dikonfigurasi;
- token invalid;
- rate limit/kuota;
- timeout;
- network error;
- unsupported media;
- malformed response;
- server error.

Jika token belum tersedia, jangan pura-pura API berjalan. Buat status konfigurasi yang jelas dan berikan petunjuk.
Jangan menyimpan token provider pihak ketiga secara terbuka di APK. Gunakan backend HTTPS/secret manager yang sesuai. Jangan menganggap Firebase AI Logic untuk Gemini otomatis menggantikan API AudD.

HASIL:
Tampilkan kandidat lagu jika benar-benar dikembalikan API:
- title;
- artist;
- album;
- label;
- external link;
- timestamp segmen.
Jangan mengarang metadata atau confidence.

KATEGORI STATUS:
- PERLU VERIFIKASI: kandidat lagu ditemukan, izin belum dibuktikan.
- BELUM DIKETAHUI: no-match pada sampel atau data belum cukup.
- PERLU PERHATIAN: data yang dimasukkan menunjukkan izin mungkin tidak tersedia/tidak sesuai.
- PEMERIKSAAN GAGAL: proses/API gagal.
Jangan menggunakan label 100% AMAN.
Bukti lisensi hanya boleh dicatat atau ditinjau berdasarkan dokumen dan informasi pengguna.

LICENSE EVIDENCE:
Buat form untuk:
- sumber musik;
- jenis lisensi;
- platform yang diizinkan;
- monetisasi;
- wilayah;
- masa berlaku;
- atribusi;
- URL/dokumen bukti;
- catatan.

HISTORY:
Gunakan database lokal yang persisten.
Simpan ID, nama file, tipe, ukuran, durasi, waktu pemeriksaan, cakupan sampel, kandidat lagu, timestamp, status, dan error.
Riwayat harus tetap ada setelah aplikasi ditutup.
Sediakan detail, pencarian/filter sederhana, hapus item, dan hapus semua dengan konfirmasi.

REPORTS:
Sediakan ekspor Markdown (.md) dan JSON (.json) melalui Android Storage Access Framework atau share sheet.
Laporan berisi waktu, file, cakupan pemeriksaan, kandidat lagu, timestamp, status, bukti lisensi yang dicatat, alasan, error, dan rekomendasi.
Jangan tulis keberhasilan palsu.

SETTINGS:
- status API/provider;
- petunjuk konfigurasi;
- privasi;
- tentang aplikasi;
- versi aplikasi;
- disclaimer.

ARSITEKTUR:
Gunakan single Activity dengan Jetpack Compose dan struktur package yang rapi sesuai batasan AI Studio.
Pisahkan UI, ViewModel/state, repository, media processing, recognition provider, licensing, local storage, dan export secara wajar.
Jangan membuat abstraksi berlebihan.
Gunakan coroutine untuk pekerjaan asynchronous.
Jangan memblokir main thread.

PRIVASI:
Minta persetujuan pengguna sebelum mengirim sampel ke layanan pihak ketiga.
Jelaskan provider penerima sampel.
Gunakan HTTPS.
Jangan log token atau isi audio.
Bersihkan cache sementara.
Jangan upload media diam-diam.
Jangan menyimpan file penuh tanpa alasan.

URUTAN PENGERJAAN:
Tahap 1: bangun fondasi aplikasi, theme, navigation, Dashboard, Scan, History, Reports, Settings. Gunakan data demo hanya jika diberi label DEMO.
Tahap 2: implementasikan media picker dan validasi metadata.
Tahap 3: implementasikan ekstraksi dan segmentasi audio.
Tahap 4: implementasikan provider recognition dan backend/credential yang aman.
Tahap 5: implementasikan pencatatan lisensi dan kategori hasil.
Tahap 6: implementasikan database history dan ekspor laporan.
Tahap 7: jalankan build dan test, perbaiki semua error.

UNTUK SEKARANG:
Mulai dengan Tahap 1. Buat proyek Android native baru yang lengkap dan konsisten, bukan hanya mockup. Pastikan semua navigasi berfungsi. Buat komponen UI reusable, theme terpusat, dan state yang jelas. Jangan membuat API palsu atau menyatakan scan sudah benar-benar berjalan. Setelah selesai, laporkan file utama yang dibuat, fitur yang aktif, fitur yang masih demo/belum dibuat, dan hasil build yang benar-benar diperiksa.
```

---

# BAGIAN XIII — PROMPT LANJUTAN PER TAHAP

## 25. Prompt Tahap 2 — Media picker

```text
Lanjutkan MUSIC GUARD AI ke Tahap 2. Implementasikan pemilihan video dan audio menggunakan Android Storage Access Framework. Tampilkan metadata file, validasi akses URI, tipe MIME, ukuran, durasi, dan track audio. Tangani file kosong, rusak, tidak didukung, video tanpa audio, dan akses ditolak. Jangan mengubah file sumber. Jangan membuat UI scan palsu. Jalankan build dan perbaiki error. Laporkan file yang diubah dan hasil pengujian.
```

## 26. Prompt Tahap 3 — Ekstraksi audio

```text
Lanjutkan ke Tahap 3. Implementasikan media preparation untuk menyiapkan sampel audio dari video/audio yang dipilih. Gunakan API Android yang sesuai dan kompatibel. Untuk video, baca track audio tanpa mengubah file sumber. Siapkan file sementara di cache privat. Jika media panjang, rancang segmentasi sesuai batas provider dan simpan timestamp tiap segmen. Tampilkan cakupan yang benar-benar diperiksa. Bersihkan file sementara setelah selesai, gagal, atau dibatalkan. Jalankan build dan uji error handling.
```

## 27. Prompt Tahap 4 — Integrasi pengenalan musik

```text
Lanjutkan ke Tahap 4. Audit dokumentasi resmi AudD di https://docs.audd.io/ dan implementasikan adapter pengenalan musik berdasarkan dokumentasi terbaru. Jangan menebak parameter request, batas ukuran, format, atau response schema. Gunakan backend HTTPS untuk menyimpan token provider secara aman; jangan menanam token pihak ketiga ke APK. Tangani match, no-match, invalid token, rate limit, timeout, jaringan gagal, dan respons tidak valid. Jika backend/token belum tersedia, siapkan integrasi yang jujur dengan status NOT CONFIGURED. Jangan membuat hasil palsu. Jalankan test/build.
```

## 28. Prompt Tahap 5 — Lisensi dan status

```text
Lanjutkan ke Tahap 5. Buat pencatatan bukti lisensi yang mencakup sumber, jenis lisensi, platform, monetisasi, wilayah, masa berlaku, atribusi, URL/dokumen, dan catatan. Buat status PERLU VERIFIKASI, BELUM DIKETAHUI, PERLU PERHATIAN, dan PEMERIKSAAN GAGAL. Jangan menyatakan bebas copyright hanya karena tidak ada hasil pengenalan. Jangan mengklaim integrasi dengan YouTube Content ID. Tampilkan alasan status dan langkah berikutnya.
```

## 29. Prompt Tahap 6 — History dan laporan

```text
Lanjutkan ke Tahap 6. Implementasikan database lokal persisten untuk ScanRecord, TrackMatch, LicenseEvidence, dan laporan. Pastikan data tetap ada setelah aplikasi ditutup. Buat daftar dan detail history, pencarian sederhana, penghapusan dengan konfirmasi, ekspor Markdown dan JSON melalui Android Storage Access Framework/share sheet. Pastikan laporan hanya memuat hasil pemeriksaan nyata. Jalankan build dan pengujian.
```

## 30. Prompt Tahap 7 — Audit akhir

```text
Lakukan audit akhir MUSIC GUARD AI. Periksa seluruh navigasi, UI, picker, validasi media, ekstraksi audio, segmentasi, timestamp, request API, parsing response, no-match, error, credential security, license evidence, history persistence, ekspor Markdown/JSON, cache cleanup, dan disclaimer. Jalankan build/test yang tersedia dan perbaiki error. Jangan mengklaim fitur lulus jika belum diuji. Berikan ringkasan fitur aktif, fitur belum selesai, hasil build, dan langkah menghasilkan APK/AAB atau mengekspor source project.
```

---

# BAGIAN XIV — CATATAN SERAH TERIMA

## 31. Hal yang harus diminta dari AI Studio setelah setiap milestone

- daftar file yang dibuat/diubah;
- ringkasan implementasi;
- fitur yang benar-benar aktif;
- fitur yang masih demo;
- hasil build;
- error yang tersisa;
- konfigurasi yang harus diisi pengguna;
- cara menguji fitur;
- ekspor ZIP source project setelah milestone besar.

**Prinsip utama:** bangun dari nol, satu tahap pada satu waktu, uji setiap tahap, dan jangan mengorbankan kejujuran hasil atau keamanan data demi tampilan yang terlihat canggih.
