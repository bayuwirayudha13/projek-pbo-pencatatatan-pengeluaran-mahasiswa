package service;

import budget.PengendaliAnggaran;
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
        daftar.add(t);
    }

    public List<Transaksi> getSemuaTransaksi() {
        return daftar;
    }

    public void ubahTransaksi(String id, Transaksi baru) {
        // TODO (Neha): cari berdasarkan id, ganti; lempar exception jika tidak ketemu
    }

    public void hapusTransaksi(String id) {
        // TODO (Neha)
    }

    public List<Transaksi> cariTransaksi(String kata) {
        return new ArrayList<>(); // TODO (Neha): cari di id, kategori, catatan (abaikan huruf besar-kecil)
    }

    public double getTotalPemasukan() {
        return 0; // TODO (Neha)
    }

    public double getTotalPengeluaran() {
        return 0; // TODO (Neha)
    }

    /** Hitung dengan memanggil hitungDampakSaldo() tiap objek (polimorfisme). */
    public double getSaldo() {
        return 0; // TODO (Neha)
    }

    public PengendaliAnggaran getAnggaran() {
        return anggaran;
    }

    public String generateId() {
        return "TRX" + (daftar.size() + 1); // TODO (Neha): pastikan tidak bentrok setelah ada yang dihapus
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
