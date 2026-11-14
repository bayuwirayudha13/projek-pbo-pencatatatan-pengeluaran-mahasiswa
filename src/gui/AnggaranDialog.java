
package gui;

import service.ManajerMySaku;
import analisis.AnalisisKeuangan;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.LocalDate;

/**
 * Dialog atur batas harian.
 * Dikerjakan oleh: ALHIYA
 */
public class AnggaranDialog extends JDialog {

    private ManajerMySaku manajer;
    private JTextField inputBatas;

    public AnggaranDialog(JFrame parent, ManajerMySaku manajer) {
        super(parent, "Atur Batas Harian", true);

        this.manajer = manajer;

        setSize(400, 230);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Panel utama
        JPanel panelUtama = new JPanel(new BorderLayout(10, 15));
        panelUtama.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 15, 20)
        );
        panelUtama.setBackground(Color.WHITE);

        // Judul
        JLabel judul = new JLabel("Pengaturan Anggaran Harian");
        judul.setFont(new Font("SansSerif", Font.BOLD, 17));

        // Informasi batas saat ini
        double batasSaatIni =
                manajer.getAnggaran().getBatasHarian();

        JLabel labelBatas = new JLabel(
            "Batas saat ini: " + formatRupiah(batasSaatIni)
        );

        JPanel panelInfo = new JPanel(new GridLayout(2, 1, 5, 5));
        panelInfo.setBackground(Color.WHITE);
        panelInfo.add(judul);
        panelInfo.add(labelBatas);

        // Input batas anggaran
        JPanel panelInput = new JPanel(new BorderLayout(5, 5));
        panelInput.setBackground(Color.WHITE);

        JLabel labelInput = new JLabel("Masukkan batas harian (Rp)");

        inputBatas = new JTextField();
        inputBatas.setToolTipText("Contoh: 50000");

        panelInput.add(labelInput, BorderLayout.NORTH);
        panelInput.add(inputBatas, BorderLayout.CENTER);

        // Tombol
        JButton tombolSimpan = new JButton("Simpan Anggaran");
        JButton tombolBatal = new JButton("Batal");

        tombolSimpan.setBackground(new Color(30, 64, 110));
        tombolSimpan.setForeground(Color.WHITE);
        tombolSimpan.setFocusPainted(false);

        JPanel panelTombol = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        panelTombol.setBackground(Color.WHITE);
        panelTombol.add(tombolBatal);
        panelTombol.add(tombolSimpan);

        // Aksi tombol
        tombolSimpan.addActionListener(e -> simpanAnggaran());
        tombolBatal.addActionListener(e -> dispose());

        panelUtama.add(panelInfo, BorderLayout.NORTH);
        panelUtama.add(panelInput, BorderLayout.CENTER);

        add(panelUtama, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(tombolSimpan);
    }

    /**
     * Menyimpan batas anggaran harian.
     */
    private void simpanAnggaran() {

        try {
            String teks = inputBatas.getText().trim();

            if (teks.isEmpty()) {
                throw new IllegalArgumentException(
                    "Batas anggaran wajib diisi."
                );
            }

            double batas = Double.parseDouble(teks);

            if (batas <= 0) {
                throw new IllegalArgumentException(
                    "Batas anggaran harus lebih besar dari 0."
                );
            }

            // Menyimpan batas melalui PengendaliAnggaran
            manajer.getAnggaran().setBatasHarian(batas);
            try {
                manajer.simpanKeFile();
            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Anggaran gagal disimpan ke file: " + e.getMessage(),
                    "Kesalahan Penyimpanan",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Menghitung pengeluaran hari ini
            AnalisisKeuangan analisis = new AnalisisKeuangan();

            double pengeluaranHariIni =
                analisis.hitungPengeluaranHarian(
                    manajer.getSemuaTransaksi(),
                    LocalDate.now()
                );

            // Menghitung status dan sisa anggaran
            String status =
                manajer.getAnggaran().cekStatus(pengeluaranHariIni);

            double sisa =
                manajer.getAnggaran().hitungSisa(pengeluaranHariIni);

            JOptionPane.showMessageDialog(
                this,
                "Anggaran berhasil disimpan!\n\n"
                + "Batas harian: " + formatRupiah(batas) + "\n"
                + "Pengeluaran hari ini: "
                + formatRupiah(pengeluaranHariIni) + "\n"
                + "Sisa anggaran: " + formatRupiah(sisa) + "\n"
                + "Status: " + status,
                "Anggaran Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Masukkan nominal berupa angka yang valid.",
                "Input Tidak Valid",
                JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Input Tidak Valid",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Mengubah angka menjadi format Rupiah.
     */
    private String formatRupiah(double nominal) {
        NumberFormat format =
            NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("id-ID")
            );

        return format.format(nominal);
    }
}
