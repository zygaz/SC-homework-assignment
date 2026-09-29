package zz.sc.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRequestDto {
    @NotBlank(message = "Content is mandatory")
    private String content;
}
