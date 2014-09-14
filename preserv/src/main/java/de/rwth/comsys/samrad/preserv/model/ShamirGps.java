package de.rwth.comsys.samrad.preserv.model;

import mpc.ShamirSharing;
import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Sam on 8/18/2014.
 */
public class ShamirGps {

    private final String RND_ALGORITHM   = "SHA1PRNG";

    /**
     * Creates the shares from given array.
     * Format: result[p][i] -> 'i'th share for peer 'p'
     *
     * @param secrets: dealer's list of secret e.g. [0, 1, 0, 0, 0] for 5 polygons
     * @param deg: polynomialDegree - at least t+1 shares are required
     * @return shares: result[p][i]
     * @throws Exception
     */
    public long[][] createShamirShares(int deg, int noOfPeers, long fieldSize, long[] secrets) {

        try {

            // Shamir initialization
            ShamirSharing shamir = new ShamirSharing();
            shamir.setRandomAlgorithm(RND_ALGORITHM);
            shamir.setFieldSize(fieldSize);
            shamir.setNumberOfPrivacyPeers(noOfPeers);
            shamir.setDegreeT(deg);
            shamir.init();

            // Generate shares
            return shamir.generateShares(secrets);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Pack shares for each privacy peer using MessagePack.
     *
     * @param shares, shares[p][i] is share i for privacy peer p
     * @return messages, messages[i] holds shares for privacy peer i
     */
    public SharesMessage[] createMsgPackMessage(long[][] shares, long shamirModulus) {
        SharesMessage[] messages = new SharesMessage[shares.length];
        long random_id = Math.abs(new Random(System.currentTimeMillis()).nextLong());

        for (int i = 0; i < shares.length; i++) {
            // Create SharesMessage for privacy peer i
            SharesMessage msg = new SharesMessage();
            msg.id = random_id;
            msg.modulus = shamirModulus;
            msg.shares = shares[i];

            // Add SharesMessage to result array
            messages[i] = msg;
        }
        return messages;
    }

    /**
     * Serialize messages using MessagePack and send them to the privacy peers.
     *
     * @param messages, the array containing the SharesMessages to be sent
     * @param ppIPs, the array containing the IPs of the privacy peers
     * @param ppPorts, the array containing the ports of the privacy peers
     * @throws java.io.IOException
     */
    public boolean shareOut(SharesMessage[] messages, String[] ppIPs, int[] ppPorts) {

        MessagePack msgpack = new MessagePack();

        for(int i = 0; i < messages.length; i++) {
            try{
                // Put SharesMessage into a map for easier handling in Python code
                Map<String, Object> msg = new HashMap<String, Object>();
                msg.put("type", "inputshares");
                msg.put("id", messages[i].id);
                msg.put("peerID", i+1);
                msg.put("m", messages[i].modulus);
                msg.put("shares", messages[i].shares);

                // Serialize message
                byte[] bytes = msgpack.write(msg);

                // Connect to privacy peer i
                Socket sock = new Socket(ppIPs[i], ppPorts[i]);
                BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());
                DataOutputStream out = new DataOutputStream(bos);
//                DataOutputStream out = new DataOutputStream(sock.getOutputStream());

                // Send message
                out.writeInt(bytes.length);
                out.flush();
                out.write(bytes);
                out.flush();

                System.out.println("Sent " + bytes.length + " bytes");

                // Close connection
                out.close();
                sock.close();

                Thread.sleep(1000);
            } catch(IOException e) {

                System.out.println("Error connecting to privacy peer " + i + ": " + e.getMessage());
                return false;
            } catch(InterruptedException e) {

                System.out.println("Error in sleeping thread" + e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Message
    public static class SharesMessage {
        public String type = "inputshares";
        public long id;
        public long modulus;
        public long[] shares;
    }
}
