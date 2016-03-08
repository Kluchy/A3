import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
	static final byte[] TERMINATOR = "\n".getBytes();
	static final byte[] ATTACK_FLAG = "%%@tTaCk_fLaG%%".getBytes();
	// used for packaging data in crypto algorithms
	static final String del = "|";

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
}
