import java.net.*;

public class Main {
	private static final int port = 12421;
	public static void main (String[] args) {
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port); //constructor binds
		} catch (SocketException e) {
			e.printStackTrace();
		}

		System.out.println("listening on port " + port);

		try {
			socket.receive(packet);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for (int i = 0; i < 512; ++i) {
				System.out.println(buffer[i]);
			}
		}
		socket.close();

	}
}
