package de.team33.libs.random.v3.range;

import de.team33.libs.random.v3.BasicRandom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class BoundsTest {

    private final BasicRandom simple = BasicRandom.simple();

  @Test
  public void projected() {
    new ProjectedTester(-20, 80, 10000).run((tester, step) -> {
      assertEquals(step.message, true, (tester.start <= step.projected) && (step.projected < tester.limit));
    });
  }

  @Test
  public void projectedUnique() {
    new ProjectedTester(-1616, -1615, 10000).run((tester, step) -> {
      assertEquals(step.message, -1616, step.projected);
    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void projectedNothing() {
    new ProjectedTester(8, 8, 10000).run((tester, step) -> {
      fail(step.message);
    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void projectedNegative() {
    new ProjectedTester(8, -12, 10000).run((tester, step) -> {
      fail(step.message);
    });
  }

  private class ProjectedTester
  {

    private final int start;

    private final int limit;

    private final int maxLoop;

    private ProjectedTester(final int start, final int limit, final int maxLoop)
    {
      this.start = start;
      this.limit = limit;
      this.maxLoop = maxLoop;
    }

    private ProjectedTester run(final BiConsumer<ProjectedTester, Step> consumer) {
      final Bounds bounds = new Bounds(start, limit);
      for (int i = 0; i < maxLoop; ++i) {
        final int anyInt = simple.anyInt();
        final int projected = bounds.projected(anyInt);
        final String message = String.format("i = %d; anyInt = %d; projected = %d", i, anyInt, projected);
        final Step step = new Step(i, anyInt, projected, message);
        consumer.accept(this, step);
      }
      return this;
    }

    private class Step {

      private final int loopIndex;

      private final int anyInt;

      private final int projected;

      private final String message;

      private Step(final int loopIndex, final int anyInt, final int projected, final String message)
      {
        this.loopIndex = loopIndex;
        this.anyInt = anyInt;
        this.projected = projected;
        this.message = message;
      }
    }
  }
}
