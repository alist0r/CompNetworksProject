package com.example.demo;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class YourGroupNameClient {
    private static final int PORT = 12421;
    private static final int HEADER = 8;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter domain suffix (e.g. umd.edu): ");
        String suffix = sc.next();
        System.out.print("Enter desired packet size (<1400): ");
        int V = sc.nextInt();
        if (V >= 1400 || V < HEADER + 1) throw new IllegalArgumentException("Bad V");

        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setSoTimeout(10000);                       // 10 s per receive

            /* 1. send handshake: "suffix V" */
            String hello = suffix + " " + V;
            byte[] helloBytes = hello.getBytes();
            DatagramPacket helloPkt = new DatagramPacket(
                    helloBytes, helloBytes.length,
                    InetAddress.getByName("127.0.0.1"), PORT);
            sock.send(helloPkt);

            /* 2. receive all data packets */
            Map<Integer, byte[]> chunks = new HashMap<>();
            int expectedPkts = -1;          // unknown until first packet
            int outOfOrder = 0;

            while (expectedPkts == -1 || chunks.size() < expectedPkts) {
                byte[] buf = new byte[V];
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                sock.receive(pkt);

                ByteBuffer bb = ByteBuffer.wrap(buf, 0, pkt.getLength());
                int seq   = bb.getInt();
                int total = bb.getInt();
                byte[] pay = Arrays.copyOfRange(buf, HEADER, pkt.getLength());

                if (expectedPkts == -1) expectedPkts = total;
                if (seq != chunks.size()) outOfOrder++;
                chunks.put(seq, pay);
            }

            /* 3. print payloads in order */
            for (int i = 0; i < expectedPkts; i++) {
                System.out.print(new String(chunks.get(i)));
            }
            System.out.println("\nNumber of out-of-order packets: " + outOfOrder);
        }
    }
}
