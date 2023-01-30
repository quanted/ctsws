package ctsws.chemicalterms;

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

import chemaxon.formats.MolImporter;
import chemaxon.jep.*;
import chemaxon.jep.context.MolContext;
import chemaxon.struc.Molecule;
import ctsws.util.StatusMessaging;

@WebServlet(name = "chemicaltermsServlet", value = "/chemicalterms")
public class ChemicalTerms extends HttpServlet {

	private ServletContext context;
	public ChemicalTerms()
	{
	}

	public ChemicalTerms(@Context ServletContext contextIn)
	{
		context = contextIn;
	}
	
	//This method is called if HTML is request
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		JSONObject joResults = new JSONObject();
		JSONObject joParams;

		String results = null;
		String chemTermParams;
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
			joParams =  new JSONObject(jb.toString());
		} catch (JSONException e) {
			// crash and burn
			throw new IOException("Error parsing JSON request string");
		}
		try
		{
			
			if (joParams.has("substrate") == false)
				throw new Exception("No chemical  specified");
				//return StatusMessaging.errorMessage("No chemical substrate specified").toString();
			
			String substrate = joParams.getString("substrate");
			
			if (joParams.has("chemicalterms") == false)
				throw new Exception("No chemical  specified");
				//return StatusMessaging.errorMessage("No chemical terms specified").toString();
			
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

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
		}
		catch(Exception ex)
		{
			results = StatusMessaging.errorMessage(ex.getMessage()).toString();
		}

		out.print(results);
		return;
	}
}
