package de.haevn.worksuite.ticket;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LogTimeRequest(@NotBlank(message = "Day is required") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
    message = "Day must be in ISO format (YYYY-MM-DD)") String day,
                             @Min(value = 0, message = "Hours cannot be negative") @Max(value = 24,
                                 message = "Hours cannot exceed 24") int hours,
                             @Min(value = 0, message = "Minutes cannot be negative") @Max(value = 59,
                                 message = "Minutes cannot exceed 59") int minutes, long activityId, String comment) {
}