package de.team33.libs.random.v3.methods;

import static org.junit.Assert.*;

import de.team33.libs.random.v3.Dispenser;
import de.team33.libs.random.v3.PoolDispenser;
import de.team33.libs.typing.v3.Type;
import org.junit.Test;


public class EnumMethodPoolTest
{

  private final EnumMethodPool<Dispenser> subject = new EnumMethodPool<>(MethodFault.instance());

  private Dispenser dispenser = new PoolDispenser<>(MethodCache.<Dispenser>builder(MethodFault.instance())
                                                      .put(int.class, dsp -> 0)
                                                      .build());

  @Test
  public void get()
  {
    final MyEnum result = subject.get(Type.of(MyEnum.class)).apply(dispenser);
    assertEquals(MyEnum.B, result);
  }

  private enum MyEnum { A, B, C }
}
