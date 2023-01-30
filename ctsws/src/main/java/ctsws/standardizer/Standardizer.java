package ctsws.standardizer;

import java.util.*;
import java.io.IOException;
import java.util.Collections;

import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.json.*;


//import chemaxon.formats.MolFormatException;
import chemaxon.formats.MolImporter;
import chemaxon.standardizer.*;
import chemaxon.struc.*;
import chemaxon.util.*;
//import chemaxon.formats.*;

import org.json.*;


// Plain old Java Object it does not extend as class or implements 
// an interface

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 

// The browser requests per default the HTML MIME type.

@WebServlet(name = "standardizerServlet", value = "/rest/standardizer")
public class Standardizer extends HttpServlet {

	@Context ServletContext context;
  
	public Standardizer(@Context ServletContext contextIn)
	{
		context = contextIn;
	}
  
	//This method is called if HTML is request
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String sStdParams;
		JSONObject joReturn = null;
		JSONObject stdzrParams;
		String retVal = null;		
		List<Changes> changes = null;
		int ii = 100;

		context = request.getServletContext();
		PrintWriter out = response.getWriter();

		StringBuffer jb = new StringBuffer();

		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { /*report an error*/ }

		try {
			stdzrParams =  new JSONObject(jb.toString());
		} catch (JSONException e) {
			// crash and burn
			throw new IOException("Error parsing JSON request string");
		}
		  
		try
		{
			String substrate = stdzrParams.getString("structure");		  
			JSONArray actions = null;
			if (stdzrParams.has("actions"))
				actions = stdzrParams.getJSONArray("actions");
			
			if (actions != null)
			{				
				StringBuilder sb = new StringBuilder();
				int nActions = actions.length();
				for (int i=0;i<nActions;i++)
				{
					String action = actions.getString(i);
					String actionStr = getActionString(action);
					
					if (actionStr != "")
					{
						if (i>0)
							sb.append("..");
					
						sb.append(actionStr);
					}
				}
				
				chemaxon.standardizer.Standardizer std = new chemaxon.standardizer.Standardizer(sb.toString());
				Molecule molecule = MolImporter.importMol(substrate);
				changes = std.standardize(molecule);
			}
			
			JSONArray array = new JSONArray();
			if (changes != null)
			{
				for (int i=0;i<changes.size();i++)					
				{
					
					Molecule mol = changes.get(i).getResult();
					MolHandler mh = new MolHandler(mol);
					String smiles = mh.toFormat("smiles");
					array.put(i,smiles);
				}
			}
			joReturn = new JSONObject();		  
			joReturn.put("results", array);
			joReturn.put("status", "success");
			  
			retVal = joReturn.toString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
		}
		catch(Exception ex)
		{
			String msg = ex.getMessage();
			retVal = "{\"status\" : \"error\", \"message\" : \"" + msg + "\"}";
		}

		out.print(retVal);
		return;
	}
	
	private String getActionString(String action) throws Exception
	{
		//String retAction = "";
		if (action.equalsIgnoreCase("aromatize"))
			return "aromatize:b";
		if (action.equalsIgnoreCase("dearamotize"))
			return "dearomatize";
		if (action.equalsIgnoreCase("clearstereo"))
			return "clearstereo";
		if (action.equalsIgnoreCase("addexplicitH"))
			return "addexplicitH";
		if (action.equalsIgnoreCase("removeExplicitH"))
			return "removeexplicitH";
		if (action.equalsIgnoreCase("neutralize"))
			return "neutralize";
		if (action.equalsIgnoreCase("tautomerize"))
			return "tautomerize";
		if (action.equalsIgnoreCase("transform"))
			return "[O:2]=[N:1]=O>>[O-:2][N+:1]=O";
		if (action.equalsIgnoreCase("untransform"))
			return "[N+](=O)[O-]>>N(=O)=O";
		
		throw new Exception("Invalid standardizer action - " + action);
	}
  
}

