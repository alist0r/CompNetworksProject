import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
	private static final int port = 12421;
	
	public static void main(String[] args) {
		//server main
		
		//byte[] buffer = new byte[1400]; //moved to within try loop
		// DatagramPacket packet = new DatagramPacket(buffer, buffer.length); //moved to within try loop
		DatagramSocket socket = null;
		
		
		try {
			socket = new DatagramSocket(port); //constructor binds
			System.out.println("Listening on port " + port);
			byte[] buffer = new byte[1400];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			
			String request = new String(
					packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
			System.out.println("Received request: \"" + request + "\"");
			String[] parts = request.split("\\s+");
			String S = parts[0];
			int V = Integer.parseInt(parts[1]);
			System.out.println("Parsed S: " + S); //optional, can be deleted or commented out
			System.out.println("Parsed V: " + V); //optional, can be deleted or commented out
			
			byte[] content = fetchWebContent(S);
			
			// following if-else can be commented out or removed. just in there now for testing.
			if (content != null) {
				System.out.println("Fetched " + content.length + " bytes from https://" + S + "/");
			} else {
				System.out.println("Failed to fetch content!");
			}
			
			int offset = 0;
			while (offset < content.length) {
				int chunkSize = Math.min(V,  content.length - offset);
				byte[] chunk = Arrays.copyOfRange(
						content, offset, offset + chunkSize
				);
				
				DatagramPacket sendPacket = new DatagramPacket(chunk, chunkSize, packet.getAddress(), packet.getPort());
				
				socket.send(sendPacket);
				offset += chunkSize;
						
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) socket.close();
		}
	

		// System.out.println("listening on port " + port); //moved up

		/*try {
			socket.receive(packet);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for (int i = 0; i < 512; ++i) {
				System.out.println(buffer[i]);
			}
		}
		socket.close(); */    //commented out since this appeared to be for testing purposes originally

	}
	
	
	
	
	/*
	 * helper function to reach out to web server to get data to return to the client
	 */
	private static byte[] fetchWebContent(String suffix) {
		try {
			URI uri = new URI("https", suffix, "/", null);
			URL url = uri.toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			
			try (InputStream in = conn.getInputStream();
					ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				byte[] chunk = new byte[4096];
				int bytesRead;
				while ((bytesRead = in.read(chunk)) != -1) {
					out.write(chunk, 0, bytesRead);
				}
				return out.toByteArray();
			} finally {
				conn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}


