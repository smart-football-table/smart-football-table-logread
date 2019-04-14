package sft.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

final class BackgroundPanel extends JPanel {

	private final BufferedImage img;
	private static final long serialVersionUID = 5889168701206425622L;

	BackgroundPanel(BufferedImage img) throws HeadlessException {
		this.img = img;
		Dimension dim = new Dimension(img.getWidth(), img.getHeight());
		setSize(dim);
		setPreferredSize(dim);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, null);
	}

}