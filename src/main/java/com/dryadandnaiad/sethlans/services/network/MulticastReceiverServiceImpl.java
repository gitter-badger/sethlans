package com.dryadandnaiad.sethlans.services.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Set;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class MulticastReceiverServiceImpl implements  MulticastReceiverService{
    private static final Logger LOG = LoggerFactory.getLogger(MulticastReceiverServiceImpl.class);

    @Value("${sethlans.multicast}")
    private String multicastIP;

    @Value("${sethlans.multicast.port}")
    private String multicastPort;

    @Async
    @Override
    public Set<String> currentSethlansClients() {
        byte[] buffer = new byte[256];
        try {
            MulticastSocket clientSocket = new MulticastSocket(Integer.parseInt(multicastPort));
            clientSocket.joinGroup(InetAddress.getByName(multicastIP));

            while(true){
                DatagramPacket msgPacket = new DatagramPacket(buffer, buffer.length);
                clientSocket.receive(msgPacket);

                String msg = new String(msgPacket.getData(), 0, msgPacket.getLength());
                LOG.debug(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
