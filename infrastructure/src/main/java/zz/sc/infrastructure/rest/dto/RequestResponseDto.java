package zz.sc.infrastructure.rest.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import zz.sc.model.ActionReason;
import zz.sc.model.Request;
import lombok.Builder;
import lombok.Data;
import zz.sc.model.RequestState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDto {
    private UUID id;
    private String name;
    private String content;
    private RequestState state;
    private ActionReason actionReason;
    private Long sequenceNumber;

    public static RequestResponseDto fromDomain(Request request) {
        return RequestResponseDto.builder()
                .id(request.getId())
                .name(request.getName())
                .content(request.getContent())
                .state(request.getState())
                .actionReason(request.getActionReason())
                .sequenceNumber(request.getSequenceNumber())
                .build();
    }
}
