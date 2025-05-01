import java.net.*;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class Main {
	public static void main(String[] args) {
		//client main
		Scanner scanner = new Scanner(System.in);	//project requests the server prompt for S and V values.  S values is the web address, v is the packet size
		System.out.print("Enter the suffix S: ");  	//could include some error checking here for valid inputs.
		String S = scanner.nextLine();
		System.out.print("Enter the packet size, V: ");
		int V = scanner.nextInt();
		
		
		
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			
			String request = S + " " + V;
			byte[] buffer = request.getBytes(StandardCharsets.UTF_8);
			InetAddress address = InetAddress.getByName("127.0.0.1");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 12421);
			socket.send(packet);;
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) socket.close();
		}
/*
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
		}*/
		socket.close();
	}
}

