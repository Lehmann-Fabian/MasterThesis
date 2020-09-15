package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.FilteredDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.FilteredDataEntryDeserializer;
import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.Warning;

public class DataAnalyst {
	
	private static Logger log = LoggerFactory.getLogger(DataAnalyst.class);
	private final Consumer<Long, FilteredDataEntry> consumer;
	private final String TOPIC;
	private final String TOPIC_OUTPUT;
	private final Function<float[], List<String>> analyst;
	private final int analyseSize;
	private final KafkaProducer<Long, String> producer;
	private final AdminConnection zk;
	private final String BOOTSTRAP_SERVERS;
	
	public DataAnalyst(String BOOTSTRAP_SERVERS, String TOPIC, Function<float[], List<String>> filter, int analyseSize) {
		
		this.TOPIC = TOPIC + "_filtered";
		this.TOPIC_OUTPUT = TOPIC + "_warnings";
		this.analyst = filter;
		this.analyseSize = analyseSize;
		this.BOOTSTRAP_SERVERS = BOOTSTRAP_SERVERS;

		final Properties propsConsumer = new Properties();

        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, this.TOPIC);
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, FilteredDataEntryDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create the consumer using props.
        this.consumer = new KafkaConsumer<>(propsConsumer);
        
        
        Properties propsProducer = new Properties();
        propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, TOPIC_OUTPUT);
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(propsProducer);

        // Subscribe to the topic.
        this.consumer.subscribe(Collections.singletonList(this.TOPIC));
        
        this.zk = new AdminConnection(BOOTSTRAP_SERVERS);

	}
	
	private void extractValues(ConsumerRecord<Long, FilteredDataEntry>[] input, float[] output) {
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i].value().getMeasurement();
		}
	}
	
	
	/**
	 * 
	 * @return The offset to handle it correctly
	 */
	private long seekToStart() {
		
		log.info("Seek to start for topic: " + TOPIC);
		
		OffsetFinder offsetFinder = new OffsetFinder(BOOTSTRAP_SERVERS, TOPIC_OUTPUT, this.zk);
		long offset = offsetFinder.getOffset();
		
		
		log.info("New offset = " + offset + " for topic: " + TOPIC);
		
		int maxTrials = 10;
		Set<TopicPartition> partitions;
		do{
			this.consumer.poll(Duration.ofMillis(1000));
			partitions = this.consumer.assignment();
		}while(partitions.isEmpty() && --maxTrials > 0);
		
		if(partitions.isEmpty()) throw new IllegalStateException("Cannot connect to topic: " + TOPIC);
		else if(partitions.size() != 1) throw new IllegalStateException("Topic has more than one partition");
		
		if(offset < 0) {
			this.consumer.seekToBeginning(partitions);
		}else {
			for (TopicPartition topicPartition : partitions) {
				this.consumer.seek(topicPartition, offset);
			}
		}
		
		return offset;
	}
	
	public void runAnalysis() {
		
		log.info("Run analysis for topic: " + TOPIC);
		
		//TODO replica to 3
		if(!zk.hasTopic(TOPIC_OUTPUT)) {
			zk.createTopic(TOPIC_OUTPUT, 1, (short) 1);
		}
		
		final int maxValues = analyseSize;
		
		float[] data = new float[maxValues];
		
		@SuppressWarnings("unchecked")
		ConsumerRecord<Long, FilteredDataEntry>[] bufferData = new ConsumerRecord[maxValues];
				
		long highestTimestamp = 0;
		long highestOffset = seekToStart();
		//Change to last offset written
		int currentIndex = 0;
		
		try {
			while(true) {
				
				ConsumerRecords<Long, FilteredDataEntry> records = this.consumer.poll(Duration.ofSeconds(1));
				
				log.info("Got " + records.count() + " for topic: " + TOPIC);
				
				for (ConsumerRecord<Long, FilteredDataEntry> consumerRecord : records) {
					
					if(consumerRecord.value().getTimestamp() >= highestTimestamp && consumerRecord.offset() > highestOffset) {
						
						highestTimestamp = consumerRecord.value().getTimestamp();
						highestOffset = consumerRecord.offset();
						
						bufferData[currentIndex++] = consumerRecord;
						
						
						//Analyse and Push
						if(currentIndex == maxValues) {
							
							extractValues(bufferData, data);
							log.info("Offset: " + bufferData[0].offset() + " value: " + data[0]);
							List<String> warnings = analyst.apply(data);
							log.info(warnings.size() + " warnings");
							for (String warningText : warnings) {
								
								Warning warning = new Warning(warningText, bufferData[0].offset(), bufferData[maxValues - 1].offset(), data, bufferData[0].value().getTimestamp(), bufferData[maxValues - 1].value().getTimestamp());
								
								ObjectMapper objectMapper = new ObjectMapper();
								String warningJSON;
								try {
									warningJSON = objectMapper.writeValueAsString(warning);
								} catch (JsonProcessingException e) {
									warningJSON = warningText;
									e.printStackTrace();
								}
								
								final ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC_OUTPUT, warningJSON);
					    		producer.send(record);
					    		log.info("Send warning: " + record + " to topic: " + TOPIC_OUTPUT);
							}
							if(!warnings.isEmpty()) producer.flush();
							
							currentIndex = 0;
							
							//Commit processed data
//							OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(bufferData[maxValues - 1].offset());
//							Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
//							
//							for(TopicPartition partition : consumer.assignment()) {
//								offsets.put(partition, offsetAndMetadata);
//							}
//							
//							consumer.commitAsync(offsets, (a,b) -> {if (b != null) log.error("Problem while sending offsets", b.getCause());});
						}
						
					}
					
				}
				
			}
		} finally {
	        producer.flush();
	        producer.close();
	    }
		
	}

}
