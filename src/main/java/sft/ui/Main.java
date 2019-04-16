package sft.ui;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.YELLOW;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.OverlayLayout;

import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScored;
import sft.ui.panel.BackgroundPanel;
import sft.ui.panel.DataPanel;
import sft.ui.panel.LogFilePanel;
import sft.ui.panel.MqttClientPanel;

public class Main {

	private boolean mqtt = false;

	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		new Main().doMain();
	}

	private void doMain() throws IOException, ParseException {
		String backgroundImage = "kicker.png";
//		String backgroundImage = "football-field-1428839_1280.png";
//		String backgroundImage = "image008.gif";
		BufferedImage img = ImageIO.read(new File(backgroundImage));
		BackgroundPanel backgroundPanel = new BackgroundPanel(img);
		backgroundPanel.setOpaque(false);

		DataPanel dataPanel = new DataPanel(img.getWidth() / 32, YELLOW);
		dataPanel.setOpaque(false);

		JFrame frame = new JFrame("SFT Viewer");
		frame.setLayout(new BorderLayout());
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(new OverlayLayout(layeredPane));

		layeredPane.add(dataPanel, -1);
		layeredPane.add(backgroundPanel, -1);
		frame.add(layeredPane, NORTH);
		Consumer<Event> consumeToPanel = consumeToPanel(dataPanel);
		frame.add(mqtt ? new MqttClientPanel(consumeToPanel) : new LogFilePanel(consumeToPanel), SOUTH);
		invokeLater(() -> {
			try {
				frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
				frame.pack();
				frame.setResizable(false);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	private Consumer<Event> consumeToPanel(DataPanel dataPanel) {
		return e -> {
			if (e instanceof BallPosition) {
				dataPanel.setPosition((BallPosition) e);
			}
			if (e instanceof TeamScored) {
				dataPanel.setScore((TeamScored) e);
			}
		};
	}

}
