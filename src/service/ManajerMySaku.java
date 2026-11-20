
package service;

import budget.PengendaliAnggaran;
import model.Pemasukan;
import model.Pengeluaran;
import model.Transaksi;
import model.TargetTabungan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pengelola seluruh data aplikasi MySaku.
 * Mengelola transaksi, sampah, anggaran, dan target tabungan.
 */
public class ManajerMySaku implements PenyimpananData {

    // ================= LOKASI FILE =================

    private static final Path DATA_DIRECTORY = Paths.get("data");

    private static final Path TRANSACTIONS_FILE =
            DATA_DIRECTORY.resolve("transaksi.csv");

    private static final Path TRASH_FILE =
            DATA_DIRECTORY.resolve("sampah.csv");

    private static final Path BUDGET_FILE =
            DATA_DIRECTORY.resolve("anggaran.csv");

    private static final Path TABUNGAN_FILE =
            DATA_DIRECTORY.resolve("tabungan.csv");

    // ================= DATA APLIKASI =================

    private ArrayList<Transaksi> daftar = new ArrayList<>();

    private ArrayList<Transaksi> daftarSampah = new ArrayList<>();

    private ArrayList<TargetTabungan> daftarTargetTabungan =
            new ArrayList<>();

    private PengendaliAnggaran anggaran =
            new PengendaliAnggaran();

    // ================= TRANSAKSI AKTIF =================

    public void tambahTransaksi(Transaksi t) {
        if (t == null) {
            throw new IllegalArgumentException(
                    "Transaksi tidak boleh null.");
        }

        daftar.add(t);
    }

    public List<Transaksi> getSemuaTransaksi() {
        return daftar;
    }

