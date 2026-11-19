
package gui;

import model.Transaksi;
import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tabel riwayat transaksi dan pencarian.
 * Dikerjakan oleh: ALHIYA
 *
 * Fitur:
 * - Menampilkan transaksi terbaru.
 * - Pencarian transaksi.
 * - Mengubah transaksi.
 * - Mode pilih untuk menghapus beberapa transaksi.
 */
public class TabelTransaksiPanel extends UITheme.RoundedPanel {

    private static final int KOLOM_CHECKBOX = 0;
    private static final int KOLOM_ID = 1;
    private static final int KOLOM_JENIS = 3;
    private static final int KOLOM_NOMINAL = 5;

    private ManajerMySaku manajer;
    private Consumer<Transaksi> onUbahTransaksi;

    private List<Transaksi> tampil = new ArrayList<>();

    private JTable tabelTransaksi;
    private DefaultTableModel modelTabel;
    private SearchField inputPencarian;

    private CardLayout layoutKonten;
    private JPanel panelKonten;
    private JLabel labelKosong;
    private JLabel labelJumlah;

    private JCheckBox pilihSemua;
    private UITheme.ModernButton tombolPilih;
    private UITheme.ModernButton tombolUbah;
    private UITheme.ModernButton tombolHapusDipilih;

    private boolean modePilih = false;
    private TableColumn kolomCheckbox;

