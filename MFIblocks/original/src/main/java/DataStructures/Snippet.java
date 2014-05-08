package DataStructures;

import java.io.File;
import java.util.ArrayList;

public class Snippet {
	public ArrayList<EntityProfile> loadEntityProfile(String filePath) {
			
			File file = new File(filePath);
			if (file.exists()) {
				ArrayList<EntityProfile> profileList = (ArrayList<EntityProfile>) SerializationUtilities.loadSerializedObject(filePath);
				return profileList;
			}
			return null;
		}
}

