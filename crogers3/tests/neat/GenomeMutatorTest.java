package crogers3.tests.neat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import crogers3.InnovationNumberProvider;
import crogers3.neat.Config;
import crogers3.neat.GenomeMutator;
import crogers3.proto.compiled.GenomeProtos.Gene;
import crogers3.proto.compiled.GenomeProtos.Genome;
import crogers3.testing.FakeRandom;

@RunWith(JUnit4.class)
public class GenomeMutatorTest {
  private final InnovationNumberProvider innovationNumberProvider = new InnovationNumberProvider();
  private final FakeRandom fakeRandom = new FakeRandom();
  private final GenomeMutator mutator = new GenomeMutator(innovationNumberProvider, fakeRandom);
  
  @Test
  public void testMutateWeights_uniformStep() {
    Genome genome = Genome.newBuilder()
        .addGene(Gene.newBuilder().setWeight(2.0))
        .addGene(Gene.newBuilder().setWeight(3.0))
        .addGene(Gene.newBuilder().setWeight(4.0))
        .build();
    
    fakeRandom.setNextBoolean(true);
    Genome mutatedGenome = mutator.maybeMutateWeights(genome);
    
    for (int i = 0; i < genome.getGeneCount(); ++i) {
      assertEquals(
          genome.getGene(i).getWeight() + Config.UNIFORM_MUTATION_STEP,
          mutatedGenome.getGene(i).getWeight(),
          0.001);
    }
  }
}