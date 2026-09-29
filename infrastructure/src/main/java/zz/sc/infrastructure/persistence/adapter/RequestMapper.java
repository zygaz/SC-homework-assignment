package zz.sc.infrastructure.persistence.adapter;

import zz.sc.infrastructure.persistence.entity.RequestEntity;
import zz.sc.model.Request;

public final class RequestMapper {

    private RequestMapper() {
        // private constructor to prevent instantiation
    }

    public static RequestEntity toEntity(Request request) {
        if (request == null) {
            return null;
        }
        return RequestEntity.builder()
                .id(request.getId())
                .name(request.getName())
                .content(request.getContent())
                .state(request.getState())
                .actionReason(request.getActionReason())
                .sequenceNumber(request.getSequenceNumber())
                .version(request.getVersion())

                .build();
    }

    public static Request toDomain(RequestEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Request(
                entity.getId(),
                entity.getName(),
                entity.getContent(),
                entity.getState(),
                entity.getActionReason(),
                entity.getSequenceNumber(),
                entity.getVersion()
        );
    }
}
