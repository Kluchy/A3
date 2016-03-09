import java.util.List;

public class Tester {

	public static void main(String[] args) {
		int x = 1;
		System.out.println(new String(("000"+x).getBytes()));
		String in = "someth|ing to do with nothing";
		String in2 = "1234567|8901234567890";
		// String in3 = "Much adi wit|h you I hate Mallory";
		String sym = "sym";
		// String trans = "keyTrnapsrkfjn43c";
		byte[] out = Util.securePack(in.getBytes(), in2.getBytes());
		out = Util.securePack(sym.getBytes(), out);
		System.out.println(new String(out));
		List<byte[]> splitOut = Util.secureUnpack(out);
		System.out.println(new String(splitOut.get(0)));
		System.out.println(new String(splitOut.get(1)));
		splitOut = Util.secureUnpack(splitOut.get(1));
		System.out.println(new String(splitOut.get(0)));
		System.out.println(new String(splitOut.get(1)));

	}	
}
