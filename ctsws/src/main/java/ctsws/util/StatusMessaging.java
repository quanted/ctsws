package ctsws.util;

import org.json.*;

public class StatusMessaging 
{

	public static JSONObject errorMessage(String message)
	{
		JSONObject jo = new JSONObject();
		try 
		{
			jo.put("status", "error");
			jo.put("message", message);
		} catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}		
		return jo;
	}
	
	public static JSONObject message(String status, String message)
	{
		JSONObject jo = new JSONObject();
		try 
		{
			jo.put("status", status);
			jo.put("message", message);
		} catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}		
		return jo;
	}
}
