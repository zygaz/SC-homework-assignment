package zz.sc.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zz.sc.model.ActionReason;

@Data
public class ReasonDto {
    @NotNull(message = "Reason is mandatory")
    private ActionReason reason;
}
