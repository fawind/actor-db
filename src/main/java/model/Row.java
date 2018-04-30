package model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class Row implements Comparable<Row> {

    private static final HashFunction HASH_FUNCTION = Hashing.sha256();

    private final long hashKey;
    private final List<String> values;

    public Row(String... values) {
        if (values.length == 0) {
            throw new RuntimeException("Cannot create empty row");
        }
        this.values = Arrays.asList(values);
        this.hashKey = HASH_FUNCTION.hashString(String.valueOf(this.values.get(0)), Charsets.UTF_8).asLong();
    }

    public List<String> getValues() {
        return values;
    }

    public <ResultType> ResultType getAt(int columnIndex) {
        return (ResultType) values.get(columnIndex);
    }

    public void updateAt(int columnIndex, String value) {
        if (columnIndex == 0) {
            // TODO: cannot update key column
        }
        values.set(columnIndex, value);
    }

    public String getKey() {
        return getAt(0);
    }

    @Override
    public int compareTo(Row other) {
        return Long.compare(getHashKey(), other.getHashKey());
    }

    public long getHashKey() {
        return hashKey;
    }
}