    public void ubahTransaksi(String id, Transaksi baru) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID transaksi wajib diisi.");
        }

        if (baru == null) {
            throw new IllegalArgumentException(
                    "Transaksi baru tidak boleh null.");
        }

        for (int i = 0; i < daftar.size(); i++) {
            if (id.equals(daftar.get(i).getId())) {
                daftar.set(i, baru);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Transaksi dengan ID " + id + " tidak ditemukan.");
    }

    /**
     * Memindahkan transaksi aktif ke Sampah.
     */
    public void hapusTransaksi(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID transaksi wajib diisi.");
        }

        for (int i = 0; i < daftar.size(); i++) {
            if (id.equals(daftar.get(i).getId())) {
                daftarSampah.add(daftar.remove(i));
                return;
            }
        }

        throw new IllegalArgumentException(
                "Transaksi dengan ID " + id + " tidak ditemukan.");
    }

    public List<Transaksi> cariTransaksi(String kata) {
        ArrayList<Transaksi> hasil = new ArrayList<>();

        if (kata == null || kata.trim().isEmpty()) {
            return hasil;
        }

        String kataKunci = kata.trim().toLowerCase();

        for (Transaksi transaksi : daftar) {
            if (containsIgnoreCase(transaksi.getId(), kataKunci)
                    || containsIgnoreCase(
                            transaksi.getKategori(), kataKunci)
                    || containsIgnoreCase(
                            transaksi.getCatatan(), kataKunci)) {

                hasil.add(transaksi);
            }
        }

        return hasil;
    }

    // ================= FITUR SAMPAH =================

    public List<Transaksi> getSemuaSampah() {
        return daftarSampah;
    }

    public void pulihkanTransaksi(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID transaksi wajib diisi.");
        }

        for (int i = 0; i < daftarSampah.size(); i++) {
            if (id.equals(daftarSampah.get(i).getId())) {
                daftar.add(daftarSampah.remove(i));
                return;
            }
        }

        throw new IllegalArgumentException(
                "Transaksi di Sampah dengan ID "
                        + id + " tidak ditemukan.");
    }

    public void hapusPermanen(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID transaksi wajib diisi.");
        }

        for (int i = 0; i < daftarSampah.size(); i++) {
            if (id.equals(daftarSampah.get(i).getId())) {
                daftarSampah.remove(i);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Transaksi di Sampah dengan ID "
                        + id + " tidak ditemukan.");
    }

    public void kosongkanSampah() {
        daftarSampah.clear();
    }

    // ================= TARGET TABUNGAN =================

    /**
     * Mengambil seluruh target tabungan.
     */
    public List<TargetTabungan> getSemuaTargetTabungan() {
        return daftarTargetTabungan;
    }

    /**
     * Menambahkan target tabungan baru.
     */
    public void tambahTargetTabungan(TargetTabungan target) {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Target tabungan tidak boleh kosong.");
        }

        if (containsIdTarget(target.getId())) {
            throw new IllegalArgumentException(
                    "ID target tabungan sudah digunakan.");
        }

        daftarTargetTabungan.add(target);
    }

    /**
     * Mengubah data target tabungan.
     */
    public void ubahTargetTabungan(
            String id, TargetTabungan targetBaru) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID target wajib diisi.");
        }

        if (targetBaru == null) {
            throw new IllegalArgumentException(
                    "Data target baru tidak boleh kosong.");
        }

        for (int i = 0; i < daftarTargetTabungan.size(); i++) {
            if (id.equals(daftarTargetTabungan.get(i).getId())) {

                // Memastikan ID tidak berubah saat mengedit target.
                targetBaru.setId(id);

                daftarTargetTabungan.set(i, targetBaru);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Target tabungan tidak ditemukan.");
    }

    /**
     * Menghapus target tabungan.
     */
    public void hapusTargetTabungan(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID target wajib diisi.");
        }

        boolean berhasil = daftarTargetTabungan.removeIf(
                target -> id.equals(target.getId())
        );

        if (!berhasil) {
            throw new IllegalArgumentException(
                    "Target tabungan tidak ditemukan.");
        }
    }

    /**
     * Menambahkan setoran ke target tabungan.
     */
    public void tambahSetoranTabungan(
            String id, double nominal) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ID target wajib diisi.");
        }

        for (TargetTabungan target : daftarTargetTabungan) {
            if (target.getId().equals(id)) {
                target.tambahSetoran(nominal);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Target tabungan tidak ditemukan.");
    }

    /**
     * Membuat ID target tabungan secara otomatis.
     */
    public String generateIdTargetTabungan() {
        int nomor = 1;

        while (containsIdTarget("TAB" + nomor)) {
            nomor++;
        }

        return "TAB" + nomor;
    }

    private boolean containsIdTarget(String id) {
        for (TargetTabungan target : daftarTargetTabungan) {
            if (id.equals(target.getId())) {
                return true;
            }
        }

        return false;
    }

    // ================= PERHITUNGAN =================

    public double getTotalPemasukan() {
        double total = 0;

        for (Transaksi transaksi : daftar) {
            if (transaksi instanceof Pemasukan) {
                total += transaksi.getNominal();
            }
        }

        return total;
    }

    public double getTotalPengeluaran() {
        double total = 0;

        for (Transaksi transaksi : daftar) {
            if (transaksi instanceof Pengeluaran) {
                total += transaksi.getNominal();
            }
        }

        return total;
    }

    /**
     * Menghitung saldo menggunakan polimorfisme.
     */
    public double getSaldo() {
        double saldo = 0;

        for (Transaksi transaksi : daftar) {
            saldo += transaksi.hitungDampakSaldo();
        }

        return saldo;
    }

    public PengendaliAnggaran getAnggaran() {
        return anggaran;
    }

    // ================= ID TRANSAKSI =================

    public String generateId() {
        int nomor = 1;

        while (containsId("TRX" + nomor)) {
            nomor++;
        }

        return "TRX" + nomor;
    }

    private boolean containsId(String id) {
        for (Transaksi transaksi : daftar) {
            if (id.equals(transaksi.getId())) {
                return true;
            }
        }

        for (Transaksi transaksi : daftarSampah) {
            if (id.equals(transaksi.getId())) {
                return true;
            }
        }

        return false;
    }

    private boolean containsIgnoreCase(
            String teks, String kataKunci) {

        return teks != null
                && teks.toLowerCase().contains(kataKunci);
    }

    // ================= PENYIMPANAN DATA =================

    @Override
    public void simpanKeFile() throws IOException {

        Files.createDirectories(DATA_DIRECTORY);

        // Simpan transaksi aktif.
        writeAtomically(
                TRANSACTIONS_FILE, ubahKeCsv(daftar));

        // Simpan transaksi sampah.
        writeAtomically(
                TRASH_FILE, ubahKeCsv(daftarSampah));

        // Simpan anggaran berdasarkan tanggal.
        ArrayList<String> barisAnggaran = new ArrayList<>();

        for (Map.Entry<LocalDate, Double> entri
                : anggaran.getSemuaAnggaran().entrySet()) {

            barisAnggaran.add(
                    entri.getKey() + ","
                            + Double.toString(entri.getValue()));
        }

        writeAtomically(BUDGET_FILE, barisAnggaran);

        // Simpan target tabungan.
        ArrayList<String> barisTabungan = new ArrayList<>();

        for (TargetTabungan target : daftarTargetTabungan) {
            barisTabungan.add(joinCsv(
                    target.getId(),
                    target.getNama(),
                    Double.toString(target.getNominalTarget()),
                    Double.toString(target.getNominalTerkumpul()),
                    target.getTanggalTarget() == null
                            ? "" : target.getTanggalTarget().toString()
            ));
        }

        writeAtomically(TABUNGAN_FILE, barisTabungan);
    }

    /**
     * Mengubah daftar transaksi menjadi baris CSV.
     */
    private ArrayList<String> ubahKeCsv(
            List<Transaksi> sumber) {

        ArrayList<String> rows = new ArrayList<>();

        for (Transaksi transaksi : sumber) {
            String ekstra = transaksi instanceof Pemasukan
                    ? ((Pemasukan) transaksi).getSumber()
                    : ((Pengeluaran) transaksi).getPrioritas();

            rows.add(joinCsv(
                    transaksi.getId(),
                    transaksi.getTanggal() == null
                            ? "" : transaksi.getTanggal().toString(),
                    transaksi.getTipeTransaksi(),
                    transaksi.getKategori(),
                    Double.toString(transaksi.getNominal()),
                    transaksi.getCatatan(),
                    ekstra
            ));
        }

        return rows;
    }

    // ================= MEMUAT DATA =================

    @Override
    public void muatDariFile() throws IOException {

        daftar.clear();
        daftarSampah.clear();
        daftarTargetTabungan.clear();

        // Muat transaksi aktif.
        if (Files.exists(TRANSACTIONS_FILE)) {
            for (String row : Files.readAllLines(
                    TRANSACTIONS_FILE, StandardCharsets.UTF_8)) {

                if (!row.trim().isEmpty()) {
                    bacaTransaksi(row, daftar);
                }
            }
        }

        // Muat transaksi sampah.
        if (Files.exists(TRASH_FILE)) {
            for (String row : Files.readAllLines(
                    TRASH_FILE, StandardCharsets.UTF_8)) {

                if (!row.trim().isEmpty()) {
                    bacaTransaksi(row, daftarSampah);
                }
            }
        }

        // Muat ulang anggaran.
        anggaran.hapusSemuaAnggaran();

        if (Files.exists(BUDGET_FILE)) {
            List<String> rows = Files.readAllLines(
                    BUDGET_FILE, StandardCharsets.UTF_8);

            for (String row : rows) {
                if (row == null || row.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] kolom = row.split(",", -1);

                    if (kolom.length == 2) {
                        LocalDate tanggal =
                                LocalDate.parse(kolom[0].trim());

                        double batas =
                                Double.parseDouble(kolom[1].trim());

                        if (Double.isFinite(batas) && batas > 0) {
                            anggaran.muatAnggaran(tanggal, batas);
                        }

                    } else if (kolom.length == 1) {
                        // Mendukung format anggaran versi lama.
                        double batas =
                                Double.parseDouble(kolom[0].trim());

                        if (Double.isFinite(batas) && batas > 0) {
                            anggaran.muatAnggaran(
                                    LocalDate.now(), batas);
                        }
                    }

                } catch (IllegalArgumentException ignored) {
                    // Abaikan baris anggaran yang tidak valid.
                }
            }
        }

        // Muat target tabungan.
        if (Files.exists(TABUNGAN_FILE)) {
            List<String> rows = Files.readAllLines(
                    TABUNGAN_FILE, StandardCharsets.UTF_8);

            for (String row : rows) {
                if (row == null || row.trim().isEmpty()) {
                    continue;
                }

                try {
                    List<String> kolom = parseCsv(row);

                    if (kolom.size() != 5) {
                        continue;
                    }

                    LocalDate tanggalTarget =
                            kolom.get(4).trim().isEmpty()
                                    ? null
                                    : LocalDate.parse(
                                            kolom.get(4).trim());

                    TargetTabungan target = new TargetTabungan(
                            kolom.get(0),
                            kolom.get(1),
                            Double.parseDouble(kolom.get(2)),
                            Double.parseDouble(kolom.get(3)),
                            tanggalTarget
                    );

                    daftarTargetTabungan.add(target);

                } catch (IllegalArgumentException ignored) {
                    // Abaikan baris target yang tidak valid.
                }
            }
        }
    }

    /**
     * Membaca satu baris CSV ke daftar transaksi.
     */
    private void bacaTransaksi(
            String row, ArrayList<Transaksi> tujuan) {

        List<String> kolom = parseCsv(row);

        if (kolom.size() != 7) {
            return;
        }

        try {
            String tipe = kolom.get(2);

            LocalDate tanggal = kolom.get(1).trim().isEmpty()
                    ? null
                    : LocalDate.parse(kolom.get(1).trim());

            double nominal =
                    Double.parseDouble(kolom.get(4));

            if ("Pemasukan".equalsIgnoreCase(tipe)) {
                tujuan.add(new Pemasukan(
                        kolom.get(0),
                        tanggal,
                        nominal,
                        kolom.get(3),
                        kolom.get(5),
                        kolom.get(6)
                ));

            } else if ("Pengeluaran".equalsIgnoreCase(tipe)) {
                tujuan.add(new Pengeluaran(
                        kolom.get(0),
                        tanggal,
                        nominal,
                        kolom.get(3),
                        kolom.get(5),
                        kolom.get(6)
                ));
            }

        } catch (IllegalArgumentException ignored) {
            // Abaikan baris transaksi yang rusak.
        }
    }

    // ================= UTILITAS FILE =================

    /**
     * Menulis file sementara sebelum mengganti file utama.
     */
    private void writeAtomically(
            Path target, List<String> rows) throws IOException {

        Path temporary = target.resolveSibling(
                target.getFileName() + ".tmp");

        try {
            Files.write(
                    temporary,
                    rows,
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    // ================= UTILITAS CSV =================

    private String joinCsv(String... values) {
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                row.append(',');
            }

            row.append(escapeCsv(values[i]));
        }

        return row.toString();
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;

        if (safeValue.contains(",")
                || safeValue.contains("\"")
                || safeValue.contains("\n")
                || safeValue.contains("\r")) {

            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }

        return safeValue;
    }

    private List<String> parseCsv(String row) {
        ArrayList<String> values = new ArrayList<>();

        StringBuilder value = new StringBuilder();

        boolean inQuotes = false;

        for (int i = 0; i < row.length(); i++) {
            char current = row.charAt(i);

            if (current == '"') {
                if (inQuotes
                        && i + 1 < row.length()
                        && row.charAt(i + 1) == '"') {

                    value.append('"');
                    i++;

                } else {
                    inQuotes = !inQuotes;
                }

            } else if (current == ',' && !inQuotes) {
                values.add(value.toString());
                value.setLength(0);

            } else {
                value.append(current);
            }
        }

        if (inQuotes) {
            return new ArrayList<>();
        }

        values.add(value.toString());

        return values;
    }
}
