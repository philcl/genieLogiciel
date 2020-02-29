package Modele.Stat;

import java.util.ArrayList;
import java.util.Objects;

public class Map {
    public ArrayList<Long> data;
    public String label;

    public Map(String year,int size) {
        data = new ArrayList<>(size);
        label = year;
    }
}
