
package gui;

import service.ManajerMySaku;
import model.Pemasukan;
import model.Pengeluaran;
import model.Transaksi;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Form input transaksi.
 * Dikerjakan oleh: ALHIYA
 */
public class FormTransaksiPanel extends JPanel {

    private ManajerMySaku manajer;
    private Runnable onTransaksiDisimpan;
    private JComboBox<String> pilihanJenis;
    private JTextField inputNominal;
    private JTextField inputTanggal;
    private JTextField inputKategori;
    private JTextField inputCatatan;

    private JLabel labelDetail;
    private JComboBox<String> pilihanDetail;

    public FormTransaksiPanel(ManajerMySaku manajer) {
        this.manajer = manajer;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);

        // Judul form
        JLabel judul = new JLabel("Tambah Transaksi");
        judul.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Membuat komponen input
        pilihanJenis = new JComboBox<>(
            new String[]{"Pemasukan", "Pengeluaran"}
        );

        inputNominal = new JTextField();
        inputTanggal = new JTextField(LocalDate.now().toString());
        inputKategori = new JTextField();
        inputCatatan = new JTextField();

        labelDetail = new JLabel("Sumber Pemasukan");

        pilihanDetail = new JComboBox<>(
            new String[]{"Orang Tua", "Beasiswa", "Kerja", "Lainnya"}
        );

        // Panel untuk menampung input
        JPanel panelInput = new JPanel(new GridLayout(0, 1, 5, 5));
        panelInput.setBackground(Color.WHITE);

        panelInput.add(new JLabel("Jenis Transaksi"));
        panelInput.add(pilihanJenis);

        panelInput.add(new JLabel("Nominal (Rp)"));
        panelInput.add(inputNominal);

        panelInput.add(new JLabel("Tanggal (yyyy-MM-dd)"));
        panelInput.add(inputTanggal);

        panelInput.add(new JLabel("Kategori"));
        panelInput.add(inputKategori);

        panelInput.add(new JLabel("Catatan"));
        panelInput.add(inputCatatan);

        panelInput.add(labelDetail);
        panelInput.add(pilihanDetail);

        // Tombol simpan
        JButton tombolSimpan = new JButton("Simpan Transaksi");
        tombolSimpan.setBackground(new Color(30, 64, 110));
        tombolSimpan.setForeground(Color.WHITE);
        tombolSimpan.setFocusPainted(false);

        // Mengubah pilihan detail sesuai jenis transaksi
        pilihanJenis.addActionListener(e -> ubahPilihanDetail());

        // Menyimpan transaksi
        tombolSimpan.addActionListener(e -> simpanTransaksi());

        add(judul, BorderLayout.NORTH);
        add(panelInput, BorderLayout.CENTER);
        add(tombolSimpan, BorderLayout.SOUTH);
    }

    private void ubahPilihanDetail() {
        String jenis = (String) pilihanJenis.getSelectedItem();

        pilihanDetail.removeAllItems();

        if ("Pemasukan".equals(jenis)) {
            labelDetail.setText("Sumber Pemasukan");

            pilihanDetail.addItem("Orang Tua");
            pilihanDetail.addItem("Beasiswa");
            pilihanDetail.addItem("Kerja");
            pilihanDetail.addItem("Lainnya");

        } else {
            labelDetail.setText("Prioritas Pengeluaran");

            pilihanDetail.addItem("Kebutuhan");
            pilihanDetail.addItem("Keinginan");
        }
    }
    public void setOnTransaksiDisimpan(Runnable callback) {
    this.onTransaksiDisimpan = callback;
}
    private void simpanTransaksi() {
        try {
            // Mengambil data dari form
            String jenis = (String) pilihanJenis.getSelectedItem();
            double nominal = Double.parseDouble(inputNominal.getText().trim());
            LocalDate tanggal = LocalDate.parse(inputTanggal.getText().trim());

            String kategori = inputKategori.getText().trim();
            String catatan = inputCatatan.getText().trim();
            String detail = (String) pilihanDetail.getSelectedItem();

            // Validasi input
            if (nominal <= 0) {
                throw new IllegalArgumentException(
                    "Nominal harus lebih dari 0."
                );
            }

            if (kategori.isEmpty()) {
                throw new IllegalArgumentException(
                    "Kategori wajib diisi."
                );
            }

            // Membuat objek transaksi
            String id = manajer.generateId();
            Transaksi transaksi;

            if ("Pemasukan".equals(jenis)) {
                transaksi = new Pemasukan(
                    id, tanggal, nominal, kategori, catatan, detail
                );
            } else {
                transaksi = new Pengeluaran(
                    id, tanggal, nominal, kategori, catatan, detail
                );
            }

            // Menambahkan transaksi ke pengelola
            manajer.tambahTransaksi(transaksi);
            if (onTransaksiDisimpan != null) {
    onTransaksiDisimpan.run();
}

            JOptionPane.showMessageDialog(
                this,
                "Transaksi berhasil disimpan!",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Mengosongkan beberapa input
            inputNominal.setText("");
            inputKategori.setText("");
            inputCatatan.setText("");
            inputTanggal.setText(LocalDate.now().toString());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Nominal harus berupa angka.",
                "Input Tidak Valid",
                JOptionPane.ERROR_MESSAGE
            );

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format tanggal harus yyyy-MM-dd.",
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
}
