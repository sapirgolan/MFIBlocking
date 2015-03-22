package il.ac.technion.ie.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;


@PrepareForTest({System.class, Utilities.class})
@RunWith(PowerMockRunner.class)
public class UtilitiesTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetUnixMFICmdLine() throws Exception {
		String unixMFICmdLine = Utilities.getUnixMFICmdLine();
		assertThat(unixMFICmdLine, allOf( containsString("exe"), containsString("fpgrowth") ));
		int commandParameterInsex = unixMFICmdLine.indexOf(" -tm -s-%d %s %s");
		File resource = new File(unixMFICmdLine.substring(0, commandParameterInsex).trim());
		Assert.assertTrue(resource.exists());
		Assert.assertTrue(resource.canExecute());
	}
	
	@Test
	public void testConvertToSeconds() {
		Assert.assertEquals("Conversion to 1 second is not right" , 1, Utilities.convertToSeconds(1000), 0);
		Assert.assertEquals("Conversion to 1.5 second is not right" , 1.5, Utilities.convertToSeconds(1500), 0);
		Assert.assertEquals("Conversion to 31.52 second is not right" , 31.52, Utilities.convertToSeconds(31520), 0);
	}

}
