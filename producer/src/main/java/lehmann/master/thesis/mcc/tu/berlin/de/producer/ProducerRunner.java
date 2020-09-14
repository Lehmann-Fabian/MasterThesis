package lehmann.master.thesis.mcc.tu.berlin.de.producer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.inspector.CheckEverythingInspector;
import lehmann.master.thesis.mcc.tu.berlin.de.inspector.DoNothingInspector;
import lehmann.master.thesis.mcc.tu.berlin.de.inspector.Inspector;

public class ProducerRunner {
	
	private static Logger log = LoggerFactory.getLogger(ProducerRunner.class);
	private static String TOPIC = "t1";
    private static String BOOTSTRAP_SERVERS = "localhost:9093";
    private final static String OUTPUT_PATH = "data";
    private final static int FREQUENCY_IN_MS = 5;
	
	public static void main(String[] args) throws Exception {
		
		boolean prod = false;
		boolean checkEverything = true;
		
		if(args.length >= 5) {
			BOOTSTRAP_SERVERS = args[0];
			TOPIC = args[1];
			String nodestart = args[2];
			TOPIC = TOPIC.substring(nodestart.length());
			if(TOPIC.length() == 0) {
				TOPIC = "t1";
			}else {
				TOPIC = "t" + TOPIC;
			}
			System.out.println("Use server: " + BOOTSTRAP_SERVERS);
			System.out.println("Use topic: " + TOPIC);
			prod = args[3].toLowerCase().equals("true");
			checkEverything = args[4].toLowerCase().equals("true");
			System.out.println("Run Prod: " + prod);
			System.out.println("Inspect all: " + prod);
		}
		
		try {
			PropertyConfigurator.configure("log4j.properties");
			
			log.info("Start");
			
//			SineCurveGenerator scg = new SineCurveGenerator(1000, 3);
			
			SingleValueProducer scg = new SingleValueProducer();
			
			HashSet<Integer> secondsToChange = new HashSet<Integer>();
			List<Integer> times = new LinkedList<Integer>();
			for (int i = 0; i < 500; i++) {
				times.add(i * 25);
			}
					
			Arrays.asList(10, 30, 70, 80, 95, 110, 115, 118, 145, 150, 165, 188, 200, 220, 225, 260);
			
			secondsToChange.addAll(times);
			
			final Inspector inspector = checkEverything ? 
					new CheckEverythingInspector(scg, FREQUENCY_IN_MS, secondsToChange, BOOTSTRAP_SERVERS, TOPIC, OUTPUT_PATH) 
					: new DoNothingInspector();
					
					
			DataProducer dataProducer = new DataProducer(BOOTSTRAP_SERVERS, TOPIC, FREQUENCY_IN_MS, scg, inspector);
			
			Thread producerThread = new Thread(() -> dataProducer.runProducer());
			producerThread.start();
			producerThread.setPriority(Thread.MAX_PRIORITY);
			
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					log.warn("In shutdown hook");
					producerThread.interrupt();
					try {
						producerThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					inspector.close();
				}
			}, "Shutdown-thread"));
			log.warn("System is running");
			
			producerThread.join();
			inspector.close();
			
			Thread.sleep(Integer.MAX_VALUE);
			
		}catch (Exception e) {
			log.error("Error while running", e);
		}
		
		
		
		
		
		if(!prod) {
		    try {
		        System.in.read();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    System.exit(0);
		}
    	
	}

}
