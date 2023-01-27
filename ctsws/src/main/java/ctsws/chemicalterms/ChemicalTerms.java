package ctsws.chemicalterms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import chemaxon.formats.MolImporter;
import chemaxon.jep.*;
import chemaxon.jep.context.MolContext;
import chemaxon.struc.Molecule;
import ctsws.util.StatusMessaging;

@Path("/chemicalterms")
public class ChemicalTerms 
{
	public ChemicalTerms()
	{

	}
	
	//This method is called if HTML is request
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String evaluateTerm(String chemTermParams)
	{
		JSONObject joResults = new JSONObject();
		//JSONObject joProps = null;
	    String results = null;
		try
		{
			JSONObject joParams = new JSONObject(chemTermParams);
			
			if (joParams.has("substrate") == false)		 				
				return StatusMessaging.errorMessage("No chemical substrate specified").toString();	
			
			String substrate = joParams.getString("substrate");
			
			if (joParams.has("chemicalterms") == false)		 				
				return StatusMessaging.errorMessage("No chemical terms specified").toString();	
			
			String chemTerms = joParams.getString("chemicalterms");
			//chemTerms = "logP()";
			
			Evaluator evaluator = new Evaluator();
			
			// create ChemJEP, compile the Chemical Terms expression
			//ChemJEP chemJEP = evaluator.compile("logP()", MolContext.class);
			ChemJEP chemJEP = evaluator.compile(chemTerms, MolContext.class);

			// create context
			MolContext context = new MolContext();
			Molecule mol = MolImporter.importMol(substrate);
			context.setMolecule(mol);

		    // get the result by evaluating the expression
		    // note: "logP()" expression returns a double, soAKIAI7TRNQM2US6MNA2AAKIAI7TRNQM2US6MNA2A
		    // evaluate_double(ChemContext) method is used			
			Object result = chemJEP.evaluate(context);
		    //double result = chemJEP.evaluate_double(context);
		    
		    joResults.put("status", "success");
			joResults.put("results", result.toString());
			results = joResults.toString();
		
		}
		catch(Exception ex)
		{
			results = StatusMessaging.errorMessage(ex.getMessage()).toString();
		}
		
		return results;
	}
}
