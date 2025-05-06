package com.example.demo;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.*;

public class YourGroupNameServer {
    /** UDP port required by the spec */
    private static final int PORT = 12421;
    /** # worker threads allowed to fetch & serve concurrently          */
    private static final ExecutorService POOL =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server listening on UDP " + PORT);
            byte[] buf = new byte[1500];                        // max Ethernet MTU
            DatagramPacket handshake = new DatagramPacket(buf, buf.length);

            while (true) {                                      // accept forever
                socket.receive(handshake);
                POOL.submit(new ClientTask(socket, handshake));
            }
        }
    }

    /** One task per client – handles HTTPS fetch & segmented UDP reply                 */
    private record ClientTask(DatagramSocket serverSock, DatagramPacket hello) implements Runnable {

        @Override public void run() {
            try {
                // 1. extract (suffix, V) from the handshake payload
                String msg = new String(hello.getData(), 0, hello.getLength()).trim();
                String[] parts = msg.split("\\s+");
                if (parts.length != 2) return;
                String suffix = parts[0];
                int    V      = Integer.parseInt(parts[1]);      // chunk size
                if (V >= 1400 || V < 8) return;                  // leave room for 8‑byte header

                // 2. pull page https://www.<suffix>
                System.out.println("Fetching https://www." + suffix + " for " +
                        hello.getAddress() + ":" + hello.getPort());
                byte[] body = fetch("https://www." + suffix);

                // 3. segment & send: 4‑byte seqNo | 4‑byte totalPkts | payload
                int header = 8;
                int payloadSize = V - header;
                int totalPkts = (int) Math.ceil(body.length / (double) payloadSize);

                for (int seq = 0; seq < totalPkts; seq++) {
                    int start = seq * payloadSize;
                    int end   = Math.min(body.length, start + payloadSize);
                    byte[] out = new byte[header + (end - start)];
                    ByteBuffer.wrap(out).putInt(seq).putInt(totalPkts);
                    System.arraycopy(body, start, out, header, end - start);

                    DatagramPacket pkt = new DatagramPacket(
                            out, out.length, hello.getAddress(), hello.getPort());
                    serverSock.send(pkt);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private static byte[] fetch(String url) throws IOException, InterruptedException {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            return resp.body();
        }
    }
}