    public TabelTransaksiPanel(ManajerMySaku manajer) {
        super(20, UITheme.CARD, true);
        this.manajer = manajer;

        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 26, 20, 26));

        // ---------- Judul ----------
        JLabel judul = UITheme.label(
            "Semua Transaksi",
            UITheme.font(Font.BOLD, 18),
            UITheme.TEXT
        );

        JLabel deskripsi = UITheme.label(
            "Riwayat seluruh pemasukan dan pengeluaran kamu",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        JPanel panelJudul = new JPanel();
        panelJudul.setLayout(new BoxLayout(panelJudul, BoxLayout.Y_AXIS));
        panelJudul.setOpaque(false);
        panelJudul.add(judul);
        panelJudul.add(Box.createVerticalStrut(4));
        panelJudul.add(deskripsi);

        // ---------- Tombol bagian atas ----------
        tombolPilih = new UITheme.ModernButton(
            "Pilih",
            UITheme.DANGER_DARK,
            UITheme.ModernButton.OUTLINE
        );

        tombolUbah = new UITheme.ModernButton(
            "Ubah Terpilih",
            UITheme.PRIMARY,
            UITheme.ModernButton.SOFT
        );

        tombolPilih.addActionListener(e -> ubahModePilih());
        tombolUbah.addActionListener(e -> ubahBarisTerpilih());

        JPanel sisiKananJudul = new JPanel(new FlowLayout(
            FlowLayout.RIGHT, 8, 0
        ));
        sisiKananJudul.setOpaque(false);
        sisiKananJudul.add(tombolUbah);
        sisiKananJudul.add(tombolPilih);

        JPanel barisJudul = new JPanel(new BorderLayout());
        barisJudul.setOpaque(false);
        barisJudul.add(panelJudul, BorderLayout.WEST);
        barisJudul.add(sisiKananJudul, BorderLayout.EAST);

        // ---------- Pencarian ----------
        inputPencarian = new SearchField(
            "Cari ID, tanggal, jenis, kategori, atau catatan"
        );

        inputPencarian.setToolTipText(
            "Cari berdasarkan ID, tanggal, jenis, kategori, atau catatan"
        );

        UITheme.ModernButton tombolCari = new UITheme.ModernButton(
            "Cari",
            UITheme.PRIMARY,
            UITheme.ModernButton.FILLED
        );

        UITheme.ModernButton tombolReset = new UITheme.ModernButton(
            "Reset",
            UITheme.TEXT_MUTED,
            UITheme.ModernButton.OUTLINE
        );

        JPanel panelTombol = new JPanel(new GridLayout(1, 2, 8, 0));
        panelTombol.setOpaque(false);
        panelTombol.add(tombolCari);
        panelTombol.add(tombolReset);

        JPanel panelPencarian = new JPanel(new BorderLayout(10, 0));
        panelPencarian.setOpaque(false);
        panelPencarian.add(inputPencarian, BorderLayout.CENTER);
        panelPencarian.add(panelTombol, BorderLayout.EAST);

        // ---------- Panel aksi mode pilih ----------
        pilihSemua = new JCheckBox("Pilih Semua");
        pilihSemua.setOpaque(false);
        pilihSemua.setFont(UITheme.font(Font.PLAIN, 12));
        pilihSemua.setForeground(UITheme.TEXT);

        tombolHapusDipilih = new UITheme.ModernButton(
            "Hapus yang Dipilih",
            UITheme.DANGER_DARK,
            UITheme.ModernButton.FILLED
        );

        tombolHapusDipilih.setEnabled(false);

        JPanel panelAksi = new JPanel(new BorderLayout());
        panelAksi.setOpaque(false);
        panelAksi.add(pilihSemua, BorderLayout.WEST);
        panelAksi.add(tombolHapusDipilih, BorderLayout.EAST);
        panelAksi.setVisible(false);

        JPanel panelAtas = new JPanel(new BorderLayout(0, 12));
        panelAtas.setOpaque(false);
        panelAtas.add(barisJudul, BorderLayout.NORTH);
        panelAtas.add(panelPencarian, BorderLayout.CENTER);
        panelAtas.add(panelAksi, BorderLayout.SOUTH);

        // ---------- Tabel ----------
        String[] kolom = {
            "Pilih", "ID", "Tanggal", "Jenis",
            "Kategori", "Nominal", "Catatan"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == KOLOM_CHECKBOX;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == KOLOM_CHECKBOX
                    ? Boolean.class
                    : String.class;
            }
        };

        tabelTransaksi = new JTable(modelTabel);
        tabelTransaksi.setRowHeight(48);
        tabelTransaksi.setFont(UITheme.font(Font.PLAIN, 13));
        tabelTransaksi.setForeground(UITheme.TEXT);
        tabelTransaksi.setBackground(Color.WHITE);
        tabelTransaksi.setSelectionBackground(UITheme.PRIMARY_SOFT);
        tabelTransaksi.setSelectionForeground(UITheme.TEXT);
        tabelTransaksi.setShowVerticalLines(false);
        tabelTransaksi.setShowHorizontalLines(true);
        tabelTransaksi.setGridColor(new Color(241, 245, 249));
        tabelTransaksi.setIntercellSpacing(new Dimension(0, 1));
        tabelTransaksi.setFillsViewportHeight(true);
        tabelTransaksi.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        // Header tabel
        JTableHeader header = tabelTransaksi.getTableHeader();
        header.setPreferredSize(new Dimension(0, 44));
        header.setBackground(UITheme.TABLE_HEAD);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setDefaultRenderer(new HeaderRenderer());

        // Renderer isi tabel
        TeksRenderer rendererTeks = new TeksRenderer();
        JenisRenderer rendererJenis = new JenisRenderer();

        for (int i = 1; i < tabelTransaksi.getColumnCount(); i++) {
            tabelTransaksi.getColumnModel().getColumn(i).setCellRenderer(
                i == KOLOM_JENIS ? rendererJenis : rendererTeks
            );
        }

        // Lebar kolom
        int[] lebar = {55, 80, 110, 140, 140, 150, 260};

        for (int i = 0; i < lebar.length; i++) {
            tabelTransaksi.getColumnModel().getColumn(i)
                .setPreferredWidth(lebar[i]);
        }

        // Sembunyikan checkbox saat pertama kali dibuka
        kolomCheckbox = tabelTransaksi.getColumnModel().getColumn(
            KOLOM_CHECKBOX
        );

        sembunyikanKolomCheckbox();

        // Klik dua kali pada baris = ubah transaksi
        tabelTransaksi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int baris = tabelTransaksi.rowAtPoint(e.getPoint());

                if (!modePilih && e.getClickCount() == 2 && baris >= 0) {
                    ubahBarisTerpilih();
                }
            }
        });

        // Pilih semua transaksi
        pilihSemua.addActionListener(e -> {
            boolean dipilih = pilihSemua.isSelected();

            for (int i = 0; i < modelTabel.getRowCount(); i++) {
                modelTabel.setValueAt(dipilih, i, KOLOM_CHECKBOX);
            }

            perbaruiTombolHapus();
        });

        // Perbarui tombol ketika checkbox berubah
        modelTabel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE
                && e.getColumn() == KOLOM_CHECKBOX) {
                perbaruiTombolHapus();
            }
        });

        tombolHapusDipilih.addActionListener(
            e -> hapusTransaksiDipilih()
        );

        JScrollPane scrollPane = new JScrollPane(tabelTransaksi);
        UITheme.styleScrollPane(scrollPane);

        scrollPane.setBorder(BorderFactory.createMatteBorder(
            1, 0, 1, 0, UITheme.BORDER
        ));

        // ---------- Tampilan kosong ----------
        labelKosong = UITheme.label(
            "Belum ada transaksi",
            UITheme.font(Font.PLAIN, 14),
            UITheme.TEXT_MUTED
        );

        labelKosong.setIcon(new UITheme.VectorIcon(
            UITheme.IconType.HISTORY,
            44,
            new Color(203, 213, 225)
        ));

        labelKosong.setHorizontalTextPosition(SwingConstants.CENTER);
        labelKosong.setVerticalTextPosition(SwingConstants.BOTTOM);
        labelKosong.setIconTextGap(14);

        JPanel panelKosong = new JPanel(new GridBagLayout());
        panelKosong.setBackground(Color.WHITE);
        panelKosong.setBorder(BorderFactory.createMatteBorder(
            1, 0, 1, 0, UITheme.BORDER
        ));
        panelKosong.add(labelKosong);

        layoutKonten = new CardLayout();
        panelKonten = new JPanel(layoutKonten);
        panelKonten.setOpaque(false);
        panelKonten.add(scrollPane, "tabel");
        panelKonten.add(panelKosong, "kosong");

        // ---------- Bagian bawah ----------
        labelJumlah = UITheme.label(
            " ",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        // ---------- Aksi pencarian ----------
        tombolCari.addActionListener(e -> cariData());
        inputPencarian.addActionListener(e -> cariData());

        tombolReset.addActionListener(e -> {
            inputPencarian.setText("");
            refreshTable();
        });

        add(panelAtas, BorderLayout.NORTH);
        add(panelKonten, BorderLayout.CENTER);
        add(labelJumlah, BorderLayout.SOUTH);

        refreshTable();
    }

    // ===================== API UNTUK PANEL LAIN =====================

    public void setOnUbahTransaksi(Consumer<Transaksi> callback) {
        this.onUbahTransaksi = callback;
    }

    public void refreshTable() {
        if (inputPencarian.getText().trim().isEmpty()) {
            tampilkanData(manajer.getSemuaTransaksi());
        } else {
            cariData();
        }
    }

    // ===================== MODE PILIH =====================

    private void ubahModePilih() {
        modePilih = !modePilih;

        if (modePilih) {
            tombolPilih.setText("Batal");
            tombolUbah.setVisible(false);

            tampilkanKolomCheckbox();
            pilihSemua.setSelected(false);
            tombolHapusDipilih.setEnabled(false);

            // Panel aksi ditampilkan
            ((JPanel) pilihSemua.getParent()).setVisible(true);

        } else {
            tombolPilih.setText("Pilih");
            tombolUbah.setVisible(true);

            sembunyikanKolomCheckbox();

            pilihSemua.setSelected(false);
            bersihkanPilihan();

            ((JPanel) pilihSemua.getParent()).setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void tampilkanKolomCheckbox() {
        kolomCheckbox.setMinWidth(45);
        kolomCheckbox.setMaxWidth(65);
        kolomCheckbox.setPreferredWidth(55);
        kolomCheckbox.setWidth(55);
    }

    private void sembunyikanKolomCheckbox() {
        kolomCheckbox.setMinWidth(0);
        kolomCheckbox.setMaxWidth(0);
        kolomCheckbox.setPreferredWidth(0);
        kolomCheckbox.setWidth(0);
    }

    private void bersihkanPilihan() {
        for (int i = 0; i < modelTabel.getRowCount(); i++) {
            modelTabel.setValueAt(false, i, KOLOM_CHECKBOX);
        }

        pilihSemua.setSelected(false);
        perbaruiTombolHapus();
    }

    // ===================== LOGIKA DATA =====================

    private LocalDate parseTanggal(Transaksi transaksi) {
        if (transaksi.getTanggal() == null) {
            return LocalDate.MIN;
        }

        String tanggal = transaksi.getTanggal().toString().trim();

        String[] formatTanggal = {
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy"
        };

        for (String format : formatTanggal) {
            try {
                return LocalDate.parse(
                    tanggal,
                    DateTimeFormatter.ofPattern(format)
                );
            } catch (DateTimeParseException e) {
                // Mencoba format tanggal berikutnya
            }
        }

        return LocalDate.MIN;
    }

    private void ubahBarisTerpilih() {
        int baris = tabelTransaksi.getSelectedRow();

        if (baris < 0 || baris >= tampil.size()) {
            JOptionPane.showMessageDialog(
                this,
                "Pilih satu transaksi di tabel terlebih dahulu.",
                "Belum Ada yang Dipilih",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (onUbahTransaksi != null) {
            onUbahTransaksi.accept(tampil.get(baris));
        }
    }

    private void tampilkanData(List<Transaksi> daftar) {
        modelTabel.setRowCount(0);

        tampil = new ArrayList<>(daftar);

        tampil.sort(
            Comparator.comparing(this::parseTanggal).reversed()
        );

        for (Transaksi transaksi : tampil) {
            String tipe = transaksi.getTipeTransaksi();
            String tanda = "";

            if ("Pemasukan".equalsIgnoreCase(tipe)) {
                tanda = "+ ";
            } else if ("Pengeluaran".equalsIgnoreCase(tipe)) {
                tanda = "- ";
            }

            Object[] baris = {
                false,
                transaksi.getId(),
                transaksi.getTanggal(),
                tipe,
                transaksi.getKategori(),
                tanda + UITheme.formatRupiah(transaksi.getNominal()),
                transaksi.getCatatan()
            };

            modelTabel.addRow(baris);
        }

        int total = manajer.getSemuaTransaksi().size();

        labelJumlah.setText(
            "Menampilkan " + tampil.size()
                + " dari " + total + " transaksi"
        );

        pilihSemua.setSelected(false);
        perbaruiTombolHapus();

        if (tampil.isEmpty()) {
            labelKosong.setText(
                total == 0
                    ? "Belum ada transaksi"
                    : "Tidak ada transaksi yang cocok dengan pencarian"
            );

            layoutKonten.show(panelKonten, "kosong");
        } else {
            layoutKonten.show(panelKonten, "tabel");
        }
    }

    private void cariData() {
        String kataKunci = inputPencarian.getText()
            .trim()
            .toLowerCase();

        List<Transaksi> hasil = new ArrayList<>();

        for (Transaksi transaksi : manajer.getSemuaTransaksi()) {
            String data = (
                transaksi.getId() + " "
                + transaksi.getTanggal() + " "
                + transaksi.getTipeTransaksi() + " "
                + transaksi.getKategori() + " "
                + transaksi.getCatatan()
            ).toLowerCase();

            if (data.contains(kataKunci)) {
                hasil.add(transaksi);
            }
        }

        tampilkanData(hasil);
    }

    // ===================== HAPUS TRANSAKSI =====================

    private void perbaruiTombolHapus() {
        int jumlahDipilih = 0;

        for (int i = 0; i < modelTabel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(
                modelTabel.getValueAt(i, KOLOM_CHECKBOX)
            )) {
                jumlahDipilih++;
            }
        }

        tombolHapusDipilih.setEnabled(jumlahDipilih > 0);

        tombolHapusDipilih.setText(
            jumlahDipilih > 0
                ? "Hapus " + jumlahDipilih + " Dipilih"
                : "Hapus yang Dipilih"
        );

        pilihSemua.setSelected(
            modelTabel.getRowCount() > 0
                && jumlahDipilih == modelTabel.getRowCount()
        );
    }

    private void hapusTransaksiDipilih() {
        List<String> idDipilih = new ArrayList<>();

        for (int i = 0; i < modelTabel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(
                modelTabel.getValueAt(i, KOLOM_CHECKBOX)
            )) {
                idDipilih.add(tampil.get(i).getId());
            }
        }

        if (idDipilih.isEmpty()) {
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin menghapus " + idDipilih.size()
                + " transaksi yang dipilih?\n"
                + "Data yang dihapus tidak dapat dikembalikan.",
            "Konfirmasi Hapus Transaksi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            for (String id : idDipilih) {
                manajer.hapusTransaksi(id);
            }

            manajer.simpanKeFile();

            modePilih = true;
            ubahModePilih();

            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                idDipilih.size() + " transaksi berhasil dihapus.",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Gagal menghapus transaksi: " + ex.getMessage(),
                "Kesalahan",
                JOptionPane.ERROR_MESSAGE
            );

            refreshTable();
        }
    }

    // ===================== RENDERER =====================

    private Color warnaBaris(boolean dipilih, int baris) {
        if (dipilih) {
            return UITheme.PRIMARY_SOFT;
        }

        return baris % 2 == 0
            ? Color.WHITE
            : UITheme.TABLE_ZEBRA;
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {

        HeaderRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            super.getTableCellRendererComponent(
                table, value, false, false, row, column
            );

            setBackground(UITheme.TABLE_HEAD);
            setForeground(UITheme.TEXT);
            setFont(UITheme.font(Font.BOLD, 12));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(SwingConstants.CENTER);

            return this;
        }
    }

    private class TeksRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            super.getTableCellRendererComponent(
                table, value, isSelected, false, row, column
            );

            setBackground(warnaBaris(isSelected, row));
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(UITheme.font(Font.PLAIN, 13));
            setForeground(UITheme.TEXT);

            if (column == KOLOM_ID) {
                setFont(UITheme.font(Font.BOLD, 13));
                setForeground(UITheme.PRIMARY);

            } else if (column == KOLOM_NOMINAL) {
                setFont(UITheme.font(Font.BOLD, 13));

                String teks = String.valueOf(value);

                if (teks.startsWith("+")) {
                    setForeground(UITheme.SUCCESS_DARK);
                } else if (teks.startsWith("-")) {
                    setForeground(UITheme.DANGER_DARK);
                }
            }

            return this;
        }
    }

    private class JenisRenderer implements TableCellRenderer {

        private final JPanel panel = new JPanel(new GridBagLayout());

        private final UITheme.Badge badge = new UITheme.Badge(
            "",
            UITheme.TEXT,
            UITheme.BORDER
        );

        JenisRenderer() {
            panel.setOpaque(true);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(0, 0, 0, 0);

            panel.add(badge, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            String jenis = value == null ? "" : value.toString();

            badge.setText(jenis);

            if ("Pemasukan".equalsIgnoreCase(jenis)) {
                badge.setWarna(
                    UITheme.SUCCESS_DARK,
                    UITheme.SUCCESS_SOFT
                );

            } else if ("Pengeluaran".equalsIgnoreCase(jenis)) {
                badge.setWarna(
                    UITheme.DANGER_DARK,
                    UITheme.DANGER_SOFT
                );

            } else {
                badge.setWarna(
                    UITheme.TEXT_MUTED,
                    UITheme.BORDER
                );
            }

            panel.setBackground(warnaBaris(isSelected, row));

            return panel;
        }
    }

    // ===================== SEARCH FIELD =====================

    private static class SearchField extends JTextField {

        private final String hint;

        SearchField(String hint) {
            this.hint = hint;

            setFont(UITheme.font(Font.PLAIN, 13));
            setForeground(UITheme.TEXT);
            setBackground(Color.WHITE);
            setCaretColor(UITheme.PRIMARY);
            setBorder(buatBorder(UITheme.BORDER));
            setPreferredSize(new Dimension(200, 42));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    setBorder(buatBorder(UITheme.PRIMARY));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setBorder(buatBorder(UITheme.BORDER));
                    repaint();
                }
            });
        }

        private static Border buatBorder(Color warna) {
            return new CompoundBorder(
                new UITheme.RoundedBorder(warna, 12, 0, 0),
                new EmptyBorder(9, 38, 9, 12)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            UITheme.antialias(g2);

            new UITheme.VectorIcon(
                UITheme.IconType.SEARCH,
                16,
                UITheme.TEXT_MUTED
            ).paintIcon(
                this,
                g2,
                14,
                (getHeight() - 16) / 2
            );

            if (getText().isEmpty()) {
                g2.setColor(new Color(156, 163, 175));
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();

                int y = (getHeight() - fm.getHeight()) / 2
                    + fm.getAscent();

                g2.drawString(hint, getInsets().left, y);
            }

            g2.dispose();
        }
    }
}
