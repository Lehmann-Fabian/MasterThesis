package lehmann.master.thesis.mcc.tu.berlin.de.producer;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.inspector.Inspector;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.RawDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.RawDataEntrySerializer;

public class DataProducer {
	
	
	private static Logger log = LoggerFactory.getLogger(DataProducer.class);
	private final KafkaProducer<Long, RawDataEntry> producer;
	private final String TOPIC;
	private final int frequencyInMs;
	private final Function<Long, Float> getNextMeasurement;
	private final AdminConnection adminConnection;
	private final Inspector inspector;
	private final int durationInMs;
	
	public DataProducer(String server, String topic, int frequencyInMs, Function<Long, Float> getNextMeasurement, Inspector inspector, int duration) {
		
		this.TOPIC = topic;
		this.frequencyInMs = frequencyInMs;
		this.getNextMeasurement = getNextMeasurement;
		this.inspector = inspector;
		this.durationInMs = duration * 1000 * 60;
		
		Properties propsProducer = new Properties();
		propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, "Data-Producer-Producer" + new Random().nextInt());
        propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, "Sensor" + topic);
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  RawDataEntrySerializer.class.getName());
//        props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        propsProducer.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
//        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
        //Newer discard any records
        propsProducer.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        propsProducer.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, Integer.MAX_VALUE);
        propsProducer.put(ProducerConfig.ACKS_CONFIG, "all");
        propsProducer.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        propsProducer.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        propsProducer.put(ProducerConfig.METADATA_MAX_IDLE_CONFIG, 5000);
        propsProducer.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 2000);
        
        propsProducer.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10);
        propsProducer.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 10);
        propsProducer.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 200);
        
        propsProducer.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 2000);
        propsProducer.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 500);
        
        propsProducer.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 2000);
        propsProducer.put(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, 1);
        producer = new KafkaProducer<>(propsProducer);
        
        this.adminConnection = new AdminConnection(server);
		
	}
	
    public void runProducer() {
    	
    	log.info("Run producer for topic " + this.TOPIC);
    	
    	//TODO set Replica to 3
    	if(!adminConnection.hasTopic(TOPIC)) adminConnection.createTopic(TOPIC, 1, (short) 3);
    	
        long time = System.currentTimeMillis();
        try {
        	long i = 0;
        	//Stop after 15min
        	while(!Thread.interrupted() && time + durationInMs > System.currentTimeMillis()) {
        		long sendTime = System.currentTimeMillis();
        		final RawDataEntry data = new RawDataEntry(sendTime, getNextMeasurement.apply(i));
	    		final ProducerRecord<Long, RawDataEntry> record = new ProducerRecord<>(TOPIC, data);
	
	    		Future<RecordMetadata> send = producer.send(record);
	    		inspector.addProducedRecord(sendTime, send);
//	    		log.info(String.format("New record: i = %d, ts = %d, m = %f", i, data.getTimestamp(), data.getMeasurement()));
	    		
	    		//Flush at least all 10 values
	    		//if(i % 10 == 0) producer.flush();
	
	            long elapsedTime = System.currentTimeMillis() - time;
	            long wait = - (elapsedTime - frequencyInMs * i);
	            if(wait > 0)
					try {
						Thread.sleep(wait);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread().interrupt();
					}
	            i++;
	            
        	}
        } finally {
            producer.flush();
            producer.close();
        }
        System.out.println("Wait a minute!");
        try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        System.out.println("DONE");
    }
    
    

}