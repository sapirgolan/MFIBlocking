package il.ac.technion.ie.converter;

import il.ac.technion.ie.metablocking.Attribute;
import il.ac.technion.ie.metablocking.EntityProfile;
import il.ac.technion.ie.utils.SerializationUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class Convertor {

	private static final String DELIMITER_VALUES_IN_FIELD = " ";

	public List<String> extractFieldsNames(final ArrayList<EntityProfile> entityProfiles) {
		ArrayList<String> result = new ArrayList<String>();
		for (EntityProfile entityProfile : entityProfiles) {
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
	public List<Map<String, String>> extractValues( final ArrayList<EntityProfile> entityProfiles ) {
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>(entityProfiles.size());
		List<String> fieldsNames = extractFieldsNames(entityProfiles);
		
		for (EntityProfile entityProfile : entityProfiles) {
			HashMap<String, String> hashMap = new HashMap<String, String>();
			for (String fieldsName : fieldsNames) {
				hashMap.put(fieldsName, null);
			}
			result.add(hashMap);
			
			HashSet<Attribute> attributes = entityProfile.getAttributes();
			for (Attribute attribute : attributes) {
				String value = hashMap.get( attribute.getName() );
				if (value != null) {
					value = value + DELIMITER_VALUES_IN_FIELD + attribute.getValue();
				} else {
					value = attribute.getValue();
				}
				hashMap.put(attribute.getName(), value);
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<EntityProfile> loadEntityProfile(String filePath) {
		
		ArrayList<EntityProfile> profileList = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(filePath);
		return profileList;
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
