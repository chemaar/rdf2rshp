package es.inf.uc3m.kr.rdf2rshp.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MappingXSDRSHP {
	
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(MappingXSDRSHP.class.getName().toString());

	private MappingXSDRSHP() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	public static void main(String []args){
		System.out.println(MappingXSDRSHP.getString("http://www.w3.org/2001/XMLSchema#string"));
	}
}
