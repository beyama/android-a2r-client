package eu.addicted2random.a2rclient.test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import junit.framework.TestCase;
import eu.addicted2random.a2rclient.Range;

public class RangeTest extends TestCase {
  
  public void testConstructor() {
    Range r = new Range(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE);
    
    assertEquals(BigDecimal.ONE, r.start);
    assertEquals(BigDecimal.TEN, r.end);
    assertEquals(BigDecimal.ONE, r.step);
    assertEquals(BigDecimal.TEN.subtract(BigDecimal.ONE), r.distance);
    
    try {
      new Range(BigDecimal.ONE, BigDecimal.TEN, new BigDecimal(-1));
      fail("No exception caught");
    } catch(IllegalArgumentException e) {
      assertEquals("Step can't be zero or less than zero", e.getMessage());
    }
    
    try {
      new Range(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO);
      fail("No exception caught");
    } catch(IllegalArgumentException e) {
      assertEquals("Step can't be zero or less than zero", e.getMessage());
    }
    
  }

  public void testConstructorWithIntegers() {
    Range r = new Range(0, 100);
    assertEquals(1, r.step.intValue());
    
    assertEquals(0, r.start.intValue());
    assertEquals(100, r.end.intValue());
    
    r = new Range(0, 100, 2);
    assertEquals(2, r.step.intValue());
    
    r = new Range(0, -2);
    assertEquals(-2, r.distance.intValue());
  }
  
  public void testConstructorWithFloats() {
    Range r = new Range(0.0f, 100.0f);
    
    assertEquals(0f, r.start.floatValue());
    assertEquals(100f, r.end.floatValue());
    
    assertNull(r.step);
    
    r = new Range(0f, 100f, 0.1f);
    assertEquals(0.1f, r.step.floatValue());
    
    r = new Range(0f, -5f);
    assertEquals(-5f, r.distance.floatValue());
  }
  
  public void testConstructorWithDoubles() {
    Range r = new Range(-1.0d, 1.0d);
    
    assertEquals(-1.0d, r.start.doubleValue());
    assertEquals(1.0d, r.end.doubleValue());
    
    assertNull(r.step);
    
    r = new Range(-1.0d, 1.0d, 0.1d);
    assertEquals(0.1d, r.step.doubleValue());
    
    r = new Range(0f, -1f);
    assertEquals(-1d, r.distance.doubleValue());
  }
  
  public void testScale() {
    Range r1 = new Range(0, 100);
    Range r2 = new Range(0.0f, 1.0f);
    
    assertEquals(1.0f, r2.scale(r1, 100).floatValue());
    assertEquals(0.5f, r2.scale(r1, 50).floatValue());
    assertEquals(0.0f, r2.scale(r1, 0).floatValue());
  }
  
  public void testRound() {
    Range r = new Range(0, 100);
    assertEquals(0, r.round(-5).intValue());
    assertEquals(100, r.round(101).intValue());
    assertEquals(50d, r.round(50.1d).doubleValue());
    
    r = new Range(0, 100, 2);
    assertEquals(0, r.round(53).scale());
    
    r = new Range(0.0f, 1.0f, 0.1f);
    assertEquals(0.1f, r.round(0.12f).floatValue());
    
    r = new Range(5, 15, 2);
    assertEquals(6, r.round(7).intValue());
    assertEquals(5, r.round(3).intValue());
    assertEquals(5, r.round(-2).intValue());
  }
  
  public void testRoundWithStep() {
    Range r = new Range(0.0f, 100.0f, 0.5f);
    
    assertEquals(0.0f, r.round(-5).floatValue());
    assertEquals(100.0f, r.round(101).floatValue());
    assertEquals(25.5f, r.round(25.6f).floatValue());
    assertEquals(26.0f, r.round(25.6f, RoundingMode.UP).floatValue());
  }
  
  public void testSteps() {
    Range r = new Range(0, 100);
    assertEquals(Integer.valueOf(100), r.steps());
    
    r = new Range(0, 1, 0.1);
    assertEquals(Integer.valueOf(10), r.steps());
    
    r = new Range(0, 100, 2);
    assertEquals(Integer.valueOf(50), r.steps());
    
    r = new Range(0.0, 1.0);
    assertNull(r.steps());
  }
  
}
