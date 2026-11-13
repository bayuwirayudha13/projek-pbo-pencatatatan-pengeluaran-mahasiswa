package service;

import budget.PengendaliAnggaran;
import model.Pemasukan;
import model.Pengeluaran;
import model.Transaksi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pengelola seluruh transaksi. Dikerjakan oleh: NEHA
 * Dipanggil oleh GUI (Alhiya) dan Main (Bayu).
 */
public class ManajerMySaku implements PenyimpananData {
    private static final Path DATA_DIRECTORY = Paths.get("data");
    private static final Path TRANSACTIONS_FILE = DATA_DIRECTORY.resolve("transaksi.csv");
    private static final Path BUDGET_FILE = DATA_DIRECTORY.resolve("anggaran.csv");

    private ArrayList<Transaksi> daftar = new ArrayList<>();
    private PengendaliAnggaran anggaran = new PengendaliAnggaran();

    public void tambahTransaksi(Transaksi t) {
        if (t == null) {
            throw new IllegalArgumentException("Transaksi tidak boleh null.");
        }
        daftar.add(t);
    }

    public List<Transaksi> getSemuaTransaksi() {
        return daftar;
    }

    public void ubahTransaksi(String id, Transaksi baru) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID transaksi wajib diisi.");
        }
        if (baru == null) {
            throw new IllegalArgumentException("Transaksi baru tidak boleh null.");
        }

        for (int i = 0; i < daftar.size(); i++) {
            if (id.equals(daftar.get(i).getId())) {
                daftar.set(i, baru);
                return;
            }
        }
        throw new IllegalArgumentException("Transaksi dengan ID " + id + " tidak ditemukan.");
    }

    public void hapusTransaksi(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID transaksi wajib diisi.");
        }

        for (int i = 0; i < daftar.size(); i++) {
            if (id.equals(daftar.get(i).getId())) {
                daftar.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("Transaksi dengan ID " + id + " tidak ditemukan.");
    }

    public List<Transaksi> cariTransaksi(String kata) {
        ArrayList<Transaksi> hasil = new ArrayList<>();
        if (kata == null || kata.trim().isEmpty()) {
            return hasil;
        }

        String kataKunci = kata.trim().toLowerCase();
        for (Transaksi transaksi : daftar) {
            if (containsIgnoreCase(transaksi.getId(), kataKunci)
                    || containsIgnoreCase(transaksi.getKategori(), kataKunci)
                    || containsIgnoreCase(transaksi.getCatatan(), kataKunci)) {
                hasil.add(transaksi);
            }
        }
        return hasil;
    }

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

    /** Hitung dengan memanggil hitungDampakSaldo() tiap objek (polimorfisme). */
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
        return false;
    }

    private boolean containsIgnoreCase(String teks, String kataKunci) {
        return teks != null && teks.toLowerCase().contains(kataKunci);
    }

    @Override
    public void simpanKeFile() throws IOException {
        Files.createDirectories(DATA_DIRECTORY);

        ArrayList<String> transaksiRows = new ArrayList<>();
        for (Transaksi transaksi : daftar) {
            String ekstra = transaksi instanceof Pemasukan
                    ? ((Pemasukan) transaksi).getSumber()
                    : ((Pengeluaran) transaksi).getPrioritas();
            transaksiRows.add(joinCsv(
                    transaksi.getId(),
                    transaksi.getTanggal() == null ? "" : transaksi.getTanggal().toString(),
                    transaksi.getTipeTransaksi(),
                    transaksi.getKategori(),
                    Double.toString(transaksi.getNominal()),
                    transaksi.getCatatan(),
                    ekstra));
        }

        writeAtomically(TRANSACTIONS_FILE, transaksiRows);
        writeAtomically(BUDGET_FILE, List.of(Double.toString(anggaran.getBatasHarian())));
    }

    @Override
    public void muatDariFile() throws IOException {
        daftar.clear();
        if (Files.exists(TRANSACTIONS_FILE)) {
            for (String row : Files.readAllLines(TRANSACTIONS_FILE, StandardCharsets.UTF_8)) {
                if (!row.trim().isEmpty()) {
                    bacaTransaksi(row);
                }
            }
        }

        if (Files.exists(BUDGET_FILE)) {
            List<String> rows = Files.readAllLines(BUDGET_FILE, StandardCharsets.UTF_8);
            if (!rows.isEmpty() && !rows.get(0).trim().isEmpty()) {
                try {
                    double batas = Double.parseDouble(rows.get(0).trim());
                    if (Double.isFinite(batas) && batas >= 0) {
                        anggaran.setBatasHarian(batas);
                    }
                } catch (NumberFormatException ignored) {
                    // Keep the default budget when the stored value is invalid.
                }
            }
        }
    }

    private void bacaTransaksi(String row) {
        List<String> kolom = parseCsv(row);
        if (kolom.size() != 7) {
            return;
        }

        try {
            String tipe = kolom.get(2);
            LocalDate tanggal = LocalDate.parse(kolom.get(1));
            double nominal = Double.parseDouble(kolom.get(4));
            if ("Pemasukan".equalsIgnoreCase(tipe)) {
                daftar.add(new Pemasukan(
                        kolom.get(0), tanggal, nominal, kolom.get(3), kolom.get(5), kolom.get(6)));
            } else if ("Pengeluaran".equalsIgnoreCase(tipe)) {
                daftar.add(new Pengeluaran(
                        kolom.get(0), tanggal, nominal, kolom.get(3), kolom.get(5), kolom.get(6)));
            }
        } catch (IllegalArgumentException | DateTimeParseException ignored) {
            // Ignore malformed rows and continue loading valid transactions.
        }
    }

    private void writeAtomically(Path target, List<String> rows) throws IOException {
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(temporary, rows, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

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
        if (safeValue.contains(",") || safeValue.contains("\"")
                || safeValue.contains("\n") || safeValue.contains("\r")) {
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
                if (inQuotes && i + 1 < row.length() && row.charAt(i + 1) == '"') {
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
