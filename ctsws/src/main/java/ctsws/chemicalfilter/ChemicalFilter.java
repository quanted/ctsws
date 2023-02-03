package ctsws.chemicalfilter;

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
import chemaxon.formats.MolImporter;
import chemaxon.calculations.ElementalAnalyser;
import chemaxon.struc.*;

import org.json.*;

@WebServlet(name = "isvalidchemicalServlet", value = "/rest/isvalidchemical")
public class ChemicalFilter extends HttpServlet {

    private ServletContext context;
    public ChemicalFilter(@Context ServletContext contextIn)
    {
        context = contextIn;
    }

    public ChemicalFilter() {};

    //THis method takes a smiles string and determines whether any of the elements are part of the excluded list
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JSONObject joReturn = null;
        String retVal = null;
        String chemical;
        JSONObject joChemStruct = null;

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
            joChemStruct =  new JSONObject(jb.toString());
        } catch (JSONException e) {
            // crash and burn
            throw new IOException("Error parsing JSON request string");
        }

        String smiles = joChemStruct.getString("structure");

        int[] excludeList = {2,3,4,5,10,11,12,13,14,18,19,20,21,22,23,24,25,26,
                            27,28,29,30,31,32,33,34,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,
                            51,52,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,
                            75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,
                            99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118};

        MolImporter mi = null;
        JSONObject joResult = new JSONObject();
        joResult.put("result","true");

        String msg = "";
        try
        {
            ElementalAnalyser elementalAnalyser = new ElementalAnalyser();

            // run plugin on target molecules
            Molecule molecule = MolImporter.importMol(smiles);
            for (int element: excludeList) {
                // set target molecule
                elementalAnalyser.setMolecule(molecule);
                int atomCount = elementalAnalyser.atomCount(element);
                if (atomCount != 0) {
                    joResult.put("result","false");
                    break;
                }
            }
                //break;
            //}
            if (mi != null)
                mi.close();
        }

        catch(Exception ex){
            msg = ex.getMessage();
        }

        String msg2 = msg;
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(joResult.toString());

        return;
    }
}
