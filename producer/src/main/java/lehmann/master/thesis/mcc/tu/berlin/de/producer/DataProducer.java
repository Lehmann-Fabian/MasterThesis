package lehmann.master.thesis.mcc.tu.berlin.de.producer;
import java.util.Properties;
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
		
		Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "Sensor" + topic);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  RawDataEntrySerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
        //Newer discard any records
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        producer = new KafkaProducer<>(props);
        
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
        		final RawDataEntry data = new RawDataEntry(System.currentTimeMillis(), getNextMeasurement.apply(i));
	    		final ProducerRecord<Long, RawDataEntry> record = new ProducerRecord<>(TOPIC, data);
	
	    		Future<RecordMetadata> send = producer.send(record);
	    		inspector.addProducedRecord(System.currentTimeMillis(), send);
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