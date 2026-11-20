
package gui;

import model.Transaksi;
import service.ManajerMySaku;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SampahPanel extends UITheme.RoundedPanel {

    private final ManajerMySaku manajer;

    private DefaultTableModel model;
    private JTable tabel;
    private JLabel labelJumlah;
    private JLabel labelKosong;

    private UITheme.ModernButton tombolPulihkan;
    private UITheme.ModernButton tombolHapus;
    private UITheme.ModernButton tombolKosongkan;

    private List<Transaksi> tampil = new ArrayList<>();

    public SampahPanel(ManajerMySaku manajer) {
        super(20, UITheme.CARD, true);

        this.manajer = manajer;

        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 26, 20, 26));

        // ================= HEADER =================

        JLabel judul = UITheme.label(
            "Sampah",
            UITheme.font(Font.BOLD, 18),
            UITheme.TEXT
        );

        JLabel deskripsi = UITheme.label(
            "Transaksi yang telah kamu hapus",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        JPanel panelJudul = new JPanel();
        panelJudul.setLayout(new BoxLayout(
            panelJudul, BoxLayout.Y_AXIS
        ));
        panelJudul.setOpaque(false);

        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        deskripsi.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelJudul.add(judul);
        panelJudul.add(Box.createVerticalStrut(5));
        panelJudul.add(deskripsi);

        // ================= TOMBOL AKSI =================

        tombolPulihkan = new UITheme.ModernButton(
            "Pulihkan",
            UITheme.PRIMARY,
            UITheme.ModernButton.FILLED
        );

        tombolHapus = new UITheme.ModernButton(
            "Hapus Permanen",
            UITheme.DANGER_DARK,
            UITheme.ModernButton.OUTLINE
        );

        tombolKosongkan = new UITheme.ModernButton(
            "Kosongkan Sampah",
            UITheme.DANGER_DARK,
            UITheme.ModernButton.SOFT
        );

        tombolPulihkan.addActionListener(e -> pulihkan());
        tombolHapus.addActionListener(e -> hapusPermanen());
        tombolKosongkan.addActionListener(e -> kosongkan());

        JPanel panelAksi = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        panelAksi.setOpaque(false);

        panelAksi.add(tombolPulihkan);
        panelAksi.add(tombolHapus);
        panelAksi.add(tombolKosongkan);

        JPanel panelAtas = new JPanel(new BorderLayout(0, 16));
        panelAtas.setOpaque(false);
        panelAtas.add(panelJudul, BorderLayout.NORTH);
        panelAtas.add(panelAksi, BorderLayout.SOUTH);

        // ================= TABEL =================

        String[] kolom = {
            "ID", "Tanggal", "Jenis",
            "Kategori", "Nominal", "Catatan"
        };

        model = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(model);
        tabel.setRowHeight(48);
        tabel.setFont(UITheme.font(Font.PLAIN, 13));
        tabel.setForeground(UITheme.TEXT);
        tabel.setBackground(Color.WHITE);
        tabel.setSelectionBackground(UITheme.PRIMARY_SOFT);
        tabel.setSelectionForeground(UITheme.TEXT);
        tabel.setShowVerticalLines(false);
        tabel.setShowHorizontalLines(true);
        tabel.setGridColor(new Color(241, 245, 249));
        tabel.setIntercellSpacing(new Dimension(0, 1));
        tabel.setFillsViewportHeight(true);
        tabel.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        // Header tabel
        JTableHeader header = tabel.getTableHeader();
        header.setPreferredSize(new Dimension(0, 44));
        header.setBackground(UITheme.TABLE_HEAD);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setDefaultRenderer(new HeaderRenderer());

        // Renderer isi tabel
        TeksRenderer rendererTeks = new TeksRenderer();
        JenisRenderer rendererJenis = new JenisRenderer();

        for (int i = 0; i < tabel.getColumnCount(); i++) {
            tabel.getColumnModel().getColumn(i).setCellRenderer(
                i == 2 ? rendererJenis : rendererTeks
            );
        }

        // Lebar kolom
        int[] lebar = {80, 110, 140, 140, 150, 260};

        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i)
                .setPreferredWidth(lebar[i]);
        }

        JScrollPane scrollPane = new JScrollPane(tabel);
        UITheme.styleScrollPane(scrollPane);

        scrollPane.setBorder(BorderFactory.createMatteBorder(
            1, 0, 1, 0, UITheme.BORDER
        ));

        // ================= TAMPILAN KOSONG =================

        labelKosong = UITheme.label(
            "Belum ada transaksi di Sampah",
            UITheme.font(Font.PLAIN, 14),
            UITheme.TEXT_MUTED
        );

        labelKosong.setIcon(new UITheme.VectorIcon(
            UITheme.IconType.HISTORY,
            44,
            new Color(203, 213, 225)
        ));

        labelKosong.setHorizontalTextPosition(
            SwingConstants.CENTER
        );
        labelKosong.setVerticalTextPosition(
            SwingConstants.BOTTOM
        );
        labelKosong.setIconTextGap(14);

        JPanel panelKosong = new JPanel(new GridBagLayout());
        panelKosong.setBackground(Color.WHITE);
        panelKosong.add(labelKosong);

        CardLayout layoutKonten = new CardLayout();
        JPanel panelKonten = new JPanel(layoutKonten);
        panelKonten.setOpaque(false);

        panelKonten.add(scrollPane, "tabel");
        panelKonten.add(panelKosong, "kosong");

        // ================= FOOTER =================

        labelJumlah = UITheme.label(
            "0 transaksi di Sampah",
            UITheme.font(Font.PLAIN, 12),
            UITheme.TEXT_MUTED
        );

        // ================= SUSUN PANEL =================

        add(panelAtas, BorderLayout.NORTH);
        add(panelKonten, BorderLayout.CENTER);
        add(labelJumlah, BorderLayout.SOUTH);

        refreshTable();
    }

    // ================= REFRESH DATA =================

    public void refreshTable() {
        model.setRowCount(0);

        tampil = new ArrayList<>(manajer.getSemuaSampah());

        for (Transaksi transaksi : tampil) {
            String tipe = transaksi.getTipeTransaksi();

            String tanda = "Pemasukan".equalsIgnoreCase(tipe)
                ? "+ "
                : "- ";

            Object[] baris = {
                transaksi.getId(),
                transaksi.getTanggal(),
                tipe,
                transaksi.getKategori(),
                tanda + UITheme.formatRupiah(
                    transaksi.getNominal()
                ),
                transaksi.getCatatan()
            };

            model.addRow(baris);
        }

        int total = tampil.size();

        labelJumlah.setText(
            "Menampilkan " + total + " transaksi di Sampah"
        );

        boolean adaData = total > 0;

        tombolPulihkan.setEnabled(adaData);
        tombolHapus.setEnabled(adaData);
        tombolKosongkan.setEnabled(adaData);

        CardLayout layout = (CardLayout)
            ((JPanel) getComponent(1)).getLayout();

        JPanel konten = (JPanel) getComponent(1);

        if (adaData) {
            layout.show(konten, "tabel");
        } else {
            layout.show(konten, "kosong");
        }
    }

    // ================= AMBIL ID =================

    private String getIdTerpilih() {
        int baris = tabel.getSelectedRow();

        if (baris < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Pilih transaksi terlebih dahulu.",
                "Belum Ada yang Dipilih",
                JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }

        return tampil.get(baris).getId();
    }

    // ================= PULIHKAN =================

    private void pulihkan() {
        String id = getIdTerpilih();

        if (id == null) return;

        int jawab = JOptionPane.showConfirmDialog(
            this,
            "Pulihkan transaksi yang dipilih?",
            "Pulihkan Transaksi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (jawab != JOptionPane.YES_OPTION) return;

        try {
            manajer.pulihkanTransaksi(id);
            manajer.simpanKeFile();

            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                "Transaksi berhasil dipulihkan.",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Gagal memulihkan transaksi: " + ex.getMessage(),
                "Kesalahan",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= HAPUS PERMANEN =================

    private void hapusPermanen() {
        String id = getIdTerpilih();

        if (id == null) return;

        int jawab = JOptionPane.showConfirmDialog(
            this,
            "Transaksi akan dihapus permanen dan tidak bisa dipulihkan. Lanjutkan?",
            "Hapus Permanen",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (jawab != JOptionPane.YES_OPTION) return;

        try {
            manajer.hapusPermanen(id);
            manajer.simpanKeFile();

            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                "Transaksi berhasil dihapus permanen.",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Gagal menghapus transaksi: " + ex.getMessage(),
                "Kesalahan",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= KOSONGKAN SAMPAH =================

    private void kosongkan() {
        int jawab = JOptionPane.showConfirmDialog(
            this,
            "Hapus seluruh transaksi di Sampah secara permanen?",
            "Kosongkan Sampah",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (jawab != JOptionPane.YES_OPTION) return;

        try {
            manajer.kosongkanSampah();
            manajer.simpanKeFile();

            refreshTable();

            JOptionPane.showMessageDialog(
                this,
                "Sampah berhasil dikosongkan.",
                "Berhasil",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Gagal mengosongkan Sampah: " + ex.getMessage(),
                "Kesalahan",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= WARNA BARIS =================

    private Color warnaBaris(boolean dipilih, int baris) {
        if (dipilih) {
            return UITheme.PRIMARY_SOFT;
        }

        return baris % 2 == 0
            ? Color.WHITE
            : UITheme.TABLE_ZEBRA;
    }

    // ================= HEADER RENDERER =================

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

    // ================= TEKS RENDERER =================

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

            if (column == 0) {
                setFont(UITheme.font(Font.BOLD, 13));
                setForeground(UITheme.PRIMARY);

            } else if (column == 4) {
                String teks = String.valueOf(value);

                setFont(UITheme.font(Font.BOLD, 13));

                if (teks.startsWith("+")) {
                    setForeground(UITheme.SUCCESS_DARK);
                } else if (teks.startsWith("-")) {
                    setForeground(UITheme.DANGER_DARK);
                }
            }

            return this;
        }
    }

    // ================= JENIS RENDERER =================

    private class JenisRenderer implements TableCellRenderer {

        private final JPanel panel =
            new JPanel(new GridBagLayout());

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
}
