package hashapi.api;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class DiapasonPartDTO {
    private Long wordNumber;
    private Long firstWordIndex;
    private Long wordLength;
}