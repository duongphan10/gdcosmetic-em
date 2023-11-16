package com.vn.em.domain.dto.request;

import com.vn.em.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PositionCreateDto {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String name;
    private String description;
    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    private Integer departmentId;

}
