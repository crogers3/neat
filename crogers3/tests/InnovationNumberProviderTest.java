package crogers3.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import crogers3.InnovationNumberProvider;
import crogers3.proto.compiled.GenomeProtos.Gene;

@RunWith(JUnit4.class)
public class InnovationNumberProviderTest {
  @Test
  public void testApplyInnovationNumber() {
    InnovationNumberProvider provider = new InnovationNumberProvider();
    
    Gene gene1 = provider.applyInnovationNumber(Gene.newBuilder()
        .setInNode(1)
        .setOutNode(2));
    Gene gene2 = provider.applyInnovationNumber(Gene.newBuilder()
        .setInNode(2)
        .setOutNode(3));
    
    assertNotEquals(gene1.getInnovationNumber(), gene2.getInnovationNumber());
    
    Gene gene3 = provider.applyInnovationNumber(Gene.newBuilder()
        .setInNode(2)
        .setOutNode(3));
    
    assertEquals(gene2.getInnovationNumber(), gene3.getInnovationNumber());
  }
}