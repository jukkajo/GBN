import java.io.Serializable;
// 24.01.2023
// @Jukka J

/* Simple class to implement the Serializable interface, which allows its 
objects to be converted to and from a byte stream
*/
public class Ack implements Serializable{
	
	private int packet;

	public Ack(int packet) {
		super();
		this.packet = packet;
	}

	public int getPacket() {
		return packet;
	}

	public void setPacket(int packet) {
		this.packet = packet;
	}
	
	

}
