package crogers3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import crogers3.proto.compiled.GenomeProtos.Gene;

public class InnovationNumberProvider {
  private final AtomicInteger nextNumber = new AtomicInteger(0);
  private final List<Gene> uniqueStructureGenes = new ArrayList<>();
  
  public Gene applyInnovationNumber(Gene.Builder geneBuilder) {
    for (Gene uniqueStructureGene : uniqueStructureGenes) {
      if (geneBuilder.getInNode() == uniqueStructureGene.getInNode()
          && geneBuilder.getOutNode() == uniqueStructureGene.getOutNode()) {
        return geneBuilder.setInnovationNumber(uniqueStructureGene.getInnovationNumber()).build();
      }
    }
    Gene geneWithInnovationNumber = geneBuilder.setInnovationNumber(nextNumber.getAndIncrement()).build();
    uniqueStructureGenes.add(geneWithInnovationNumber);
    return geneWithInnovationNumber;
  }
  
  public void clearGenes() {
    uniqueStructureGenes.clear();
  }
}