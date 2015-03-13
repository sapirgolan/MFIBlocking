package il.ac.technion.ie.model;

        import java.util.ArrayList;
        import java.util.BitSet;
        import java.util.List;

/**
 * Created by I062070 on 13/03/2015.
 */
        public class NeighborsVector extends NeighborsVectorAbstract {
            private Integer representRecord;
            private BitSet relatives;

            public NeighborsVector(Integer recordsId, int size) {
                representRecord = recordsId;
                relatives = new BitSet(size);
                relatives.set(switchIdToIndex(representRecord), true);
            }

            public void exitsNeighbor(Integer matchedId) {
                relatives.set(switchIdToIndex(matchedId), true);
            }

            public void exitsNeighbors(List<Integer> matchedIds) {
                for (Integer matchedId : matchedIds) {
                    this.exitsNeighbor(matchedId);
                }
            }

            @Override
            public List<Integer> getNeighbors() {
                List<Integer> result = new ArrayList<>();
                for (int i = relatives.nextSetBit(0); i >= 0; i = relatives.nextSetBit(i+1)) {
                    result.add(swtichIndexToId(i));
                }
                return result;
            }

            @Override
            public int numberOfNeighbors() {
                return relatives.cardinality();
            }

            public int getReresentativeId() {
                return representRecord;
            }

            private int switchIdToIndex(int id) {
                return id -1;
            }

            private int swtichIndexToId(int index) {
                return index +1;
            }

            @Override
            public String toString() {
                List<Integer> neighbors = this.getNeighbors();
                StringBuilder builder = new StringBuilder();
                builder.append("{");
                for (Integer neighbor : neighbors) {
                    if (this.representRecord == neighbor) {
                        builder.append("*" + neighbor + "*");
                    } else {
                        builder.append(neighbor);
                    }
                    builder.append(",");
                }
                builder.deleteCharAt(builder.length()-1);
                builder.append("}");

                return builder.toString();
    }
}
