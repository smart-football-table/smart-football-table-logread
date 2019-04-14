package reader.junit.rules;

import java.io.IOException;
import java.net.ServerSocket;

public class NetUtils {

	static int randomPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0);) {
			return socket.getLocalPort();
		}
	}

}
