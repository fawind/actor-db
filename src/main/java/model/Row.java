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

    private final List<String> values;

    public Row(String... values) {
        if (values.length == 0) {
            throw new RuntimeException("Cannot create empty row");
        }
        this.values = Arrays.asList(values);
    }

    public <ResultType> ResultType getAt(int columnIndex) {
        return (ResultType) values.get(columnIndex);
    }

    public void updateAt(int columnIndex, String value) {
        values.set(columnIndex, value);
    }

    public String getKey() {
        return getAt(0);
    }

    @Override
    public int compareTo(Row other) {
        return getKey().compareTo(other.getKey());
    }

    public long getHashKey() {
         return HASH_FUNCTION.hashString(String.valueOf(getKey()), Charsets.UTF_8).asLong();
         // return Long.valueOf(getKey());
    }
}
