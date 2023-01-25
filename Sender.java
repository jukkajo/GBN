import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;
// 25.01.2023
// @Jukka J

// Sender for GBN on top of UDP, simple approach without GUI/eventlisteners and other hassle
public class Sender {

	//Quantity of data from the application layer in the segment a.k.a maximum segment size
	public static int maxSeqSize = 4;
	// Number of packets sent without acking
	public static int winSize = 2;
	// Delay in milliseconds before resending all the non acked packets
	public static int delay = 30;
	// Probability of loss
	public static double prob = 0.1;
	
        public static final String ansires = "\u001B[0m";
        public static final String ansiyel = "\u001B[33m";
        public static final String ansiblu = "\u001B[36m";

	public static void main(String[] args) throws Exception{

		// Sequence number of the last packet sent (rcvbase)
		int seqNumLast = 0;
		
		// Sequence number of the last acked packet
		int seqNumLastAck = 0;

                // Here we ask params from user
                // If answer is "def", default params are used
                Scanner sc= new Scanner(System.in);
                System.out.println(ansiyel + "(Note) Without valid values, program won't work and also, receiver program\nshould have same values for maxSeqSize and prob\n");
                System.out.println("To use default settings, return 'def', this will lead you to sent message\nany other key pressed allows you to se parameters\n"+ansiblu);
                String which= sc.nextLine();
                
                if(!which.equals("def")) {
                    System.out.println("Give maximum segment size (e.g 4)");
                    Scanner sc2 = new Scanner(System.in);
                    maxSeqSize = Integer.parseInt(sc2.nextLine());
                    System.out.println("Give probability of loss during packet sending (e.g 0.1)");
                    Scanner sc3 = new Scanner(System.in);
                    prob =  Double.valueOf(sc3.nextLine());
                    System.out.println("Give number of packets sent without acking (e.g 2)");
                    Scanner sc4= new Scanner(System.in);
                    winSize = Integer.parseInt(sc4.nextLine());
                    System.out.println("Give delay for non ACK:ed packets (e.g 30)");
                    Scanner sc5= new Scanner(System.in);
                    delay = Integer.parseInt(sc5.nextLine());
                }
                 System.out.println(maxSeqSize + " " + prob + " " + winSize+" "+delay);
                
                Scanner sc6 = new Scanner(System.in);
                System.out.println("Give message to be send");
                String str = sc6.nextLine();
		byte[] fileBytes = str.getBytes();

		System.out.println("Data size: " + fileBytes.length + " bytes");

		// Last packet sequence number
		int lastSeq = (int) Math.ceil( (double) fileBytes.length / maxSeqSize);

		System.out.println("Number of packets to send: " + lastSeq);

		DatagramSocket receiverDgram = new DatagramSocket();

		// Address of receiver
		InetAddress receiverAddress = InetAddress.getByName("localhost");
		
		// List of packets sent
		ArrayList<RDTPacket> sent = new ArrayList<RDTPacket>();

		while(true){

			while(seqNumLast - seqNumLastAck < winSize && seqNumLast < lastSeq){

				// Storage for bytes
				byte[] storedBytes = new byte[maxSeqSize];

				// Copy segment of data bytes to array
				storedBytes = Arrays.copyOfRange(fileBytes, seqNumLast*maxSeqSize, seqNumLast*maxSeqSize + maxSeqSize);

				// Initalizing new RDTPacket object
				RDTPacket rdtObj = new RDTPacket(seqNumLast, storedBytes, (seqNumLast == lastSeq-1) ? true : false);

				// Convertansiblu+ the RDTPacket object
				byte[] sendData = Converter.toBytes(rdtObj);

				// Create the packet
				DatagramPacket dgram = new DatagramPacket(sendData, sendData.length, receiverAddress, 9876 );

				System.out.println("Sending packet, seqnum related: " + seqNumLast +  " and size of data: " + sendData.length + " bytes");

				// Append packet to the sent list
				sent.add(rdtObj);
				
				// Send with random probability of loss
				if(Math.random() > prob){
					receiverDgram.send(dgram);
				}else{
					System.out.println("*Packet loss, seqnum related: " + seqNumLast+"*");
				}

				seqNumLast++;

			}
			
			// Storage for receiver's ACKs
			byte[] ackBytes = new byte[40];
			
			// ACK packet gen
			DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
			
			try{
				// If not ACK in time specified
				receiverDgram.setSoTimeout(delay);
			
				// Lets receive
				receiverDgram.receive(ack);
				
				// Convert the Ack object
				Ack ackObject = (Ack) Converter.toObject(ack.getData());
				System.out.println("Received ACK: " + ackObject.getPacket());
	
				// Seqnum of the Ack matches, break out from loop
				if(ackObject.getPacket() == lastSeq){
					break;
				}
				
				seqNumLastAck = Math.max(seqNumLastAck, ackObject.getPacket());
				
			}catch(SocketTimeoutException e){
				
				for(int i = seqNumLastAck; i < seqNumLast; i++){
					
					// Convert the RDTPacket object
					byte[] sendData = Converter.toBytes(sent.get(i));

					// Create the packet
					DatagramPacket dgram = new DatagramPacket(sendData, sendData.length, receiverAddress, 9876 );
					
					// Send with some probability
					if(Math.random() > prob){
						receiverDgram.send(dgram);
					}else{
						System.out.println("*Packet loss, seqnum related: " + sent.get(i).getSeq() + "+");
					}

					System.out.println("Resending, seqnum related: " + sent.get(i).getSeq() +  " and size of data " + sendData.length + " bytes");
				}
			}
			
		
		}
		
		System.out.println("Transmitting process complete");

	}

}
