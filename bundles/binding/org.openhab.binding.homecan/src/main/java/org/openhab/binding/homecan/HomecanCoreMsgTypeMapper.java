package org.openhab.binding.homecan;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author thomas
 * 
 */
public class HomecanCoreMsgTypeMapper implements HomecanMsgTypeMapper {	
	
	static private final Logger logger = LoggerFactory.getLogger(HomecanCoreMsgTypeMapper.class);

	public static String unsignedByteToString(byte b) {
		return b < 0 ? Integer.toString(256 + b) : Byte.toString(b);
	}

	@Override
	public byte[] toHomecanMsgData(Type type, HomecanMsg.MsgType msgtype) {
		byte[] data = null;
		Class<? extends Type> typeClass = type.getClass();
		if (typeClass.equals(UpDownType.class)) {
			if (msgtype == HomecanMsg.MsgType.UPDOWN) {
				data = new byte[1];
				data[0] = (byte) (type.equals(UpDownType.UP) ? 0 : 1);
				return data;
			}
		} else if (typeClass.equals(IncreaseDecreaseType.class)) {
			if (msgtype == HomecanMsg.MsgType.INCDEC) {
				data = new byte[1];
				data[0] = (byte) (type.equals(IncreaseDecreaseType.INCREASE) ? 0 : 1);
				return data;
			}
		} else if (typeClass.equals(OnOffType.class)) {
			if (msgtype == HomecanMsg.MsgType.ONOFF) {
				data = new byte[1];
				data[0] = (byte) (type.equals(OnOffType.OFF) ? 0 : 1);
				return data;
			} else if (msgtype == HomecanMsg.MsgType.DIMMER_LEARN) {
				return null;
			}
		} else if (typeClass.equals(PercentType.class)) {
			PercentType pType = (PercentType) type;
			switch (msgtype) {
				case DIMMER:
				case POSITION:
				case SHADE:
					data = new byte[1];
					data[0] = (byte) (pType.intValue() * 255 / 100);
					return data;
				default:
					return null;
			}
		} else if (typeClass.equals(DecimalType.class)) {
			DecimalType dType = (DecimalType) type;
			switch (msgtype) {
				case KWB_HK:
				case HUMIDITY:
				case UV_INDEX:
					data = new byte[1];
					data[0] = dType.byteValue();
					return data;
				case LUMINOSITY:
				case TEMPERATURE:
				case WIND_SPEED:
				case WIND_DIRECTION:
				case RAIN:
				case AIR_PRESSURE:
				case FLOAT:
					data = new byte[4];
					ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putFloat(dType.floatValue());
					return data;
				case UINT32:
					data = new byte[4];
					ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).putInt(dType.intValue());
					return data;
				default:
					return null;
			}
		} else if (typeClass.equals(StringType.class)) {
			String[] strings;
			StringType sType = (StringType) type;
			switch (msgtype) {
				case KEY_SEQUENCE:
					strings = sType.toString().replace("[", "").replace("]", "").split(", ");
					data = new byte[strings.length < 8 ? strings.length : 8];
					for (int i = 0; i < data.length; i++) {
						data[i] = Byte.parseByte(strings[i]);
					}
					return data;
				case IR:
					strings = sType.toString().replace("[", "").replace("]", "").split(", ");
					ByteBuffer buffer = ByteBuffer.allocate(6);
					buffer.put(new Integer(strings[0]).byteValue()); //put  protocol
					buffer.putShort(new Integer(strings[1]).shortValue()); //put  address
					buffer.putShort(new Integer(strings[2]).shortValue()); //put  command
					buffer.put(new Integer(strings[3]).byteValue()); //put  flags					
					return buffer.array();
				case STRING:
				case HEARTBEAT:
					byte[] bytes = sType.toString().getBytes();
					return Arrays.copyOf(bytes, bytes.length < 8 ? bytes.length : 8);
				default:
					return null;
			}
		} else if (typeClass.equals(OpenClosedType.class)) {
			if (msgtype == HomecanMsg.MsgType.OPENCLOSED) {
				data = new byte[1];
				data[0] = (byte) (type.equals(OpenClosedType.CLOSED) ? 0 : 1);
				return data;
			}
		} else if (typeClass.equals(StopMoveType.class)) {
			if (msgtype == HomecanMsg.MsgType.STOPMOVE) {
				data = new byte[1];
				data[0] = (byte) (type.equals(StopMoveType.STOP) ? 0 : 1);
				return data;
			}
		} else if (typeClass.equals(DateTimeType.class)) {
			logger.warn("Unmapped typeClass received '{}'", typeClass);
			return null; // not yet used
		}
		return null;
	}

	@Override
	public Type toType(HomecanMsg msg, Class<? extends Type> typeClass) {
		if (typeClass.equals(UpDownType.class)) {
			return msg.data[0] == 0 ? UpDownType.UP : UpDownType.DOWN;
		} else if (typeClass.equals(IncreaseDecreaseType.class)) {
			return msg.data[0] == 0 ? IncreaseDecreaseType.INCREASE : IncreaseDecreaseType.DECREASE;
		} else if (typeClass.equals(OnOffType.class)) {
			return msg.data[0] == 0 ? OnOffType.OFF : OnOffType.ON;
		} else if (typeClass.equals(PercentType.class)) {	
			int i = (msg.data[0]&0xFF);
			String s = Integer.toString(i * 100 / 255);
			return PercentType.valueOf(s);
		} else if (typeClass.equals(DecimalType.class)) {
			switch (msg.msgtype) {
				case KWB_HK:
				case HUMIDITY:
				case UV_INDEX:
					return DecimalType.valueOf(Integer.toString(msg.data[0]&0xFF));
				case LUMINOSITY:
				case TEMPERATURE:
				case WIND_SPEED:
				case WIND_DIRECTION:
				case RAIN:
				case AIR_PRESSURE:
				case FLOAT:
					return DecimalType.valueOf(Float.toString(ByteBuffer.wrap(msg.data).order(ByteOrder.LITTLE_ENDIAN).getFloat()));
				case UINT32:
					return DecimalType.valueOf(Integer.toString(ByteBuffer.wrap(msg.data).order(ByteOrder.LITTLE_ENDIAN).getInt()));
				default:
					break;
			}
		} else if (typeClass.equals(StringType.class)) {
			if (msg.msgtype == HomecanMsg.MsgType.KEY_SEQUENCE) {
				return StringType.valueOf(Arrays.toString(msg.data));
			} else if (msg.msgtype == HomecanMsg.MsgType.FTKID) {
				return StringType.valueOf(Integer.toString(ByteBuffer.wrap(msg.data).order(ByteOrder.LITTLE_ENDIAN).getInt()));
			} else if (msg.msgtype == HomecanMsg.MsgType.IR) {
				ByteBuffer buffer = ByteBuffer.wrap(msg.data).order(ByteOrder.LITTLE_ENDIAN);
				String protocol = Integer.toString(buffer.get()&0xFF);
				String address = Integer.toString(buffer.getShort()&0xFFFF);
				String command = Integer.toString(buffer.getShort()&0xFFFF);
				String flags = Integer.toString(buffer.get()&0xFF);
				return StringType.valueOf("["+protocol+","+address+","+command+","+flags+"]");
			} else {
				return StringType.valueOf(new String(msg.data));
			}
		} else if (typeClass.equals(OpenClosedType.class)) {
			return msg.data[0] == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
		} else if (typeClass.equals(StopMoveType.class)) {
			return msg.data[0] == 0 ? StopMoveType.STOP : StopMoveType.MOVE;
		} else if (typeClass.equals(DateTimeType.class)) {
			logger.warn("Unmapped msgtype received '{}'", msg.msgtype);
			return null; // not yet used
		}
		logger.warn("Unmapped msgtype received '{}'", msg.msgtype);
		return null;
	}

}
