package crogers3.neuralnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import crogers3.Pair;

public class Neuron implements Node {
  private final int id;
  private final List<Pair<Node, Double>> inputs = new ArrayList<>();
  private Optional<Boolean> cachedValue = Optional.absent();
  
  public Neuron(int id) {
    this.id = id;
  }
  
  @Override
  public int getId() {
    return id;
  }
  
  public void addInput(Node node, Double weight) {
    inputs.add(new Pair<>(node, weight));
  }
  
  @Override
  public boolean getValue(Set<Node> parents) {
    if (cachedValue.isPresent()) {
      return cachedValue.get();
    }
    if (parents.contains(this)) {
      return cachedValue.isPresent() && cachedValue.get();
    }
    Double summedInputs = 0.0;
    Set<Node> newParents = Sets.union(parents, ImmutableSet.of(this));
    for (Pair<Node, Double> input : inputs) {
      if (input.getFirst().getValue(newParents)) {
        summedInputs = summedInputs + input.getSecond();
      }
    }
    boolean value = summedInputs > 0;
    cachedValue = Optional.of(value);
    return value;
  }
  
  @Override
  public void clearCachedValue() {
    cachedValue = Optional.absent();
  }
  
  @Override
  public String toString() {
    return String.format("Neuron(%d): %b", id, Collections.emptySet());
  }
  
}