package com.vrund.tasks;
import jakarta.validation.constraints.*;
record TaskInput(@NotBlank @Size(max = 140) String title,
                 @Size(max = 2000) String description,
                 @NotNull Boolean completed) {}
