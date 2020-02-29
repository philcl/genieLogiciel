package Modele.Stat;

import java.util.ArrayList;

public class StatutTicket {
    public ArrayList<String> radarChartLabels = new ArrayList<>();
    public ArrayList<Map> radarChartData = new ArrayList<>();

    public boolean contient(String valueOf) {
        for(Map map : radarChartData)
        {
            if(map.label==valueOf)
            {
                return true;
            }
        }
        return false;
    }
}
