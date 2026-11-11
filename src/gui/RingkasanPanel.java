package gui;

import service.ManajerMySaku;

import javax.swing.*;

/** Ringkasan keuangan. Dikerjakan oleh: ALHIYA */
public class RingkasanPanel extends JPanel {
    private ManajerMySaku manajer;

    public RingkasanPanel(ManajerMySaku manajer) {
        this.manajer = manajer;
        add(new JLabel("Ringkasan keuangan (TODO Alhiya)"));
    }
}
