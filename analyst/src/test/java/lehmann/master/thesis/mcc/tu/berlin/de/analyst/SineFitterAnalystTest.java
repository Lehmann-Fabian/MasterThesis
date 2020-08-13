package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class SineFitterAnalystTest {

	@Test
	public void test() {
		Random r = new Random();
		for (int p = 0; p < 1000; p++) {
			int l = 1000;
			double a = 5;
			SineCurveGenerator sg = new SineCurveGenerator(l, a);
			float[] d = new float[300];
			long start = r.nextInt(50000);
			for (long i = 0; i < d.length; i++) {
				d[(int) i] = sg.apply(i + start);
				System.out.println(d[(int) i]);
			}
			SineFitterAnalyst sineFitterAnalyst = new SineFitterAnalyst(200, a, l, 0.1);
			System.out.println(p);
			assertTrue(sineFitterAnalyst.apply(d).isEmpty());
		}
		
	}
	
}
