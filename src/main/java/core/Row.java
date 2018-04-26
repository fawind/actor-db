package core;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class Row {
    private final List<String> values;

    public Row(String... values) {
        if (values.length == 0) {
            throw new RuntimeException("Cannot create empty row");
        }
        this.values = Arrays.asList(values);
    }

    public String getAt(int columnIndex) {
        return values.get(columnIndex);
    }

    public void updateAt(int columnIndex, String value) {
        values.set(columnIndex, value);
    }

}
