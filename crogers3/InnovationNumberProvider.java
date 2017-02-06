package crogers3;

import java.util.concurrent.atomic.AtomicInteger;

public class InnovationNumberProvider {
  private final AtomicInteger nextNumber = new AtomicInteger(0);
  
  public int getNextNumber() {
    return nextNumber.getAndIncrement();
  }
}