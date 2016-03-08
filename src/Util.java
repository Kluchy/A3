import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
	static final int HEAD_FIELD_SIZE = 4;
	static final byte[] ATTACK_FLAG = "%%@tTaCk_fLaG%%".getBytes();
	// used for packaging data in crypto algorithms
	static final String del = "|";

	/**
	 * pre-condition packet size must fit within 8 bytes.
	 * @param number
	 * @return
	 */
	static byte[] size2Byte(int num) {
		String str = "";
		if (num < 10) {
			str = "000"+num;
		}
		else if (num < 100) {
			str = "00" + num;
		}
		else if (num < 1000) {
			str = "0" + num;
		}
		else {
			str = "" + num;
		}
		return str.getBytes();
	}
	//	static byte[] size2Byte(byte[] packet) {
	//		ByteBuffer size = ByteBuffer.allocate(8);
	//		size.putInt(packet.length);
	//		return size.array();
	//	}
	//	static byte[] size2Byte(byte[] packet) {
	//		ByteBuffer size = ByteBuffer.allocate(8);
	//		size.putInt(packet.length);
	//		return size.array();
	//	}

	static int byte2Int(byte[] block) {
		String str = new String(block);
		while (str.charAt(0) == '0') {
			str = str.substring(1);
		}
		return Integer.parseInt(str);
	}

	/**
	 * @spec Helper
	 * @param one
	 * @param two
	 * @return byte[] with all elements of
	 *           one followed by all elements of two
	 */
	static byte[] concat(byte[] one, byte[] two) {
		byte[] oneTwo = new byte[one.length + two.length];
		System.arraycopy(one, 0, oneTwo, 0, one.length);
		System.arraycopy(two, 0, oneTwo, one.length, two.length);
		return oneTwo;
	}

	/**
	 * @spec intended for transferring data through TCP
	 * @param one
	 * @param two
	 * @return byte[ one + 'del' + two ]
	 */
	static byte[] pack(byte[] one, byte[] two) {
		byte[] oneAnd = Util.concat(one, del.getBytes());
		return Util.concat(oneAnd, two);
	}

	/**
	 * @spec used to receive data from TCP connection
	 * @param bytes
	 * @return bytes, if bytes does not contain the 'del' delimiter or
	 *         List< half1, half2 > such that half1 + 'del' + half2 = bytes 
	 */
	static List<byte[]> unpack(byte[] bytes) {
		ArrayList<byte[]> res = new ArrayList<byte[]>();
		byte target = del.getBytes()[0];
		int targetIndex = -1;
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			if (b == target) {
				targetIndex = i;
				break;
			}
		}
		if (targetIndex == -1) {
			res.add(bytes);
			return res;
		}
		res.add(Arrays.copyOfRange(bytes, 0, targetIndex));
		res.add(Arrays.copyOfRange(bytes, targetIndex+1, bytes.length));
		return res;
		//		String in = new String(bytes);
		//		int indexOfDel = in.indexOf(del);
		//		if (indexOfDel == -1) {
		//			res.add(bytes);
		//			return res;
		//		}
		//		res.add(in.substring(0,indexOfDel).getBytes());
		//		res.add(in.substring(indexOfDel+1).getBytes());
		//		return res;
	}

	static byte[] securePack(byte[] one, byte[] two) {
		byte[] size1 = size2Byte(one.length);
		byte[] size2 = size2Byte(two.length);
		byte[] data = concat(one, two);
		//		byte[] metadata = concat(size1, concat(size2,data));
		byte[] metadata = concat(size1, concat(size2,data));
		return metadata;
	}

	static List<byte[]> secureUnpack(byte[] pack) {
		List<byte[]> result = new ArrayList<byte[]>();
		byte[] temp = Arrays.copyOfRange(pack, 0, HEAD_FIELD_SIZE);//unpack(pack);
		int size1 = byte2Int(temp);//Integer.parseInt(new String(temp.get(0)));
		temp = Arrays.copyOfRange(pack, HEAD_FIELD_SIZE, HEAD_FIELD_SIZE*2);//unpack(temp.get(1));
		int size2 = byte2Int(temp);//Integer.parseInt(new String(temp.get(0)));
		byte[] data = Arrays.copyOfRange(pack, HEAD_FIELD_SIZE*2, HEAD_FIELD_SIZE*2 + size1+size2);//temp.get(1);
		byte[] one = Arrays.copyOfRange(data, 0, size1);
		byte[] two = Arrays.copyOfRange(data, size1, size1 + size2);
		result.add(one);
		result.add(two);
		return result;
	}
}
