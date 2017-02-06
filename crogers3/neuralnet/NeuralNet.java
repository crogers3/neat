package crogers3.neuralnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import crogers3.Pair;
import crogers3.proto.compiled.GenomeProtos.Gene;
import crogers3.proto.compiled.GenomeProtos.Genome;

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
      values.put(outputId, outputNode.getValue());
    }
    for (Node node : nodes.values()) {
      node.clearCachedValue();
    }
    return values.build();
  }
  
  public static NeuralNet fromGenome(Genome genome) {
    Set<Integer> allIds = new HashSet<>();
    Multimap<Integer, Pair<Integer, Double>> idsWithInputsToInputWeightPair = HashMultimap.create();
    Set<Integer> idsWithOutputs = new HashSet<>();
    
    for (Gene gene : genome.getGeneList()) {
      if (!gene.getEnabled()) {
        continue;
      }
      allIds.add(gene.getInNode());
      allIds.add(gene.getOutNode());
      idsWithInputsToInputWeightPair.put(gene.getOutNode(), new Pair<>(gene.getInNode(), gene.getWeight()));
      idsWithOutputs.add(gene.getInNode());
    }
    
    Set<Integer> inputIds = Sets.difference(allIds, idsWithInputsToInputWeightPair.keySet());
    Set<Integer> outputIds = Sets.difference(allIds, idsWithOutputs);
    
    Map<Integer, Node> nodes = new HashMap<>();
    for (Integer outputId : outputIds) {
      createNode(outputId, idsWithInputsToInputWeightPair, nodes);
    }
    
    return new NeuralNet(nodes, inputIds, outputIds);
  }
  
  private static Node createNode(
      Integer nodeId,
      Multimap<Integer, Pair<Integer, Double>> nodeIdToInputWeightPair,
      Map<Integer, Node> nodes) {
    if (nodes.containsKey(nodeId)) {
      return nodes.get(nodeId);
    }
    if (!nodeIdToInputWeightPair.containsKey(nodeId)) {
      Node node = new InputNode();
      nodes.put(nodeId, node);
      return node;
    }
    Neuron neuron = new Neuron();
    for (Pair<Integer, Double> inputWeightPair : nodeIdToInputWeightPair.get(nodeId)) {
      Node inputNode = createNode(inputWeightPair.getFirst(), nodeIdToInputWeightPair, nodes);
      neuron.addInput(inputNode, inputWeightPair.getSecond());
    }
    nodes.put(nodeId, neuron);
    return neuron;
  }
}