# SmartBudget - Aplikasi Dompet Digital

SmartBudget adalah sebuah aplikasi dompet digital yang berfungsi untuk membantu pengguna melacak pemasukan dan pengeluaran isi dompet kita dalam kehidupan sehari-hari untuk membantu dalam membangun kebiasaan finansial yang baik.

> Proyek ini diciptakan sebagai projek pengganti Ujian Akhir Semester (UAS) Universitas Cokroaminoto Palopo mata kuliah **'Aplikasi Mobile II'** pada tahun akademik 2026/2025.

## Identitas Pengembang
* **Nama:** Jericho Christian Tenri
* **NIM:** 2304411729
* **Kelas:** 5K RPL 3
* **Prodi:** INFORMATIKA - Universitas Cokroaminoto Palopo

---

## Checklist Fitur-Fitur
Aplikasi ini telah memenuhi seluruh kriteria kelulusan UAS:

**1. Otentikasi Firebase (Firebase Auth)**
* ✔️ Login & Register aman menggunakan Email/Password.
* ✔️ Login & Register aman mengunakan Akun Google.

**2. Implementasi Database Firebase & CRUD**
* ✔️ **Create (Input):** Menambahkan sebuah transaksi yang mengandung tanggal, jam, jumlah transaksi, serta deskripsi transaksi.
* ✔️ **Read (Tampil):** Menampilkan daftar transaksi yang telah di input di halaman utama.
* ✔️ **Update (Edit):** Mengubah tanggal, jam, jumlah transaksi, serta deskripsi transaksi dan mengubahnya di Database serta juga di aplikasi.
* ✔️ **Delete (Hapus):** Menghapus sebuah transaksi yang di pilih dan menghapusnya dari Database.

**3. Komponen Android**
* ✔️ **Fragment:** Menggunakan Fragment untuk menampilkan daftar tugas pada setiap kategori.
* ✔️ **Intent:** Perpindahan antar halaman (Login -> Halaman Utama -> HalamanOpsi -> Login).
* ✔️ **RecyclerView:** List tugas yang responsif yang menampilkan daftar transaksi berdasarkan tanggal.
* ✔️ **Notification:** Notifikasi otomatis saat terjadi pembuatan, pengeditan, dan penghapusan transaksi.

---

## Screenshot Aplikasi
## Screenshot Aplikasi

<table>
  <tr>
    <td align="center">
      <img src="Screenshots/Halaman%20Login.jpg" width="250">
      <br>
      <em>Halaman Login</em>
    </td>
    <td align="center">
      <img src="Screenshots/Halaman%20List.jpg" width="250">
      <br>
      <em>Halaman List Transaksi</em>
    </td>
    <td align="center">
      <img src="Screenshots/Halaman%20Add.jpg" width="250">
      <br>
      <em>Halaman Tambah Data</em>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="Screenshots/Halaman%20Edit.jpg" width="250">
      <br>
      <em>Halaman Edit Data</em>
    </td>
    <td align="center">
      <img src="Screenshots/Halaman%20Delete.jpg" width="250">
      <br>
      <em>Halaman Delete Data</em>
    </td>
    <td align="center">
      <img src="Screenshots/Halaman%20Notifikasi.jpg" width="250">
      <br>
      <em>Contoh muncul Notifikasi</em>
    </td>
  </tr>
</table>
