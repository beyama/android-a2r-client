package eu.addicted2random.a2rclient.test.osc;

import eu.addicted2random.a2rclient.osc.Address;
import junit.framework.TestCase;

public class AddressTest extends TestCase {
  
  private void assertValidToken(String token) {
    assertTrue(String.format("Token %s should be valid", token), Address.isValidToken(token));
  }
  
  private void assertNotValidToken(String token) {
    assertFalse(String.format("Token %s should not be valid", token), Address.isValidToken(token));
  }
  
  private void assertValidAddress(String address) {
    assertTrue(String.format("Address %s should be valid", address), Address.isValidAddress(address));
  }
  
  private void assertNotValidAddress(String address) {
    assertFalse(String.format("Address %s should not be valid", address), Address.isValidAddress(address));
  }
  
  private void assertValidPattern(String pattern) {
    assertTrue(String.format("Pattern %s should be valid", pattern), Address.isValidPattern(pattern));
  }
  
  private void assertNotValidPattern(String pattern) {
    assertFalse(String.format("Pattern %s should not be valid", pattern), Address.isValidPattern(pattern));
  }
  
  public void testIsValidToken() {
    assertValidToken("foo");
    assertValidToken("bar");
    assertValidToken("f1k2");
    
    assertNotValidToken("bar/baz");
    assertNotValidToken("ba{r,z}");
    assertNotValidToken("ba[a-z]");
    assertNotValidToken("ba*");
    assertNotValidToken("b*r");
    assertNotValidToken("b?r"); 
  }
  
  public void testIsValidAddress() {
    assertValidAddress("/");
    assertValidAddress("/foo");
    assertValidAddress("/foo/bar");
    assertValidAddress("/foo/bar.get");
    
    assertNotValidAddress("//");
    assertNotValidAddress("foo/bar");
    assertNotValidAddress("/foo//");
    assertNotValidAddress("/foo//bar");
    assertNotValidAddress("/foo/{bar,baz}");
    assertNotValidAddress("/foo[0-1]");
  }
  
  public void testIsValidPattern() {
    assertValidPattern("//foo");
    assertValidPattern("/foo/ba[0-9]");
    assertValidPattern("/foo/ba?");
    
    assertNotValidPattern("/foo/bar");
  }

}
