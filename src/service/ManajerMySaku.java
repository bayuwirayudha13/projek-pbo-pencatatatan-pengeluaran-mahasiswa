package service;

import budget.PengendaliAnggaran;
import model.Pemasukan;
import model.Pengeluaran;
import model.Transaksi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pengelola seluruh transaksi. Dikerjakan oleh: NEHA
 * Dipanggil oleh GUI (Alhiya) dan Main (Bayu).
 */
public class ManajerMySaku implements PenyimpananData {
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
        // TODO (Neha): tulis transaksi.csv dan anggaran.csv
    }

    @Override
    public void muatDariFile() throws IOException {
        // TODO (Neha): baca CSV; file belum ada = data kosong
    }
}
