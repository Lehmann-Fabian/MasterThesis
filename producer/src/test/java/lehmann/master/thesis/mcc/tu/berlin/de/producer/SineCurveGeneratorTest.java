package lehmann.master.thesis.mcc.tu.berlin.de.producer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SineCurveGeneratorTest {

	@Test
	public void test() {
		SineCurveGenerator sg = new SineCurveGenerator(100, 5);
		
		for (long l = 0; l <= 100; l++) {
			double v = 0;
			int repeatations = 1000000;
			for (int i = 0; i < repeatations; i++) {
				v += sg.apply(l) / repeatations;
			}
			double expect = Math.sin((l / 100.0) * (2 * Math.PI)) * 5;
			assertEquals(expect, v, 0.001);
			
			System.out.println(v);
			
			if(l == 30) {
				sg.setNextAmplitude(1);
				sg.setPeriodLength(10);
			}
			
		}
		
		
		for (long l = 101; l <= 120; l++) {
			double v = 0;
			int repeatations = 1000000;
			for (int i = 0; i < repeatations; i++) {
				v += sg.apply(l) / repeatations;
			}
			double expect = Math.sin(((l - 100) / 10.0) * (2 * Math.PI));
			assertEquals(expect, v, 0.1);
			System.out.println(v);
		}
		
	}
	
	
	


}