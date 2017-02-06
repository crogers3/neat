package crogers3.neuralnet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import crogers3.Pair;

public class Neuron implements Node {
  private final List<Pair<Node, Double>> inputs = new ArrayList<>();
  private Optional<Boolean> cachedValue = Optional.absent();
  
  public void addInput(Node node, Double weight) {
    inputs.add(new Pair<>(node, weight));
  }
  
  @Override
  public boolean getValue() {
    if (cachedValue.isPresent()) {
      return cachedValue.get();
    }
    Double summedInputs = 0.0;
    for (Pair<Node, Double> input : inputs) {
      if (input.getFirst().getValue()) {
        summedInputs = summedInputs + input.getSecond();
      }
    }
    boolean value = summedInputs > 0;
    cachedValue = Optional.of(value);
    return value;
  }
  
  @Override
  public List<Node> getAncestors() {
    return inputs.stream().map((Pair<Node, Double> pair) -> pair.getFirst()).collect(Collectors.toList());
  }
  
  @Override
  public void clearCachedValue() {
    cachedValue = Optional.absent();
  }
  
}