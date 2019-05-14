package de.team33.libs.random.v4;

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;


public class BoundsTest
{

  private static final Random RANDOM = new Random();

  private static final int LIMIT = 1000;

  @Test
  public void limitedMax()
  {
    //noinspection NumericOverflow
    final Bounds bounds = new Bounds(Integer.MAX_VALUE + 1);
    IntStream.concat(
      IntStream.of(0, 1, 2, 3, Integer.MAX_VALUE),
      IntStream.generate(() -> RANDOM.nextInt(Integer.MAX_VALUE))
               .limit(LIMIT)
    ).forEach(value -> Assert.assertEquals(value, bounds.limited(value)));
  }

  @Test
  public void limitedSuperMax()
  {
    final Bounds bounds = new Bounds(0);
    IntStream.concat(
      IntStream.of(-3, -2, -1, 0, 1, 2, 3, Integer.MAX_VALUE, Integer.MIN_VALUE),
      IntStream.generate(RANDOM::nextInt)
               .limit(LIMIT)
    ).forEach(value -> Assert.assertEquals(value, bounds.limited(value)));
  }

  @Test
  public void limitedMin()
  {
    final Bounds bounds = new Bounds(1);
    IntStream.concat(
      IntStream.of(0, 1, 2, 3, Integer.MAX_VALUE),
      IntStream.generate(() -> RANDOM.nextInt(Integer.MAX_VALUE))
               .limit(LIMIT)
    ).forEach(value -> Assert.assertEquals(0, bounds.limited(value)));
  }

  @Test
  public void limitedSuperMin()
  {
    final Bounds bounds = new Bounds(1);
    IntStream.concat(
      IntStream.of(-3, -2, -1, 0, 1, 2, 3, Integer.MAX_VALUE, Integer.MIN_VALUE),
      IntStream.generate(RANDOM::nextInt)
               .limit(LIMIT)
    ).forEach(value -> Assert.assertEquals(0, bounds.limited(value)));
  }

  @Test
  public void limited()
  {
    final int max = 29;
    final Bounds bounds = new Bounds(29);
    for ( int expected = 0; expected < max; ++expected )
    {
      for ( int index = 0; index < LIMIT; ++index)
      {
        final int value = expected + (index * max);
        final int result = bounds.limited(value);
        final String message = String.format("%n%n"
                                             + "\tmax =      %d%n"
                                             + "\texpected = %d%n"
                                             + "\tvalue =    %d%n"
                                             + "\tresult =   %d%n%n", max, expected, value, result);
        Assert.assertEquals(message, expected, result);
      }
    }
  }
}
