package api.model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Row implements Comparable<Row>, Serializable {

    private static final HashFunction HASH_FUNCTION = Hashing.sha256();

    private long hashKey;
    private List<String> values;

    // Used for serialization
    private Row() {}

    public Row(String... values) {
        if (values.length == 0) {
            throw new RuntimeException("Cannot create empty row");
        }
        this.values = new ArrayList<>(Arrays.asList(values));
        this.hashKey = hash(String.valueOf(this.values.get(0)));
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

    public long getHashKey() {
        return hashKey;
    }

    public static long hash(String toHash) {
        return HASH_FUNCTION.hashString(toHash, Charsets.UTF_8).asLong();
    }

    @Override
    public int compareTo(Row other) {
        return Long.compare(getHashKey(), other.getHashKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return hashKey == row.hashKey &&
                Objects.equals(values, row.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashKey, values);
    }

    @Override
    public String toString() {
        return "Row{" +
                "values=" + values +
                '}';
    }
}
