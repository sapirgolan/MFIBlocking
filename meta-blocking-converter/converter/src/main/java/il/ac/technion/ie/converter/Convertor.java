package il.ac.technion.ie.converter;

import il.ac.technion.ie.utils.SerializationUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import DataStructures.Attribute;
import DataStructures.EntityProfile;



public class Convertor {

	private static final String DELIMITER_VALUES_IN_FIELD = " ";
	private static final CharSequence CSV_SEPERATOR = ",";
	private static final CharSequence NEW_LINE = "\n";
	private SortedSet<String> fieldsNames;

	public SortedSet<String> extractFieldsNames(final List<EntityProfile> list) {
		SortedSet<String> result = new TreeSet<String>();
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
		SortedSet<String> fieldsNames = extractFieldsNames(entityProfiles);
		
		for (EntityProfile entityProfile : entityProfiles) {
			SortedMap<String,String> hashMap = initMap(fieldsNames);
			result.add(hashMap);
			
			HashSet<Attribute> attributes = entityProfile.getAttributes();
			for (Attribute attribute : attributes) {
				String value = hashMap.get( attribute.getName() );
				value = buildValue(attribute, value);
				hashMap.put(attribute.getName(), value);
			}
			/*String imdbId = hashMap.get("imdbId");
			if (imdbId!= null  && imdbId.equals(Integer.getInteger(imdbId).toString())  ) {
				hashMap.put("title", imdbId);
			}*/
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
	private SortedMap<String, String> initMap(SortedSet<String> fieldsNames) {
		SortedMap<String, String> soredMap = new TreeMap<String, String>();
		for (String fieldsName : fieldsNames) {
			soredMap.put(fieldsName, null);
		}
		return soredMap;
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
					
					int indexOf = value.indexOf(CSV_SEPERATOR.toString());
					while (indexOf > -1) {
						value = value.substring(0, indexOf) + value.substring(indexOf + 1);
						indexOf = value.indexOf(CSV_SEPERATOR.toString());
					}

					bufferedWriter.write(value);
					if (iterator.hasNext()) {
						bufferedWriter.append(CSV_SEPERATOR);
					}
				}
				bufferedWriter.newLine();
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
			
			for (Iterator<String> iterator = fieldsNames.iterator(); iterator.hasNext();) {
				String title = (String) iterator.next();
				writer.append(title);
				writer.append(CSV_SEPERATOR);
			}
			writer.append(NEW_LINE);
		    return writer;
		} catch (IOException e) {
			System.err.println("Failed to create FileWriter");
			e.printStackTrace();
			return null;
		}
		
	}

	private File createCsvFile(String name) {
		File file = new File(name + "_dataset" + ".csv");
		int index = 0;
		while (file.exists() &&  !file.delete()) {
			file = new File (name + "_dataset" + "_" +  index + ".csv");
			index++;
		}
		return file;
	}

	public SortedSet<String> getFieldsNames() {
		return fieldsNames;
	}

	public void setFieldsNames(SortedSet<String> fieldsNames) {
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
