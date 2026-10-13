# Floating Pop-up Downloader (Overlay Melayang)

Fitur ini memungkinkan pengguna mengunduh media dari TikTok dan YouTube secara instan lewat gelembung melayang (*floating bubble*) di atas layar aplikasi lain dengan sekali klik untuk mendeteksi link terakhir dari papan klip (*clipboard*), tanpa harus membuka atau berpindah layar aplikasi.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Pembaruan Preferensi Pengguna**:
> - **Format Video TikTok Pop-up**: Mengunduh video versi standar tanpa tanda air (*Regular / SD No-Watermark* melalui `data.play`), bukan versi HD, agar proses unduh berlangsung instan, ringan, dan hemat penyimpanan, namun tetap bersih tanpa watermark.
> - **Penamaan Berkas TikTok**: Menggunakan format `sd-<id>.mp4` (atau `sd-<aweme_id>.mp4`) sesuai aturan penamaan yang telah ditetapkan.
> - **Format YouTube Pop-up**: Mengunduh video standar 720p MP4 melalui Naze API.
> - **Metode Pengaktifan**: Sakelar di Laci Menu Samping (*Navigation Drawer*) dan Notifikasi Cepat (*Sticky Quick Notification*) di bilah status.

- **Keputusan Terkonfirmasi 1 (Akses Izin Hamparan Layar)**: Memanfaatkan izin `SYSTEM_ALERT_WINDOW` (*Draw over other apps*) dengan dialog panduan ramah pengguna jika izin belum diberikan, serta layanan latar depan (`FloatingDownloaderService`) agar gelembung tetap stabil dan tidak dimatikan oleh sistem Android.
- **Keputusan Terkonfirmasi 2 (Format Unduhan TikTok Cepat Tanpa Watermark)**:
  - Untuk unduhan otomatis lewat pop-up, sistem memprioritaskan URL video biasa tanpa watermark (`data.play`), bukan `data.hdplay`. Jika konten adalah slide foto TikTok, sistem akan mengunduh foto-foto slide tersebut.
- **Keputusan Terkonfirmasi 3 (Gestur & Kontrol Interaktif)**:
  - **Klik Singkat (Single Click)**: Membaca teks terakhir di papan klip (*clipboard*). Jika berupa link TikTok atau YouTube, langsung memicu unduhan di latar belakang tanpa membuka dialog atau mengubah layar aplikasi.
  - **Tekan Lama (Long Press)**: Menampilkan menu mini kendali di samping gelembung (Tombol Deteksi Cepat, Tombol Matikan Pop-up, dan Tombol Tutup Menu).
  - **Seret ke Bawah (Drag to Dismiss)**: Menampilkan zona target lingkaran merah di bagian bawah layar saat gelembung diseret. Jika gelembung dilepas di zona tersebut, pop-up otomatis dinonaktifkan.
- **Keputusan Terkonfirmasi 4 (Notifikasi Cepat & Sinkronisasi)**: Notifikasi persisten di panel notifikasi Android dengan 2 tombol aksi: *📥 Deteksi & Download* dan *❌ Matikan Pop-up*. Status sakelar di Laci Menu selalu sinkron dengan status layanan pop-up.

---

### 1. Overview & Core Concept

- **Apa Fungsinya**: Pengguna yang sedang menonton video di TikTok, YouTube, Instagram, atau browser dapat cukup menekan tombol "Salin Tautan" (Copy Link), lalu langsung mengetuk gelembung melayang App3 di tepi layar. Sistem otomatis mengambil tautan dari papan klip, memproses unduhan video versi biasa tanpa watermark (SD No-Watermark untuk TikTok, 720p untuk YouTube), dan langsung memasukkannya ke antrean `DownloadManager` serta galeri riwayat lokal App3.
- **Target Pengguna**: Pengguna yang ingin mengoleksi video secara cepat tanpa ribet berpindah aplikasi atau terganggu dialog unduhan.
- **Nilai Tambah**: Sangat cepat (*frictionless*), hemat kuota dengan kualitas bersih tanpa watermark, dan menjaga alur penggunaan ponsel pengguna tetap lancar.

---

### 2. User Experience & Visual Design

- **Alur Pengaktifan**:
  1. Pengguna membuka Laci Samping (*Navigation Drawer*) di App3 Studio.
  2. Terdapat kartu pengaturan baru **"Pop-up Downloader Melayang"** dengan toggle switch yang interaktif.
  3. Saat pertama kali diaktifkan, jika izin hamparan layar belum ada, aplikasi menampilkan dialog konfirmasi yang mengarahkan ke pengaturan sistem Android *Tampilkan di atas aplikasi lain*.
  4. Begitu aktif, gelembung melayang muncul di tepi layar ponsel, dan notifikasi cepat muncul di bilah status.

- **Desain Gelembung Melayang (Floating Bubble)**:
  - Bentuk lingkaran elegan (56dp) dengan gradien warna tema aktif (sesuai tema pilihan pengguna di App3).
  - Ikon unduh (*Download*) dengan animasi berputar/pulsing saat sedang mendeteksi tautan atau memproses API.
  - Dapat digeser bebas (*draggable*) dengan pergerakan halus dan magnetis ke tepi layar saat dilepas.

