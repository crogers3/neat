package crogers3.neuralnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import crogers3.proto.compiled.NeatProtos.Gene;
import crogers3.proto.compiled.NeatProtos.Genome;

public class NeuralNet {
  private final ImmutableMap<Integer, Node> nodes;
  private final ImmutableSet<Integer> inputIds;
  private final ImmutableSet<Integer> outputIds;
  
  public NeuralNet(Map<Integer, Node> nodes, Set<Integer> inputIds, Set<Integer> outputIds) {
    // Validate args
    Preconditions.checkArgument(Sets.intersection(inputIds, outputIds).isEmpty());
    
    this.nodes = ImmutableMap.copyOf(nodes);
    this.inputIds = ImmutableSet.copyOf(inputIds);
    this.outputIds = ImmutableSet.copyOf(outputIds);
  }
  
  public void setInputValue(Integer inputId, boolean value) {
    Preconditions.checkArgument(inputIds.contains(inputId));
    ((InputNode) nodes.get(inputId)).setValue(value);
  }
  
  public ImmutableSet<Integer> getInputIds() {
    return inputIds;
  }
  
  public ImmutableMap<Integer, Boolean> getValues() {
    ImmutableMap.Builder<Integer, Boolean> values = ImmutableMap.builder();
    for (Integer outputId : outputIds) {
      Node outputNode = nodes.get(outputId);
      values.put(outputId, outputNode.getValue(ImmutableSet.of()));
    }
    for (Node node : nodes.values()) {
      node.clearCachedValue();
    }
    return values.build();
  }
  
  public static NeuralNet fromGenome(Genome genome) {
    Set<Integer> allIds = new HashSet<>();
    for (Gene gene : genome.getGeneList()) {
      allIds.add(gene.getInNode());
      allIds.add(gene.getOutNode());
    }
    Set<Integer> inputIds = new HashSet<>(allIds);
    Set<Integer> outputIds = new HashSet<>(allIds);
    for (Gene gene : genome.getGeneList()) {
      outputIds.remove(gene.getInNode());
      inputIds.remove(gene.getOutNode());
    }
    
    Map<Integer, InputNode> inputNodes = new HashMap<>();
    Map<Integer, Neuron> neurons = new HashMap<>();
    // Create nodes
    for (Integer id : allIds) {
      if (inputIds.contains(id)) {
        inputNodes.put(id, new InputNode(id));
      } else {
        neurons.put(id, new Neuron(id));
      }
    }
    // Wire connections
    for (Gene gene : genome.getGeneList()) {
      if (!gene.getEnabled()) {
        continue;
      }
      Node inputNode;
      if (inputIds.contains(gene.getInNode())) {
        inputNode = inputNodes.get(gene.getInNode());
      } else {
        inputNode = neurons.get(gene.getInNode());
      }
      Neuron neuron = neurons.get(gene.getOutNode());
      neuron.addInput(inputNode, gene.getWeight());
    }
    
    Map<Integer, Node> nodes = new HashMap<>();
    nodes.putAll(inputNodes);
    nodes.putAll(neurons);
    return new NeuralNet(nodes, inputIds, outputIds);
  }
}