package Modele.Stat;

import java.util.ArrayList;
import java.util.Objects;

public class Map {
    public ArrayList<Long> data;
    public String label;

    public Map(String year,int size) {
        data = new ArrayList<>();
        for (int i=0; i<size;i++)
        {
            data.add(0L);
        }
        label = year;
    }
}
