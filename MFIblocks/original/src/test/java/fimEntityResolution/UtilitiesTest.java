package fimEntityResolution;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest(Utilities.class)
public class UtilitiesTest {
	
	@Rule
	public PowerMockRule rule = new PowerMockRule();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetUnixMFICmdLine() throws Exception {
		String unixMFICmdLine = Utilities.getUnixMFICmdLine();
		assertThat(unixMFICmdLine, allOf( containsString("target/classes"), containsString("fpgrowth/fpgrowth.exe") ));
	}

}
