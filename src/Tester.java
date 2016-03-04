import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Tester {

	@Test
	public void test() {
		String s = "boo";
		byte[] ss = s.getBytes();
//		assertEquals(s, new String(ss));
//		var str = System.Text.Encoding.Default.GetString(ss);
		byte[] x = new byte[] {'2','4'};
		byte[] y = new byte[] {'2','4'};
		assertTrue(x[0]==y[0]);
		List<byte[]> sX = Principal.unpack(x);
		assertEquals(1,sX.size());
		assertEquals(x, sX.get(0));
		byte [] z = Principal.pack(x,y);
		for (byte q : z)
			System.out.println(q);
		List<byte[]> zz = Principal.unpack(z);
		for (byte[] w: zz) {
			System.out.println("--------------------");
			for (byte ww: w) {
				System.out.println(ww);

			}
		}
		byte[] b = Arrays.copyOfRange(y, 0, 1);
		for (byte bb: b) {
			System.out.println(bb);

		}
		try {
			Client c = new Client("8080");
			c.send("boo");
			c.send("aaaaaaa");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
