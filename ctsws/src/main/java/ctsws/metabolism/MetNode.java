package ctsws.metabolism;

import chemaxon.metabolism.Metabolite;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class MetNode
{
    public Metabolite metabolite;

    public String smiles;
    public ArrayList<MetNode> children;

    public  MetNode(String smilesIn, Metabolite metaboliteIn)
    {
        smiles = smilesIn;
        metabolite = metaboliteIn;
        children = new ArrayList<MetNode>();
    }


    public JSONObject ToJson()
    {
        JSONObject jo = null;
        JSONObject joMetProps = null;
        JSONObject joGenOne = null;
        JSONObject joReturn = null;
        try
        {
            jo = new JSONObject();

            for (int i = 0; i < children.size(); i++) {
                MetNode node = children.get(i);
                jo.put(node.metabolite.getKey(), node.ToJson());
            }

            joMetProps = getMetaboliteProperties(metabolite);
            joMetProps.put("metabolites", jo);

            if (metabolite.getGeneration() == 0)
            {
                joGenOne = new JSONObject();
                joGenOne.put(metabolite.getKey(), joMetProps);
                joReturn = joGenOne;
            }
            else
                joReturn = joMetProps;

        }
        catch (Exception exep)
        {

        }
        return joReturn;
        //return joMetProps;
    }




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

}