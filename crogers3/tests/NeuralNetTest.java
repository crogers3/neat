package crogers3.tests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import crogers3.neuralnet.NeuralNet;
import crogers3.proto.compiled.GenomeProtos.Gene;
import crogers3.proto.compiled.GenomeProtos.Genome;

@RunWith(JUnit4.class)
public class NeuralNetTest {

  @Test
  public void testOrGenome() {
    int inputId1 = 1;
    int inputId2 = 2;
    int outputId = 3;
    Genome orGenome = Genome.newBuilder()
        .addGene(Gene.newBuilder()
            .setInNode(inputId1)
            .setOutNode(outputId)
            .setWeight(1)
            .setEnabled(true)
            .setInnovationNumber(1))
        .addGene(Gene.newBuilder()
            .setInNode(inputId2)
            .setOutNode(outputId)
            .setWeight(1)
            .setEnabled(true)
            .setInnovationNumber(2))
        .build();
    testNeuralNet(
        NeuralNet.fromGenome(orGenome),
        ImmutableSet.of(),
        (idToValue) -> {
          return ImmutableMap.of(outputId, idToValue.values().stream().reduce((val1, val2) -> val1 || val2).get());
        });
  }
  
  @Test
  public void testAndGenome() {
    int biasInput = 0;
    int inputId1 = 1;
    int inputId2 = 2;
    int outputId = 3;
    Genome andGenome = Genome.newBuilder()
        .addGene(Gene.newBuilder()
            .setInNode(inputId1)
            .setOutNode(outputId)
            .setWeight(1)
            .setEnabled(true)
            .setInnovationNumber(1))
        .addGene(Gene.newBuilder()
            .setInNode(inputId2)
            .setOutNode(outputId)
            .setWeight(1)
            .setEnabled(true)
            .setInnovationNumber(2))
        .addGene(Gene.newBuilder()
            .setInNode(biasInput)
            .setOutNode(outputId)
            .setWeight(-1.5)
            .setEnabled(true)
            .setInnovationNumber(3))
        .build();
    testNeuralNet(
        NeuralNet.fromGenome(andGenome),
        ImmutableSet.of(biasInput),
        (idToValue) -> {
           return ImmutableMap.of(outputId, idToValue.values().stream().reduce((val1, val2) -> val1 && val2).get());
        });
  }
  
  private void testNeuralNet(
      NeuralNet neuralNet, Set<Integer> biasIds, Function<Map<Integer, Boolean>, Map<Integer, Boolean>> expected) {
    for (int biasId : biasIds) {
      neuralNet.setInputValue(biasId, true);
    }
    
    ImmutableList<Integer> inputIds = ImmutableList.copyOf(Sets.difference(neuralNet.getInputIds(), biasIds));
    for (int inputVector = 0; inputVector < Math.pow(2, inputIds.size()); ++inputVector) {
      Map<Integer, Boolean> inputValues = new HashMap<>();
      for (int inputOffset = 0; inputOffset < inputIds.size(); ++inputOffset) {
        int inputId = inputIds.get(inputOffset);
        boolean inputValue = ((inputVector >> inputOffset) & 1) > 0;
        inputValues.put(inputId, inputValue);
        neuralNet.setInputValue(inputId, inputValue);
      }
      Map<Integer, Boolean> expectedOutput = expected.apply(inputValues);
      assertEquals(expectedOutput, neuralNet.getValues());
    }
  }

}
