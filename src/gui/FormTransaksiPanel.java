
package gui;

import analisis.AnalisisKeuangan;
import model.Pemasukan;
import model.Pengeluaran;
import model.Transaksi;
import service.ManajerMySaku;
import util.InputTidakValidException;
import util.ValidasiInput;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Form tambah, ubah, dan hapus transaksi.
 * Dikerjakan oleh: ALHIYA
 *
 * Mode tambah : tombol "Tambah".
 * Mode ubah   : tombol "Ubah", "Hapus", "Batal" (dipicu isiUntukUbah).
 */
public class FormTransaksiPanel extends UITheme.RoundedPanel {

    private static final String PEMASUKAN = "Pemasukan";
    private static final String PENGELUARAN = "Pengeluaran";

    private ManajerMySaku manajer;
    private AnalisisKeuangan analisis = new AnalisisKeuangan();
    private Runnable onTransaksiDisimpan;

    // null = mode tambah, selain itu = ID transaksi yang sedang diubah
    private String idSedangDiubah = null;

    private JLabel labelJudul;
    private JLabel labelSubjudul;

    private SegmentedToggle toggleJenis;
    private JTextField inputNominal;
    private JLabel labelHintNominal;
    private JTextField inputTanggal;
    private JComboBox<String> pilihanKategori;
    private JLabel labelDetail;
    private JComboBox<String> pilihanDetail;
    private JTextField inputCatatan;

    private CardLayout layoutTombol;
    private JPanel panelTombol;
    private UITheme.ModernButton tombolTambah;
    private UITheme.ModernButton tombolUbah;
    private UITheme.ModernButton tombolHapus;
    private UITheme.ModernButton tombolBatal;

    public FormTransaksiPanel(ManajerMySaku manajer) {
        super(20, UITheme.CARD, true);
        this.manajer = manajer;

        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(24, 26, 24, 26));

        // ---------- Judul ----------
        labelJudul = UITheme.label(
            "Tambah Transaksi", UITheme.font(Font.BOLD, 20), UITheme.TEXT
        );
        labelSubjudul = UITheme.label(
            "Catat pemasukan dan pengeluaranmu",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );
        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelSubjudul.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelJudul = new JPanel();
        panelJudul.setLayout(new BoxLayout(panelJudul, BoxLayout.Y_AXIS));
        panelJudul.setOpaque(false);
        panelJudul.add(labelJudul);
        panelJudul.add(Box.createVerticalStrut(4));
        panelJudul.add(labelSubjudul);

        // ---------- Komponen input ----------
        toggleJenis = new SegmentedToggle(this::ubahPilihanJenis);

        inputNominal = new HintField("Contoh: 50000");
        inputTanggal = new HintField("yyyy-MM-dd");
        inputTanggal.setText(LocalDate.now().toString());
        inputCatatan = new HintField("Contoh: makan siang di kantin");

        pilihanKategori = new JComboBox<>();
        pilihanDetail = new JComboBox<>();

        UITheme.styleField(inputNominal);
        UITheme.styleField(inputTanggal);
        UITheme.styleField(inputCatatan);
        UITheme.styleCombo(pilihanKategori);
        UITheme.styleCombo(pilihanDetail);

        labelDetail = buatLabelField("Sumber Pemasukan");

        labelHintNominal = UITheme.label(
            " ", UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );

