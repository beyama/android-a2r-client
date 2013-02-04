package eu.addicted2random.a2rclient.services.osc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jboss.netty.channel.Channel;

import com.illposed.osc.OSCMessage;

public class MethodInvokingEndpoint implements Endpoint {

  private final Method method;
  
  private final Object instance;
  
  public MethodInvokingEndpoint(Method method, Object instance) {
    super();
    this.method = method;
    this.instance = instance;
  }
  
  /**
   * Are these arguments applicable to method?
   * 
   * @param args Arguments
   * @return
   */
  public boolean isApplicable(Object ...args) {
    Type[] types = method.getGenericParameterTypes();
    if(types.length != args.length) return false;
    
    for(int i = 0; i < types.length; i++) {
      if(!types[i].equals(args[i]))
        return false;
    }
    
    return true;
  }
  
  protected boolean handleError(Channel channel, OSCMessage message, Exception exception) {
    return false;
  }

  @Override
  public void call(Channel channel, OSCMessage message) {
    try {
      method.invoke(this.instance, message.getArguments());
    } catch (IllegalArgumentException e) {
      handleError(channel, message, e);
    } catch (IllegalAccessException e) {
      handleError(channel, message, e);
    } catch (InvocationTargetException e) {
      handleError(channel, message, e);
    }
  }

}
