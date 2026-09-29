package zz.sc.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRequestDto {
    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Content is mandatory")
    private String content;
}
