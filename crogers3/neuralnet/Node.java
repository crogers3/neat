package crogers3.neuralnet;

import java.util.List;

public interface Node {
  boolean getValue();
  
  List<Node> getAncestors();
  
  void clearCachedValue();
}