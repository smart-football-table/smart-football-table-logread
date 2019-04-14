import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Color.BLACK;
import static java.awt.Color.YELLOW;
import static java.awt.Font.PLAIN;
import static java.util.Collections.emptyList;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.OverlayLayout;
import javax.swing.Timer;

import event.BallPosition;
import event.Event;
import event.TeamScored;
import reader.log.LogReader;

public class Main {

	private static class LogFilePanel extends JPanel {

		private static final long serialVersionUID = -1088486354322072946L;

		private final Consumer<Event> eventConsumer;

		private final static String playText = ">";
		private final static String pauseText = "||";

		private final JSlider slider = makeSlider();
		private final JButton playButton = makePlayButton();

		private List<Event> events = emptyList();

		private long start;
		private long nanosOfFirstEvent;

		private Timer timer = new Timer(1, e -> {
			int next = slider.getValue() + 1;
			if (next >= slider.getMaximum()) {
				((Timer) e.getSource()).stop();
				playButton.setText(playText);
			} else {
				Event event = events.get(next);
				long diffOfFile = event.nanos - nanosOfFirstEvent;
				long nowRunning = System.nanoTime() - start;
				if (nowRunning >= diffOfFile) {
					slider.setValue(next);
				}
			}
		});

		private void setEvents(List<Event> events) {
			this.events = events;
			slider.setValue(0);
			slider.setMaximum(events.size() - 1);
		}

		public LogFilePanel(Consumer<Event> eventConsumer) throws FileNotFoundException, IOException, ParseException {
			this.eventConsumer = eventConsumer;
			setLayout(new BorderLayout());
			add(playButton, WEST);
			add(slider, CENTER);
			add(makeFileOpen(), EAST);
		}

		private JButton makeFileOpen() {
			JButton fileOpen = new JButton("Open");
			fileOpen.addActionListener(e -> {
				JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(getRootPane()) == APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						setEvents(LogReader.read(new FileInputStream(file)));
					} catch (IOException | ParseException e1) {
						showMessageDialog(this, e1.getMessage(), "Error", ERROR_MESSAGE);
					}
				}
			});
			return fileOpen;
		}

		private JButton makePlayButton() {
			JButton playButton = new JButton(playText);
			playButton.addActionListener(e -> {
				if (timer.isRunning()) {
					timer.stop();
					playButton.setText(playText);
				} else {
					timerPlay();
				}
			});
			return playButton;
		}

		private void timerPlay() {
			playButton.setText(pauseText);
			start = System.nanoTime();
			nanosOfFirstEvent = events.isEmpty() ? 0 : events.get(slider.getValue()).nanos;
			timer.start();
		}

		private JSlider makeSlider() {
			JSlider slider = new JSlider();
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setMajorTickSpacing(100);
			slider.setMinimum(0);
			slider.setMaximum(0);
			slider.setValue(0);
			slider.addChangeListener(e -> {
				boolean running = timer.isRunning();
				if (running) {
					timer.stop();
				}
				eventConsumer.accept(events.get(slider.getValue()));
				if (running) {
					timerPlay();
				}
			});
			return slider;
		}

	}

	private DataPanel dataPanel;

	private static final class BackgroundPanel extends JPanel {

		private final BufferedImage img;
		private static final long serialVersionUID = 5889168701206425622L;

		private BackgroundPanel(BufferedImage img) throws HeadlessException {
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

	private static final class DataPanel extends JPanel {

		private static final long serialVersionUID = 5889168701206425622L;

		private final int ballSize;
		private final Color ballColor;
		private final Font font = new Font("TimesRoman", PLAIN, 60);

		private Map<Integer, Integer> scores = new HashMap<>();
		private int posX = -1;
		private int posY = -1;

		public DataPanel(int ballSize, Color ballColor) {
			this.ballSize = ballSize;
			this.ballColor = ballColor;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintBall(g);
			paintScore(g);
		}

		private void paintBall(Graphics g) {
			if (posX >= 0 && posY >= 0) {
				g.setColor(ballColor);
				g.fillOval(posX - ballSize / 2, posY - ballSize / 2, ballSize, ballSize);
			}
		}

		private void paintScore(Graphics g) {
			g.setFont(font);
			String score = scores.getOrDefault(0, 0) + ":" + scores.getOrDefault(1, 0);
			int topX = (getWidth() - g.getFontMetrics().stringWidth(score)) / 2;
			g.setColor(BLACK);
			g.drawString(score, topX, 100);
		}

		public void setScore(TeamScored teamScored) {
			scores.put(teamScored.team, teamScored.score);
			posX = -1;
			posY = -1;
			repaint();
		}

		private void setPosition(BallPosition pos) {
			posX = (int) (pos.x * getWidth());
			posY = (int) (pos.y * getHeight());
			repaint();
		}

	}

	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		new Main().doMain();
	}

	private void doMain() throws IOException, ParseException {
		String backgroundImage = "football-field-1428839_1280.png";
//		String backgroundImage = "image008.gif";
		BufferedImage img = ImageIO.read(new File(backgroundImage));
		BackgroundPanel backgroundPanel = new BackgroundPanel(img);
		backgroundPanel.setOpaque(false);

		dataPanel = new DataPanel(img.getWidth() / 32, YELLOW);
		dataPanel.setOpaque(false);

		JFrame frame = new JFrame("SFT Viewer");
		frame.setLayout(new BorderLayout());
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(new OverlayLayout(layeredPane));

		layeredPane.add(dataPanel, -1);
		layeredPane.add(backgroundPanel, -1);
		frame.add(layeredPane, NORTH);
		LogFilePanel logFilePanel = new LogFilePanel(event -> {
			if (event instanceof BallPosition) {
				dataPanel.setPosition((BallPosition) event);
			}
			if (event instanceof TeamScored) {
				dataPanel.setScore((TeamScored) event);
			}
		});

		frame.add(logFilePanel, SOUTH);
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

}
