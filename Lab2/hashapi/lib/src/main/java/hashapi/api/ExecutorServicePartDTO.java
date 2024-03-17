package hashapi.api;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ExecutorServicePartDTO {
    private Long taskId;
    private Long executorId;
    private String hash;
    private List<DiapasonPartDTO> parts;
}