        inputNominal.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    perbaruiHintNominal();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    perbaruiHintNominal();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    perbaruiHintNominal();
                }
            }
        );

        // Tekan Enter di kolom teks = aksi utama sesuai mode
        inputNominal.addActionListener(e -> aksiUtama());
        inputTanggal.addActionListener(e -> aksiUtama());
        inputCatatan.addActionListener(e -> aksiUtama());

        // ---------- Tombol ----------
        tombolTambah = buatTombol(
            "Tambah Pemasukan", UITheme.SUCCESS_DARK,
            UITheme.ModernButton.FILLED
        );
        tombolUbah = buatTombol(
            "Ubah", UITheme.PRIMARY, UITheme.ModernButton.FILLED
        );
        tombolHapus = buatTombol(
            "Hapus", UITheme.DANGER, UITheme.ModernButton.OUTLINE
        );
        tombolBatal = buatTombol(
            "Batal", UITheme.TEXT_MUTED, UITheme.ModernButton.OUTLINE
        );

        tombolTambah.addActionListener(e -> prosesSimpan(false));
        tombolUbah.addActionListener(e -> prosesSimpan(true));
        tombolHapus.addActionListener(e -> hapusTransaksiTerpilih());
        tombolBatal.addActionListener(e -> keluarModeUbah());

        JPanel kartuTambah = new JPanel(new BorderLayout());
        kartuTambah.setOpaque(false);
        kartuTambah.add(tombolTambah, BorderLayout.CENTER);

        JPanel kartuUbah = new JPanel(new GridLayout(1, 3, 10, 0));
        kartuUbah.setOpaque(false);
        kartuUbah.add(tombolUbah);
        kartuUbah.add(tombolHapus);
        kartuUbah.add(tombolBatal);

        layoutTombol = new CardLayout();
        panelTombol = new JPanel(layoutTombol);
        panelTombol.setOpaque(false);
        panelTombol.add(kartuTambah, "tambah");
        panelTombol.add(kartuUbah, "ubah");

        // ---------- Susunan field ----------
        JPanel barisLabelNominal = new JPanel(new BorderLayout());
        barisLabelNominal.setOpaque(false);
        barisLabelNominal.add(
            buatLabelField("Nominal (Rp)"), BorderLayout.WEST
        );
        barisLabelNominal.add(labelHintNominal, BorderLayout.EAST);

        JPanel duaKolom = new JPanel(new GridLayout(1, 2, 14, 0));
        duaKolom.setOpaque(false);
        duaKolom.add(buatKolom(buatLabelField("Tanggal"), inputTanggal));
        duaKolom.add(buatKolom(buatLabelField("Kategori"), pilihanKategori));

        JPanel panelInput = new JPanel();
        panelInput.setLayout(new BoxLayout(panelInput, BoxLayout.Y_AXIS));
        panelInput.setOpaque(false);

        tambahBaris(panelInput, toggleJenis, 18);
        tambahBaris(panelInput,
            buatKolom(barisLabelNominal, inputNominal), 14);
        tambahBaris(panelInput, duaKolom, 14);
        tambahBaris(panelInput,
            buatKolom(labelDetail, pilihanDetail), 14);
        tambahBaris(panelInput,
            buatKolom(buatLabelField("Catatan (opsional)"), inputCatatan),
            0);

        // Isi awal kategori dan detail
        ubahPilihanJenis();
        setModeUbah(false);

        add(panelJudul, BorderLayout.NORTH);
        add(panelInput, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);
    }

    // ===================== API UNTUK PANEL LAIN =====================

    public void setOnTransaksiDisimpan(Runnable callback) {
        this.onTransaksiDisimpan = callback;
    }

    /**
     * Mengisi form dengan data transaksi dan masuk ke mode ubah.
     */
    public void isiUntukUbah(Transaksi t) {
        if (t == null) {
            return;
        }

        idSedangDiubah = t.getId();

        toggleJenis.setSelected(t.getTipeTransaksi());
        ubahPilihanJenis();

        inputNominal.setText(formatNominalUntukInput(t.getNominal()));
        inputTanggal.setText(
            t.getTanggal() == null
                ? LocalDate.now().toString()
                : t.getTanggal().toString()
        );
        pilihAtauTambah(pilihanKategori, t.getKategori());
        inputCatatan.setText(t.getCatatan() == null ? "" : t.getCatatan());

        if (t instanceof Pemasukan) {
            pilihAtauTambah(pilihanDetail, ((Pemasukan) t).getSumber());
        } else if (t instanceof Pengeluaran) {
            pilihAtauTambah(pilihanDetail, ((Pengeluaran) t).getPrioritas());
        }

        setModeUbah(true);
    }

    // ===================== PEMBUAT KOMPONEN =====================

    private JLabel buatLabelField(String teks) {
        return UITheme.label(
            teks, UITheme.font(Font.BOLD, 12), UITheme.TEXT
        );
    }

    private JPanel buatKolom(JComponent label, JComponent isi) {
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        isi.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel kolom = new JPanel();
        kolom.setLayout(new BoxLayout(kolom, BoxLayout.Y_AXIS));
        kolom.setOpaque(false);
        kolom.add(label);
        kolom.add(Box.createVerticalStrut(6));
        kolom.add(isi);
        return kolom;
    }

    private void tambahBaris(JPanel induk, JComponent baris, int jarak) {
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.setMaximumSize(new Dimension(
            Integer.MAX_VALUE, baris.getPreferredSize().height
        ));
        induk.add(baris);
        if (jarak > 0) {
            induk.add(Box.createVerticalStrut(jarak));
        }
    }

    private UITheme.ModernButton buatTombol(
        String teks, Color warna, int gaya
    ) {
        UITheme.ModernButton tombol =
            new UITheme.ModernButton(teks, warna, gaya);
        tombol.setPreferredSize(new Dimension(100, 44));
        return tombol;
    }

    // ===================== LOGIKA TAMPILAN =====================

    private void ubahPilihanJenis() {

        String jenis = toggleJenis.getSelected();

        pilihanKategori.removeAllItems();
        pilihanDetail.removeAllItems();

        if (PEMASUKAN.equals(jenis)) {

            pilihanKategori.addItem("Uang Saku");
            pilihanKategori.addItem("Beasiswa");
            pilihanKategori.addItem("Hasil Kerja");
            pilihanKategori.addItem("Hadiah");
            pilihanKategori.addItem("Lainnya");

            labelDetail.setText("Sumber Pemasukan");

            pilihanDetail.addItem("Orang Tua");
            pilihanDetail.addItem("Kampus");
            pilihanDetail.addItem("Klien");
            pilihanDetail.addItem("Keluarga");
            pilihanDetail.addItem("Lainnya");

            tombolTambah.setText("Tambah Pemasukan");
            tombolTambah.setBase(UITheme.SUCCESS_DARK);

        } else {

            pilihanKategori.addItem("Makanan");
            pilihanKategori.addItem("Transportasi");
            pilihanKategori.addItem("Akademik");
            pilihanKategori.addItem("Internet");
            pilihanKategori.addItem("Hiburan");
            pilihanKategori.addItem("Belanja");
            pilihanKategori.addItem("Lainnya");

            labelDetail.setText("Prioritas Pengeluaran");

            pilihanDetail.addItem("Kebutuhan");
            pilihanDetail.addItem("Keinginan");

            tombolTambah.setText("Tambah Pengeluaran");
            tombolTambah.setBase(UITheme.DANGER);
        }

        revalidate();
        repaint();
    }

    private void setModeUbah(boolean ubah) {
        if (ubah) {
            labelJudul.setText("Ubah Transaksi");
            labelSubjudul.setText(
                "Mengedit " + idSedangDiubah
                + ". Ubah data lalu tekan Ubah."
            );
            layoutTombol.show(panelTombol, "ubah");
        } else {
            labelJudul.setText("Tambah Transaksi");
            labelSubjudul.setText("Catat pemasukan dan pengeluaranmu");
            layoutTombol.show(panelTombol, "tambah");
        }
    }

    private void keluarModeUbah() {
        idSedangDiubah = null;
        resetInput();
        setModeUbah(false);
    }

    private void resetInput() {
        inputNominal.setText("");
        inputCatatan.setText("");
        inputTanggal.setText(LocalDate.now().toString());
    }

    private void aksiUtama() {
        prosesSimpan(idSedangDiubah != null);
    }

    /**
     * Menampilkan nominal dalam format rupiah di sebelah label.
     */
    private void perbaruiHintNominal() {
        String teks = inputNominal.getText();

        if (teks.trim().isEmpty()) {
            labelHintNominal.setText(" ");
            return;
        }

        try {
            double nominal = ValidasiInput.validasiNominal(
                normalisasiNominal(teks)
            );
            labelHintNominal.setText("= " + UITheme.formatRupiah(nominal));
            labelHintNominal.setForeground(UITheme.TEXT_MUTED);
        } catch (InputTidakValidException e) {
            labelHintNominal.setText("Nominal belum valid");
            labelHintNominal.setForeground(UITheme.DANGER_DARK);
        }
    }

    // ===================== LOGIKA DATA =====================

    private String normalisasiNominal(String teks) {
        String hasil = teks == null ? "" : teks.trim();
        hasil = hasil.replaceAll("(?i)^rp\\s*", "");
        if (hasil.matches("\\d{1,3}(\\.\\d{3})+")) {
            hasil = hasil.replace(".", "");
        }
        return hasil;
    }

    private String formatNominalUntukInput(double nominal) {
        if (nominal == Math.rint(nominal)) {
            return String.valueOf((long) nominal);
        }
        return String.valueOf(nominal);
    }

    private void pilihAtauTambah(JComboBox<String> combo, String nilai) {
        if (nilai == null) {
            return;
        }

        boolean ada = false;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (nilai.equals(combo.getItemAt(i))) {
                ada = true;
                break;
            }
        }
        if (!ada) {
            combo.addItem(nilai);
        }
        combo.setSelectedItem(nilai);
    }

    /**
     * Menambah transaksi baru, atau mengubah transaksi yang sedang diedit.
     */
    private void prosesSimpan(boolean modeUbah) {

        boolean ubah = modeUbah && idSedangDiubah != null;

        try {

            String jenis = toggleJenis.getSelected();

            double nominal = ValidasiInput.validasiNominal(
                normalisasiNominal(inputNominal.getText())
            );
            LocalDate tanggal = ValidasiInput.validasiTanggal(
                inputTanggal.getText()
            );
            String kategori = ValidasiInput.validasiWajibIsi(
                (String) pilihanKategori.getSelectedItem(),
                "Kategori"
            );
            String detail = ValidasiInput.validasiWajibIsi(
                (String) pilihanDetail.getSelectedItem(),
                PEMASUKAN.equals(jenis)
                    ? "Sumber pemasukan"
                    : "Prioritas pengeluaran"
            );
            String catatan = inputCatatan.getText().trim();

            String id = ubah ? idSedangDiubah : manajer.generateId();

            Transaksi transaksi;
            if (PEMASUKAN.equals(jenis)) {
                transaksi = new Pemasukan(
                    id, tanggal, nominal, kategori, catatan, detail
                );
            } else {
                transaksi = new Pengeluaran(
                    id, tanggal, nominal, kategori, catatan, detail
                );
            }

            if (ubah) {
                manajer.ubahTransaksi(id, transaksi);
            } else {
                manajer.tambahTransaksi(transaksi);
            }

            try {
                manajer.simpanKeFile();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Transaksi gagal disimpan ke file: " + e.getMessage(),
                    "Kesalahan Penyimpanan",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (onTransaksiDisimpan != null) {
                onTransaksiDisimpan.run();
            }

            String pesan = ubah
                ? "Transaksi berhasil diubah!"
                : "Transaksi berhasil disimpan!";
            String peringatan = cekPeringatanAnggaran(transaksi);

            if (peringatan != null) {
                JOptionPane.showMessageDialog(
                    this,
                    pesan + "\n\n" + peringatan,
                    "Peringatan Anggaran",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    pesan,
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

            if (ubah) {
                keluarModeUbah();
            } else {
                resetInput();
            }

        } catch (InputTidakValidException e) {

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
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
     * Menghapus transaksi yang sedang diedit, setelah konfirmasi.
     */
    private void hapusTransaksiTerpilih() {

        if (idSedangDiubah == null) {
            return;
        }

        int pilihan = JOptionPane.showOptionDialog(
            this,
            "Yakin ingin menghapus transaksi " + idSedangDiubah + "?\n"
                + "Tindakan ini tidak dapat dibatalkan.",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            new Object[]{"Hapus", "Batal"},
            "Batal"
        );

        if (pilihan != 0) {
            return;
        }

        try {
            manajer.hapusTransaksi(idSedangDiubah);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Gagal Menghapus",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            manajer.simpanKeFile();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                this,
                "Perubahan gagal disimpan ke file: " + e.getMessage(),
                "Kesalahan Penyimpanan",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (onTransaksiDisimpan != null) {
            onTransaksiDisimpan.run();
        }

        JOptionPane.showMessageDialog(
            this,
            "Transaksi berhasil dihapus.",
            "Berhasil",
            JOptionPane.INFORMATION_MESSAGE
        );

        keluarModeUbah();
    }

    /**
     * Mengembalikan peringatan jika pengeluaran pada tanggal tertentu
     * mendekati atau melampaui anggaran tanggal tersebut.
     */
    private String cekPeringatanAnggaran(Transaksi transaksi) {

        if (!(transaksi instanceof Pengeluaran)) {
            return null;
        }

        LocalDate tanggalTransaksi = transaksi.getTanggal();

        if (tanggalTransaksi == null) {
            return null;
        }

        // Peringatan hanya ditampilkan untuk transaksi hari ini.
        LocalDate hariIni = LocalDate.now();

        if (!hariIni.equals(tanggalTransaksi)) {
            return null;
        }

        // Ambil anggaran khusus tanggal hari ini.
        double batas = manajer.getAnggaran()
            .getBatasHarian(tanggalTransaksi);

        // Jika belum diatur, transaksi tetap bisa disimpan.
        if (batas <= 0) {
            return null;
        }

        // Hitung seluruh pengeluaran pada tanggal transaksi.
        double terpakai = analisis.hitungPengeluaranHarian(
            manajer.getSemuaTransaksi(), tanggalTransaksi
        );

        String status = manajer.getAnggaran().cekStatus(
            tanggalTransaksi, terpakai
        );

        if ("MELAMPAUI".equals(status)) {
            return "Pengeluaran hari ini MELAMPAUI batas harian!\n"
                + "Batas: " + UITheme.formatRupiah(batas) + "\n"
                + "Terpakai: " + UITheme.formatRupiah(terpakai) + "\n"
                + "Kelebihan: "
                + UITheme.formatRupiah(terpakai - batas);
        }

        if ("MENDEKATI".equals(status)) {
            return "Pengeluaran hari ini MENDEKATI batas harian.\n"
                + "Batas: " + UITheme.formatRupiah(batas) + "\n"
                + "Terpakai: " + UITheme.formatRupiah(terpakai) + "\n"
                + "Sisa: " + UITheme.formatRupiah(batas - terpakai);
        }

        return null;
    }

    // ===================== KOMPONEN KECIL =====================

    private static class HintField extends JTextField {

        private final String hint;

        HintField(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.antialias(g2);
                g2.setColor(new Color(156, 163, 175));
                g2.setFont(getFont());

                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2
                    + fm.getAscent();
                g2.drawString(hint, in.left, y);
                g2.dispose();
            }
        }
    }

    private static class SegmentedToggle extends JComponent {

        private String terpilih = PEMASUKAN;

        SegmentedToggle(Runnable saatBerubah) {
            setPreferredSize(new Dimension(200, 44));
            setMinimumSize(new Dimension(100, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    String pilihan = e.getX() < getWidth() / 2
                        ? PEMASUKAN : PENGELUARAN;
                    if (!pilihan.equals(terpilih)) {
                        terpilih = pilihan;
                        repaint();
                        saatBerubah.run();
                    }
                }
            });
        }

        String getSelected() {
            return terpilih;
        }

        void setSelected(String jenis) {
            terpilih = PEMASUKAN.equals(jenis)
                ? PEMASUKAN : PENGELUARAN;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);

            int w = getWidth();
            int h = getHeight();
            int pad = 4;
            int lebar = (w - pad * 2) / 2;

            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            boolean masuk = PEMASUKAN.equals(terpilih);
            g2.setColor(masuk ? UITheme.SUCCESS_DARK : UITheme.DANGER);
            g2.fillRoundRect(
                pad + (masuk ? 0 : lebar), pad, lebar, h - pad * 2, 12, 12
            );

            gambarSegmen(g2, PEMASUKAN, UITheme.IconType.INCOME,
                pad, lebar, h, masuk);
            gambarSegmen(g2, PENGELUARAN, UITheme.IconType.EXPENSE,
                pad + lebar, lebar, h, !masuk);

            g2.dispose();
        }

        private void gambarSegmen(
            Graphics2D g2, String teks, UITheme.IconType ikon,
            int x, int lebar, int tinggi, boolean aktif
        ) {
            Color warna = aktif ? Color.WHITE : UITheme.TEXT_MUTED;

            g2.setFont(UITheme.font(Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();

            int ukuranIkon = 16;
            int jarak = 6;
            int total = ukuranIkon + jarak + fm.stringWidth(teks);
            int mulai = x + (lebar - total) / 2;

            new UITheme.VectorIcon(ikon, ukuranIkon, warna)
                .paintIcon(this, g2, mulai, (tinggi - ukuranIkon) / 2);

            g2.setColor(warna);
            int y = (tinggi - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(teks, mulai + ukuranIkon + jarak, y);
        }
    }
}
