package sft.ui.panel;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.BorderLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sft.event.Event;
import sft.reader.mqtt.MqttClientAdapter;

public class MqttClientPanel extends JPanel {

	private static final long serialVersionUID = -2788803620364292300L;

	private static final int MQTT_DEFAULT_PORT = 1883;

	private JTextField hostPort;
	private JButton connect;

	private boolean connected;

	private transient MqttClientAdapter mqttClientAdapter;

	public MqttClientPanel(Consumer<Event> consumer) {
		setLayout(new BorderLayout());
		hostPort = new JTextField("localhost:" + MQTT_DEFAULT_PORT);
		add(hostPort, CENTER);
		connect = new JButton();
		setConnected(false);
		connect.addActionListener(e -> {
			if (connected) {
				mqttClientAdapter.close();
				setConnected(false);
			} else {
				try {
					String[] split = hostPort.getText().split("\\:");
					mqttClientAdapter = new MqttClientAdapter(split[0],
							split.length > 1 ? Integer.parseInt(split[1]) : MQTT_DEFAULT_PORT, consumer);
					setConnected(true);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(getParent(), ex.getMessage(), "Error", ERROR_MESSAGE);
				}
			}
		});
		add(connect, EAST);
	}

	private void setConnected(boolean connected) {
		this.connected = connected;
		connect.setText(connected ? "Disconnect" : "Connect");
	}

}
