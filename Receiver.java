import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.*;
// 25.01.2023
// @Jukka J

// Simple receiver for GBN on top of UDP, simple approach without GUI/eventlisteners and other hassle
public class Receiver {
	
	// Maximum segment size -> quantity of data from the application layer / segment
	public static int maxSeqSize = 4;
	// Probability of loss during packet sending
	public static double prob = 0.1;
	// byte-count of a converted RDTPacket object 
        public static final int baseSize = 83;
        
        public static final String ansires = "\u001B[0m";
        public static final String ansiyel = "\u001B[33m";
        public static final String ansiblu = "\u001B[36m";
        
	public static void main(String[] args) throws Exception{
                System.out.println(ansiyel+"Give maximum segment size (e.g 4) and probability of loss during packet sending (e.g 0.1)\nnote that sender program should have identical values for those parameters.");
                
                System.out.println("First, give value for maxSeqSize:");
                Scanner sc2 = new Scanner(System.in);
                maxSeqSize = Integer.parseInt(sc2.nextLine());
                
                System.out.println("And for probability:"+ansiblu);
                Scanner sc3 = new Scanner(System.in);
                prob =  Double.valueOf(sc3.nextLine());
		DatagramSocket sOrigin = new DatagramSocket(9876);
		byte[] receivedData = new byte[maxSeqSize + baseSize];
		
		int waitingFor = 0;
		
		ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();
		
		boolean ifLast = false;
		
		while(!ifLast){
			
			System.out.println("Waiting for sender...");
			
			// Receive packet
			DatagramPacket recDgPacket = new DatagramPacket(receivedData, receivedData.length);
			sOrigin.receive(recDgPacket);
			
			
			// Convert to a RDTPacket object
			RDTPacket packet = (RDTPacket) Converter.toObject(recDgPacket.getData());
			
			System.out.println("Packet and sequence number " + packet.getSeq() + " received (last: " + packet.isLast() + " )");
		
			if(packet.getSeq() == waitingFor && packet.isLast()){
				
				waitingFor++;
				received.add(packet);
				
				System.out.println("Last packet has been received");
				
				ifLast = true;
				
			}else if(packet.getSeq() == waitingFor){
				waitingFor++;
				received.add(packet);
				System.out.println("Packed added to buffer");
			}else{
				System.out.println("Packet discarded");
			}
			
			// Ack object
			Ack ackObject = new Ack(waitingFor);
			
			// Convert
			byte[] ackBytes = Converter.toBytes(ackObject);
			
			
			DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, recDgPacket.getAddress(), recDgPacket.getPort());
			
			// Send with some probability of loss
			if(Math.random() > prob){
				sOrigin.send(ackPacket);
			}else{
				System.out.println("Ack lost (with sequence number) " + ackObject.getPacket());
			}
			
			System.out.println("Sending ACK to seq " + waitingFor + " with " + ackBytes.length  + " bytes");
			

		}
		
		// loop to print received data on console
		System.out.println("Data:\n");
		
		for(RDTPacket p : received){
			for(byte b: p.getData()){
				System.out.print((char) b);
			}
		}
		System.out.println("");
		
	}
	
	
}
