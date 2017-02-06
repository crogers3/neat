package crogers3.neat;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import crogers3.InnovationNumberProvider;
import crogers3.proto.compiled.GenomeProtos.Gene;
import crogers3.proto.compiled.GenomeProtos.Genome;

public class GenomeMutator {
  private final InnovationNumberProvider innovationNumberProvider;
  
  public GenomeMutator(InnovationNumberProvider innovationNumberProvider) {
    this.innovationNumberProvider = innovationNumberProvider;
  }
  
  public Genome maybeMutateWeights(Genome genome) {
    Random random = new Random();
    if (random.nextFloat() > Config.GENOME_MUTATION_CHANCE) {
      return genome;
    }
    Genome.Builder mutatedGenomeBuilder = Genome.newBuilder();
    int multiplier = -1 * random.nextInt(2);
    for (Gene gene : genome.getGeneList()) {
      double mutatedWeight;
      if (random.nextFloat() < Config.UNIFORM_MUTATION_CHANCE) {
        mutatedWeight = gene.getWeight() + (multiplier * Config.UNIFORM_MUTATION_STEP);
      } else {
        mutatedWeight = 2 * random.nextFloat() * Config.MAX_RANDOM_MUTATION_STEP;
        mutatedWeight = mutatedWeight - (mutatedWeight / 2);
      }
      mutatedGenomeBuilder.addGene(gene.toBuilder().setWeight(mutatedWeight));
    }
    return mutatedGenomeBuilder.build();
  }
  
  public Genome maybeAddNode(Genome genome) {
    Random random = new Random();
    if (random.nextDouble() > Config.NEW_NODE_CHANCE) {
      return genome;
    }
    int newNodeId = genome.getGeneList()
        .stream()
        .map(gene -> Math.max(gene.getInNode(), gene.getOutNode()))
        .max((id1, id2) -> id1 - id2)
        .get();
    Genome.Builder mutatedGenome = genome.toBuilder();
    
    // Pick random connection
    int index = random.nextInt(genome.getGeneCount());
    Gene gene = genome.getGene(index);
    
    mutatedGenome.setGene(index, gene.toBuilder().setEnabled(false));
    mutatedGenome.addGene(
        Gene.newBuilder()
            .setInNode(gene.getInNode())
            .setOutNode(newNodeId)
            .setEnabled(true)
            .setWeight(1.0)
            .setInnovationNumber(innovationNumberProvider.getNextNumber()));
    mutatedGenome.addGene(
        Gene.newBuilder()
            .setInNode(newNodeId)
            .setOutNode(gene.getOutNode())
            .setEnabled(true)
            .setWeight(gene.getWeight())
            .setInnovationNumber(innovationNumberProvider.getNextNumber()));
    
    return mutatedGenome.build();
  }
  
  public Genome maybeAddConnection(Genome genome) {
    Random random = new Random();
    if (random.nextDouble() > Config.NEW_CONNECTION_CHANCE) {
      return genome;
    }
    Set<Set<Integer>> connections =
        genome
            .getGeneList()
            .stream()
            .map((Gene gene) -> ImmutableSet.of(gene.getInNode(), gene.getOutNode()))
            .collect(Collectors.toSet());
    List<Integer> allNodes =
        connections
            .stream()
            .reduce(Collections.emptySet(), (result, set) -> Sets.union(result, set))
            .stream()
            .collect(Collectors.toList());
    while (!connections.isEmpty()) {
      Integer inNode = allNodes.get(random.nextInt(allNodes.size()));
      Integer outNode = allNodes.get(random.nextInt(allNodes.size()));
      if (connections.remove(ImmutableSet.of(inNode, outNode))) {
        continue;
      }
      double randomWeight = 2 * random.nextFloat() * Config.MAX_RANDOM_MUTATION_STEP;
      randomWeight = randomWeight - (randomWeight / 2);
      return genome.toBuilder()
          .addGene(
              Gene.newBuilder()
                  .setInNode(inNode)
                  .setOutNode(outNode)
                  .setEnabled(true)
                  .setWeight(randomWeight)
                  .setInnovationNumber(innovationNumberProvider.getNextNumber()))
          .build();
    }
    return genome;
  }
}