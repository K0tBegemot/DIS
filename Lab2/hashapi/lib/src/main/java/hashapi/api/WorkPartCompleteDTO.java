package hashapi.api;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WorkPartCompleteDTO {
    private Long taskId;
    private Long executorId;
    private List<String> data;
}
