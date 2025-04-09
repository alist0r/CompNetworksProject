import java.net.*;

public class Main {
	public static void main(String[] args) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		byte[] buffer = new byte[512];
		for (int i = 0; i < 512; ++i) {
			buffer[i] = (byte)(i % 256);
		}

		InetAddress address = null;
		try {
			address = InetAddress.getByName("127.0.0.1");
		} catch (Exception e) {
			e.printStackTrace();
		}

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 12421);
		try {
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		socket.close();
	}
}

