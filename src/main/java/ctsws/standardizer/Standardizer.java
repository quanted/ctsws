package ctsws.standardizer;

import java.util.*;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

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

//Sets the path to base URL + /hello
@Path("/standardizer")
public class Standardizer {

	@Context ServletContext context;
  
	
  
	//This method is called if HTML is request
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getStandardized(String sStdParams)
	{
		JSONObject joReturn = null;
		String retVal = null;		
		List<Changes> changes = null;
		int ii = 100;
		//String realPath = context.getRealPath("/");
		  
		try
		{
			JSONObject stdzrParams = new JSONObject(sStdParams);
			 		  
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
			
		}
		catch(Exception ex)
		{
			String msg = ex.getMessage();
			retVal = "{\"status\" : \"error\", \"message\" : \"" + msg + "\"}";
		}
		
		return retVal;
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

