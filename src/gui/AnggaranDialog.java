
package gui;

import analisis.AnalisisKeuangan;
import service.ManajerMySaku;
import util.InputTidakValidException;
import util.ValidasiInput;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Dialog untuk mengatur, memperbarui, dan menghapus anggaran harian
 * berdasarkan tanggal yang dipilih.
 *
 * Dikerjakan oleh: ALHIYA
 */
public class AnggaranDialog extends JDialog {

    private static final String KARTU_FORM = "form";
    private static final String KARTU_HASIL = "hasil";

    private final ManajerMySaku manajer;
    private final AnalisisKeuangan analisis = new AnalisisKeuangan();

    private CardLayout layoutKartu;
    private JPanel areaKartu;

    // Tampilan form
    private JTextField inputBatas;
    private JLabel labelHint;
    private JLabel labelBatasSaatIni;
    private JLabel labelTerpakaiInfo;
    private JLabel labelJudulForm;
    private JLabel labelSubjudulForm;

    // Filter tanggal
    private JSpinner inputTanggal;
    private LocalDate tanggalDipilih = LocalDate.now();

    private UITheme.ModernButton tombolSimpan;
    private UITheme.ModernButton tombolHapus;

    // Tampilan hasil
    private UITheme.Badge badgeHasil;
    private JLabel labelHasilBatas;
    private JLabel labelHasilTerpakai;
    private JLabel labelHasilSisa;
    private UITheme.ModernButton tombolSelesai;

    public AnggaranDialog(JFrame parent, ManajerMySaku manajer) {
        super(parent, "Atur Batas Harian", true);

        this.manajer = manajer;

        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(Color.WHITE);
        isi.setBorder(new EmptyBorder(26, 28, 24, 28));
        setContentPane(isi);

        JPanel kartuForm = buatKartuForm();
        JPanel kartuHasil = buatKartuHasil();

        layoutKartu = new CardLayout();
        areaKartu = new JPanel(layoutKartu);
        areaKartu.setOpaque(false);
        areaKartu.add(kartuForm, KARTU_FORM);
        areaKartu.add(kartuHasil, KARTU_HASIL);

        int tinggi = Math.max(
            kartuForm.getPreferredSize().height,
            kartuHasil.getPreferredSize().height
        );

        areaKartu.setPreferredSize(new Dimension(440, tinggi));
        isi.add(areaKartu, BorderLayout.CENTER);

        perbaruiInfo();

        getRootPane().setDefaultButton(tombolSimpan);

        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        pack();
        setLocationRelativeTo(parent);
    }

    // ===================== TAMPILAN FORM =====================

    private JPanel buatKartuForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        labelJudulForm = UITheme.label(
            "Atur Anggaran Harian",
            UITheme.font(Font.BOLD, 19),
            UITheme.TEXT
        );

