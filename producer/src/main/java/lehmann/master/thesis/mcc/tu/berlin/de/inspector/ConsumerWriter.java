package lehmann.master.thesis.mcc.tu.berlin.de.inspector;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.DataEntry;

public class ConsumerWriter <T extends DataEntry> {
	
	private static Logger log = LoggerFactory.getLogger(ConsumerWriter.class);
	private KafkaConsumer<Long, T> consumer;
	private final String outputPath;
	private final String header;
	private boolean writeHeader = true;
	private final String TOPIC;

	public ConsumerWriter(String BOOTSTRAP_SERVERS, String TOPIC, String deserializer, String outputFolder, String header) {
		
		this.TOPIC = TOPIC;
		this.header = header;
		final Properties propsConsumer = new Properties();
		
		String groupID = "ConsumerWriter_" + new Random().nextInt(Integer.MAX_VALUE);
		
		propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        propsConsumer.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propsConsumer.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000);
        
        propsConsumer.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 6000);
        propsConsumer.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        propsConsumer.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 6000);
        propsConsumer.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        
        propsConsumer.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        
        // Create the consumer using props.
        this.consumer = new KafkaConsumer<>(propsConsumer);
        
//        this.consumer.subscribe(Collections.singletonList(TOPIC));
        this.consumer.assign(Collections.singleton(new TopicPartition(this.TOPIC, 0)));


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        this.outputPath = outputFolder + TOPIC + ".csv";
		
	}
	
	public void consume() {
		
		log.info("Start consuming topic: " + this.TOPIC);
		
		PrintWriter pw = null;
		
		try {
			
			pw = new PrintWriter(outputPath);
			
			if(writeHeader) {
				pw.print("Consumer.Time,Kafka.Time,Kafka.Offset,");
				pw.println(header);
				writeHeader = false;
			}
				
			boolean lastPollWasEmpty = false;
			
			boolean interrupted = false;
			
			while(!lastPollWasEmpty || !(interrupted = Thread.interrupted())) {
				
				try {
					ConsumerRecords<Long, T> records = this.consumer.poll(Duration.ofSeconds(1));
					
					lastPollWasEmpty = records.isEmpty();
					
					long time = System.currentTimeMillis();
					
					log.info("Received " + records.count() + " records for topic " + this.TOPIC);
					
					for (ConsumerRecord<Long, T> consumerRecord : records) {
						
						String line = String.format("%d,%d,%d,%s", time, consumerRecord.timestamp(), consumerRecord.offset(), consumerRecord.value().getCSVData());
						pw.println(line);
						
					}
				}catch (org.apache.kafka.common.errors.InterruptException e) {
					log.error("Topic: " + this.TOPIC, e);
					lastPollWasEmpty = true;
					Thread.currentThread().interrupt();
				}catch (Exception e) {
					log.error("Topic: " + this.TOPIC, e);
				}finally {
//					pw.flush();
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("Topic: " + this.TOPIC, e);
		} finally {
			if(pw != null) {
				pw.close();
			}
		}
		
		
	}

}
