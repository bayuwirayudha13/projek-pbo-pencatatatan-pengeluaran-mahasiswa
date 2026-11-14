
package gui;

import service.ManajerMySaku;
import model.Transaksi;
import analisis.AnalisisKeuangan;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Ringkasan keuangan.
 * Dikerjakan oleh: ALHIYA
 */
public class RingkasanPanel extends JPanel {

    private ManajerMySaku manajer;
    private AnalisisKeuangan analisis;

    private JLabel labelPemasukan;
    private JLabel labelPengeluaran;
    private JLabel labelSaldo;
    private JLabel labelAnggaran;

    public RingkasanPanel(ManajerMySaku manajer) {
        this.manajer = manajer;
        this.analisis = new AnalisisKeuangan();

        setLayout(new GridLayout(1, 4, 15, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        labelPemasukan = new JLabel("Rp 0");
        labelPengeluaran = new JLabel("Rp 0");
        labelSaldo = new JLabel("Rp 0");
        labelAnggaran = new JLabel("Belum diatur");

        add(buatKartu("Total Pemasukan", labelPemasukan,
                new Color(30, 130, 80)));

        add(buatKartu("Total Pengeluaran", labelPengeluaran,
                new Color(190, 65, 65)));

        add(buatKartu("Saldo Akhir", labelSaldo,
                new Color(30, 64, 110)));

        add(buatKartu("Anggaran Harian", labelAnggaran,
                new Color(180, 120, 30)));

        refreshRingkasan();
    }

    private JPanel buatKartu(
            String judul, JLabel labelNilai, Color warna) {

        JPanel kartu = new JPanel();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(Color.WHITE);

        kartu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(
            new Font("SansSerif", Font.PLAIN, 13)
        );

        labelNilai.setFont(
            new Font("SansSerif", Font.BOLD, 16)
        );
        labelNilai.setForeground(warna);

        kartu.add(labelJudul);
        kartu.add(Box.createVerticalStrut(10));
        kartu.add(labelNilai);

        return kartu;
    }

    /**
     * Memperbarui seluruh informasi keuangan.
     */
    public void refreshRingkasan() {

        double totalPemasukan = 0;
        double totalPengeluaran = 0;
        double saldo = 0;

        for (Transaksi transaksi : manajer.getSemuaTransaksi()) {

            double dampak = transaksi.hitungDampakSaldo();
            saldo += dampak;

            if (dampak > 0) {
                totalPemasukan += dampak;
            } else {
                totalPengeluaran += Math.abs(dampak);
            }
        }

        NumberFormat formatRupiah =
            NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("id-ID")
            );

        labelPemasukan.setText(formatRupiah.format(totalPemasukan));
        labelPengeluaran.setText(formatRupiah.format(totalPengeluaran));
        labelSaldo.setText(formatRupiah.format(saldo));

        // Menghitung anggaran hari ini
        double batas = manajer.getAnggaran().getBatasHarian();

        if (batas <= 0) {
            labelAnggaran.setText("Belum diatur");
            labelAnggaran.setForeground(Color.GRAY);
            return;
        }

        double pengeluaranHariIni =
            analisis.hitungPengeluaranHarian(
                manajer.getSemuaTransaksi(),
                LocalDate.now()
            );

        double sisa =
            manajer.getAnggaran().hitungSisa(pengeluaranHariIni);

        String status =
            manajer.getAnggaran().cekStatus(pengeluaranHariIni);

        labelAnggaran.setText(
            "<html>Batas: " + formatRupiah.format(batas)
            + "<br>Terpakai: " + formatRupiah.format(pengeluaranHariIni)
            + "<br>Sisa: " + formatRupiah.format(sisa)
            + "<br>Status: " + status + "</html>"
        );

        if ("MELAMPAUI".equals(status)) {
            labelAnggaran.setForeground(new Color(190, 65, 65));
        } else if ("MENDEKATI".equals(status)) {
            labelAnggaran.setForeground(new Color(180, 120, 30));
        } else {
            labelAnggaran.setForeground(new Color(30, 130, 80));
        }
    }
}
