package fimEntityResolution;

import com.esotericsoftware.kryo.Kryo;
import il.ac.technion.ie.bitsets.EWAH_BitSet_Factory;
import il.ac.technion.ie.utils.ClearerFactory;
import il.ac.technion.ie.data.structure.Clearer;
import il.ac.technion.ie.model.FrequentItem;
import il.ac.technion.ie.data.structure.IFRecord;
import il.ac.technion.ie.data.structure.SetPairIF;
import org.apache.spark.serializer.KryoRegistrator;
/***
 * In order to use Kryo serialization we have to register our classes. (Spark tuning)
 * @author Jonathan Svirsky
 */
public class MyRegistrator implements KryoRegistrator {

	@Override
	public void registerClasses(Kryo kryo) {
		kryo.register(EWAH_BitSet_Factory.class);
		kryo.register(Clearer.class);
		kryo.register(ClearerFactory.class);
		kryo.register(IFRecord.class);
		kryo.register(SetPairIF.class);
		kryo.register(FrequentItem.class);
	}

}