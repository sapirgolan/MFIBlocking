package il.ac.technion.ie.canopy.algorithm;

import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.util.List;

public class CanopyTest {

    @Test(expected = CanopyParametersException.class)
    public void testConstractorT2SmallerThanT1() throws Exception {
        new Canopy(PowerMockito.mock(List.class), 0.6, 0.4);
    }
}