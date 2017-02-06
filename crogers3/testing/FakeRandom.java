package crogers3.testing;

import java.util.Random;

public class FakeRandom extends Random {
  private static final long serialVersionUID = 1883024080165010673L;

  private int nextInt = 0;
  private boolean nextBoolean = false;
  private double nextDouble = 0.0;
  
  @Override
  public int nextInt(int bound) {
    return nextInt;
  }
  
  public void setNextInt(int nextInt) {
    this.nextInt = nextInt;
  }
  
  @Override
  public boolean nextBoolean() {
    return nextBoolean;
  }
  
  public void setNextBoolean(boolean nextBoolean) {
    this.nextBoolean = nextBoolean;
  }
  
  @Override
  public double nextDouble() {
    return nextDouble;
  }
  
  public void setNextDouble(double nextDouble) {
    this.nextDouble = nextDouble;
  }
}