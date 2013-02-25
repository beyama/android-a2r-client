package eu.addicted2random.a2rclient.jsonrpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base implementation of {@link IPayload}.
 * 
 * @see eu.addicted2random.a2rclient.jsonrpc.IPayload
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public class Payload implements IPayload {
  
  private final Object payload;
  
  /**
   * Construct a {@link Payload} instance with payload data.
   * 
   * @param payload The payload object.
   */
  public Payload(Object payload) {
    if(payload instanceof Object[]) {
      Object[] array = (Object[])payload;
      List<Object> list = new ArrayList<Object>(array.length);
      Collections.addAll(list, array);
      this.payload = list;
    } else {
      this.payload = payload;
    }
  }
  
  /**
   * Construct an empty {@link Payload} instance.
   */
  public Payload() {
    this((Object)null);
  }
  
  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#hasPayload()
   */
  @Override
  public boolean hasPayload() {
    return this.payload != null;
  }
  
  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#isList()
   */
  @Override
  public boolean isList() {
    return payload != null && payload instanceof List;
  }
  
  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#isMap()
   */
  @Override
  public boolean isMap() {
    return payload != null && payload instanceof Map;
  }
  
  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#getMap()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMap() {
    if(isMap())
      return (Map<String, Object>)payload;
    return null;
  }
  
  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#getList()
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<Object> getList() {
    if(isList())
      return (List<Object>)payload;
    return null;
  }

  /* (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.IPayload#getPayload()
   */
  @Override
  public Object getPayload() {
    return payload;
  }

  /* 
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.JSONSerializable#toJSON()
   */
  @Override
  public Object toJSON() throws JSONException {
    if(isList())
      return Message.wrap(getList());
    if(isMap())
      return Message.wrap(getMap());
    return JSONObject.NULL;
  }

  /*
   * (non-Javadoc)
   * @see eu.addicted2random.a2rclient.jsonrpc.JSONSerializable#toJSONString()
   */
  @Override
  public String toJSONString() throws JSONException {
    return toJSON().toString();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((payload == null) ? 0 : payload.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Payload other = (Payload) obj;
    if (payload == null) {
      if (other.payload != null)
        return false;
    } else if (!payload.equals(other.payload))
      return false;
    return true;
  }

}
