package com.goeuro.app;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Service class responsible for fetching the data from the remote server and writing it into csv file location
 * 
 * @author girishbhat.m7@gmail.com
 *
 */
public class AppService
{
	private static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_POSITION = "geo_position";
	private static final String KEY_TYPE = "type";
	private static final String KEY_NAME = "name";
	private static final String KEY_ID = "_id";
	private static final String KEY = "url";
	private static final String APP_PROPERTIES = "app.properties";
	private static final String FILE_HEDER = "_id, name, type, latitude, longitude";
	private static final String DELIMETER = ",";
	private static final String NEW_LINE = "\n";

	@SuppressWarnings("unchecked")
	public void generateCSV(String location)
	{
		if (location == null)
		{
			throw new IllegalArgumentException();
		}
		String jsonData = getLocationDetails(location);
		if (jsonData == null)
		{
			System.err.println("Error occured getiing resource...!!!");
			return;
		}
		JSONParser parser = new JSONParser();
		JSONArray array = null;
		try
		{
			array = (JSONArray) parser.parse(jsonData);
		} catch (Exception e)
		{
			System.err.println("Error occured parsing json data..!!!");
			return;
		}
		if (array.size() == 0)
		{
			System.out.println("Empty resource found..!!!");
			return;
		}
		Iterator<JSONObject> iterator = array.iterator();
		List<Location> locations = new ArrayList<>(array.size());
		Location locationObj = null;
		while (iterator.hasNext())
		{
			locationObj = new Location();
			JSONObject obj = iterator.next();
			locationObj.setId(obj.get(KEY_ID).toString());
			locationObj.setName(obj.get(KEY_NAME).toString());
			locationObj.setType(obj.get(KEY_TYPE).toString());
			JSONObject geoPosition = (JSONObject) obj.get(KEY_POSITION);
			locationObj.setLatitude(geoPosition.get(KEY_LATITUDE).toString());
			locationObj.setLongitude(geoPosition.get(KEY_LONGITUDE).toString());
			locations.add(locationObj);
		}
		writeToCSV(locations);
	}

	private String getLocationDetails(String location)
	{
		String url = getURL();
		if (url == null)
		{
			System.err.println("No resource url found..!!!");
			return null;
		}
		try (CloseableHttpClient httpClient = HttpClients.createDefault())
		{
			HttpGet httpGet = new HttpGet(url + location);
			httpGet.addHeader("accept", "application/json");
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			return IOUtils.toString(httpResponse.getEntity().getContent());
		} catch (IOException e)
		{
			System.err.println("Error occured while connecting to resource url " + url);
			return null;
		} catch (Exception ex)
		{
			System.err.println("Error occured while connecting to resource content ");
			return null;
		}
	}

	private String getURL()
	{
		Properties properties = new Properties();
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(APP_PROPERTIES))
		{
			properties.load(inputStream);
			return properties.getProperty(KEY);
		} catch (IOException e)
		{
			System.err.println("Resource file not found..!!!!");
			return null;
		}
	}

	private void writeToCSV(List<Location> locations)
	{
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter destination file path eg: c:\\temp\\data.csv");
		String filePath = sc.next();
		try (FileWriter fw = new FileWriter(filePath))
		{
			fw.append(FILE_HEDER);
			fw.append(NEW_LINE);
			for (Location location : locations)
			{
				fw.append(location.getId());
				fw.append(DELIMETER);
				fw.append(location.getName());
				fw.append(DELIMETER);
				fw.append(location.getType());
				fw.append(DELIMETER);
				fw.append(location.getLatitude());
				fw.append(DELIMETER);
				fw.append(location.getLongitude());
				fw.append(NEW_LINE);
			}
			fw.flush();
			System.out.println("File created @ : "+filePath);
		} catch (IOException e)
		{
			System.err.println("Error occured while wrting to csv file");
		}
		sc.close();
	}
}
