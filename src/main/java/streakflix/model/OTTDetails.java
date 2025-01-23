package streakflix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ott")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OTTDetails {
    private String ottId;
    private String ottName;
}
