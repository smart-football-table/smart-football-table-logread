package sft.ui.panel;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.WEST;
import static java.util.Collections.emptyList;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

import sft.event.Event;
import sft.event.EventInTime;
import sft.reader.log.LogReader;

public class LogFilePanel extends JPanel {

	private static final long serialVersionUID = -1088486354322072946L;

	private final transient Consumer<Event> eventConsumer;

	private static final String playText = ">";
	private static final String pauseText = "||";

	private final JSlider timeSlider = makeSlider();
	private final JButton playButton = makePlayButton();

	private transient List<EventInTime> eventsInTime = emptyList();

	private long start;
	private long nanosOfFirstEvent;

	private Timer timer = new Timer(1, e -> {
		int next = timeSlider.getValue() + 1;
		if (next >= timeSlider.getMaximum()) {
			((Timer) e.getSource()).stop();
			playButton.setText(playText);
		} else {
			EventInTime eventInTime = eventsInTime.get(next);
			long diffOfFile = eventInTime.nanos - nanosOfFirstEvent;
			long nowRunning = System.nanoTime() - start;
			if (nowRunning >= diffOfFile) {
				timeSlider.setValue(next);
			}
		}
	});

	private void setEvents(List<EventInTime> eventsInTime) {
		this.eventsInTime = eventsInTime;
		timeSlider.setValue(0);
		timeSlider.setMaximum(eventsInTime.size() - 1);
	}

	public LogFilePanel(Consumer<Event> eventConsumer) throws FileNotFoundException, IOException, ParseException {
		this.eventConsumer = eventConsumer;
		setLayout(new BorderLayout());
		add(playButton, WEST);
		add(timeSlider, CENTER);
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
		JButton button = new JButton(playText);
		button.addActionListener(e -> {
			if (timer.isRunning()) {
				timer.stop();
				button.setText(playText);
			} else {
				timerPlay();
			}
		});
		return button;
	}

	private void timerPlay() {
		playButton.setText(pauseText);
		start = System.nanoTime();
		nanosOfFirstEvent = eventsInTime.isEmpty() ? 0 : eventsInTime.get(timeSlider.getValue()).nanos;
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
			eventConsumer.accept(eventsInTime.get(slider.getValue()).getEvent());
			if (running) {
				timerPlay();
			}
		});
		return slider;
	}

}