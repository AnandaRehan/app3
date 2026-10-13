# Perbaikan Deteksi Papan Klip (Clipboard) pada Pop-up Downloader

Rencana perbaikan menyeluruh untuk masalah papan klip yang tidak terdeteksi saat gelembung melayang (*floating bubble*) diklik setelah menyalin tautan TikTok atau YouTube.

### Analisis Akar Masalah (Root Cause)

1. **Kebijakan Keamanan & Privasi Android 10+ (API 29+)**:
   - Pada Android 10 ke atas, sistem Android secara ketat memblokir akses ke `ClipboardManager.getPrimaryClip()` jika aplikasi berjalan di latar belakang tanpa fokus jendela aktif (*window focus*).
   - Layanan latar depan (`FloatingDownloaderService`) dan jendela *overlay* (`FloatingBubbleView`) dibuat dengan flag `WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE` agar pengguna tetap dapat berinteraksi dengan aplikasi lain di bawahnya.
   - Dampaknya: Saat pengguna berada di luar aplikasi utama (misalnya sedang membuka TikTok atau YouTube) dan mengetuk gelembung melayang, `clipboard.primaryClip` otomatis menghasilkan `null` dari sistem Android, memicu status *"Papan klip kosong!"*.

---

### Solusi & Rencana Implementasi

#### 1. Translucent Trampoline Activity (`ClipboardCaptureActivity`)
- **Mekanisme Kerja**:
  - Saat pengguna mengetuk gelembung melayang atau tombol notifikasi, sistem memanggil `ClipboardCaptureActivity`.
  - Activity ini dikonfigurasi dengan tema transparan (`android:theme="@android:style/Theme.Translucent.NoTitleBar"`) dan transisi instan tanpa animasi (`overridePendingTransition(0, 0)`).
  - Begitu jendela mendapatkan fokus (`onWindowFocusChanged(true)`), Activity membaca data teks dari `ClipboardManager.primaryClip`.
  - Teks tersebut langsung dikirimkan kembali ke `FloatingDownloaderService.triggerDownload(clipboardText)`, dan Activity segera memanggil `finish()`.
- **Pengalaman Pengguna (UX)**:
  - Proses berlangsung dalam hitungan milidetik (~20-50ms) tanpa kedipan, tanpa animasi perpindahan layar, dan tanpa menutup aplikasi yang sedang dibuka pengguna (TikTok/YouTube). Pengguna tetap merasa berada di aplikasi asalnya secara mulus.

#### 2. Peningkatan Regex & Pembersihan Teks Tautan
- Memastikan ekstraksi URL di `FloatingDownloadProcessor` dan `TikWmService` dapat mengenali:
  - Domain TikTok: `tiktok.com`, `vt.tiktok.com`, `vm.tiktok.com`, `tt.site`, `douyin.com`, `tiktokv.com`.
  - Format teks salinan lengkap (misalnya teks yang menyertakan kalimat pengantar dan tagar seperti *"Lihat video ini di TikTok: https://vt.tiktok.com/xxxx/"*).
  - Domain YouTube: `youtube.com`, `youtu.be`, `m.youtube.com`, link YouTube Shorts.

#### 3. Opsi Tambahan: Tombol "Tempel Cepat" di Menu Pop-up (Fallback)
- Pada menu tekan lama (*long-press*) gelembung melayang:
  - Tambahkan tombol pintasan "Tempel Link Manual" yang memunculkan dialog masukan cepat mini jika perangkat pengguna memiliki sistem keamanan OEM (seperti MIUI/OneUI clipboard protection) yang membatasi pembacaan otomatis.

#### 4. Uji Coba & Verifikasi
- Uji alur salin tautan TikTok (baik tautan langsung maupun dengan teks pengantar) dari luar aplikasi.
- Verifikasi gelembung melayang berhasil mendeteksi tautan, menampilkan animasi status, dan langsung mengunduh video biasa tanpa watermark ke galeri unduhan.
- Verifikasi kompilasi aplikasi dengan `compile_applet`.
