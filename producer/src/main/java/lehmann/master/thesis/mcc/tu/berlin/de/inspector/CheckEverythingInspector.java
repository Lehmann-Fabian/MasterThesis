package lehmann.master.thesis.mcc.tu.berlin.de.inspector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.producer.SineCurveGenerator;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.SingleValueProducer;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.FilteredDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.FilteredDataEntryDeserializer;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.Triple;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.RawDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.RawDataEntryDeserializer;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.Warning;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.WarningDeserializer;

public class CheckEverythingInspector implements Inspector {
	
	private static Logger log = LoggerFactory.getLogger(CheckEverythingInspector.class);
	private final SingleValueProducer getNextMeasurement;
	private final Queue<Triple> producedData =  new LinkedBlockingQueue<Triple>();
	private long producedElements = 0;
	private double startAmplitude;
	private int startPeriodLength;
	private final int frequencyInMs;
	private HashSet<Integer> secondsToChange;
	private final Thread[] threads = new Thread[4];
	private final String outputFolder;
	private final PrintWriter modelChangeWriter;

	public CheckEverythingInspector(SingleValueProducer getNextMeasurement, int frequencyInMs, HashSet<Integer> secondsToChange, String BOOTSTRAP_SERVERS, String TOPIC, String outputPath) {
		this.getNextMeasurement = getNextMeasurement;
		this.frequencyInMs = frequencyInMs;
		this.secondsToChange = secondsToChange;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		this.outputFolder = outputPath + File.separator + sdf.format(new Date()) + File.separator;
		
		new File(this.outputFolder).mkdirs();
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(this.outputFolder + TOPIC + "_modelchange.csv");
			pw.println("timestamp,value,producedElements");
		}catch (Exception e) {
			e.printStackTrace();
		}
		this.modelChangeWriter = pw;
		
		init(BOOTSTRAP_SERVERS, TOPIC);
	}

	
	private void init(final String BOOTSTRAP_SERVERS, final String TOPIC) {
		
		log.info("Init all consumer for topic: " + TOPIC);
		
		@SuppressWarnings("rawtypes")
		ConsumerWriter[] cw = new ConsumerWriter[3]; 
		
		cw[0] = new ConsumerWriter<RawDataEntry>(BOOTSTRAP_SERVERS, TOPIC, RawDataEntryDeserializer.class.getName(), 
				outputFolder, "Data.Timestamp,Data.Measurement");
		
		cw[1] = new ConsumerWriter<FilteredDataEntry>(BOOTSTRAP_SERVERS, TOPIC + "_filtered", FilteredDataEntryDeserializer.class.getName(), 
				outputFolder, "Data.Offset,Data.Timestamp,Data.Measurement");
		
		cw[2] = new ConsumerWriter<Warning>(BOOTSTRAP_SERVERS, TOPIC + "_warnings", WarningDeserializer.class.getName(), 
				outputFolder, "Record.Warning,Record.BeginOffset,Record.EndOffset,Record.BeginTimestamp,Record.EndTimestamp,Record.Values");
		
		for (int i = 0; i < cw.length; i++) {
			@SuppressWarnings("rawtypes")
			final ConsumerWriter c = cw[i];
			threads[i] = new Thread(() -> c.consume());
			threads[i].start();
		}
		
		//Thread to check produced content;
		threads[3] = new Thread(() -> {
			
			PrintWriter printWriter = null;
			
			try {
				
				printWriter = new PrintWriter(outputFolder + TOPIC + "_produced.csv");
				
				printWriter.println("Kafka.Offset,Kafka.Timestamp,Kafka.acktime.local,Producer.Timestamp,ProducedElements");
				boolean interrupted = false;
				
				long l = 0;
				
				while(!producedData.isEmpty() || (!Thread.interrupted() && !interrupted)) {
					
					try {
						Triple poll = producedData.poll();
						if(poll != null) {
							long sendTimestamp = poll.getTimestamp();
							Future<RecordMetadata> data = poll.getData();
							long producedElements = poll.getProducedElements();
							String line = null;
							
							boolean gotResult = false;
							
							while(!gotResult) {
								try {
									RecordMetadata recordMetadata = data.get();
									line = String.format("%d,%d,%d,%d,%d", recordMetadata.offset(), recordMetadata.timestamp(), System.currentTimeMillis(), sendTimestamp, producedElements);
									if(l++ % 200 == 0) log.info("Send message successfully " + recordMetadata.offset());
									gotResult = true;
								} catch (InterruptedException e) {
									interrupted = true;
									e.printStackTrace();
								} catch (ExecutionException e) {
									e.printStackTrace();
									line = e.getMessage();
									gotResult = true;
								}
							}
							printWriter.println(line);
						} else {
							printWriter.flush();
						}
					}catch(Exception e) {
						log.error("Topic: " + TOPIC, e);
						e.printStackTrace();
					}
					
				}

			} catch (FileNotFoundException e) {
				log.error("Topic: " + TOPIC, e);
				e.printStackTrace();
			}finally {
				if(printWriter != null) printWriter.close();
			}
			
			
		});
		threads[3].start();
		
	}


	private long lastChange = 0;
	private boolean changedBack = true;
	private int lastChecked = 0;
	@Override
	public void addProducedRecord(long sendTime, Future<RecordMetadata> send) {
		producedData.add(new Triple(sendTime, send, producedElements));
		producedElements++;
		
		int currentSecond = (int) (producedElements / (1000.0 / frequencyInMs));
		
		
		if (!changedBack && lastChange + 100 < producedElements) {
			changedBack = true;
			getNextMeasurement.setAdditionalFactor(0);
			//first data with new value will be producedElements + 1, thus we have to increment it before
			informChange(0, producedElements);
			log.info("Value was reverted to default!");
		}
		
		if (lastChecked != currentSecond) {
			
			System.out.println(currentSecond);
			
			lastChecked = currentSecond;
			
			if(secondsToChange.contains(currentSecond)) {
				lastChange = producedElements;
				changedBack = false;
				int value = 10;
				
				getNextMeasurement.setAdditionalFactor(value);
				informChange(10, producedElements);
				log.info("Value to " + value);
			}
			
		}
	}
	@Override
	public void close() {
		log.warn("Stop all consumers!");
		for (Thread thread : threads) {
			thread.interrupt();
		}
		this.modelChangeWriter.close();
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void informChange(double value, long producedElements) {
		this.modelChangeWriter.println(String.format(Locale.US, "%d,%f,%d", System.currentTimeMillis(), value, producedElements));
		this.modelChangeWriter.flush();
	}

}
