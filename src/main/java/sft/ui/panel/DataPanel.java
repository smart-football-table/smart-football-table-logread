package sft.ui.panel;

import static java.awt.Color.BLACK;
import static java.awt.Font.PLAIN;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import sft.event.BallPosition;
import sft.event.TeamScore;

public final class DataPanel extends JPanel {

	private static final long serialVersionUID = 5889168701206425622L;

	private final transient BufferedImage ball;
	private final Font timesRomanBig = new Font("TimesRoman", PLAIN, 60);

	private final Map<Integer, Integer> scores = new HashMap<>();
	private int posX = -1;
	private int posY = -1;

	public DataPanel(BufferedImage ball) {
		this.ball = ball;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintBall(g);
		paintScore(g);
	}

	private void paintBall(Graphics g) {
		if (posX >= 0 && posY >= 0) {
			g.drawImage(ball, posX, posY, null);
		}
	}

	private void paintScore(Graphics g) {
		g.setFont(timesRomanBig);
		String score = scores.getOrDefault(0, 0) + ":" + scores.getOrDefault(1, 0);
		int topX = (getWidth() - g.getFontMetrics().stringWidth(score)) / 2;
		g.setColor(BLACK);
		g.drawString(score, topX, 100);
	}

	public void setScore(TeamScore teamScored) {
		scores.put(teamScored.team, teamScored.score);
		posX = -1;
		posY = -1;
		repaint();
	}

	public void setPosition(BallPosition pos) {
		posX = (int) (pos.x * getWidth());
		posY = (int) (pos.y * getHeight());
		repaint();
	}

}