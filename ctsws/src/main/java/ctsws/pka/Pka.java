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
// import chemaxon.standardizer.*;
import chemaxon.struc.*;
import chemaxon.util.*;
//import chemaxon.formats.*;
import chemaxon.marvin.calculations.*;

import org.json.*;


// Plain old Java Object it does not extend as class or implements 
// an interface

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 

// The browser requests per default the HTML MIME type.

@WebServlet(name = "pkaServlet", value = "/rest/pka")
public class Pka extends HttpServlet {

	private ServletContext context;

	public Pka() {}
	public Pka(@Context ServletContext contextIn)
	{
		context = contextIn;
	}



	private String message;

    public void init() {
        message = "Hello World!";
    }

    @Produces(MediaType.APPLICATION_JSON)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        ServletContext ctx = request.getServletContext();
        String path = ctx.getRealPath("/");

        // Hello
        PrintWriter out = response.getWriter();
        String retVal = "{\"status\" : \"success\", \"message\" : \"" + path + "\"}";
        out.print(retVal);

        return;
    }


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
		JSONObject pkaParams;

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
				pkaParams =  new JSONObject(jb.toString());
			} catch (JSONException e) {
				// crash and burn
				throw new IOException("Error parsing JSON request string");
			}


			/*
			Example request:
			{
			  "inputFormat": "smiles",
			  "micro": false,
			  "outputFormat": "mrv",
			  "outputStructureIncluded": false,
			  "pKaLowerLimit": -20,
			  "pKaUpperLimit": 10,
			  "prefix": "DYNAMIC",
			  "structure": "NC(CC1=CC=CC=C1)C(O)=O",
			  "temperature": 298,
			  "types": "pKa, acidic, basic"
			}

			Example response from rest-v1 
			{
			    "pkaValuesByAtom": [
			        {
			            "atomIndex": 0,
			            "value": 9.45
			        },
			        {
			            "atomIndex": 10,
			            "value": 2.47
			        }
			    ],
			    "basicValuesByAtom": [
			        {
			            "atomIndex": 0,
			            "value": 9.45
			        }
			    ],
			    "acidicValuesByAtom": [
			        {
			            "atomIndex": 10,
			            "value": 2.47
			        }
			    ],
			    "maxBasicValue": 9.45,
			    "minAcidicValue": 2.47
			}
			*/

			String structure = pkaParams.getString("structure");
			String inputFormat = pkaParams.getString("inputFormat");
			boolean micro = pkaParams.getBoolean("micro");
			String outputFormat = pkaParams.getString("outputFormat");
			boolean outputStructureIncluded = pkaParams.getBoolean("outputStructureIncluded");
			int pKaLowerLimit = pkaParams.getInt("pKaLowerLimit");
			int pKaUpperLimit = pkaParams.getInt("pKaUpperLimit");
			String prefix = pkaParams.getString("prefix");
			int temperature = pkaParams.getInt("temperature");
			String types = pkaParams.getString("types");


			pKaPlugin plugin = new pKaPlugin();
		    plugin.setMaxIons(6);
		    plugin.setBasicpKaLowerLimit(-5.0);
		    plugin.setAcidicpKaUpperLimit(25.0);
		    plugin.setpHLower(3.0);
		    plugin.setpHUpper(6.0);
		    plugin.setpHStep(1.0);


		    Molecule mol = MolImporter.importMol(structure);
		    plugin.setMolecule(mol);  // set molecule with SMILES?

			plugin.run();  // TODO: is this needed?

			// get the 3 strongest ACIDIC pKa values
			double[] acidicpKa = new double[3];
			int[] acidicIndexes = new int[3];
			plugin.getMacropKaValues(pKaPlugin.ACIDIC, acidicpKa, acidicIndexes);

			// get the 3 strongest BASIC pKa values
			double[] basicpKa = new double[3];
			int[] basicIndexes = new int[3];
			plugin.getMacropKaValues(pKaPlugin.BASIC, basicpKa, basicIndexes);

			int count = mol.getAtomCount();

			ArrayList<Double> pkas = new ArrayList<>();
			ArrayList<Double> pkbs = new ArrayList<>();

			JSONObject jsonPkaDict = new JSONObject();

			// get pKa values for each atom
			for (int atomIndex=0; atomIndex < count; ++atomIndex) {
				// get ACIDIC and BASIC pKa values
				double apka = plugin.getpKa(atomIndex, pKaPlugin.ACIDIC);
				double bpka = plugin.getpKa(atomIndex, pKaPlugin.BASIC);

				System.out.println("atomIndex: " + atomIndex);
				System.out.println("pka: " + apka);
				System.out.println("pkb: " + bpka);

				if (!Double.isNaN(apka)) {
					pkas.add(apka);
					jsonPkaDict.put(Double.toString(apka), atomIndex);
				}

				if (!Double.isNaN(bpka)) {
					pkbs.add(bpka);
					jsonPkaDict.put(Double.toString(bpka), atomIndex);
				}

			}

			JSONObject jsonPkaResponse = new JSONObject();
			jsonPkaResponse.put("pka", pkas);
			jsonPkaResponse.put("pkb", pkbs);
			jsonPkaResponse.put("pka_dict", jsonPkaDict);

		    joReturn = new JSONObject();
		    joReturn.put("results", jsonPkaResponse);
		    joReturn.put("status", "success");

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			out.print(joReturn.toString());

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
  
}

