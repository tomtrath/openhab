package org.openhab.binding.homecan;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class HomecanMsg {

	public static enum MsgType{
		ONOFF				(0x00),
		OPENCLOSED			(0x01),
		MOTION				(0x02),
		DIMMER				(0x03),
		KWB_HK				(0x04),
		POSITION			(0x05),
		SHADE				(0x06),
		STOPMOVE			(0x07),
		UPDOWN				(0x08),
		TEMPERATURE			(0x09),
		HUMIDITY			(0x0A),
		LUMINOSITY			(0x0B),
		UV_INDEX			(0x0C),
		AIR_PRESSURE		(0x0D),
		WIND_SPEED			(0x0E),
		WIND_DIRECTION		(0x0F),
		RAIN				(0x10),
		INCDEC				(0x11),
		FTKID				(0x12),	
		//KEY_PRESSED		(0x16),
		//KEY_RELEASED		(0x17),
		
		FLOAT				(0x20),
		UINT32				(0x21),
				
		KEY_SEQUENCE		(0x80),
		STRING				(0x81),		
		IR					(0x83),
		//LED				(0x84),
		BUZZER				(0x85),
		
		CHANNEL_CONFIG		(0xE0),
		GET_CONFIG			(0xE1),
		CLEAR_CONFIG		(0xE2),
		STORE_CONFIG 		(0xE3),
		DIMMER_LEARN		(0xE4),
		REQUEST_STATE		(0xE5),
		BOOTLOADER			(0xF0),
		CALL_BOOTLOADER		(0xF1),
		HEARTBEAT			(0xFF);

		public static Map<Byte, MsgType> lookup = new HashMap<Byte, MsgType>();
		int value;
		
		private MsgType(int val) {
			//lookup.put(new Byte((byte) val), this);
			value = val;			
		}		
		
		public byte getValue() {
			return (byte)value;
		}	
		
		private static MsgType[] values = values();
		
		public static MsgType valueOf(byte msgtype) {			
			for (int i=0;i<values.length;i++) {
				if (values[i].getValue()==msgtype) {
					return values[i];
				}
			}
			return null;
            //return lookup.get(new Byte(msgtype));
        }
	}
	
	protected byte header;
	protected MsgType msgtype;
	protected byte address;
	protected byte channel;
	protected byte[] data;	
	
	public HomecanMsg (DatagramPacket receivePacket) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(receivePacket.getData());
		int len = receivePacket.getLength();		
		header = byteBuffer.get();
		msgtype = MsgType.valueOf(byteBuffer.get())	;
		address = byteBuffer.get();
		channel = byteBuffer.get();
		if (len>4) {
			data = new byte[len-4];
			for (int i=0;i<data.length;i++) {
				data[i] = byteBuffer.get();			
			}
		} else { 
			data = null;
		}
	}
	
	public HomecanMsg(byte address,MsgType msgType, byte channel,byte[] data) {
		header = 0x0F; // set to CommandMsg with default Priority
		this.msgtype = msgType;
		this.address = address;
		this.channel = channel;
		if (data!=null) {
			this.data = new byte[data.length];
			for (int i=0;i<data.length;i++) {
				this.data[i] = data[i];			
			}
		} else this.data = null;
	}


	public DatagramPacket getPacket(InetAddress ip, int port) {
		ByteBuffer outputBuffer = ByteBuffer.allocate(4+(data!=null?data.length:0));
		outputBuffer.put(header);
		outputBuffer.put(msgtype.getValue());
		outputBuffer.put(address);
		outputBuffer.put(channel);
		if (data!=null) {
			for (int i=0;i<data.length;i++) {
				outputBuffer.put(data[i]);
			}
		}		
		return new DatagramPacket(outputBuffer.array(), outputBuffer.capacity(), ip, port);
	}
	
	@Override
	public String toString() {
		return "HomecanMsg [header=" + header + ", msgtype=" + msgtype + ", address=" + address + ", channel=" + channel + ", data=" + Arrays.toString(data)
				+ "]";
	}	
	
	public byte[] getData() {
		return data;
	}

	public boolean isCommand() {
		return (header&0x01)==1;
	}
	
	public boolean isUpdate() {
		return (header&0x01)==0;
	}
}
