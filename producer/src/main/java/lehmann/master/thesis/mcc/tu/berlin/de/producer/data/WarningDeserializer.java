package lehmann.master.thesis.mcc.tu.berlin.de.producer.data;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WarningDeserializer implements Deserializer<Warning> {

	public Warning deserialize(String arg0, byte[] arg1) {
		ObjectMapper mapper = new ObjectMapper();
		Warning warning = null;
		try {
			warning = mapper.readValue(arg1, Warning.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return warning;
	}	

}