        labelSubjudulForm = UITheme.label(
            "Batasi pengeluaran per hari agar keuangan tetap aman",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        labelJudulForm.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelSubjudulForm.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel teksKepala = new JPanel();
        teksKepala.setLayout(new BoxLayout(teksKepala, BoxLayout.Y_AXIS));
        teksKepala.setOpaque(false);
        teksKepala.add(labelJudulForm);
        teksKepala.add(Box.createVerticalStrut(3));
        teksKepala.add(labelSubjudulForm);

        JPanel kepala = new JPanel(new BorderLayout(14, 0));
        kepala.setOpaque(false);

        kepala.add(
            new KotakIkon(
                UITheme.IconType.BUDGET,
                24,
                48,
                UITheme.PRIMARY_DARK,
                UITheme.PRIMARY_SOFT
            ),
            BorderLayout.WEST
        );

        kepala.add(teksKepala, BorderLayout.CENTER);

        // ---------- Filter tanggal ----------

        JLabel labelTanggal = UITheme.label(
            "Lihat anggaran berdasarkan tanggal",
            UITheme.font(Font.BOLD, 12),
            UITheme.TEXT
        );

        SpinnerDateModel modelTanggal = new SpinnerDateModel(
            new Date(),
            null,
            null,
            Calendar.DAY_OF_MONTH
        );

        inputTanggal = new JSpinner(modelTanggal);

        JSpinner.DateEditor editorTanggal =
            new JSpinner.DateEditor(inputTanggal, "dd MMMM yyyy");

        inputTanggal.setEditor(editorTanggal);
        inputTanggal.setFont(UITheme.font(Font.PLAIN, 13));
        inputTanggal.setPreferredSize(new Dimension(180, 38));

        inputTanggal.addChangeListener(e -> {
            Date tanggal = (Date) inputTanggal.getValue();

            tanggalDipilih = tanggal.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

            perbaruiInfoTanggal();
        });

        JPanel panelTanggal = new JPanel(new BorderLayout(0, 8));
        panelTanggal.setOpaque(false);
        panelTanggal.add(labelTanggal, BorderLayout.NORTH);
        panelTanggal.add(inputTanggal, BorderLayout.CENTER);

        // ---------- Kartu informasi ----------

        labelBatasSaatIni = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 17),
            UITheme.PRIMARY_DARK
        );

        labelTerpakaiInfo = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 17),
            UITheme.TEXT
        );

        UITheme.RoundedPanel kartuInfo =
            new UITheme.RoundedPanel(16, new Color(239, 246, 255));

        kartuInfo.setLayout(new GridLayout(1, 2, 16, 0));
        kartuInfo.setBorder(new EmptyBorder(16, 18, 16, 18));

        kartuInfo.add(
            buatBlokInfo("Batas saat ini", labelBatasSaatIni)
        );

        kartuInfo.add(
            buatBlokInfo("Terpakai pada tanggal", labelTerpakaiInfo)
        );

        // ---------- Input anggaran ----------

        inputBatas = new HintField("Contoh: 50000");
        UITheme.styleField(inputBatas);
        inputBatas.setToolTipText("Contoh: 50000");

        labelHint = UITheme.label(
            " ",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        inputBatas.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    perbaruiHint();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    perbaruiHint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    perbaruiHint();
                }
            }
        );

        JLabel labelInput = UITheme.label(
            "Batas harian baru (Rp)",
            UITheme.font(Font.BOLD, 12),
            UITheme.TEXT
        );

        JPanel barisLabel = new JPanel(new BorderLayout());
        barisLabel.setOpaque(false);
        barisLabel.add(labelInput, BorderLayout.WEST);
        barisLabel.add(labelHint, BorderLayout.EAST);

        // ---------- Tombol nominal cepat ----------

        JPanel panelCepat = new JPanel(new GridLayout(1, 3, 8, 0));
        panelCepat.setOpaque(false);

        panelCepat.add(buatTombolCepat(25000));
        panelCepat.add(buatTombolCepat(50000));
        panelCepat.add(buatTombolCepat(100000));

        // ---------- Tombol aksi ----------

        UITheme.ModernButton tombolBatal =
            new UITheme.ModernButton(
                "Batal",
                UITheme.TEXT_MUTED,
                UITheme.ModernButton.OUTLINE
            );

        tombolHapus = new UITheme.ModernButton(
            "Hapus",
            UITheme.DANGER_DARK,
            UITheme.ModernButton.SOFT
        );

        tombolSimpan = new UITheme.ModernButton(
            "Simpan Anggaran",
            UITheme.PRIMARY,
            UITheme.ModernButton.FILLED
        );

        tombolBatal.setPreferredSize(new Dimension(90, 44));
        tombolHapus.setPreferredSize(new Dimension(90, 44));
        tombolSimpan.setPreferredSize(new Dimension(130, 44));

        tombolBatal.addActionListener(e -> dispose());
        tombolHapus.addActionListener(e -> hapusAnggaran());
        tombolSimpan.addActionListener(e -> simpanAnggaran());

        JPanel panelTombol = new JPanel(new GridLayout(1, 3, 8, 0));
        panelTombol.setOpaque(false);
        panelTombol.add(tombolBatal);
        panelTombol.add(tombolHapus);
        panelTombol.add(tombolSimpan);

        // ---------- Susun komponen ----------

        tambah(panel, kepala, 18);
        tambah(panel, panelTanggal, 16);
        tambah(panel, kartuInfo, 20);
        tambah(panel, barisLabel, 6);
        tambah(panel, inputBatas, 10);
        tambah(panel, panelCepat, 24);

        panel.add(Box.createVerticalGlue());

        tambah(panel, panelTombol, 0);

        return panel;
    }

    private JPanel buatBlokInfo(String judul, JLabel labelNilai) {
        JLabel labelJudul = UITheme.label(
            judul,
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelNilai.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel blok = new JPanel();
        blok.setLayout(new BoxLayout(blok, BoxLayout.Y_AXIS));
        blok.setOpaque(false);
        blok.add(labelJudul);
        blok.add(Box.createVerticalStrut(4));
        blok.add(labelNilai);

        return blok;
    }

    private UITheme.ModernButton buatTombolCepat(int nominal) {
        UITheme.ModernButton tombol =
            new UITheme.ModernButton(
                UITheme.formatRupiah(nominal),
                UITheme.PRIMARY,
                UITheme.ModernButton.SOFT
            );

        tombol.setFont(UITheme.font(Font.BOLD, 12));
        tombol.setBorder(new EmptyBorder(8, 8, 8, 8));

        tombol.addActionListener(e -> {
            inputBatas.setText(String.valueOf(nominal));
            inputBatas.requestFocusInWindow();
        });

        return tombol;
    }

    private void tambah(
        JPanel induk,
        JComponent komponen,
        int jarak
    ) {
        komponen.setAlignmentX(Component.LEFT_ALIGNMENT);

        komponen.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                komponen.getPreferredSize().height
            )
        );

        induk.add(komponen);

        if (jarak > 0) {
            induk.add(Box.createVerticalStrut(jarak));
        }
    }

    // ===================== TAMPILAN HASIL =====================

    private JPanel buatKartuHasil() {
        JPanel panel = new JPanel(new BorderLayout(0, 22));
        panel.setOpaque(false);

        JLabel judul = UITheme.label(
            "Anggaran Tersimpan",
            UITheme.font(Font.BOLD, 19),
            UITheme.TEXT
        );

        JLabel subjudul = UITheme.label(
            "Batas harianmu sudah diperbarui",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        badgeHasil = new UITheme.Badge(
            "AMAN",
            UITheme.SUCCESS_DARK,
            UITheme.SUCCESS_SOFT
        );

        badgeHasil.setFont(UITheme.font(Font.BOLD, 12));
        badgeHasil.setBorder(new EmptyBorder(6, 18, 6, 18));

        JComponent centang = new CentangHijau();

        centang.setAlignmentX(Component.CENTER_ALIGNMENT);
        judul.setAlignmentX(Component.CENTER_ALIGNMENT);
        subjudul.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgeHasil.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel atas = new JPanel();
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        atas.setOpaque(false);

        atas.add(centang);
        atas.add(Box.createVerticalStrut(14));
        atas.add(judul);
        atas.add(Box.createVerticalStrut(4));
        atas.add(subjudul);
        atas.add(Box.createVerticalStrut(14));
        atas.add(badgeHasil);

        // ---------- Rincian ----------

        labelHasilBatas = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 14),
            UITheme.TEXT
        );

        labelHasilTerpakai = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 14),
            UITheme.TEXT
        );

        labelHasilSisa = UITheme.label(
            "-",
            UITheme.font(Font.BOLD, 14),
            UITheme.TEXT
        );

        JPanel rincian = new JPanel();
        rincian.setLayout(new BoxLayout(rincian, BoxLayout.Y_AXIS));
        rincian.setOpaque(false);

        rincian.add(buatBaris("Batas harian", labelHasilBatas));
        rincian.add(
            buatBaris("Pengeluaran tanggal dipilih", labelHasilTerpakai)
        );
        rincian.add(buatBaris("Sisa anggaran", labelHasilSisa));

        // ---------- Tombol ----------

        tombolSelesai = new UITheme.ModernButton(
            "Selesai",
            UITheme.PRIMARY,
            UITheme.ModernButton.FILLED
        );

        tombolSelesai.setPreferredSize(new Dimension(100, 44));
        tombolSelesai.addActionListener(e -> dispose());

        panel.add(atas, BorderLayout.NORTH);
        panel.add(rincian, BorderLayout.CENTER);
        panel.add(tombolSelesai, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buatBaris(String judul, JLabel labelNilai) {
        JLabel labelJudul = UITheme.label(
            judul,
            UITheme.font(Font.PLAIN, 13),
            UITheme.TEXT_MUTED
        );

        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);

        baris.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                    0, 0, 1, 0, new Color(241, 245, 249)
                ),
                new EmptyBorder(12, 2, 12, 2)
            )
        );

        baris.add(labelJudul, BorderLayout.WEST);
        baris.add(labelNilai, BorderLayout.EAST);

        baris.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                baris.getPreferredSize().height
            )
        );

        return baris;
    }

    private void tampilkanHasil(
        double batas,
        double terpakai,
        double sisa,
        String status
    ) {
        labelHasilBatas.setText(UITheme.formatRupiah(batas));
        labelHasilTerpakai.setText(UITheme.formatRupiah(terpakai));
        labelHasilSisa.setText(UITheme.formatRupiah(sisa));

        labelHasilSisa.setForeground(
            sisa < 0 ? UITheme.DANGER_DARK : UITheme.TEXT
        );

        badgeHasil.setText(status);

        if ("MELAMPAUI".equals(status)) {
            badgeHasil.setWarna(
                UITheme.DANGER_DARK,
                UITheme.DANGER_SOFT
            );
        } else if ("MENDEKATI".equals(status)) {
            badgeHasil.setWarna(
                UITheme.WARNING_DARK,
                UITheme.WARNING_SOFT
            );
        } else {
            badgeHasil.setWarna(
                UITheme.SUCCESS_DARK,
                UITheme.SUCCESS_SOFT
            );
        }

        layoutKartu.show(areaKartu, KARTU_HASIL);
        getRootPane().setDefaultButton(tombolSelesai);
        tombolSelesai.requestFocusInWindow();
    }

    // ===================== LOGIKA DATA =====================

    /**
     * Memperbarui informasi berdasarkan tanggal yang dipilih.
     */
    private void perbaruiInfo() {
        double batas = manajer.getAnggaran()
            .getBatasHarian(tanggalDipilih);

        double terpakai = analisis.hitungPengeluaranHarian(
            manajer.getSemuaTransaksi(),
            tanggalDipilih
        );

        boolean sudahDiatur = batas > 0;

        labelBatasSaatIni.setText(
            sudahDiatur
                ? UITheme.formatRupiah(batas)
                : "Belum diatur"
        );

        labelTerpakaiInfo.setText(
            UITheme.formatRupiah(terpakai)
        );

        labelJudulForm.setText(
            sudahDiatur
                ? "Perbarui Anggaran Harian"
                : "Atur Anggaran Harian"
        );

        labelSubjudulForm.setText(
            sudahDiatur
                ? "Ubah batas pengeluaran harian sesuai kebutuhanmu"
                : "Batasi pengeluaran per hari agar keuangan tetap aman"
        );

        tombolSimpan.setText(
            sudahDiatur
                ? "Perbarui Anggaran"
                : "Simpan Anggaran"
        );

        tombolHapus.setVisible(sudahDiatur);

        // Isi nominal sesuai anggaran tanggal terpilih.
        inputBatas.setText(
            sudahDiatur ? formatNominalInput(batas) : ""
        );
    }

    /**
     * Dipanggil setiap kali tanggal pada kalender berubah.
     */
    private void perbaruiInfoTanggal() {
        perbaruiInfo();
        revalidate();
        repaint();
    }

    private String formatNominalInput(double nominal) {
        return String.valueOf((long) nominal);
    }

    private void perbaruiHint() {
        String teks = inputBatas.getText();

        if (teks.trim().isEmpty()) {
            labelHint.setText(" ");
            return;
        }

        try {
            double nominal = ValidasiInput.validasiNominal(
                normalisasiNominal(teks)
            );

            labelHint.setText(
                "= " + UITheme.formatRupiah(nominal)
            );

            labelHint.setForeground(UITheme.TEXT_MUTED);

        } catch (InputTidakValidException e) {
            labelHint.setText("Nominal belum valid");
            labelHint.setForeground(UITheme.DANGER_DARK);
        }
    }

    private String normalisasiNominal(String teks) {
        String hasil = teks == null ? "" : teks.trim();

        hasil = hasil.replaceAll("(?i)^rp\\s*", "");

        if (hasil.matches("\\d{1,3}(\\.\\d{3})+")) {
            hasil = hasil.replace(".", "");
        }

        return hasil;
    }

    // ===================== SIMPAN ANGGARAN =====================

    private void simpanAnggaran() {
        double batasSebelumnya = manajer.getAnggaran()
            .getBatasHarian(tanggalDipilih);

        try {
            double batas = ValidasiInput.validasiNominal(
                normalisasiNominal(inputBatas.getText())
            );

            // Simpan anggaran untuk tanggal yang dipilih.
            manajer.getAnggaran().setBatasHarian(
                tanggalDipilih,
                batas
            );

            try {
                manajer.simpanKeFile();
            } catch (IOException e) {
                // Pulihkan data sebelumnya jika penyimpanan gagal.
                pulihkanAnggaranSebelumnya(batasSebelumnya);

                JOptionPane.showMessageDialog(
                    this,
                    "Anggaran gagal disimpan ke file: "
                        + e.getMessage(),
                    "Kesalahan Penyimpanan",
                    JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            double terpakai = analisis.hitungPengeluaranHarian(
                manajer.getSemuaTransaksi(),
                tanggalDipilih
            );

            String status = manajer.getAnggaran().cekStatus(
                tanggalDipilih,
                terpakai
            );

            double sisa = manajer.getAnggaran().hitungSisa(
                tanggalDipilih,
                terpakai
            );

            tampilkanHasil(
                batas,
                terpakai,
                sisa,
                status
            );

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
     * Mengembalikan anggaran sebelumnya setelah penyimpanan gagal.
     */
    private void pulihkanAnggaranSebelumnya(double batasSebelumnya) {
        if (batasSebelumnya > 0) {
            manajer.getAnggaran().setBatasHarian(
                tanggalDipilih,
                batasSebelumnya
            );
        } else {
            manajer.getAnggaran().hapusAnggaran(tanggalDipilih);
        }
    }

    // ===================== HAPUS ANGGARAN =====================

    private void hapusAnggaran() {
        double batasSebelumnya = manajer.getAnggaran()
            .getBatasHarian(tanggalDipilih);

        if (batasSebelumnya <= 0) {
            return;
        }

        int pilihan = JOptionPane.showConfirmDialog(
            this,
            "Apakah kamu yakin ingin menghapus anggaran untuk tanggal "
                + tanggalDipilih + "?\n"
                + "Anggaran pada tanggal lain tidak akan terhapus.",
            "Konfirmasi Hapus Anggaran",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (pilihan != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manajer.getAnggaran().hapusAnggaran(tanggalDipilih);
            manajer.simpanKeFile();

            JOptionPane.showMessageDialog(
                this,
                "Anggaran tanggal " + tanggalDipilih
                    + " berhasil dihapus.",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (IOException e) {
            // Pulihkan anggaran jika penyimpanan penghapusan gagal.
            manajer.getAnggaran().setBatasHarian(
                tanggalDipilih,
                batasSebelumnya
            );

            perbaruiInfoTanggal();

            JOptionPane.showMessageDialog(
                this,
                "Anggaran gagal dihapus: " + e.getMessage(),
                "Kesalahan Penyimpanan",
                JOptionPane.ERROR_MESSAGE
            );
        }
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

                FontMetrics fm = g2.getFontMetrics();

                int y = (getHeight() - fm.getHeight()) / 2
                    + fm.getAscent();

                g2.drawString(
                    hint,
                    getInsets().left,
                    y
                );

                g2.dispose();
            }
        }
    }

    private static class KotakIkon extends JComponent {
        private final UITheme.IconType ikon;
        private final int ukuranIkon;
        private final Color warna;
        private final Color latar;

        KotakIkon(
            UITheme.IconType ikon,
            int ukuranIkon,
            int ukuranKotak,
            Color warna,
            Color latar
        ) {
            this.ikon = ikon;
            this.ukuranIkon = ukuranIkon;
            this.warna = warna;
            this.latar = latar;

            Dimension ukuran = new Dimension(
                ukuranKotak,
                ukuranKotak
            );

            setPreferredSize(ukuran);
            setMinimumSize(ukuran);
            setMaximumSize(ukuran);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            UITheme.antialias(g2);

            g2.setColor(latar);

            g2.fillRoundRect(
                0, 0, getWidth(), getHeight(), 16, 16
            );

            new UITheme.VectorIcon(
                ikon,
                ukuranIkon,
                warna
            ).paintIcon(
                this,
                g2,
                (getWidth() - ukuranIkon) / 2,
                (getHeight() - ukuranIkon) / 2
            );

            g2.dispose();
        }
    }

    private static class CentangHijau extends JComponent {
        CentangHijau() {
            Dimension ukuran = new Dimension(64, 64);

            setPreferredSize(ukuran);
            setMinimumSize(ukuran);
            setMaximumSize(ukuran);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            UITheme.antialias(g2);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(UITheme.SUCCESS_SOFT);
            g2.fillOval(0, 0, w, h);

            g2.setColor(UITheme.SUCCESS_DARK);

            g2.setStroke(
                new BasicStroke(
                    4f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
                )
            );

            g2.drawLine(
                (int) (w * 0.29),
                (int) (h * 0.52),
                (int) (w * 0.44),
                (int) (h * 0.67)
            );

            g2.drawLine(
                (int) (w * 0.44),
                (int) (h * 0.67),
                (int) (w * 0.72),
                (int) (h * 0.35)
            );

            g2.dispose();
        }
    }
}
