package crogers3.neuralnet;

import java.util.Set;

public interface Node {
  boolean getValue(Set<Node> parents);
  
  int getId();
  
  void clearCachedValue();
}