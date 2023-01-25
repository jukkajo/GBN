import java.io.Serializable;
import java.util.Arrays;

// 23.01.2023
// @Jukka J
// Class to create packet object according RDT protocol
public class RDTPacket implements Serializable {

	public int seq;
	
	public byte[] data;
	
	public boolean last;

	public RDTPacket(int seq, byte[] data, boolean last) {
		super();
		this.seq = seq;
		this.data = data;
		this.last = last;
	}
	
	@Override
	public String toString() {
	        String starting = "UDPPacket [seq=";
	        String dat = ", data=";
	        String lst = ", last=";
	        String closing = "]";
	        
		return starting + seq + dat + Arrays.toString(data)
				+ lst + last + closing;
	}

        // Setters------------------------
	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
        // -------------------------------

        // Getters------------------------
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
        // -------------------------------

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}
	
}
