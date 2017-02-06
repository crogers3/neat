package crogers3.neat;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import crogers3.proto.compiled.GenomeProtos.Gene;
import crogers3.proto.compiled.GenomeProtos.Genome;

public class GenomeCompatability {
  public boolean areCompatable(Genome genome1, Genome genome2) {
    return distance(genome1, genome2) <= Config.CT;
  }
  
  public double distance(Genome genome1, Genome genome2) {
    Map<Integer, Gene> genes1 =
        genome1.getGeneList().stream().collect(Collectors.toMap(gene -> gene.getInnovationNumber(), gene -> gene));
    Integer maxInnovationNumber1 = Collections.max(genes1.keySet());
    Map<Integer, Gene> genes2 =
        genome2.getGeneList().stream().collect(Collectors.toMap(gene -> gene.getInnovationNumber(), gene -> gene));
    Integer maxInnovationNumber2 = Collections.max(genes2.keySet());
    
    double N = Math.max(genome1.getGeneCount(), genome2.getGeneCount());
    double E = Sets.union(genes1.keySet(), genes2.keySet()).stream()
        .filter(i -> i > Math.max(maxInnovationNumber1, maxInnovationNumber2)).count();
    double D = Sets.union(genes1.keySet(), genes2.keySet()).size() - E;
    
    double W = 0.0;
    for (Integer matching : Sets.intersection(genes1.keySet(), genes2.keySet())) {
      W = W + Math.abs(genes1.get(matching).getWeight() - genes2.get(matching).getWeight());
    }
    
    return (Config.C1 * E / N) + (Config.C2 * D / N) + (Config.C3 * W);
  }
}