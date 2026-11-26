package cinemex.tmdb.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReleaseDatesDTO {
    public List<Pais> results;

    public static class Pais {
        @SerializedName("iso_3166_1")
        public String pais;
        public List<Fecha> releaseDates;
    }

    public static class Fecha {
        public String certification;
    }
}
