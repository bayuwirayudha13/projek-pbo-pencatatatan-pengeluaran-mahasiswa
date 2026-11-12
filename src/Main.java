import budget.PengendaliAnggaran;
import gui.MySakuGUI;
import model.Pemasukan;
import model.Pengeluaran;
import model.Transaksi;
import service.ManajerMySaku;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;

/** Titik masuk aplikasi. Dikerjakan oleh: BAYU */
public class Main {
    public static void main(String[] args) {
        // 1. Panggil tes sementara untuk Minggu 2 (Bisa dihapus/di-comment saat Minggu 3)
        tesModulBayuMinggu2();

        // 2. Kode utama aplikasi (Kontrak Modul Bayu untuk Integrasi)
        ManajerMySaku manajer = new ManajerMySaku();
        try {
            manajer.muatDariFile();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new MySakuGUI(manajer).setVisible(true));
    }

    /**
     * Method pengujian sementara Minggu 2 untuk mengecek:
     * - Model Transaksi, Pemasukan, Pengeluaran (overriding & validasi)
     * - PengendaliAnggaran (hitungSisa & cekStatus: AMAN / MENDEKATI / MELAMPAUI)
     */
    private static void tesModulBayuMinggu2() {
        System.out.println("=== [TES MINGGU 2] MODEL TRANSAKSI ===");
        Transaksi masuk = new Pemasukan("T01", LocalDate.now(), 500000, "Uang Saku", "Bulanan", "Orang Tua");
        Transaksi keluar1 = new Pengeluaran("T02", LocalDate.now(), 25000, "Makan", "Makan siang", "Kebutuhan");
        Transaksi keluar2 = new Pengeluaran("T03", LocalDate.now(), 20000, "Jajan", "Kopi susu", "Keinginan");

        System.out.println(masuk.getTipeTransaksi() + " (" + masuk.getKategori() + ") -> Dampak Saldo: " + masuk.hitungDampakSaldo());
        System.out.println(keluar1.getTipeTransaksi() + " (" + keluar1.getKategori() + ") -> Dampak Saldo: " + keluar1.hitungDampakSaldo());
        System.out.println(keluar2.getTipeTransaksi() + " (" + keluar2.getKategori() + ") -> Dampak Saldo: " + keluar2.hitungDampakSaldo());

        System.out.println("\n=== [TES MINGGU 2] PENGENDALI ANGGARAN ===");
        PengendaliAnggaran anggaran = new PengendaliAnggaran(50000);
        System.out.println("Batas Harian: Rp" + anggaran.getBatasHarian());

        // Tes AMAN (< 80%), MENDEKATI (>= 80%), MELAMPAUI (> 100%)
        double[] sampelPengeluaran = {25000, 40000, 55000};
        for (double totalHariIni : sampelPengeluaran) {
            System.out.println("Total Hari Ini: Rp" + totalHariIni
                    + " | Sisa: Rp" + anggaran.hitungSisa(totalHariIni)
                    + " | Status: " + anggaran.cekStatus(totalHariIni));
        }
        System.out.println("==========================================\n");
    }
}