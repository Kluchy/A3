import static org.junit.Assert.*;

import org.junit.Test;

public class Tester {

	@Test
	public void test() {
		String s = "boo";
		byte[] ss = s.getBytes();
		assertEquals(s, new String(ss));
//		var str = System.Text.Encoding.Default.GetString(ss);
	}

}
