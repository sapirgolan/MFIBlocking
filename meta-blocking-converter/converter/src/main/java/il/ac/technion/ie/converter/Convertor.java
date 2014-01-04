package il.ac.technion.ie.converter;

import il.ac.technion.ie.utils.SerializationUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import DataStructures.Attribute;
import DataStructures.EntityProfile;



public class Convertor {

	private static final String DELIMITER_VALUES_IN_FIELD = " ";
	private static final CharSequence CSV_SEPERATOR = ",";
	private static final CharSequence NEW_LINE = "\n";
	private List<String> fieldsNames;

	public List<String> extractFieldsNames(final List<EntityProfile> list) {
		ArrayList<String> result = new ArrayList<String>();
		for (EntityProfile entityProfile : list) {
			HashSet<Attribute> attributes = entityProfile.getAttributes();
			for (Attribute attribute : attributes) {
				String name = attribute.getName();
				if (result.contains(name) == false) {
					result.add(name);
				}
			}
		}
		this.setFieldsNames(result);
		return result;
	}
	
	//TODO: change to map of Map<String, StringBuilder> 
	public List<Map<String, String>> extractValues( final List<EntityProfile> entityProfiles ) {
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>(entityProfiles.size());
		List<String> fieldsNames = extractFieldsNames(entityProfiles);
		
		for (EntityProfile entityProfile : entityProfiles) {
			HashMap<String, String> hashMap = initMap(fieldsNames);
			result.add(hashMap);
			
			HashSet<Attribute> attributes = entityProfile.getAttributes();
			for (Attribute attribute : attributes) {
				String value = hashMap.get( attribute.getName() );
				value = buildValue(attribute, value);
				hashMap.put(attribute.getName(), value);
			}
		}
		return result;
	}

	private String buildValue(Attribute attribute, String value) {
		if (value != null) {
			value = value + DELIMITER_VALUES_IN_FIELD + attribute.getValue();
		} else {
			value = attribute.getValue();
		}
		return value;
	}

	/**
	 * Init a Map. Adds to it keys whose value are the content of fieldsNames parameter. The values of those new
	 * entries is null.
	 * @param fieldsNames
	 * @return
	 */
	private HashMap<String, String> initMap(List<String> fieldsNames) {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		for (String fieldsName : fieldsNames) {
			hashMap.put(fieldsName, null);
		}
		return hashMap;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<EntityProfile> loadEntityProfile(String filePath) {
		
		File file = new File(filePath);
		if (file.exists()) {
			ArrayList<EntityProfile> profileList = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(filePath);
			return profileList;
		}
		return null;
	}
	
	public void doAlgorithm(String filePath) {
		ArrayList<EntityProfile> entityProfiles = loadEntityProfile( filePath );
		List<Map<String,String>> extractValues = extractValues(entityProfiles);
		print(extractValues);
	}

	private void print(List<Map<String, String>> extractValues) {
		File csvFile = createCsvFile("records");
		Writer writer = createFileHeaders(csvFile);
		fillFileContent(writer, extractValues);
		/*for (Map<String, String> map : extractValues) {
//			System.out.println("Priniting new entity");
			for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value = map.get(key) == null ? "" : map.get(key); 
				System.out.println("   " + key + ": " + value );
			}
		}*/
	}
	
	private void fillFileContent(Writer writer,
			List<Map<String, String>> extractValues) {
		if (writer == null) {
			return;
		}
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		for (Map<String, String> map : extractValues) {
			Iterator<String> iterator = map.keySet().iterator();
			try {
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					String value = map.get(key) == null ? "" : map.get(key);

					bufferedWriter.append(value);
					if (iterator.hasNext()) {
						bufferedWriter.append(CSV_SEPERATOR);
					}
				}
				bufferedWriter.append(NEW_LINE);
			} catch (IOException e) {
				try {
					bufferedWriter.close();
				} catch (IOException e1) {
					System.out.println("Failed to close BufferedWriter");
					e1.printStackTrace();
				}
				System.err.println("Failed to add value to output file");
				e.printStackTrace();
			}
		}
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			System.out.println("Failed to close BufferedWriter");
			e.printStackTrace();
		}
	}

	private Writer createFileHeaders(File csvFile) {
		try {
			FileWriter writer = new FileWriter(csvFile);
			
			int size = fieldsNames.size();
			for (int i = 0; i < size-1; i++) {
				writer.append(fieldsNames.get(i));
				writer.append(CSV_SEPERATOR);
			}
			if (size > 0) {
				writer.append(fieldsNames.get(size-1));
				writer.append(NEW_LINE);
			}
//		    writer.
		    return writer;
		} catch (IOException e) {
			System.err.println("Failed to create FileWriter");
			e.printStackTrace();
			return null;
		}
		
	}

	private File createCsvFile(String name) {
		File file = new File(name + "_dataset.csv");
		if (file.exists()) {
			file.delete();
		}
		return file;
	}

	public List<String> getFieldsNames() {
		return fieldsNames;
	}

	public void setFieldsNames(List<String> fieldsNames) {
		this.fieldsNames = fieldsNames;
	}

	public static void main (String[] args) {
		Convertor convertor = new Convertor();
		if (args.length == 0) {
			System.err.println("ERROR!!!! parameters were supplied");
			System.exit(1);
		}
		
		convertor.doAlgorithm( args[0] );
		
	}
}
