
package gui;

import model.TargetTabungan;
import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TabunganPanel extends JPanel {

    private final ManajerMySaku manajer;

    private JPanel panelDaftarTarget;
    private JLabel labelJumlahTarget;
    private JLabel labelTotalTarget;
    private JLabel labelTotalTerkumpul;

    private static final Locale LOKAL_INDONESIA =
        new Locale("id", "ID");

    private static final DateTimeFormatter FORMAT_TANGGAL =
        DateTimeFormatter.ofPattern(
            "dd MMMM yyyy",
            LOKAL_INDONESIA
        );

    public TabunganPanel(ManajerMySaku manajer) {
        this.manajer = manajer;

        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        buatTampilan();
        refreshData();
    }

    // =====================================================
    // FORMAT NOMINAL RUPIAH
    // =====================================================

    private String formatRupiah(double nominal) {
        NumberFormat formatter =
            NumberFormat.getNumberInstance(LOKAL_INDONESIA);

        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);

        return "Rp " + formatter.format(nominal);
    }

    // =====================================================
    // TAMPILAN UTAMA
    // =====================================================

    private void buatTampilan() {
        JPanel bagianAtas = new JPanel(new BorderLayout(0, 18));
        bagianAtas.setOpaque(false);

        JPanel judulPanel = new JPanel();
        judulPanel.setLayout(new BoxLayout(judulPanel, BoxLayout.Y_AXIS));
        judulPanel.setOpaque(false);

        JLabel judul = UITheme.label(
            "Target Tabungan",
            UITheme.font(Font.BOLD, 24),
            UITheme.TEXT
        );

        JLabel subjudul = UITheme.label(
            "Rencanakan tujuan keuangan dan pantau progres tabunganmu.",
            UITheme.font(Font.PLAIN, 13),
            UITheme.TEXT_MUTED
        );

        judulPanel.add(judul);
        judulPanel.add(Box.createVerticalStrut(5));
        judulPanel.add(subjudul);

        bagianAtas.add(judulPanel, BorderLayout.WEST);

        JPanel ringkasan = new JPanel(new GridLayout(1, 3, 14, 0));
        ringkasan.setOpaque(false);

        labelJumlahTarget = new JLabel("0");
        labelTotalTarget = new JLabel("Rp 0");
        labelTotalTerkumpul = new JLabel("Rp 0");

        ringkasan.add(buatKartuRingkasan(
            "Jumlah Target",
            labelJumlahTarget,
            UITheme.PRIMARY
        ));

        ringkasan.add(buatKartuRingkasan(
            "Total Nominal Target",
            labelTotalTarget,
            UITheme.ACCENT
        ));

        ringkasan.add(buatKartuRingkasan(
            "Total Terkumpul",
            labelTotalTerkumpul,
            UITheme.SUCCESS
        ));

        bagianAtas.add(ringkasan, BorderLayout.SOUTH);
        add(bagianAtas, BorderLayout.NORTH);

        JPanel bagianDaftar = new JPanel(new BorderLayout(0, 14));
        bagianDaftar.setOpaque(false);

        JPanel headerDaftar = new JPanel(new BorderLayout());
        headerDaftar.setOpaque(false);

        JLabel labelDaftar = UITheme.label(
            "Daftar Target",
            UITheme.font(Font.BOLD, 17),
            UITheme.TEXT
        );

        UITheme.ModernButton tombolTambah =
            new UITheme.ModernButton(
                "+ Tambah Target",
                UITheme.PRIMARY,
                UITheme.ModernButton.FILLED
            );

        tombolTambah.addActionListener(e -> dialogTambahTarget());

        headerDaftar.add(labelDaftar, BorderLayout.WEST);
        headerDaftar.add(tombolTambah, BorderLayout.EAST);

        bagianDaftar.add(headerDaftar, BorderLayout.NORTH);

        panelDaftarTarget = new JPanel();
        panelDaftarTarget.setLayout(
            new BoxLayout(panelDaftarTarget, BoxLayout.Y_AXIS)
        );
        panelDaftarTarget.setBackground(UITheme.BG);

        JScrollPane scrollPane = new JScrollPane(panelDaftarTarget);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(UITheme.BG);
        scrollPane.getViewport().setBackground(UITheme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        UITheme.styleScrollPane(scrollPane);

        bagianDaftar.add(scrollPane, BorderLayout.CENTER);
        add(bagianDaftar, BorderLayout.CENTER);
    }

    private JPanel buatKartuRingkasan(
        String judul,
        JLabel nilai,
        Color warna
    ) {
        UITheme.RoundedPanel kartu =
            new UITheme.RoundedPanel(18, Color.WHITE, true);

        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel labelJudul = UITheme.label(
            judul,
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        nilai.setFont(UITheme.font(Font.BOLD, 21));
        nilai.setForeground(warna);

        kartu.add(labelJudul);
        kartu.add(Box.createVerticalStrut(10));
        kartu.add(nilai);

        return kartu;
    }

    // =====================================================
    // REFRESH DATA
    // =====================================================

    public void refreshData() {
        List<TargetTabungan> daftar =
            manajer.getSemuaTargetTabungan();

        panelDaftarTarget.removeAll();

        double totalTarget = 0;
        double totalTerkumpul = 0;

        for (TargetTabungan target : daftar) {
            totalTarget += target.getNominalTarget();
            totalTerkumpul += target.getNominalTerkumpul();

            panelDaftarTarget.add(buatKartuTarget(target));
            panelDaftarTarget.add(Box.createVerticalStrut(14));
        }

        labelJumlahTarget.setText(String.valueOf(daftar.size()));
        labelTotalTarget.setText(formatRupiah(totalTarget));
        labelTotalTerkumpul.setText(formatRupiah(totalTerkumpul));

        if (daftar.isEmpty()) {
            JLabel kosong = UITheme.label(
                "Belum ada target tabungan. Yuk, buat target pertamamu!",
                UITheme.font(Font.PLAIN, 13),
                UITheme.TEXT_MUTED
            );

            kosong.setAlignmentX(Component.CENTER_ALIGNMENT);

            panelDaftarTarget.add(Box.createVerticalStrut(35));
            panelDaftarTarget.add(kosong);
        }

        panelDaftarTarget.revalidate();
        panelDaftarTarget.repaint();
    }

    // =====================================================
    // KARTU TARGET
    // =====================================================

    private JPanel buatKartuTarget(TargetTabungan target) {
        UITheme.RoundedPanel kartu =
            new UITheme.RoundedPanel(18, Color.WHITE, true);

        kartu.setLayout(new BorderLayout(0, 14));
        kartu.setBorder(new EmptyBorder(18, 20, 18, 20));
        kartu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bagianInfo = new JPanel(new BorderLayout());
        bagianInfo.setOpaque(false);

        JPanel informasi = new JPanel();
        informasi.setLayout(new BoxLayout(informasi, BoxLayout.Y_AXIS));
        informasi.setOpaque(false);

        JLabel nama = UITheme.label(
            target.getNama(),
            UITheme.font(Font.BOLD, 16),
            UITheme.TEXT
        );

        String tanggal = target.getTanggalTarget() == null
            ? "Belum ditentukan"
            : target.getTanggalTarget().format(FORMAT_TANGGAL);

        JLabel tanggalLabel = UITheme.label(
            "Target tanggal: " + tanggal,
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        informasi.add(nama);
        informasi.add(Box.createVerticalStrut(5));
        informasi.add(tanggalLabel);

        JLabel status = new JLabel(
            target.isTercapai() ? "TERCAPAI" : "BERJALAN"
        );

        status.setFont(UITheme.font(Font.BOLD, 10));
        status.setOpaque(true);
        status.setBorder(new EmptyBorder(6, 10, 6, 10));
        status.setForeground(
            target.isTercapai()
                ? UITheme.SUCCESS_DARK
                : UITheme.PRIMARY
        );
        status.setBackground(
            target.isTercapai()
                ? UITheme.SUCCESS_SOFT
                : UITheme.PRIMARY_SOFT
        );

        bagianInfo.add(informasi, BorderLayout.CENTER);
        bagianInfo.add(status, BorderLayout.EAST);

        JPanel bagianProgres = new JPanel(new BorderLayout(0, 8));
        bagianProgres.setOpaque(false);

        double persentase = target.getPersentase();

        JLabel nominal = UITheme.label(
            formatRupiah(target.getNominalTerkumpul())
                + " dari "
                + formatRupiah(target.getNominalTarget()),
            UITheme.font(Font.BOLD, 13),
            UITheme.TEXT
        );

        JLabel persen = UITheme.label(
            String.format(
                LOKAL_INDONESIA,
                "%.1f%%",
                persentase
            ),
            UITheme.font(Font.BOLD, 12),
            UITheme.PRIMARY
        );

        JPanel barisNominal = new JPanel(new BorderLayout());
        barisNominal.setOpaque(false);
        barisNominal.add(nominal, BorderLayout.WEST);
        barisNominal.add(persen, BorderLayout.EAST);

        UITheme.RoundedProgressBar progress =
            new UITheme.RoundedProgressBar();

        progress.setNilai((int) Math.round(persentase));
        progress.setWarna(
            target.isTercapai()
                ? UITheme.SUCCESS
                : UITheme.PRIMARY
        );

        JLabel sisa = UITheme.label(
            target.isTercapai()
                ? "Target tabungan sudah tercapai!"
                : "Kekurangan: " + formatRupiah(
                    target.getSisaTabungan()
                ),
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        JPanel progresIsi = new JPanel();
        progresIsi.setLayout(new BoxLayout(progresIsi, BoxLayout.Y_AXIS));
        progresIsi.setOpaque(false);

        progresIsi.add(barisNominal);
        progresIsi.add(Box.createVerticalStrut(9));
        progresIsi.add(progress);
        progresIsi.add(Box.createVerticalStrut(7));
        progresIsi.add(sisa);

        bagianProgres.add(progresIsi, BorderLayout.CENTER);

        JPanel tombolPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        tombolPanel.setOpaque(false);

        UITheme.ModernButton tombolSetor =
            new UITheme.ModernButton(
                "+ Setor",
                UITheme.SUCCESS,
                UITheme.ModernButton.FILLED
            );

        UITheme.ModernButton tombolEdit =
            new UITheme.ModernButton(
                "Edit",
                UITheme.PRIMARY,
                UITheme.ModernButton.OUTLINE
            );

        UITheme.ModernButton tombolHapus =
            new UITheme.ModernButton(
                "Hapus",
                UITheme.DANGER,
                UITheme.ModernButton.OUTLINE
            );

        tombolSetor.addActionListener(e -> dialogSetoran(target));
        tombolEdit.addActionListener(e -> dialogEditTarget(target));
        tombolHapus.addActionListener(e -> hapusTarget(target));

        tombolPanel.add(tombolSetor);
        tombolPanel.add(tombolEdit);
        tombolPanel.add(tombolHapus);

        JPanel bagianBawah = new JPanel(new BorderLayout());
        bagianBawah.setOpaque(false);
        bagianBawah.add(tombolPanel, BorderLayout.EAST);

        JPanel isi = new JPanel(new BorderLayout(0, 14));
        isi.setOpaque(false);
        isi.add(bagianInfo, BorderLayout.NORTH);
        isi.add(bagianProgres, BorderLayout.CENTER);
        isi.add(bagianBawah, BorderLayout.SOUTH);

        kartu.add(isi, BorderLayout.CENTER);

        return kartu;
    }

    // =====================================================
    // PEMILIH TANGGAL
    // =====================================================

    private JSpinner buatPemilihTanggal(LocalDate tanggalAwal) {
        Date tanggal = Date.from(
            tanggalAwal.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        SpinnerDateModel model = new SpinnerDateModel(
            tanggal,
            null,
            null,
            java.util.Calendar.DAY_OF_MONTH
        );

        JSpinner spinner = new JSpinner(model);

        JSpinner.DateEditor editor =
            new JSpinner.DateEditor(spinner, "dd MMMM yyyy");

        spinner.setEditor(editor);
        editor.getTextField().setEditable(false);
        editor.getTextField().setBackground(Color.WHITE);
        editor.getTextField().setFont(UITheme.font(Font.PLAIN, 13));

        spinner.setPreferredSize(new Dimension(220, 40));

        return spinner;
    }

    private LocalDate ambilTanggal(JSpinner spinner) {
        Date tanggal = (Date) spinner.getValue();

        return tanggal.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }

    // =====================================================
    // DIALOG TAMBAH TARGET
    // =====================================================

    private void dialogTambahTarget() {
        JTextField inputNama = new JTextField();
        JTextField inputNominal = new JTextField();

        UITheme.styleField(inputNama);
        UITheme.styleField(inputNominal);

        JSpinner inputTanggal =
            buatPemilihTanggal(LocalDate.now().plusMonths(1));

        JPanel form = buatFormDialog();

        tambahField(form, "Nama Target", inputNama);
        tambahField(form, "Nominal Target (Rp)", inputNominal);
        tambahField(form, "Tanggal Target", inputTanggal);

        int hasil = tampilkanDialog(form, "Tambah Target Tabungan");

        if (hasil != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String nama = inputNama.getText().trim();
            double nominal = bacaNominal(inputNominal.getText());
            LocalDate tanggal = ambilTanggal(inputTanggal);

            if (nama.isEmpty()) {
                tampilkanPesan("Nama target harus diisi.");
                return;
            }

            if (nominal <= 0) {
                tampilkanPesan("Nominal target harus lebih dari 0.");
                return;
            }

            TargetTabungan target = new TargetTabungan(
                manajer.generateIdTargetTabungan(),
                nama,
                nominal,
                0,
                tanggal
            );

            manajer.tambahTargetTabungan(target);
            simpanDanRefresh();

        } catch (NumberFormatException ex) {
            tampilkanPesan("Nominal tidak valid. Masukkan angka saja.");
        } catch (Exception ex) {
            tampilkanPesan(
                "Gagal menambahkan target: " + ex.getMessage()
            );
        }
    }

    // =====================================================
    // DIALOG EDIT TARGET
    // =====================================================

    private void dialogEditTarget(TargetTabungan target) {
        JTextField inputNama = new JTextField(target.getNama());

        JTextField inputNominal = new JTextField(
            String.valueOf(target.getNominalTarget())
        );

        UITheme.styleField(inputNama);
        UITheme.styleField(inputNominal);

        LocalDate tanggalAwal = target.getTanggalTarget() == null
            ? LocalDate.now().plusMonths(1)
            : target.getTanggalTarget();

        JSpinner inputTanggal = buatPemilihTanggal(tanggalAwal);

        JPanel form = buatFormDialog();

        tambahField(form, "Nama Target", inputNama);
        tambahField(form, "Nominal Target (Rp)", inputNominal);
        tambahField(form, "Tanggal Target", inputTanggal);

        int hasil = tampilkanDialog(form, "Edit Target Tabungan");

        if (hasil != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String nama = inputNama.getText().trim();
            double nominal = bacaNominal(inputNominal.getText());
            LocalDate tanggal = ambilTanggal(inputTanggal);

            if (nama.isEmpty()) {
                tampilkanPesan("Nama target harus diisi.");
                return;
            }

            if (nominal <= 0) {
                tampilkanPesan("Nominal target harus lebih dari 0.");
                return;
            }

            TargetTabungan targetBaru = new TargetTabungan(
                target.getId(),
                nama,
                nominal,
                target.getNominalTerkumpul(),
                tanggal
            );

            manajer.ubahTargetTabungan(target.getId(), targetBaru);
            simpanDanRefresh();

        } catch (NumberFormatException ex) {
            tampilkanPesan("Nominal tidak valid. Masukkan angka saja.");
        } catch (Exception ex) {
            tampilkanPesan(
                "Gagal mengubah target: " + ex.getMessage()
            );
        }
    }

    // =====================================================
    // DIALOG SETORAN
    // =====================================================

    private void dialogSetoran(TargetTabungan target) {
        JTextField inputNominal = new JTextField();
        UITheme.styleField(inputNominal);

        JPanel form = buatFormDialog();

        JLabel informasi = UITheme.label(
            "Target: " + target.getNama(),
            UITheme.font(Font.BOLD, 13),
            UITheme.TEXT
        );

        JLabel terkumpul = UITheme.label(
            "Terkumpul: " + formatRupiah(
                target.getNominalTerkumpul()
            ),
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        form.add(informasi);
        form.add(Box.createVerticalStrut(5));
        form.add(terkumpul);
        form.add(Box.createVerticalStrut(15));

        tambahField(form, "Nominal Setoran (Rp)", inputNominal);

        int hasil = tampilkanDialog(form, "Tambah Setoran");

        if (hasil != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double nominal = bacaNominal(inputNominal.getText());

            if (nominal <= 0) {
                tampilkanPesan("Nominal setoran harus lebih dari 0.");
                return;
            }

            manajer.tambahSetoranTabungan(target.getId(), nominal);
            simpanDanRefresh();

        } catch (NumberFormatException ex) {
            tampilkanPesan("Nominal setoran tidak valid.");
        } catch (Exception ex) {
            tampilkanPesan(
                "Gagal menambahkan setoran: " + ex.getMessage()
            );
        }
    }

    // =====================================================
    // HAPUS TARGET
    // =====================================================

    private void hapusTarget(TargetTabungan target) {
        int pilihan = JOptionPane.showConfirmDialog(
            this,
            "Apakah kamu yakin ingin menghapus target \""
                + target.getNama() + "\"?",
            "Hapus Target",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (pilihan != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manajer.hapusTargetTabungan(target.getId());
            simpanDanRefresh();

        } catch (Exception ex) {
            tampilkanPesan(
                "Gagal menghapus target: " + ex.getMessage()
            );
        }
    }

    // =====================================================
    // HELPER FORM DAN DIALOG
    // =====================================================

    private JPanel buatFormDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 5, 5, 5));
        panel.setPreferredSize(new Dimension(360, 230));

        return panel;
    }

    private void tambahField(
        JPanel panel,
        String judul,
        JComponent komponen
    ) {
        JLabel label = UITheme.label(
            judul,
            UITheme.font(Font.BOLD, 12),
            UITheme.TEXT
        );

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        komponen.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(komponen);
        panel.add(Box.createVerticalStrut(14));
    }

    private int tampilkanDialog(JPanel isi, String judul) {
        return JOptionPane.showConfirmDialog(
            this,
            isi,
            judul,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
    }

    // =====================================================
    // MEMBACA NOMINAL
    // =====================================================

    private double bacaNominal(String teks) {
        String angka = teks.trim()
            .replace("Rp", "")
            .replace("rp", "")
            .replace(".", "")
            .replace(",", "")
            .replace(" ", "");

        if (angka.isEmpty()) {
            throw new NumberFormatException("Nominal belum diisi.");
        }

        return Double.parseDouble(angka);
    }

    private void tampilkanPesan(String pesan) {
        JOptionPane.showMessageDialog(
            this,
            pesan,
            "Informasi",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =====================================================
    // SIMPAN DAN REFRESH
    // =====================================================

    private void simpanDanRefresh() {
        try {
            manajer.simpanKeFile();
            refreshData();

        } catch (Exception ex) {
            tampilkanPesan(
                "Data berubah, tetapi gagal disimpan: "
                    + ex.getMessage()
            );
        }
    }
}
