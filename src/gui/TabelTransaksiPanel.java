
package gui;

import model.Transaksi;
import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * Data transaksi diurutkan berdasarkan tanggal terbaru ke terlama.
 * Seluruh isi tabel dan header ditampilkan rata tengah.
 *
 * Klik dua kali pada baris atau tombol "Ubah Terpilih"
 * untuk mengubah transaksi.
 */
public class TabelTransaksiPanel extends UITheme.RoundedPanel {

    private static final int KOLOM_ID = 0;
    private static final int KOLOM_JENIS = 2;
    private static final int KOLOM_NOMINAL = 4;

    private ManajerMySaku manajer;
    private Consumer<Transaksi> onUbahTransaksi;

    // Transaksi yang sedang tampil, urutannya sama dengan baris tabel
    private List<Transaksi> tampil = new ArrayList<>();

    private JTable tabelTransaksi;
    private DefaultTableModel modelTabel;
    private SearchField inputPencarian;

    private CardLayout layoutKonten;
    private JPanel panelKonten;
    private JLabel labelKosong;
    private JLabel labelJumlah;

    public TabelTransaksiPanel(ManajerMySaku manajer) {
        super(20, UITheme.CARD, true);
        this.manajer = manajer;

        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 26, 20, 26));

        // ---------- Bagian atas: judul + tombol ubah ----------
        JLabel judul = UITheme.label(
            "Semua Transaksi", UITheme.font(Font.BOLD, 18), UITheme.TEXT
        );

        JLabel deskripsi = UITheme.label(
            "Klik dua kali pada baris untuk mengubah atau menghapus",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        deskripsi.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelJudul = new JPanel();
        panelJudul.setLayout(new BoxLayout(panelJudul, BoxLayout.Y_AXIS));
        panelJudul.setOpaque(false);
        panelJudul.add(judul);
        panelJudul.add(Box.createVerticalStrut(4));
        panelJudul.add(deskripsi);

        UITheme.ModernButton tombolUbah = new UITheme.ModernButton(
            "Ubah Terpilih", UITheme.PRIMARY, UITheme.ModernButton.SOFT
        );

        tombolUbah.addActionListener(e -> ubahBarisTerpilih());

        JPanel sisiKananJudul = new JPanel(new GridBagLayout());
        sisiKananJudul.setOpaque(false);
        sisiKananJudul.add(tombolUbah);

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
            "Cari", UITheme.PRIMARY, UITheme.ModernButton.FILLED
        );

        UITheme.ModernButton tombolReset = new UITheme.ModernButton(
            "Reset", UITheme.TEXT_MUTED, UITheme.ModernButton.OUTLINE
        );

        JPanel panelTombol = new JPanel(new GridLayout(1, 2, 8, 0));
        panelTombol.setOpaque(false);
        panelTombol.add(tombolCari);
        panelTombol.add(tombolReset);

        JPanel panelPencarian = new JPanel(new BorderLayout(10, 0));
        panelPencarian.setOpaque(false);
        panelPencarian.add(inputPencarian, BorderLayout.CENTER);
        panelPencarian.add(panelTombol, BorderLayout.EAST);

        JPanel panelAtas = new JPanel(new BorderLayout(0, 16));
        panelAtas.setOpaque(false);
        panelAtas.add(barisJudul, BorderLayout.NORTH);
        panelAtas.add(panelPencarian, BorderLayout.CENTER);

        // ---------- Tabel ----------
        String[] kolom = {
            "ID", "Tanggal", "Jenis", "Kategori", "Nominal", "Catatan"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

        for (int i = 0; i < tabelTransaksi.getColumnCount(); i++) {
            tabelTransaksi.getColumnModel().getColumn(i).setCellRenderer(
                i == KOLOM_JENIS ? rendererJenis : rendererTeks
            );
        }

        // Lebar kolom
        int[] lebar = {80, 110, 140, 140, 150, 260};

        for (int i = 0; i < lebar.length; i++) {
            tabelTransaksi.getColumnModel().getColumn(i)
                .setPreferredWidth(lebar[i]);
        }

        // Klik dua kali pada baris = ubah
        tabelTransaksi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2
                    && tabelTransaksi.rowAtPoint(e.getPoint()) >= 0) {
                    ubahBarisTerpilih();
                }
            }
        });

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
            UITheme.IconType.HISTORY, 44, new Color(203, 213, 225)
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
            " ", UITheme.font(Font.PLAIN, 12), UITheme.TEXT_MUTED
        );

        // ---------- Aksi pencarian ----------
        tombolCari.addActionListener(e -> cariData());
        inputPencarian.addActionListener(e -> cariData());

        // Reset pencarian
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

    /**
     * Menampilkan transaksi dari ManajerMySaku.
     * Jika kolom pencarian terisi, filter tetap diterapkan.
     */
    public void refreshTable() {
        if (inputPencarian.getText().trim().isEmpty()) {
            tampilkanData(manajer.getSemuaTransaksi());
        } else {
            cariData();
        }
    }

    // ===================== LOGIKA DATA =====================

    /**
     * Mengubah tanggal transaksi menjadi LocalDate.
     * Mendukung format yyyy-MM-dd, dd-MM-yyyy, dan dd/MM/yyyy.
     */
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

    /**
     * Menampilkan data transaksi berdasarkan tanggal terbaru.
     */
    private void tampilkanData(List<Transaksi> daftar) {
        modelTabel.setRowCount(0);

        // Salin daftar agar urutan data asli tidak berubah
        tampil = new ArrayList<>(daftar);

        // Urutkan dari tanggal terbaru ke tanggal terlama
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
            "Menampilkan " + tampil.size() + " dari " + total + " transaksi"
        );

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

    /**
     * Mencari transaksi berdasarkan kata kunci.
     * Hasil pencarian tetap diurutkan berdasarkan tanggal.
     */
    private void cariData() {
        String kataKunci = inputPencarian.getText()
            .trim()
            .toLowerCase();

        List<Transaksi> hasil = new ArrayList<>();

        for (Transaksi transaksi : manajer.getSemuaTransaksi()) {

            String data = (
                transaksi.getId() + " " +
                transaksi.getTanggal() + " " +
                transaksi.getTipeTransaksi() + " " +
                transaksi.getKategori() + " " +
                transaksi.getCatatan()
            ).toLowerCase();

            if (data.contains(kataKunci)) {
                hasil.add(transaksi);
            }
        }

        tampilkanData(hasil);
    }

    // ===================== RENDERER =====================

    private Color warnaBaris(boolean dipilih, int baris) {
        if (dipilih) {
            return UITheme.PRIMARY_SOFT;
        }

        return baris % 2 == 0 ? Color.WHITE : UITheme.TABLE_ZEBRA;
    }

    /**
     * Header seluruh kolom rata tengah.
     */
    private class HeaderRenderer extends DefaultTableCellRenderer {

        HeaderRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column
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

    /**
     * Seluruh isi teks tabel rata tengah.
     */
    private class TeksRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column
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

    /**
     * Badge jenis transaksi berada di tengah kolom.
     */
    private class JenisRenderer implements TableCellRenderer {

        private final JPanel panel = new JPanel(new GridBagLayout());

        private final UITheme.Badge badge = new UITheme.Badge(
            "", UITheme.TEXT, UITheme.BORDER
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
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column
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

    // ===================== KOMPONEN KECIL =====================

    /**
     * Kolom pencarian dengan ikon kaca pembesar dan teks petunjuk.
     */
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
                UITheme.IconType.SEARCH, 16, UITheme.TEXT_MUTED
            ).paintIcon(this, g2, 14, (getHeight() - 16) / 2);

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
