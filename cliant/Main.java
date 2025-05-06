import java.net.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class Main {
	private static final int SERVER_PORT = 12421;
	private static final int TIMEOUT_MS = 5000; 
	public static void main(String[] args) throws Exception {
		//client main
		
		//took out the first try loop and set main to track exceptions. 
		
		Scanner scanner = new Scanner(System.in);	//project requests the server prompt for S and V values.  S values is the web address, v is the packet size
		System.out.print("Enter the suffix S: ");  	//could include some error checking here for valid inputs.
		String S = scanner.nextLine();
		System.out.print("Enter the packet size, V: ");
		int V = scanner.nextInt();
		
		
		
		
		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT_MS);
				
		String request = S + " " + V;
		byte[] reqBuf = request.getBytes(StandardCharsets.UTF_8);
		InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
		DatagramPacket reqPacket = new DatagramPacket(reqBuf, reqBuf.length, serverAddr, SERVER_PORT);
		socket.send(reqPacket);;
		System.out.println("Sent Request: " + request);
		System.out.println("Waiting for data packets.");
		
		int expectedSeq = 0;
		int outOfOrderCnt = 0;
		
		while (true) {
			try {
				byte[] recvBuf = new byte[V];
				DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(recvPacket);
				
				ByteBuffer bb = ByteBuffer.wrap(recvBuf, 0, Integer.BYTES);
				int seqNum = bb.getInt();
				if (seqNum != expectedSeq) {
					outOfOrderCnt++;
				}
				expectedSeq = seqNum + 1;
				
				String chunk = new String(
						recvPacket.getData(), 0, recvPacket.getLength(), StandardCharsets.UTF_8
				);
				System.out.println(chunk);
			} catch (SocketTimeoutException e) {
				System.out.println("Timed out.");
				break;
			}
		}
		
		System.out.println("Number of out-of-order packets: " + outOfOrderCnt);
		
		
		
		
		socket.close();	
			/*
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) socket.close();
		}*/
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
		//socket.close();
	}
}

