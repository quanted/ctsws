package ctsws.util;
import java.util.HashMap;

public class Node<T> 
{
    private T _data;
    private Node<T> _parent;
    private HashMap<String, Node<T>> children;
    private String key;
    
    public Node(String key, T data)
    {
    	_data = data;
    	children = new HashMap<String, Node<T>>();
    	this.key = key;
    }
    
    public String getKey()
    {
    	return _data.toString();
    }
    
    public String getParentKey()
    {
    	if (_parent == null)
    		return null;
    	else
    		return _parent.getKey();
    }
    
    public T getData()
    {
    	return _data;
    }
    
    public HashMap<String, Node<T>> getChildren()
    {
    	return children;
    }
    
    public boolean childExists(String key)
    {
    	return children.containsKey(key);
    }
    
    public void addChild(Node<T> node)
    {
    	if (children == null)
    		children = new HashMap<String, Node<T>>();
    	
    	children.put(node.getKey(), node);
    }
}
