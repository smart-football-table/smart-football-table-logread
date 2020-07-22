package reader.junit.rules;

import java.io.IOException;
import java.net.ServerSocket;

public final class NetUtils {

	private NetUtils() {
		super();
	}

	public static int randomPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0);) {
			return socket.getLocalPort();
		}
	}

}
