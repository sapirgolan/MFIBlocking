package il.ac.technion.ie.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.BlockDescriptor;
import il.ac.technion.ie.search.core.SearchEngine;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BlockUtils.class)
public class BlockUtilsTest {

    @Test
    public void testConvertBlockToDescriptor() throws Exception {
        Block block = PowerMockito.spy(new Block(Arrays.asList(1, 7, 2)));
        SearchEngine engine = PowerMockito.mock(SearchEngine.class);
        List<String> value = Arrays.asList("record 7", "attributes", "list");
        PowerMockito.when(engine.getRecordAttributes(Mockito.eq("7"))).thenReturn(value);

        //mock response of "block.findBlockRepresentatives()"
        HashMap<Integer, Float> representatives = Maps.newHashMap(ImmutableMap.of(2, 0.4F, 7, 0.4F));
        PowerMockito.doReturn(representatives).when(block).findBlockRepresentatives();

        BlockDescriptor descriptor = BlockUtils.convertBlockToDescriptor(block, engine);
        MatcherAssert.assertThat(descriptor.getMembers(), Matchers.contains(1, 7, 2));
        MatcherAssert.assertThat(descriptor.getBlockRepresentatives(), Matchers.containsInAnyOrder(2, 7));
        MatcherAssert.assertThat(descriptor.getContentOfRecord(7), Matchers.equalTo(value));
    }

    @Test
    public void testConvertBlocksToDescriptors_cache() throws Exception {
        //Variables
        Block blockOne = new Block(Arrays.asList(1, 7, 2));
        Block blockTwo = new Block(Arrays.asList(8, 10, 2));
        SearchEngine searchEngine = PowerMockito.mock(SearchEngine.class);

        //Mock Behavior
        PowerMockito.mockStatic(BlockUtils.class);
        PowerMockito.when(BlockUtils.convertBlocksToDescriptors(Mockito.any(List.class), Mockito.eq(searchEngine))).
                thenCallRealMethod();

        PowerMockito.when(BlockUtils.convertBlockToDescriptor(Mockito.any(Block.class), Mockito.eq(searchEngine)))
                .thenReturn(new BlockDescriptor(blockOne), new BlockDescriptor(blockTwo));

        //execute
        List<BlockDescriptor> blockDescriptors = BlockUtils.convertBlocksToDescriptors(Arrays.asList(blockOne, blockTwo, blockTwo), searchEngine);
        MatcherAssert.assertThat(blockDescriptors, Matchers.hasSize(3));

        //verify
        PowerMockito.verifyStatic(Mockito.times(2));
        BlockUtils.convertBlockToDescriptor(Mockito.any(Block.class), Mockito.eq(searchEngine));
    }
}