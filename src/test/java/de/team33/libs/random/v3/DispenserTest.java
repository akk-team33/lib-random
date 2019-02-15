package de.team33.libs.random.v3;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import de.team33.libs.random.misc.Features;
import de.team33.libs.typing.v3.Type;
import org.junit.Assert;
import org.junit.Test;


public class DispenserTest
{

  private static final Key<BasicRandom.Simple> BASIC = new Key<>();

  private static final Features.Stage STAGE = Features.builder()
                                                      .setup(BASIC, BasicRandom.Simple::new)
                                                      .prepare();

  private final Dispenser subject;

  public DispenserTest()
  {
    subject = new Impl(STAGE);
  }

  @Test
  public void get()
  {
    Assert.assertTrue(subject.get(int.class) instanceof Integer);
    Assert.assertTrue(subject.get(Date.class) instanceof Date);
  }

  private static class Impl extends Dispenser {

    private final Map<Type, Function> methods = new HashMap<>();

    Impl(final Features.Stage stage)
    {
      super(stage);
      add(int.class, dsp -> dsp.getFeatures().get(BASIC).anyInt());
      add(Date.class, dsp -> new Date(dsp.getFeatures().get(BASIC).anyLong()));
    }

    private <T> void add(final Class<T> type, final Function<Dispenser, T> method)
    {
      add(Type.of(type), method);
    }

    private <T> void add(final Type<T> type, final Function<Dispenser, T> method)
    {
      methods.put(type, method);
    }

    @Override
    <T> Function<Dispenser, T> getMethod(final Type<T> type)
    {
      //noinspection unchecked
      return Optional.ofNullable(methods.get(type))
                     .orElseThrow(() -> new IllegalArgumentException("unknown: " + type.toString()));
    }
  }


  private static class Key<T> extends Features.Key<T>
  {
  }
}
