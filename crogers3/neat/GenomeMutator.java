package crogers3.neat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  private final Random random;
  
  public GenomeMutator(InnovationNumberProvider innovationNumberProvider, Random random) {
    this.innovationNumberProvider = innovationNumberProvider;
    this.random = random;
  }
  
  public Genome maybeMutateWeights(Genome genome) {
    if (random.nextDouble() > Config.GENOME_MUTATION_CHANCE) {
      return genome;
    }
    Genome.Builder mutatedGenomeBuilder = Genome.newBuilder();
    for (Gene gene : genome.getGeneList()) {
      double mutatedWeight;
      if (random.nextDouble() < Config.UNIFORM_MUTATION_CHANCE) {
        mutatedWeight = gene.getWeight() + ((random.nextBoolean() ? 1 : -1) * Config.UNIFORM_MUTATION_STEP);
      } else {
        mutatedWeight = 2 * random.nextDouble() * Config.MAX_RANDOM_MUTATION_STEP;
        mutatedWeight = mutatedWeight - (mutatedWeight / 2);
      }
      mutatedGenomeBuilder.addGene(gene.toBuilder().setWeight(mutatedWeight));
    }
    return mutatedGenomeBuilder.build();
  }
  
  public Genome maybeAddNode(Genome genome) {
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
    mutatedGenome.addGene(innovationNumberProvider.applyInnovationNumber(
        Gene.newBuilder()
            .setInNode(gene.getInNode())
            .setOutNode(newNodeId)
            .setEnabled(true)
            .setWeight(1.0)));
    mutatedGenome.addGene(innovationNumberProvider.applyInnovationNumber(
        Gene.newBuilder()
            .setInNode(newNodeId)
            .setOutNode(gene.getOutNode())
            .setEnabled(true)
            .setWeight(gene.getWeight())));
    
    return mutatedGenome.build();
  }
  
  public Genome maybeAddConnection(Genome genome) {
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
      double randomWeight = 2 * random.nextDouble() * Config.MAX_RANDOM_MUTATION_STEP;
      randomWeight = randomWeight - (randomWeight / 2);
      return genome.toBuilder()
          .addGene(innovationNumberProvider.applyInnovationNumber(
              Gene.newBuilder()
                  .setInNode(inNode)
                  .setOutNode(outNode)
                  .setEnabled(true)
                  .setWeight(randomWeight)))
          .build();
    }
    return genome;
  }
  
  public Genome crossoverGenomes(Genome genome1, Double fitness1, Genome genome2, Double fitness2) {
    Map<Integer, Gene> genes1 =
        genome1.getGeneList().stream()
            .collect(Collectors.toMap(gene -> gene.getInnovationNumber(), gene -> gene));
    Map<Integer, Gene> genes2 =
        genome2.getGeneList().stream()
            .collect(Collectors.toMap(gene -> gene.getInnovationNumber(), gene -> gene));
    List<Integer> all = Sets.union(genes1.keySet(), genes2.keySet()).stream().sorted().collect(Collectors.toList());
    Set<Integer> matching = Sets.intersection(genes1.keySet(), genes2.keySet());
    Double maxFitness = Math.max(fitness1, fitness2);
    
    Genome.Builder builder = Genome.newBuilder();
    for (Integer innovationNumber : all) {
      boolean disabledIn1 = genes1.containsKey(innovationNumber) && !genes1.get(innovationNumber).getEnabled();
      boolean disabledIn2 = genes2.containsKey(innovationNumber) && !genes2.get(innovationNumber).getEnabled();
      boolean enabled = (disabledIn1 || disabledIn2) ? random.nextDouble() > Config.CHILD_DISABLE_CHANCE : true;
      
      if (matching.contains(innovationNumber)) {
        Gene gene = (random.nextBoolean()) ? genes1.get(innovationNumber) : genes2.get(innovationNumber);
        builder.addGene(gene.toBuilder().setEnabled(enabled));
      } else {
        if (fitness1.equals(maxFitness) && genes1.containsKey(innovationNumber)) {
          builder.addGene(genes1.get(innovationNumber).toBuilder().setEnabled(enabled));
        } else if (fitness2.equals(maxFitness) && genes2.containsKey(innovationNumber)) {
          builder.addGene(genes2.get(innovationNumber).toBuilder().setEnabled(enabled));
        }
      }
    }
    return builder.build();
  }
}