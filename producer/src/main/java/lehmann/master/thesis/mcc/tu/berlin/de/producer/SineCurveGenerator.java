package lehmann.master.thesis.mcc.tu.berlin.de.producer;

import java.util.Random;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.inspector.DoNothingInspector;
import lehmann.master.thesis.mcc.tu.berlin.de.inspector.Inspector;

public class SineCurveGenerator implements Function<Long, Float>{
	
	private static Logger log = LoggerFactory.getLogger(SineCurveGenerator.class);
	private int periodLength;
	private int nextPeriodLength;
	private double amplitude;
	private double nextAmplitude;
	private long lastPeriodEnd = 0;
	private Random random = new Random(1000);
	private Inspector inspector = new DoNothingInspector();
	
	public SineCurveGenerator(int periodLength, double amplitude) {
		this.periodLength = periodLength;
		this.nextPeriodLength = periodLength;
		this.amplitude = amplitude;
		this.nextAmplitude = amplitude;
	}
	
	public void setPeriodLength(int periodLength) {
		this.nextPeriodLength = periodLength;
	}
	
	public void setNextAmplitude(double amplitude) {
		this.nextAmplitude = amplitude;
	}
	
	public void registerInspector(Inspector ins) {
		this.inspector = ins;
	}

	@Override
	public Float apply(Long x) {
		
		long pos = (x - lastPeriodEnd) % periodLength;
		
		
		if(pos == 0) {
			if(outstandingChange()) {
				this.inspector.informChange(nextAmplitude, nextPeriodLength);
			}
			this.periodLength = nextPeriodLength;
			this.amplitude = nextAmplitude;
			this.lastPeriodEnd = x;
			log.info(String.format("New Period with a = %.2f & pl = %d", amplitude, periodLength));
		}
		
		double x_value = (pos / (double) periodLength) * (2 * Math.PI);
		
		double value = Math.sin(x_value) * this.amplitude;
		double noise = random.nextGaussian() * 0.1;
		
		return (float) (value + noise);
	}

	public double getAmplitude() {
		return amplitude;
	}

	public int getPeriodLength() {
		return periodLength;
	}
	
	public boolean outstandingChange() {
		return periodLength != nextPeriodLength || amplitude != nextAmplitude;
	}
	
	
	
}
