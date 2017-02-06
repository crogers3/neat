package crogers3.neuralnet;

import java.util.ArrayList;
import java.util.List;

public class InputNode implements Node {
  private boolean value;

  @Override
  public boolean getValue() {
    return value;
  }
  
  @Override
  public List<Node> getAncestors() {
    return new ArrayList<>();
  }

  public void setValue(boolean value) {
    this.value = value;
  }
  
  @Override
  public void clearCachedValue() {}
  
  
}