- **Zona Tutup Saat Digeser (Drop Target)**:
  - Begitu pengguna mulai menyeret gelembung, zona target setengah lingkaran merah transparan dengan ikon silang (✕) muncul di bagian bawah layar.
  - Jika gelembung berada di dalam radius zona target, gelembung membesar sedikit dan zona berubah merah tegas. Melepaskannya di sini akan mematikan layanan dan menutup gelembung.

- **Menu Mini Tekan Lama (Long Press Expansion)**:
  - Menekan lama gelembung memunculkan bar horizontal animasi berlatar kaca buram (*glassmorphic surface*) dengan 2 tombol aksi:
    - Tombol Hijau / Primer: "Deteksi Link Clipboard"
    - Tombol Merah: "Matikan Pop-up"

---

### 3. Key Product Decisions & Trade-Offs

- **Keputusan 1: TikTok SD No-Watermark (data.play) untuk Unduhan Pop-up**:
  - *Pendekatan*: Mengambil `data.play` sebagai sumber unduhan video TikTok utama pada fitur pop-up, bukan `data.hdplay`.
  - *Alasan*: Video SD no-watermark berukuran lebih ringkas, memulai unduhan jauh lebih cepat tanpa jeda transcoding server pihak ketiga, namun tetap mempertahankan kualitas visual yang jernih dan bebas watermark.
  - *Penamaan Berkas*: Berkas disimpan dengan awalan `sd-<id>.mp4`.

- **Keputusan 2: Layanan Latar Depan (Foreground Service) dengan Notifikasi Persisten**:
  - *Pendekatan*: Menggunakan Android `ForegroundService` dengan channel notifikasi khusus berkategori utilitas.
  - *Alasan*: Menjamin proses di latar belakang tidak dimatikan oleh sistem Android, dan memberi pengguna kendali mematikan pop-up kapan saja lewat panel notifikasi.

- **Keputusan 3: Penyelarasan Status StateFlow Terpusat**:
  - *Pendekatan*: `FloatingDownloaderService` mempublikasikan status `isServiceRunning` via `StateFlow` dan broadcast receiver sehingga sakelar di Laci Menu selalu akurat secara real-time.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   MainActivity / Drawer UI              │
│       [Toggle Switch: Pop-up Downloader Melayang]      │
└──────────────────────────┬─────────────────────────────┘
                           │ (Start / Stop Intent)
                           ▼
┌────────────────────────────────────────────────────────┐
│             FloatingDownloaderService                   │
│  - WindowManager (TYPE_APPLICATION_OVERLAY)            │
│  - Sticky Notification + Quick Actions                 │
│  - Touch & Drag Listener + Bottom Dismiss Zone         │
└──────────────┬──────────────────────────┬──────────────┘
               │ (Single Click)           │ (Long Press)
               ▼                          ▼
┌──────────────────────────┐   ┌─────────────────────────┐
│     ClipboardManager     │   │ Mini Action Menu Bubble │
│  - Ambil link terakhir   │   │ [Deteksi] [Matikan Pop] │
└──────────────┬───────────┘   └─────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────────────────┐
│             Link Routing & Download Engine              │
│  - TikTok   -> TikWmService (Ambil SD data.play)       │
│  - YouTube  -> NazeYoutube (Ambil 720p MP4)            │
│  - Enqueue via Android DownloadManager                 │
│  - Simpan riwayat ke Room: DownloadedMediaRepository   │
└────────────────────────────────────────────────────────┘
```

#### Alur Komponen & Modifikasi:
1. **AndroidManifest.xml**:
   - Menambahkan izin `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, dan `POST_NOTIFICATIONS`.
   - Mendaftarkan deklarasi `<service android:name=".service.FloatingDownloaderService" />` dan receiver aksi cepat.
2. **FloatingDownloaderService.kt**:
   - Mengelola penambahan tampilan melayang ke `WindowManager`.
   - Mengatur notifikasi latar depan dengan channel `channel_floating_downloader`.
   - Menangani sentuhan, seretan (*drag*), deteksi tabrakan dengan zona tutup (*dismiss area*), dan transformasi menu saat ditekan lama.
   - Memanggil `TikWmService` dengan opsi video biasa (SD no watermark) dan `NazeYoutubeService` untuk YouTube.
3. **FloatingBubbleView.kt**:
   - Tata letak gelembung melayang dan zona drop bawah yang responsif terhadap input sentuh pengguna.
4. **MainActivity.kt & Drawer Sheet**:
   - Menambahkan kartu pengaturan switch di laci samping untuk mengaktifkan/menonaktifkan pop-up.
   - Pengecekan izin `Settings.canDrawOverlays(this)` beserta dialog penjelasan jika belum diizinkan.
5. **Download Integration**:
   - Meneruskan berkas ke `DownloadManager` dan mencatatnya ke `DownloadedMediaRepository` di Room Database.
