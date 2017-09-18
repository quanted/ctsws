package ctsws.chemicalterms;

//import org.json.JSONArray;
import org.json.JSONObject;

public class ChemicalTermsParameters 
{

	protected String _substrate = null;
	public String getSubstrate() {return _substrate;}
	public void setSubstrate(String substrate) {_substrate = substrate;}
	
	protected String _chemTerm = null;
	public String getChemicalTerm() {return _chemTerm;}
	public void setChemicalTerm(String chemTerm) {_chemTerm = chemTerm;}
	
	public ChemicalTermsParameters(JSONObject joChemTerm)
	{
		 try
		 {
			 _substrate = joChemTerm.getString("substrate").trim();
			 _chemTerm = joChemTerm.getString("chemicalterm").trim();
		 }
		 catch(Exception ex)
		 {
			 
		 }
	}

}

