
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
    private JComboBox<String> pilihanKategori;
    private JTextField inputCatatan;

    private JLabel labelDetail;
    private JComboBox<String> pilihanDetail;

    public FormTransaksiPanel(ManajerMySaku manajer) {
        this.manajer = manajer;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(220, 225, 230)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);

        // Judul form
        JLabel judul = new JLabel("Tambah Transaksi");
        judul.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Pilihan jenis transaksi
        pilihanJenis = new JComboBox<>(
            new String[]{"Pemasukan", "Pengeluaran"}
        );

        inputNominal = new JTextField();
        inputTanggal = new JTextField(LocalDate.now().toString());
        pilihanKategori = new JComboBox<>();
        inputCatatan = new JTextField();

        labelDetail = new JLabel("Sumber Pemasukan");
        pilihanDetail = new JComboBox<>();

        // Panel input
        JPanel panelInput = new JPanel(
            new GridLayout(0, 1, 5, 5)
        );
        panelInput.setBackground(Color.WHITE);

        panelInput.add(new JLabel("Jenis Transaksi"));
        panelInput.add(pilihanJenis);

        panelInput.add(new JLabel("Nominal (Rp)"));
        panelInput.add(inputNominal);

        panelInput.add(new JLabel("Tanggal (yyyy-MM-dd)"));
        panelInput.add(inputTanggal);

        panelInput.add(new JLabel("Kategori"));
        panelInput.add(pilihanKategori);

        panelInput.add(new JLabel("Catatan"));
        panelInput.add(inputCatatan);

        panelInput.add(labelDetail);
        panelInput.add(pilihanDetail);

        // Tombol simpan
        JButton tombolSimpan = new JButton("Simpan Transaksi");
        tombolSimpan.setBackground(new Color(30, 64, 110));
        tombolSimpan.setForeground(Color.WHITE);
        tombolSimpan.setFocusPainted(false);

        // Mengubah kategori dan detail saat jenis berubah
        pilihanJenis.addActionListener(e -> ubahPilihanJenis());

        // Inisialisasi pilihan pertama
        ubahPilihanJenis();

        tombolSimpan.addActionListener(e -> simpanTransaksi());

        add(judul, BorderLayout.NORTH);
        add(panelInput, BorderLayout.CENTER);
        add(tombolSimpan, BorderLayout.SOUTH);
    }

    /**
     * Mengatur pilihan kategori dan detail
     * berdasarkan jenis transaksi.
     */
    private void ubahPilihanJenis() {

        String jenis = (String) pilihanJenis.getSelectedItem();

        pilihanKategori.removeAllItems();
        pilihanDetail.removeAllItems();

        if ("Pemasukan".equals(jenis)) {

            // Kategori pemasukan
            pilihanKategori.addItem("Uang Saku");
            pilihanKategori.addItem("Beasiswa");
            pilihanKategori.addItem("Hasil Kerja");
            pilihanKategori.addItem("Hadiah");
            pilihanKategori.addItem("Lainnya");

            // Sumber pemasukan
            labelDetail.setText("Sumber Pemasukan");

            pilihanDetail.addItem("Orang Tua");
            pilihanDetail.addItem("Kampus");
            pilihanDetail.addItem("Klien");
            pilihanDetail.addItem("Keluarga");
            pilihanDetail.addItem("Lainnya");

        } else {

            // Kategori pengeluaran
            pilihanKategori.addItem("Makanan");
            pilihanKategori.addItem("Transportasi");
            pilihanKategori.addItem("Akademik");
            pilihanKategori.addItem("Internet");
            pilihanKategori.addItem("Hiburan");
            pilihanKategori.addItem("Belanja");
            pilihanKategori.addItem("Lainnya");

            // Prioritas pengeluaran
            labelDetail.setText("Prioritas Pengeluaran");

            pilihanDetail.addItem("Kebutuhan");
            pilihanDetail.addItem("Keinginan");
        }
    }

    public void setOnTransaksiDisimpan(Runnable callback) {
        this.onTransaksiDisimpan = callback;
    }

    /**
     * Menyimpan transaksi.
     */
    private void simpanTransaksi() {

        try {

            String jenis = (String) pilihanJenis.getSelectedItem();

            double nominal = Double.parseDouble(
                inputNominal.getText().trim()
            );

            LocalDate tanggal = LocalDate.parse(
                inputTanggal.getText().trim()
            );

            String kategori = (String) pilihanKategori.getSelectedItem();
            String catatan = inputCatatan.getText().trim();
            String detail = (String) pilihanDetail.getSelectedItem();

            // Validasi nominal
            if (nominal <= 0) {
                throw new IllegalArgumentException(
                    "Nominal harus lebih dari 0."
                );
            }

            // Validasi kategori
            if (kategori == null || kategori.isEmpty()) {
                throw new IllegalArgumentException(
                    "Kategori wajib dipilih."
                );
            }

            // Validasi detail
            if (detail == null || detail.isEmpty()) {
                throw new IllegalArgumentException(
                    "Silakan pilih sumber atau prioritas transaksi."
                );
            }

            // Membuat transaksi
            String id = manajer.generateId();
            Transaksi transaksi;

            if ("Pemasukan".equals(jenis)) {

                transaksi = new Pemasukan(
                    id,
                    tanggal,
                    nominal,
                    kategori,
                    catatan,
                    detail
                );

            } else {

                transaksi = new Pengeluaran(
                    id,
                    tanggal,
                    nominal,
                    kategori,
                    catatan,
                    detail
                );
            }

            // Menambahkan transaksi
            manajer.tambahTransaksi(transaksi);

            // Memperbarui tabel dan ringkasan
            if (onTransaksiDisimpan != null) {
                onTransaksiDisimpan.run();
            }

            JOptionPane.showMessageDialog(
                this,
                "Transaksi berhasil disimpan!",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Mengosongkan input
            inputNominal.setText("");
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
