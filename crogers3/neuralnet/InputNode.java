package crogers3.neuralnet;

import java.util.Collections;
import java.util.Set;

public class InputNode implements Node {
  private final int id;
  private boolean value;
  
  public InputNode(int id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return id;
  }
  
  @Override
  public boolean getValue(Set<Node> parents) {
    return value;
  }
  
  public void setValue(boolean value) {
    this.value = value;
  }
  
  @Override
  public void clearCachedValue() {}
  
  @Override
  public String toString() {
    return String.format("InputNode(%d): %b", id, getValue(Collections.emptySet()));
  }
  
}