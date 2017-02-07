package crogers3.neat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import crogers3.InnovationNumberProvider;
import crogers3.neuralnet.NeuralNet;
import crogers3.proto.compiled.NeatProtos.Gene;
import crogers3.proto.compiled.NeatProtos.Genome;
import crogers3.proto.compiled.NeatProtos.Population;
import crogers3.proto.compiled.NeatProtos.Species;

public class XorTrainer {
  private static final int NUM_NETWORKS = 150;
  private static final int BIAS_ID = 1;
  private static final int INPUT_1_ID = 2;
  private static final int INPUT_2_ID = 3;
  private static final int OUTPUT_ID = 4;

  private final InnovationNumberProvider innovationNumberProvider = new InnovationNumberProvider();
  private final Random random = new Random();
  private final GenomeMutator mutator = new GenomeMutator(innovationNumberProvider, random);
  
  public Population createStarterPopulation() {
    Genome.Builder starterStructure =
        Genome.newBuilder()
            .addGene(innovationNumberProvider.applyInnovationNumber(
                Gene.newBuilder().setInNode(BIAS_ID).setOutNode(OUTPUT_ID).setEnabled(true)))
            .addGene(innovationNumberProvider.applyInnovationNumber(
                Gene.newBuilder().setInNode(INPUT_1_ID).setOutNode(OUTPUT_ID).setEnabled(true)))
            .addGene(innovationNumberProvider.applyInnovationNumber(
                Gene.newBuilder().setInNode(INPUT_2_ID).setOutNode(OUTPUT_ID).setEnabled(true)));
    Population.Builder population = Population.newBuilder().addSpecies(Species.newBuilder());
    for (int i = 0; i < NUM_NETWORKS; ++i) {
      Genome.Builder genome = starterStructure.clone();
      for (Gene.Builder gene : genome.getGeneBuilderList()) {
        gene.setWeight((random.nextBoolean() ? 1.0 : -1.0) * random.nextDouble() * Config.MAX_RANDOM_MUTATION_STEP);
      }
      population.getSpeciesBuilder(0).addGenome(genome);
    }
    return population.build();
  }
  
  public double fitness(Genome genome) {
    NeuralNet neuralNet = NeuralNet.fromGenome(genome);
    double distance = 0.0;
    neuralNet.setInputValue(BIAS_ID, true);
    
    // Test all permutations
    neuralNet.setInputValue(INPUT_1_ID, false);
    neuralNet.setInputValue(INPUT_2_ID, false);
    distance = neuralNet.getValues().get(OUTPUT_ID) ? 1.0 : 0.0;
    
    neuralNet.setInputValue(INPUT_1_ID, true);
    neuralNet.setInputValue(INPUT_2_ID, false);
    distance = neuralNet.getValues().get(OUTPUT_ID) ? 0.0 : 1.0;
    
    neuralNet.setInputValue(INPUT_1_ID, false);
    neuralNet.setInputValue(INPUT_2_ID, true);
    distance = neuralNet.getValues().get(OUTPUT_ID) ? 0.0 : 1.0;
    
    neuralNet.setInputValue(INPUT_1_ID, true);
    neuralNet.setInputValue(INPUT_2_ID, true);
    distance = neuralNet.getValues().get(OUTPUT_ID) ? 1.0 : 0.0;
    
    return Math.pow(4.0 - distance, 2);
  }
  
  public double sharedFitness(Genome genome, Species species) {
    return fitness(genome) / species.getGenomeCount();
  }
  
  public Population iterate(Population population) {
    List<Genome> genomes = population.getSpeciesList().stream()
        .map(species -> species.getGenomeList())
        .reduce(Collections.emptyList(), (result, genomeList) -> {
          result.addAll(genomeList);
          return result;
        });
    List<Genome> mutatedGenomes = genomes.stream()
        .map(genome -> mutator.maybeMutateGenome(genome)).collect(Collectors.toList());
    
    // Species
    Population.Builder newPopulation = Population.newBuilder();
    for (Species species : population.getSpeciesList()) {
      newPopulation.addSpecies(Species.newBuilder());
    }
    for (Genome mutatedGenome : mutatedGenomes) {
      boolean addedToSpecies = false;
      for (int i = 0; i < population.getSpeciesCount(); ++i) {
        if (GenomeCompatability.areCompatable(mutatedGenome, population.getSpecies(i).getRepresentative())) {
          newPopulation.getSpeciesBuilder(i).addGenome(mutatedGenome);
          addedToSpecies = true;
          break;
        }
      }
      if (!addedToSpecies) {
        if (newPopulation.getSpeciesCount() == population.getSpeciesCount()) {
          newPopulation.addSpecies(Species.newBuilder().addGenome(mutatedGenome).setRepresentative(mutatedGenome));
        } else {
          newPopulation.getSpeciesBuilder(newPopulation.getSpeciesCount() - 1).addGenome(mutatedGenome);
        }
      }
    }

    return null;
  }
  
  public void run() {
    Population starterPopulation = createStarterPopulation();
  }
  
  public static void main(String[] args) {

  }
}