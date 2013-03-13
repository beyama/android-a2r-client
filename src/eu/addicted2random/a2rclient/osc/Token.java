package eu.addicted2random.a2rclient.osc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Token {

  private final Token parent;
  private final String name;
  private final String address;
  
  private Node node;
  private Map<String, Token> children = null;
  
  public Token(Token parent, String token, Node node) {
    super();
    
    this.parent = parent;
    this.name = token;
    this.node = node;
    if(parent == null) {
      this.address = "";
    } else {
      if(!Address.isValidToken(token))
        throw new IllegalArgumentException("Invalid token `" + String.valueOf(token) + "`");
      
      this.address = parent.getAddress() + "/" + token;
      this.parent.addChild(this);
    }
  }
  
  public Token(Token parent, String token) {
    this(parent, token, null);
  }

  /**
   * Get parent token.
   * @return
   */
  public Token getParent() {
    return parent;
  }

  /**
   * Get name of token.
   * @return
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get full address of token.
   * @return
   */
  public String getAddress() {
    return address;
  }

  /**
   * Get node.
   * @return
   */
  public Node getNode() {
    return node;
  }
  
  /**
   * Set node.
   * @param node
   */
  public void setNode(Node node) {
    this.node = node;
  }

  /**
   * Get children.
   * @return
   */
  public Map<String, Token> getChildren() {
    return children;
  }
  
  /**
   * Get child by token.
   * @param token
   * @return
   */
  public Token getChild(String token) {
    if(children == null) return null;
    return children.get(token);
  }
  
  /**
   * Add a child node.
   * @param child
   */
  protected void addChild(Token child) {
    if(children == null) children = new HashMap<String, Token>();
    
    if(children.containsKey(child.getName()))
      throw new IllegalArgumentException(String.format("child with token `%s` already registered", child.getName()));
    children.put(child.getName(), child);
  }
  
  protected Token removeChild(Token token) {
    Token child = getChild(token.getName());
    if(child != null && child.equals(child)) {
      return children.remove(token.getName());
    }
    return null;
  }
  
  /**
   * Dispose this token.
   * 
   * @param removeFromParent
   */
  public void dispose(boolean removeFromParent) {
    synchronized (this) {
      if(children != null) {
        Token child;
        Iterator<String> iter = children.keySet().iterator();
        
        while(iter.hasNext()) {
          child = getChild(iter.next());
          
          if(child != null)
            child.dispose(false);
        }
      }
      if(node != null) {
        node.dispose();
        node = null;
      }
      
      if(parent != null) {
        if(removeFromParent)
          parent.removeChild(this);
        children = null;
      }
    }
  }
  
}
