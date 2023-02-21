package ctsws.metabolism;

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
//import javax.ws.rs.*;
//import javax.ws.rs.core.*;

//import com.chemaxon.calculations.io.formats.*;
import chemaxon.formats.MolImporter;
//import chemaxon.formats.MolImporter;
import chemaxon.metabolism.*;
import chemaxon.struc.*;
//import chemaxon.util.*;
//import chemaxon.formats.*;

import org.json.*;


// Plain old Java Object it does not extend as class or implements
// an interface

// The class registers its methods for the HTTP GET request using the @GET annotation.
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML.

// The browser requests per default the HTML MIME type.

//@Path("/")
@WebServlet(name = "metabolizerServlet", value = "/rest/metabolizer")
public class Metabolizer extends HttpServlet {

	public Metabolizer() {}
	private ServletContext context;
	public Metabolizer(@Context ServletContext contextIn)
	{
		context = contextIn;
	}
	//@Context ServletContext context;

	//This method is called if HTML is request
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		JSONObject joReturn = null;
		String retVal = null;

		context = request.getServletContext();

		String realPath = context.getRealPath("/");
		String sMetabParams;
		JSONObject metabolizerParams;

		PrintWriter out = response.getWriter();

		try
		{
			StringBuffer jb = new StringBuffer();
			String line = null;
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);
			} catch (Exception e) { /*report an error*/ }

			try {
				//metabolizerParams =  HTTP.toJSONObject(jb.toString());
				metabolizerParams =  new JSONObject(jb.toString());
			} catch (JSONException e) {
				// crash and burn
				throw new IOException("Error parsing JSON request string");
			}

			String structure = metabolizerParams.getString("structure");

			//We need the ability to filter out redundant metabolites
			//But now we need to be able to toggle this functionality
			boolean unique_metabolites = false;
			if (metabolizerParams.has("unique_metabolites"))
				unique_metabolites = metabolizerParams.getBoolean("unique_metabolites");


			JSONArray transformationLibs = null;
			if (metabolizerParams.has("transformationLibraries"))
				transformationLibs = metabolizerParams.getJSONArray("transformationLibraries");

			double likelyLimit = metabolizerParams.getDouble("likelyLimit");

			int genLimit = metabolizerParams.getInt("generationLimit");
		    int popLimit = metabolizerParams.getInt("populationLimit");
		    String excludeCond = metabolizerParams.getString("excludeCondition");

			//Don't load a library if mammalian_metabolism is specified, the default (Human metabolism) library is loaded.
			chemaxon.metabolism.Metabolizer mtblzer = new chemaxon.metabolism.Metabolizer();

		    ArrayList<RxnMolecule> reactionList = new ArrayList<RxnMolecule>();
		    //Get a list of reactions from reaction libraries.
		    if (transformationLibs != null) {

				String transLib = null;
				MolImporter importer = null;

				//No longer loading multiple reaction libraries.
				//They are combined into single libraries.
				String rxnLib = transformationLibs.getString(0);

				if (!rxnLib.equalsIgnoreCase("mammalian_metabolism")) {
					transLib = context.getInitParameter(transformationLibs.getString(0));
					transLib = transLib.replace('\\', File.separatorChar);
					importer = new MolImporter(realPath + transLib);
					RxnMolecule tempMolecule;
					while ((tempMolecule = ((RxnMolecule) importer.read())) != null) {
						reactionList.add(tempMolecule);
					}
					importer.close();
					importer = null;
					mtblzer.setReactions(reactionList);
				}
			}

		    	/*int nLibs = transformationLibs.length();
		    	switch(nLibs)
		    	{
		    		case 0:
		    			break;
		    		case 1:
		    			//transLib = transformationLibs.getString(0);
						transLib = context.getInitParameter(transformationLibs.getString(0));
		    			break;
		    		case 2:
		    			transLib = context.getInitParameter("combined_abioticreduction_hydrolysis");
		    			break;
		    		default:
		    			break;
		    			//Need to throw error
		    	}
		    	*/
		    	//for (int i=0;i<nLibs;i++)
		    	//{
		    	/*
				if (transLib != null)
		    	{
		    		//transLib = context.getInitParameter(transformationLibs.getString(i));
					transLib = transLib.replace('\\', File.separatorChar);
		    		importer = new MolImporter(realPath + transLib);
		    		RxnMolecule tempMolecule;
		    		while ((tempMolecule=((RxnMolecule)importer.read()))!= null)
		    		{
		    			reactionList.add(tempMolecule);
		    		}
		    		importer.close();
		    		importer = null;
		    	}
		    	*/
		    	//}
		    //}



		    mtblzer.setExhaustive(false);
		    mtblzer.setLikelyLimit(likelyLimit);
		    mtblzer.setPopulationLimit(popLimit);
		    mtblzer.setGenerationLimit(genLimit);
		    mtblzer.setExcludeCondition(excludeCond);

		    //This guy does most of the work
		    List<Metabolite> lstMetabolites = metabolize(mtblzer, structure, unique_metabolites);

			JSONObject joMetabs = null;
		    //JSONObject joParent = new JSONObject();
			JSONArray jaParent = new JSONArray();

			HashMap<String, String> hashMap = new HashMap<String, String>();
		    //recurseMetaboliteTree(lstMetabolites.get(0), hashMap);
			MetNode node = RecurseMetabolites(lstMetabolites.get(0), hashMap);
			joMetabs = node.ToJson();

		    //joParent.put("metabolites", joMetabs);
			jaParent.put(joMetabs);
		    JSONObject joMetTree = new JSONObject();
		    //joMetTree.put(structure, joParent);
			joMetTree.put(structure, jaParent);
			//joMetTree.put(structure, joMetabs);

		    joReturn = new JSONObject();
		    joReturn.put("results", joMetTree);
		    joReturn.put("status", "success");

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			out.print(joReturn.toString());

			//retVal = joReturn.toString();
		}
		catch(Exception e)
		{
			String msg = e.getMessage();
			retVal = "{\"status\" : \"error\", \"message\" : \"" + msg + "\"}";
			out.print(retVal);
		}

		out.flush();
		return;
	}

	public void destroy() {
	}
  
  //Runs the metabolizer and returns a list of metabolites
  private List<Metabolite> metabolize(chemaxon.metabolism.Metabolizer metabolizer, String substrate, boolean unique_metabolites) throws Exception
  {
	  Molecule molecule = MolImporter.importMol(substrate);
	  metabolizer.setSubstrate(molecule,substrate);

	  // execute enumeration and get the resulting metabolite list
	  List<Metabolite> lstMetabolites = metabolizer.enumerate();
	  List<Metabolite> lstMetabolitesUnique = new ArrayList<Metabolite>();

//	  for (int i=0;i<lstMetabolitesTmp.size();i++)
//	  {
//	  	Metabolite metabolite = lstMetabolitesTmp.get(i);
//	  	if (metabolite.getState() != Metabolite.State.EXTINCT)
//			lstMetabolitesNoExtinct.add(metabolite);
//	  }

	  //Get a list of unique metabolites sorted by global accumulation (major metabolite indicator)
	  //We need to call calculateGlobals even if we arent doing unique metabolites
	  //Will assign the values to the full list of metabolites
	  lstMetabolitesUnique = metabolizer.calculateGlobals(lstMetabolites);

	  if (!unique_metabolites) {
		  HashMap<String, Double> hashMap = new HashMap<String, Double>();
		  for (int i = 0; i < lstMetabolitesUnique.size(); i++) {
			  Metabolite metab = lstMetabolitesUnique.get(i);
			  hashMap.put(metab.getKey(), metab.getGlobalAccumulation());
		  }

		  for (int i = 0; i < lstMetabolites.size(); i++) {
			  Metabolite metab = lstMetabolites.get(i);
			  Double globAccum = hashMap.get(metab.getKey());
			  metab.setGlobalAccumulation(globAccum);
		  }
	  }
	  else {
		  lstMetabolites = lstMetabolitesUnique;
	  }


		  //lstMetabolites = lstMetabolitesUnique;



	  //lstMetabolites = metabolizer.calculateGlobals(lstMetabolitesNoExtinct);
	  if (lstMetabolites != null && lstMetabolites.size() > 0)
	  {
		  Collections.sort(lstMetabolites, new Comparator<Metabolite>()
		  {
			//@Override
			public int compare(final Metabolite o1, final Metabolite o2)
			{
				return o1.getGeneration() - o2.getGeneration();
			}
		  } );
	  }

	  return lstMetabolites;
  }

	private MetNode RecurseMetabolites(Metabolite metabolite, HashMap<String, String> hashMap)
	{
		MetNode node = null;
		try
		{
			if (metabolite == null)
				return null;

			node = new MetNode(metabolite.getKey(), metabolite);
			int numChildren = metabolite.getChildCount();

			for (int i=0;i<metabolite.getChildCount();i++)
			{
				MetNode childNode = RecurseMetabolites(metabolite.getChild(i), hashMap);
				String key = childNode.metabolite.getKey();

				if (!hashMap.containsKey(key)) {
						node.children.add(childNode);
						hashMap.put(key, key);
				}


				//if (hashMap.containsKey(childNode.get_metabolite().getKey()))
				//    node.add(childNode);
				//return node;
			}
		}
		catch(Exception ex)
		{
			String msg = ex.getMessage();
			String msg2 = msg;
		}
		return node;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("fragments")
	public String getFragments(String sChemical)
	{
		JSONObject joReturn = null;
		String retVal = null;

		//context = request.getServletContext();

		String realPath = context.getRealPath("/");
		//String fragmentLib = "ReactorFragment_v1.0.mrv";
		String fragmentLib = "";

		try
		{
			fragmentLib = context.getInitParameter("fragments");
			JSONObject metabolizerParams = new JSONObject(sChemical);

			String structure = metabolizerParams.getString("structure");
			JSONArray transformationLibs = null;

			ArrayList<RxnMolecule> reactionList = new ArrayList<RxnMolecule>();
			MolImporter importer = null;
			importer = new MolImporter(realPath + fragmentLib);
			RxnMolecule tempMolecule;
			while ((tempMolecule=((RxnMolecule)importer.read()))!= null)
			{
				reactionList.add(tempMolecule);
			}
			importer.close();

			chemaxon.metabolism.Metabolizer mtblzer = new chemaxon.metabolism.Metabolizer();
			mtblzer.setReactions(reactionList);
			mtblzer.setExhaustive(true);
			//mtblzer.setLikelyLimit(likelyLimit);
			//mtblzer.setPopulationLimit(popLimit);
			//mtblzer.setGenerationLimit(genLimit);
			//mtblzer.setExcludeCondition(excludeCond);

			Molecule molecule = MolImporter.importMol(structure);
			mtblzer.setSubstrate(molecule,structure);

			// execute enumeration and get the resulting metabolite list
			List<Metabolite> lstMetabolites = null;
			List<Metabolite> lstMetabolitesTmp = mtblzer.enumerate();
			lstMetabolites = mtblzer.calculateGlobals(lstMetabolitesTmp);
			List<String> lstSmiles = new ArrayList<String>();

			for (int i=0; i<lstMetabolites.size(); i++)
			{
				Metabolite metabolite = lstMetabolites.get(i);
				lstSmiles.add(metabolite.getKey());
			}

			JSONObject joFragments = new JSONObject();
			joFragments.put("fragments", lstSmiles);
			retVal = joFragments.toString();
		}
		catch(Exception ex)
		{
			String msg = ex.getMessage();
			String msg2 = msg;
		}
		return retVal;
	}


/*
  private JSONObject recurseMetaboliteTree(Metabolite metabolite, JSONObject joMetabParent, HashMap<String, Metabolite> hashMap)
  {
	  try
	  {
		  if (metabolite == null)
		      return null;

          JSONObject joMet = getMetaboliteProperties(metabolite);


          for (int i=0;i< metabolite.getChildCount(); i++)
          {
              JSONObject joChild = recurseMetaboliteTree(metabolite.getChild(i), joMet, hashMap);
          }

          if (metabolite.getState() == Metabolite.State.EXTINCT)
		      return null;

		  if (hashMap.containsKey(metabolite.getKey()))
		  	  return null;

		  hashMap.put(metabolite.getKey(), metabolite);
          joMetabParent.put(metabolite.getKey(), joMet);
		  
		  JSONObject joMetChildren = new JSONObject();
		  
		  if (metabolite.getChildCount() > 0)
		  	joMet.put("metabolites", joMetChildren);


	  }
	  catch(Exception ex)
	  {
		  
	  }	  
  }
*/
  /*
    private void recurseMetaboliteTreeOrig(Metabolite metabolite, JSONObject joMetabIn, HashMap<String, Metabolite> hashMap)
    {
        try
        {
            if (metabolite == null)
                return;

            if (metabolite.getState() == Metabolite.State.EXTINCT)
                return;

            if (hashMap.containsKey(metabolite.getKey()))
                return;

            hashMap.put(metabolite.getKey(), metabolite);


            JSONObject joMet = getMetaboliteProperties(metabolite);
            joMetabIn.put(metabolite.getKey(), joMet);

            JSONObject joMetChildren = new JSONObject();

            if (metabolite.getChildCount() > 0)
                joMet.put("metabolites", joMetChildren);

            for (int i=0;i< metabolite.getChildCount(); i++)
            {
                recurseMetaboliteTree(metabolite.getChild(i), joMetChildren, hashMap);
            }
        }
        catch(Exception ex)
        {

        }
    }

*/
/*
  private CTSMetabolite getCTSMetabolite(Metabolite metabolite) throws JSONException
  {
	  if (metabolite == null)
		  return null;
	  
	  CTSMetabolite met = null;
	  try
	  {		  	 
		  met = new CTSMetabolite(metabolite.getKey());
		  met.accumulation =  metabolite.getAccumulation();
	      met.degradation = metabolite.getDegradation();
	      met.formationRate = metabolite.getFormation();
	      met.generation = metabolite.getGeneration();
	      met.globalAccumulation = metabolite.getGlobalAccumulation();      
	      met.globalFormationRate = metabolite.getGlobalFormation();
	      met.globalProduction = metabolite.getGlobalProduction();
	      met.likelihood = metabolite.getLikelihood().toString();
	      met.phase = metabolite.getPhase();
	      met.production = metabolite.getProduction();
	      met.route = metabolite.getRoute();
	      met.routes = metabolite.getRoutes();
	      met.state = metabolite.getState().toString();
	      met.transmissivity = metabolite.getTransmissivity();
	      met.parentKey = metabolite.getParent().getKey();
      
	  }
	  catch(Exception ex)
	  {
		  
	  }
	  return met;
      
  }
*/

/*
  private JSONObject getMetaboliteProperties(Metabolite metabolite) throws JSONException
  {
    JSONObject jo = new JSONObject();

    jo.put("accumulation", metabolite.getAccumulation());
    jo.put("globalAccumulation", metabolite.getGlobalAccumulation());

    jo.put("degradation", metabolite.getDegradation());
    jo.put("generation", metabolite.getGeneration());

    jo.put("formationRate", metabolite.getFormation());
    jo.put("globalFormationRate", metabolite.getGlobalFormation());

    jo.put("likelihood", metabolite.getLikelihood());

    jo.put("production", metabolite.getProduction());
    jo.put("globalProduction", metabolite.getGlobalProduction() );

    jo.put("phase", metabolite.getPhase());
    jo.put("state", metabolite.getState().toString());

    jo.put("transmissivity", metabolite.getTransmissivity());

    jo.put("route", metabolite.getRoute());
    jo.put("routes", metabolite.getRoutes());
    
    try
    {
    	Metabolite metab = metabolite.getParent();
    	if (metab != null)
    		jo.put("parent", metab.getKey());

    
    	jo.put("smiles", metabolite.getKey());
    }
    catch(IOException ioEx)
    {
      //Not much good with out a smiles string
      jo = null;
    }

    return jo;

  }
*/

}
