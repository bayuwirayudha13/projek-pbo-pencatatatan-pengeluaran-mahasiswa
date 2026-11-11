# MySaku

Aplikasi desktop pencatat dan pengendali keuangan harian mahasiswa (Java Swing, OOP, penyimpanan CSV).

Tim (Kelompok 3): Bayu Wirayudha, Neha Anggreany Octavia, Alhiya Salsabilla

## Cara menjalankan

Buka folder ini di IDE (IntelliJ IDEA / NetBeans / VS Code), tandai `src` sebagai source root, lalu jalankan `Main.java`.

Lewat terminal (Linux/macOS):

    javac -d out $(find src -name "*.java")
    java -cp out Main

Windows (Command Prompt):

    dir /s /b src\*.java > sources.txt
    javac -d out @sources.txt
    java -cp out Main

## Pembagian modul

| Package | Pemilik | Isi |
|---|---|---|
| `Main.java`, `model/`, `budget/` | Bayu | Transaksi, Pemasukan, Pengeluaran, PengendaliAnggaran, Main |
| `util/`, `service/` | Neha | ValidasiInput, InputTidakValidException, PenyimpananData, ManajerMySaku |
| `gui/`, `analisis/` | Alhiya | MySakuGUI, FormTransaksiPanel, TabelTransaksiPanel, RingkasanPanel, AnggaranDialog, AnalisisKeuangan |

Aturan: jangan mengedit file di package milik orang lain tanpa memberi tahu pemiliknya.

## Kesepakatan

- Format CSV: `id,tanggal,tipe,kategori,nominal,catatan,ekstra` (ekstra = sumber untuk pemasukan, prioritas untuk pengeluaran)
- Format tanggal: `yyyy-MM-dd`
- Status anggaran: `MENDEKATI` jika total harian >= 80% batas, `MELAMPAUI` jika > 100%
- Nama method di kerangka ini adalah kontrak; jangan diubah sepihak

## Aturan Git

1. Jangan push langsung ke `main`; kerjakan di branch sendiri lalu buka Pull Request.
2. Setiap PR ditinjau minimal satu anggota lain.
3. Sebelum mulai kerja: `git switch main` lalu `git pull origin main`.
4. Pastikan project bisa dikompilasi sebelum membuka PR.

## Branch

| Anggota | Minggu 2 | Minggu 3 |
|---|---|---|
| Bayu | model-transaksi, budget-anggaran | integrasi-main, docs-pengujian |
| Neha | util-validasi, service-manajer | fitur-csv |
| Alhiya | gui-layout, analisis-keuangan | gui-integrasi |
