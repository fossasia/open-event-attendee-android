import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.GregorianCalendar;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Main {

	private final static String URL = "";
	public static final String PART_URL = "https://docs.google.com/spreadsheets/d/1tt6LXrnc_onSZEbAzzA0bZiq_mtPFNHE8a8P5a6T-tQ/gviz/tq?gid=";

	public static final String VERSION_TRACK_URL = PART_URL + "614071948";
	private static Calendar calendar;

	public static void main(String[] args) throws Exception {
		calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		java.util.Calendar javaCalendar = java.util.Calendar.getInstance();
		javaCalendar.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER);
		javaCalendar.set(java.util.Calendar.DAY_OF_MONTH, 25);

		// initialise as an all-day event..
		VEvent christmas = new VEvent(new Date(javaCalendar.getTime()), "Christmas Day");

		// Generate a UID for the event..
		UidGenerator ug = new UidGenerator("1");
		christmas.getProperties().add(ug.generateUid());

		calendar.getComponents().add(christmas);


		JSONArray jsonArray = readJsonFromUrl(VERSION_TRACK_URL);
		String name;
		String url;
		String venue;
		String mapLocation;
		String version;
		String forceTrack;

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				name = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(0)
						.getString("v");
				url = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(1)
						.getString("f");
				venue = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(2)
						.getString("v");
				version = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(8)
						.getString("v");
				mapLocation = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(5)
						.getString("v");

				fetchData(PART_URL+url, venue, name, (i+23)*100);





			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		FileOutputStream fout = new FileOutputStream("mycalendar.ics");

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(calendar, fout);

	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			JSONArray jsonText = removePaddingFromString(readAll(rd));
			return jsonText;
		} finally {
			is.close();
		}
	}

	private static JSONArray removePaddingFromString(String response) {
		response = response.replaceAll("\"v\":null", "\"v\":\"\"");
		response = response.replaceAll("null", "{\"v\": \"\"}");
		response = response.substring(response.indexOf("(") + 1, response.length() - 2);
		try {
			JSONObject jObj = new JSONObject(response);
			jObj = jObj.getJSONObject("table");
			JSONArray jArray = jObj.getJSONArray("rows");
			//            Log.d(TAG, jArray.toString());
			return jArray;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;

	}

	private static void fetchData(String sheetUrl, final String venue, final String forceTrack, final int id) throws IOException, JSONException {



		JSONArray jsonArray = readJsonFromUrl(sheetUrl);

		String firstName;
		String lastName;
		String time;
		String date;
		String organization;
		String email;
		String blog;
		String twitter;
		String typeOfProposal;
		String topicName;
		String field;
		String day;
		String proposalAbstract;
		String description;
		String url;
		String fullName;
		String linkedIn;


		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				firstName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.FIRST_NAME)
						.getString("v");
				lastName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.LAST_NAME)
						.getString("v");
				time = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TIME)
						.getString("f");
				date = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.DATE)
						.getString("v");
				organization = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.ORGANIZATION)
						.getString("v");
				email = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.EMAIL)
						.getString("v");
				blog = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.BLOG)
						.getString("v");
				twitter = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TWITTER)
						.getString("v");
				typeOfProposal = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TYPE_OF_PROPOSAL)
						.getString("v");
				topicName = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TOPIC_NAME)
						.getString("v");
				field = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.TRACK)
						.getString("v");
				proposalAbstract = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.ABSTRACT)
						.getString("v");
				description = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.DESCRIPTION)
						.getString("v");
				url = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.URL)
						.getString("v");
				linkedIn = jsonArray.getJSONObject(i).getJSONArray("c").getJSONObject(Constants.LINKEDIN)
						.getString("v");
				String logData = "First Name: %s\nLast Name: %s\nDate: %s\nTime: %s\nOrganization: %s\nEmail: %s\nBlog: %s\nTwitter: %s\nType Of Proposal: %s\nTopic Name:%s\nTrack: %s\nAbstarct: %s\nDescription: %s\nURL: %s";
				logData = String.format(logData, firstName, lastName, date, time, organization, email, blog, twitter, typeOfProposal, topicName, field, proposalAbstract, description, url);
				//                        Log.d(TAG, logData);
				System.out.println(logData);
				int id2 = id + i;
				if (date.equals("") || firstName.equals("") || time.equals("") || topicName.equals("")) {
					continue;
				}
				String[] dayDate = date.split(" ");
				day = dayDate[0];
				date = dayDate[1] + " " + dayDate[2];
				fullName = firstName + " " + lastName;
				calendar.getComponents().add(StringToDate(date, time, topicName));

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}


	}

	public static VEvent StringToDate(String sDate, String sTime, String eventName) throws SocketException {
		if (sTime != null) {
			sTime = sTime.replaceAll(" ", "");
			String amPm = sTime.substring(Math.max(sTime.length() - 2, 0));
			String time = sTime.substring(0, sTime.length() - 2);
			String[] hrMin = time.split(":");
			String[] date = sDate.split(" ");
			int hour = Integer.parseInt(hrMin[0]);
			int min = Integer.parseInt(hrMin[1]);

			if (amPm.equals("PM") || amPm.equals("pm") || amPm.equals("Pm") || amPm.equals("pM")) {
				if (hour > 0 && hour < 12) {
					hour += 12;
				}
			} else if (amPm.equals("AM") || amPm.equals("am") || amPm.equals("Am") || amPm.equals("aM"))
				if (hour == 12) {
					hour = 0;
				}

			TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
			TimeZone timezone = registry.getTimeZone("Asia/Singapore");
			VTimeZone tz = timezone.getVTimeZone();
			java.util.Calendar startDate = new GregorianCalendar();
			startDate.setTimeZone(timezone);
			startDate.set(java.util.Calendar.MONTH, java.util.Calendar.MARCH);
			startDate.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(date[1]));
			startDate.set(java.util.Calendar.YEAR, 2015);
			startDate.set(java.util.Calendar.HOUR_OF_DAY, hour);
			startDate.set(java.util.Calendar.MINUTE, min);
			startDate.set(java.util.Calendar.SECOND, 0);
			java.util.Calendar endDate = new GregorianCalendar();
			endDate.setTimeZone(timezone);
			endDate.set(java.util.Calendar.MONTH, java.util.Calendar.MARCH);
			endDate.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(date[1]));
			endDate.set(java.util.Calendar.YEAR, 2015);
			endDate.set(java.util.Calendar.HOUR_OF_DAY, hour);
			endDate.set(java.util.Calendar.MINUTE, min);
			endDate.set(java.util.Calendar.SECOND, 0);
			DateTime start = new DateTime(startDate.getTime());
			DateTime end = new DateTime(endDate.getTime());
			VEvent meeting = new VEvent(start, end, eventName);

			// add timezone info..
			meeting.getProperties().add(tz.getTimeZoneId());

			// generate unique identifier..
			UidGenerator ug = new UidGenerator("uidGen");
			Uid uid = ug.generateUid();
			meeting.getProperties().add(uid);

			return meeting;
		}

		java.util.Calendar startDate = new GregorianCalendar();
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		TimeZone timezone = registry.getTimeZone("America/Mexico_City");
		VTimeZone tz = timezone.getVTimeZone();
		startDate.setTimeZone(timezone);
		startDate.set(java.util.Calendar.MONTH, java.util.Calendar.MARCH);
		String[] date = sDate.split(" ");
		startDate.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(date[1]));
		startDate.set(java.util.Calendar.YEAR, 2015);
		java.util.Calendar endDate = new GregorianCalendar();
		endDate.setTimeZone(timezone);
		endDate.set(java.util.Calendar.MONTH, java.util.Calendar.MARCH);
		endDate.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(date[1]));
		endDate.set(java.util.Calendar.YEAR, 2015);
		DateTime start = new DateTime(startDate.getTime());
		DateTime end = new DateTime(endDate.getTime());
		VEvent meeting = new VEvent(start, end, eventName);

		// add timezone info..
		meeting.getProperties().add(tz.getTimeZoneId());

		// generate unique identifier..
		UidGenerator ug = new UidGenerator("uidGen");
		Uid uid = ug.generateUid();
		meeting.getProperties().add(uid);

		return meeting;
	}
}






