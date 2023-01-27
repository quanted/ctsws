package ctsws.metabolism;

public class CTSMetabolite 
{
	private String _key;
	public String parentKey;
	
	public double accumulation;
    public double globalAccumulation;

    public int degradation;
    public int generation;

    public int formationRate;
    public int globalFormationRate;

    public String likelihood;

    public double production;
    public double globalProduction;

    public int phase;
    public String state;

    public double transmissivity;

    public String route;
    public String routes;
    
    public CTSMetabolite(String key)
    {
    	_key = key;
    }
    
    @Override
    public String toString() 
    {
    	return _key;
    }
    
    public String getKey()
    {
    	return _key;
    }

}
