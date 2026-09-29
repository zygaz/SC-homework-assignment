package zz.sc.infrastructure.rest;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import zz.sc.usecase.RequestManagementUseCase;
import zz.sc.model.PageQuery;
import zz.sc.model.PageResult;
import zz.sc.model.Request;
import zz.sc.model.RequestState;
import zz.sc.infrastructure.rest.dto.CreateRequestDto;
import zz.sc.infrastructure.rest.dto.PageResponseDto;
import zz.sc.infrastructure.rest.dto.ReasonDto;
import zz.sc.infrastructure.rest.dto.RequestResponseDto;
import zz.sc.infrastructure.rest.dto.UpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestManagementUseCase useCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponseDto createRequest(@Valid @RequestBody CreateRequestDto dto) {
        Request request = useCase.createRequest(dto.getName(), dto.getContent());
        return RequestResponseDto.fromDomain(request);
    }

    @PutMapping("/{id}")
    public RequestResponseDto updateContent(@PathVariable UUID id, @Valid @RequestBody UpdateRequestDto dto) {
        Request request = useCase.updateContent(id, dto.getContent());
        return RequestResponseDto.fromDomain(request);
    }

    @PostMapping("/{id}/verify")
    public RequestResponseDto verifyRequest(@PathVariable UUID id) {
        Request request = useCase.verifyRequest(id);
        return RequestResponseDto.fromDomain(request);
    }

    @PostMapping("/{id}/accept")
    public RequestResponseDto acceptRequest(@PathVariable UUID id) {
        Request request = useCase.acceptRequest(id);
        return RequestResponseDto.fromDomain(request);
    }

    @PostMapping("/{id}/publish")
    public RequestResponseDto publishRequest(@PathVariable UUID id) {
        Request request = useCase.publishRequest(id);
        return RequestResponseDto.fromDomain(request);
    }

    @PostMapping("/{id}/reject")
    public RequestResponseDto rejectRequest(@PathVariable UUID id, @Valid @RequestBody ReasonDto dto) {
        Request request = useCase.rejectRequest(id, dto.getReason());
        return RequestResponseDto.fromDomain(request);
    }

    @DeleteMapping("/{id}")
    public RequestResponseDto deleteRequest(@PathVariable UUID id, @Valid @RequestBody ReasonDto dto) {
        Request request = useCase.deleteRequest(id, dto.getReason());
        return RequestResponseDto.fromDomain(request);
    }

    @GetMapping("/{id}")
    public RequestResponseDto getRequest(@PathVariable UUID id) {
        Request request = useCase.getRequest(id);
        return RequestResponseDto.fromDomain(request);
    }

    @GetMapping
    public PageResponseDto<RequestResponseDto> getRequests(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RequestState state,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        PageQuery query = new PageQuery(page, size);
        PageResult<Request> result = useCase.getRequests(name, state, query);
        return PageResponseDto.fromPageResult(result, RequestResponseDto::fromDomain);
    }
}
