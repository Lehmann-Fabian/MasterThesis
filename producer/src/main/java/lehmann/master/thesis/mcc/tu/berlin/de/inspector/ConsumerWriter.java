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
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.producer.data.DataEntry;

public class ConsumerWriter <T extends DataEntry> {
	
	private static Logger log = LoggerFactory.getLogger(ConsumerWriter.class);
	private KafkaConsumer<Long, T> consumer;
	private final Properties propsConsumer;
	private final String outputPath;
	private final String header;
	private boolean writeHeader = true;
	private final String TOPIC;

	public ConsumerWriter(String BOOTSTRAP_SERVERS, String TOPIC, String deserializer, String outputFolder, String header) {
		
		this.TOPIC = TOPIC;
		this.header = header;
		this.propsConsumer = new Properties();
		
		String groupID = "ConsumerWriter_" + new Random().nextInt(Integer.MAX_VALUE);
		propsConsumer.put(ConsumerConfig.CLIENT_ID_CONFIG, "Consumer-Writer-Consumer" + new Random().nextInt());
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
        
        propsConsumer.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 5000);
        
        
        propsConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 10);
        propsConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 10);
        propsConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 200);
        
        propsConsumer.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 2000);
        
        propsConsumer.put(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 5000);
        
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
			
			long highestOffset = 0;
			long lastFlush = 0;
			
			while(!lastPollWasEmpty || !(interrupted = Thread.interrupted())) {
				
				
				try {
					ConsumerRecords<Long, T> records = this.consumer.poll(Duration.ofSeconds(1));
					
					lastPollWasEmpty = records.isEmpty();
					
//					if(records.isEmpty()) {
//						
//						try {
//							consumer.unsubscribe();
//							TopicPartition topicPartition = new TopicPartition(this.TOPIC, 0);
//							this.consumer.assign(Collections.singleton(topicPartition));
//							this.consumer.seekToEnd(Collections.singleton(topicPartition));
//							long last = this.consumer.position(topicPartition);
//							if(highestOffset < 0) {
//								this.consumer.seekToBeginning(Collections.singleton(topicPartition));
//							}else {
//								this.consumer.seek(topicPartition, Math.min(highestOffset, last));
//							}
//						}catch (Exception e) {
//							log.error("While fetching partitions of " + TOPIC + "...", e);
//						}
//						
//					}
					
					long time = System.currentTimeMillis();
					
					log.info("Received " + records.count() + " records for topic " + this.TOPIC);
					
					for (ConsumerRecord<Long, T> consumerRecord : records) {
						
						if(highestOffset != consumerRecord.offset()) {
							String line = String.format("%d,%d,%d,%s", time, consumerRecord.timestamp(), consumerRecord.offset(), consumerRecord.value().getCSVData());
							pw.println(line);
							
							highestOffset = Math.max(highestOffset, consumerRecord.offset());							
						}
						
					}
				}catch (org.apache.kafka.common.errors.InterruptException e) {
					log.error("Topic: " + this.TOPIC, e);
					lastPollWasEmpty = true;
					Thread.currentThread().interrupt();
				}catch (Exception e) {
					log.error("Topic: " + this.TOPIC, e);
				}finally {
					if(lastFlush + 2000 < System.currentTimeMillis()) {
						pw.flush();
						lastFlush = System.currentTimeMillis();
					}
				}
				
				if(interrupted) {
					Thread.currentThread().interrupt();
				}
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("Topic: " + this.TOPIC, e);
		} finally {
			log.info("Closed output for " + TOPIC);
			if(pw != null) {
				pw.close();
			}
		}
		
		
	}

}
