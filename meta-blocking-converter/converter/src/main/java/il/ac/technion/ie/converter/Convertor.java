package il.ac.technion.ie.converter;

import il.ac.technion.ie.metablocking.Attribute;
import il.ac.technion.ie.metablocking.EntityProfile;
import il.ac.technion.ie.utils.SerializationUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class Convertor {

	private static final String DELIMITER_VALUES_IN_FIELD = " ";

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
		for (Map<String, String> map : extractValues) {
			System.out.println("Priniting new entity");
			for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String value = map.get(key) == null ? "" : map.get(key); 
				System.out.println("   " + key + ": " + value );
			}
		}
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
