package Modele.Stat;

import java.util.ArrayList;

public class StatutTicket {
    public ArrayList<String> radarChartLabels;
    public ArrayList<Map> radarChartData;

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
